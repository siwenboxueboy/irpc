package common.config;

import enums.SerializeEnum;

import java.io.IOException;

import static common.constants.RpcConstants.*;

/**
 * 这个对象主要是负责将properties的配置转换成本地的一个Map结构进行管理
 */
public class PropertiesBootstrap {
    private volatile boolean configIsReady;

    public static final String SERVER_PORT = "irpc.serverPort";
    public static final String REGISTER_ADDRESS = "irpc.registerAddr";
    public static final String REGISTER_TYPE = "irpc.registerType";
    public static final String APPLICATION_NAME = "irpc.applicationName";
    public static final String PROXY_TYPE = "irpc.proxyType";
    public static final String PROXY_ROUTER_STRATEGY = "irpc.routerStrategy";

    public static final String SERVER_SERIALIZE_TYPE = "irpc.serverSerialize";

    public static final String CLIENT_SERIALIZE_TYPE = "irpc.clientSerialize";

    public static final String CLIENT_DEFAULT_TIME_OUT = "irpc.client.default.timeout";

    public static final String SERVER_BIZ_THREAD_NUMS = "irpc.server.biz.thread.nums";

    public static final String SERVER_QUEUE_SIZE = "irpc.server.queue.size";

    public static final String SERVER_MAX_CONNECTION = "irpc.server.max.connection";

    /**
     * 服务提供者配置
     */
    public static ServerConfig loadServerConfigFromLocal() {
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadServerConfigFromLocal fail,e is {}", e);
        }
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setServerPort(PropertiesLoader.getPropertiesInteger(SERVER_PORT));
        serverConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        serverConfig.setRegisterType(PropertiesLoader.getPropertiesStr(REGISTER_TYPE));
        serverConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        serverConfig.setServerSerialize(SerializeEnum.valueOf(PropertiesLoader.getPropertiesStrDefault(SERVER_SERIALIZE_TYPE, SerializeEnum.JDK.name())));
        serverConfig.setServerBizThreadNums(PropertiesLoader.getPropertiesIntegerDefault(SERVER_BIZ_THREAD_NUMS, DEFAULT_THREAD_NUMS));
        serverConfig.setServerQueueSize(PropertiesLoader.getPropertiesIntegerDefault(SERVER_QUEUE_SIZE, DEFAULT_QUEUE_SIZE));
        serverConfig.setMaxConnections(PropertiesLoader.getPropertiesIntegerDefault(SERVER_MAX_CONNECTION,DEFAULT_MAX_CONNECTION_NUMS));
        return serverConfig;
    }

    /**
     * 服务消费者配置
     */
    public static ClientConfig loadClientConfigFromLocal() {
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadClientConfigFromLocal fail,e is {}", e);
        }
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        clientConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        clientConfig.setRegisterType(PropertiesLoader.getPropertiesStr(REGISTER_TYPE));
        clientConfig.setProxyType(PropertiesLoader.getPropertiesStr(PROXY_TYPE));
        clientConfig.setRouterStrategy(PropertiesLoader.getPropertiesStr(PROXY_ROUTER_STRATEGY));
        clientConfig.setClientSerialize(SerializeEnum.valueOf(PropertiesLoader.getPropertiesStrDefault(CLIENT_SERIALIZE_TYPE, SerializeEnum.JDK.name())));
        clientConfig.setTimeOut(PropertiesLoader.getPropertiesIntegerDefault(CLIENT_DEFAULT_TIME_OUT, DEFAULT_TIMEOUT));
        return clientConfig;
    }
}
