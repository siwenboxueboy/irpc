package client;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RpcReferenceWrapper<T> {

    private Class<T> aimClass;

    private Map<String, Object> attatchments = new ConcurrentHashMap<>();

    public boolean isAsync() {
        Object r = attatchments.get("async");
        if (r == null || r.equals(false)) {
            return false;
        }
        return Boolean.valueOf(true);
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

    public void setTimeOut(int timeOut) {
        attatchments.put("timeOut", timeOut);
    }

    public String getTimeOUt() {
        return String.valueOf(attatchments.get("timeOut"));
    }

    /**
     * 设置容错策略
     *
     * @param tolerant
     */
    public void setTolerant(String tolerant){
        this.attatchments.put("tolerant",tolerant);
    }

    /**
     * 失败重试次数
     */
    public int getRetry(){
        if(attatchments.get("retry")==null){
            return 0;
        }else {
            return (int) attatchments.get("retry");
        }
    }

    public void setRetry(int retry){
        this.attatchments.put("retry",retry);
    }


}
