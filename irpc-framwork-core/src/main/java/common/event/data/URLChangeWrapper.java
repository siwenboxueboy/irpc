package common.event.data;

import lombok.Data;

import java.util.List;

@Data
public class URLChangeWrapper {
    private String serviceName;

    private List<String> providerUrl;
}
