package common.config;

import enums.SerializeEnum;
import lombok.Data;

@Data
public class ServerConfig {

    private String registerAddr;
    private String applicationName;
    private Integer serverPort;
    /**
     * 服务端服务注册、发现类型 zookeeper
     * */
    private String registerType;
    /**
     * 服务端序列化方式 example: hession2,kryo,jdk,fastjson
     */
    private SerializeEnum serverSerialize;

    /**
     * 服务端业务线程数目
     */
    private Integer serverBizThreadNums;

    /**
     * 服务端接收队列的大小
     */
    private Integer serverQueueSize;

}
