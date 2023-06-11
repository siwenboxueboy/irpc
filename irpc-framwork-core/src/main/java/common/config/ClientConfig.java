package common.config;

import enums.SerializeEnum;
import lombok.Data;

@Data
public class ClientConfig {
    private String proxyType;
    private String registerAddr;
    private String applicationName;
    private String routerStrategy;
    /**
     * 客户端序列化方式 example: hession2,kryo,jdk,fastjson
     */
    private SerializeEnum clientSerialize;
}
