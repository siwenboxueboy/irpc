package client;

import com.alibaba.fastjson.JSON;
import common.RpcDecoder;
import common.RpcEncoder;
import common.RpcInvocation;
import common.RpcProtocol;
import common.config.ClientConfig;
import common.config.PropertiesBootstrap;
import common.constants.RpcConstants;
import common.event.IRpcListenerLoader;
import common.utils.CommonUtils;
import enums.SerializeEnum;
import filter.client.ClientFilterChain;
import filter.client.ClientLogFilterImpl;
import filter.client.DirectInvokeFilterImpl;
import filter.client.GroupFilterImpl;
import interfaces.DataService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import proxy.javassist.JavassistProxyFactory;
import proxy.jdk.JDKProxyFactory;
import registy.URL;
import registy.zookeeper.AbstractRegister;
import registy.zookeeper.ZookeeperRegister;
import router.RandomRouterImpl;
import router.RotateRouterImpl;

import java.util.List;
import java.util.Map;

import static common.cache.CommonClientCache.*;
import static common.constants.RpcConstants.RANDOM_ROUTER_TYPE;
import static common.constants.RpcConstants.ROTATE_ROUTER_TYPE;

@Slf4j
public class Client {

    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    @Getter
    @Setter
    private ClientConfig clientConfig;

    private AbstractRegister abstractRegister;

    private IRpcListenerLoader iRpcListenerLoader;

    @Getter
    private Bootstrap bootstrap = new Bootstrap();

    /**
     * 初始化客户端服务监听服务器
     * 主要工作：
     * 1. 初始化netty配置，注意：仅初始化配置，没有和服务提供者建立通信连接
     * 2. 初始化 事件-监听 相关资源配置
     * 3. 初始化 代理工厂
     */
    public RpcReference initClientApplication() throws InterruptedException {
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            /**
             * @param ch
             * @throws Exception
             */
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });

        // 初始化服务监听器
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();

        this.clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();
        CLIENT_CONFIG = this.clientConfig;
        RpcReference rpcReference;

        if (RpcConstants.JAVASSIST_PROXY_TYPE.equals(clientConfig.getProxyType())) {
            rpcReference = new RpcReference(new JavassistProxyFactory());
        } else {
            rpcReference = new RpcReference(new JDKProxyFactory());
        }
        return rpcReference;
    }

    private void initClientConfig() {
        //初始化路由策略

        String routerStrategy = clientConfig.getRouterStrategy();
        switch (routerStrategy) {
            case RANDOM_ROUTER_TYPE:
                IROUTER = new RandomRouterImpl();
                break;
            case ROTATE_ROUTER_TYPE:
                IROUTER = new RotateRouterImpl();
                break;
            default:
                throw new RuntimeException("no match routerStrategy for" + routerStrategy);
        }
        // 设置客户端序列化方式
        SerializeEnum clientSerialize = clientConfig.getClientSerialize();
        CLIENT_SERIALIZE_FACTORY = SerializeEnum.getSerializeFactory(clientSerialize);

        // 初始化过滤链 指定过滤的顺序
        ClientFilterChain clientFilterChain = new ClientFilterChain();
        clientFilterChain.addClientFilter(new DirectInvokeFilterImpl());
        clientFilterChain.addClientFilter(new GroupFilterImpl());
        clientFilterChain.addClientFilter(new ClientLogFilterImpl());
        CLIENT_FILTER_CHAIN = clientFilterChain;
    }

    /**
     * 启动服务之前需要预先订阅对应的dubbo服务
     *
     * @param serviceBean
     */
    public void doSubscribeService(Class serviceBean) {
        if (abstractRegister == null) {
            abstractRegister = new ZookeeperRegister(clientConfig.getRegisterAddr());
        }
        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", CommonUtils.getIpAddress());
        Map<String, String> result = abstractRegister.getServiceWeightMap(serviceBean.getName());
        URL_MAP.put(serviceBean.getName(), result);
        abstractRegister.subscribe(url);
    }

    /**
     * 开始和各个provider建立连接
     */
    public void doConnectServer() {
        for (URL providerURL : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(providerURL.getServiceName());
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerURL.getServiceName(), providerIp);
                } catch (InterruptedException e) {
                    log.error("[doConnectServer] connect fail ", e);
                }
            }
            URL url = new URL();
            url.addParameter("servicePath", providerURL.getServiceName() + "/provider");
            url.addParameter("providerIps", JSON.toJSONString(providerIps));
            //客户端在此新增一个订阅的功能
            // 为什么要在这里做这个事情？ 上面connect连接了提供者，这里要监听提供者是否下线，如果下线了，客户端要主动断开连接
            abstractRegister.doAfterSubscribe(url);
        }
    }

    private void startClient() {
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }

    /**
     * 异步发送信息的任务
     */
    class AsyncSendJob implements Runnable {

        public AsyncSendJob() {

        }

        /**
         * 将请求发送任务交给单独的IO线程区负责，实现异步化，提升发送性能。
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            while (true) {
                try {
                    // 阻塞模式 阻塞队列
                    RpcInvocation rpcInvocation = SEND_QUEUE.take();
                    // 将RpcInvocation封装到RpcProtocol对象中，然后发送给服务端，这里正好对应了上文中的ServerHandler

                    // netty的通道负责发送数据给服务端
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(rpcInvocation);
                    if (channelFuture != null){
                        RpcProtocol rpcProtocol = new RpcProtocol(CLIENT_SERIALIZE_FACTORY.serialize(rpcInvocation));
                        channelFuture.channel().writeAndFlush(rpcProtocol);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        // 初始化客户端网络服务器以及客户端代理
        RpcReference rpcReference = client.initClientApplication();
        // 初始化客户端路由
        client.initClientConfig();

        RpcReferenceWrapper<DataService> rpcReferenceWrapper = new RpcReferenceWrapper<>();
        rpcReferenceWrapper.setAimClass(DataService.class);
        rpcReferenceWrapper.setGroup("dev");
        rpcReferenceWrapper.setServiceToken("token-a");
        // 获得代理对象
        DataService dataService = rpcReference.get(rpcReferenceWrapper);
        // 订阅相关服务接口
        client.doSubscribeService(DataService.class);
        ConnectionHandler.setBootstrap(client.getBootstrap());
        // 连接服务提供者
        client.doConnectServer();
        // 启动异步IO发送数据线程
        client.startClient();

        for (int i = 0; i < 100; i++) {
            try {
                String result = dataService.sendData("test");
                System.out.println(result);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
