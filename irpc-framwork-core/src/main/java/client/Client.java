package client;

import com.alibaba.fastjson.JSON;
import common.RpcDecoder;
import common.RpcEncoder;
import common.RpcInvocation;
import common.RpcProtocol;
import common.config.ClientConfig;
import common.config.PropertiesBootstrap;
import common.event.IRpcListenerLoader;
import common.utils.CommonUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proxy.javassist.JavassistProxyFactory;
import proxy.jdk.JDKProxyFactory;
import registy.URL;
import registy.zookeeper.AbstractRegister;
import registy.zookeeper.ZookeeperRegister;

import java.util.List;

import static common.cache.CommonClientCache.SEND_QUEUE;
import static common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;

public class Client {
    private Logger logger = LoggerFactory.getLogger(Client.class);

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
        EventLoopGroup clientGroup = new NioEventLoopGroup();
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
        RpcReference rpcReference;

        if ("javassist".equals(clientConfig.getProxyType())) {
            rpcReference = new RpcReference(new JavassistProxyFactory());
        } else {
            rpcReference = new RpcReference(new JDKProxyFactory());
        }
        return rpcReference;
    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        RpcReference rpcReference = client.initClientApplication();
        DataService dataService = rpcReference.get(DataService.class);
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
        abstractRegister.subscribe(url);
    }

    /**
     * 开始和各个provider建立连接
     */
    public void doConnectServer() {
        for (String providerServiceName : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(providerServiceName);
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerServiceName, providerIp);
                } catch (InterruptedException e) {
                    logger.error("[doConnectServer] connect fail ", e);
                }
            }
            URL url = new URL();
            url.setServiceName(providerServiceName);
            //客户端在此新增一个订阅的功能
            // todo 为什么要在这里做这个事情？
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
                    RpcInvocation data = SEND_QUEUE.take();
                    // 将RpcInvocation封装到RpcProtocol对象中，然后发送给服务端，这里正好对应了上文中的ServerHandler
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());
                    // netty的通道负责发送数据给服务端
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data.getTargetServiceName());
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
