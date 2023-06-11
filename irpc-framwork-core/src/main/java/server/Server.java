package server;


import cn.hutool.core.lang.Console;
import common.RpcDecoder;
import common.RpcEncoder;
import common.config.PropertiesBootstrap;
import common.config.ServerConfig;
import common.event.IRpcListenerLoader;
import common.utils.CommonUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import registy.RegistryService;
import registy.URL;
import registy.zookeeper.ZookeeperRegister;

import static common.cache.CommonServerCache.*;

@Slf4j
public class Server {


    private static EventLoopGroup bossGroup = null;
    private static EventLoopGroup workerGroup = null;

    private static IRpcListenerLoader iRpcListenerLoader;

    @Getter
    @Setter
    private ServerConfig serverConfig;

    public void startApplication() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1024).option(ChannelOption.SO_SNDBUF, 16 * 1024).option(ChannelOption.SO_RCVBUF, 16 * 1024).option(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                Console.log("初始化provider过程");
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ServerHandler());
            }
        });
        this.batchExportUrl();
        bootstrap.bind(serverConfig.getServerPort()).sync();
    }

    /**
     * 服务提供者设置要暴露的服务接口信息
     */
    public void exportService(Object serviceBean) {
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length > 1) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        // todo 这个为什么不放在batchExportUrl()方法中？
        if (REGISTRY_SERVICE == null) {
            REGISTRY_SERVICE = new ZookeeperRegister(serverConfig.getRegisterAddr());
        }
        //默认选择该对象的第一个实现接口
        Class interfaceClass = classes[0];

        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);

        URL url = new URL();
        url.setServiceName(interfaceClass.getName());
        url.setApplicationName(serverConfig.getApplicationName());
        url.addParameter("host", CommonUtils.getIpAddress());
        url.addParameter("port", String.valueOf(serverConfig.getServerPort()));
        // 加入缓存，后续在batchExportUrl中处理，注册到zk中
        PROVIDER_URL_SET.add(url);
    }

    /**
     * 服务提供者暴露服务接口，方便客户端进行调用
     */
    public void batchExportUrl() {
        Thread task = new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (URL url : PROVIDER_URL_SET) {
                // 服务端注册服务接口
                REGISTRY_SERVICE.register(url);
            }
        });
        task.start();
    }

    private void initServerConfig() {
        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);
    }

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        // 初始化服务配置
        server.initServerConfig();
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        // 暴露服务提供者接口
        server.exportService(new DataServiceImpl());
        // 启动服务
        server.startApplication();
        log.info("启动成功");
    }

}
