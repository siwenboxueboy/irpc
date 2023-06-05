package common.event;

import java.util.List;

public class URLChangeWrapper {
    private List<String> providerUrl;
    private String serviceName;

    public void setProviderUrl(List<String> providerUrl) {
        this.providerUrl = providerUrl;
    }

    public List<String> getProviderUrl() {
        return providerUrl;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
