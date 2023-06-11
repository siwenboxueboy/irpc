package common.config;

import enums.SerializeEnum;

import java.io.IOException;

/**
 * 这个对象主要是负责将properties的配置转换成本地的一个Map结构进行管理
 */
public class PropertiesBootstrap {
    private volatile boolean configIsReady;

    public static final String SERVER_PORT = "irpc.serverPort";
    public static final String REGISTER_ADDRESS = "irpc.registerAddr";
    public static final String APPLICATION_NAME = "irpc.applicationName";
    public static final String PROXY_TYPE = "irpc.proxyType";
    public static final String PROXY_ROUTER_STRATEGY = "irpc.routerStrategy";

    public static final String SERVER_SERIALIZE_TYPE = "irpc.serverSerialize";

    public static final String CLIENT_SERIALIZE_TYPE = "irpc.clientSerialize";

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
        serverConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        serverConfig.setServerSerialize(SerializeEnum.valueOf(PropertiesLoader.getPropertiesStrDefault(SERVER_SERIALIZE_TYPE, SerializeEnum.JDK.name())));
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
        clientConfig.setProxyType(PropertiesLoader.getPropertiesStr(PROXY_TYPE));
        clientConfig.setRouterStrategy(PropertiesLoader.getPropertiesStr(PROXY_ROUTER_STRATEGY));
        clientConfig.setClientSerialize(SerializeEnum.valueOf(PropertiesLoader.getPropertiesStrDefault(CLIENT_SERIALIZE_TYPE, SerializeEnum.JDK.name())));
        return clientConfig;
    }
}
