package common.config;

import lombok.Data;

@Data
public class ClientConfig {
    private String proxyType;
    private String registerAddr;
    private String applicationName;
}
