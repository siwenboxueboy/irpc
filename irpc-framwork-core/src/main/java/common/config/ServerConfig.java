package common.config;

import lombok.Data;

@Data
public class ServerConfig {

    private String registerAddr;
    private String applicationName;
    private Integer serverPort;

}
