package registy.zookeeper;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProviderNodeInfo {

    private String serviceName;
    private String address;
    private Integer weight;
    private String registryTime;
}
