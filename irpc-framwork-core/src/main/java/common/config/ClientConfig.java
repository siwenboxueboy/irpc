package common.config;

import lombok.Data;

@Data
public class ClientConfig {
    private String serverAddr;
    private int port;
}
