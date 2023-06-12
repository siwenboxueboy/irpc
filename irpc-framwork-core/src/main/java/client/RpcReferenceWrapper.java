package client;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RpcReferenceWrapper<T> {

    private Class<T> aimClass;

    private Map<String, Object> attatchments = new ConcurrentHashMap<>();

    public boolean isAsync() {
        return Boolean.valueOf(String.valueOf(attatchments.get("async")));
    }

    public void setAsync(boolean async) {
        this.attatchments.put("async", async);
    }

    public String getUrl() {
        return String.valueOf(attatchments.get("url"));
    }

    public void setUrl(String url) {
        attatchments.put("url", url);
    }

    public String getServiceToken() {
        return String.valueOf(attatchments.get("serviceToken"));
    }

    public void setServiceToken(String serviceToken) {
        attatchments.put("serviceToken", serviceToken);
    }

    public String getGroup() {
        return String.valueOf(attatchments.get("group"));
    }

    public void setGroup(String group) {
        attatchments.put("group", group);
    }

}
