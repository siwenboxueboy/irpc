package common.config;

import enums.SerializeEnum;
import lombok.Data;

@Data
public class ClientConfig {
    private String registerType;
    /**
     * 代理类型 example: jdk,javassist
     */
    private String proxyType;
    private String registerAddr;
    private String applicationName;
    /**
     * 负载均衡策略 example:random,rotate
     */
    private String routerStrategy;
    /**
     * 客户端序列化方式 example: hession2,kryo,jdk,fastjson
     */
    private SerializeEnum clientSerialize;
}
