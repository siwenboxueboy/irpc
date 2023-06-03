package common;

import lombok.Data;

/**
 * 自定义协议中content的消息体
 */
@Data
public class RpcInvocation {
    //请求的目标方法，例如findUser
    private String targetMethod;
    //请求的目标服务名称，例如：com.sise.user.UserService
    private String targetServiceName;
    //请求参数信息
    private Object[] args;
    // 这个对象里的uuid字段，主要是用于匹配请求和响应的一个关键值。
    // 当请求从客户端发出的时候，会有一个uuid用于记录发出的请求，待数据返回的时候通过uuid来匹配对应的请求线程，并且返回给调用线程。
    private String uuid;
    //接口响应的数据塞入这个字段中（如果是异步调用或者void类型，这里就为空）
    private Object response;
}
