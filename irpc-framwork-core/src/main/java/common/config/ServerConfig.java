package common.config;

import enums.SerializeEnum;
import lombok.Data;

@Data
public class ServerConfig {

    private String registerAddr;
    private String applicationName;
    private Integer serverPort;
    private String registerType;
    /**
     * 服务端序列化方式 example: hession2,kryo,jdk,fastjson
     */
    private SerializeEnum serverSerialize;

}
