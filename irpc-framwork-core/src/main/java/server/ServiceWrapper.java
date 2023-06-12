package server;

import lombok.Data;
import lombok.ToString;

/**
 * 服务包装类
 * */
@Data
public class ServiceWrapper {

    public ServiceWrapper() {
    }

    public ServiceWrapper(Object serviceObj, String group) {
        this.serviceObj = serviceObj;
        this.group = group;
    }

    /**
     * 对外暴露的具体服务对象
     */
    private Object serviceObj;

    /**
     * 具体暴露服务的分组
     */
    private String group = "default";

    /**
     * 整个应用的token校验
     */
    private String serviceToken = "";

    /**
     * 限流策略
     */
    private Integer limit = -1;

}
