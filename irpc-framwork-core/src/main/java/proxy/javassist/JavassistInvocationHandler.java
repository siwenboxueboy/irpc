package proxy.javassist;

import client.RpcReferenceWrapper;
import common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static common.cache.CommonClientCache.RESP_MAP;
import static common.cache.CommonClientCache.SEND_QUEUE;
import static common.constants.RpcConstants.DEFAULT_TIMEOUT;

public class JavassistInvocationHandler implements InvocationHandler {
    private final static Object OBJECT = new Object();

    private RpcReferenceWrapper rpcReferenceWrapper;

    private Long timeOut = Long.valueOf(DEFAULT_TIMEOUT);

    public JavassistInvocationHandler(RpcReferenceWrapper rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
        Long timeOutTemp = Long.valueOf(String.valueOf(rpcReferenceWrapper.getAttatchments().get("timeOut")));
        if (timeOutTemp != null){
            timeOut = timeOutTemp;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttatchments());
        rpcInvocation.setUuid(UUID.randomUUID().toString());

        //代理类内部将请求放入到发送队列中，等待发送队列发送请求
        SEND_QUEUE.add(rpcInvocation);
        //既然是异步请求，就没有必要再在RESP_MAP中判断是否有响应结果了
/*        if (rpcReferenceWrapper.isAsync()) {
            return null;
        }*/
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        long beginTime = System.currentTimeMillis();
        int retryTimes = 0;

        // todo 以下代码为什么要这么设计？
        //如果请求数据在指定时间内返回则返回给客户端调用方
        while (System.currentTimeMillis() - beginTime < timeOut || rpcInvocation.getRetry() > 0) {
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object != null && object instanceof RpcInvocation) {
                RpcInvocation rpcInvocationResp = (RpcInvocation) object;
                //正常结果 调用次数为0
                if (rpcInvocationResp.getRetry() == 0 || (rpcInvocationResp.getRetry() != 0 && rpcInvocationResp.getE() == null)) {
                    // 移除map中引用 避免内存泄漏
                    RESP_MAP.remove(rpcInvocation.getUuid());
                    return rpcInvocationResp.getResponse();
                } else if (rpcInvocationResp.getE() != null) {
                    if (rpcInvocationResp.getRetry() == 0) {
                        RESP_MAP.remove(rpcInvocation.getUuid());
                        return rpcInvocationResp.getResponse();
                    }
                }
            }
            if (OBJECT.equals(object)) {
                //超时重试
                if (System.currentTimeMillis() - beginTime > timeOut) {
                    retryTimes++;
                    //重新请求
                    rpcInvocation.setResponse(null);
                    //每次重试之后都会将retry值扣减1
                    rpcInvocation.setRetry(rpcInvocation.getRetry() - 1);
                    RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
                    SEND_QUEUE.add(rpcInvocation);
                }
            }
        }
        // 应对一些请求超时的情况
        RESP_MAP.remove(rpcInvocation.getUuid());
        throw new TimeoutException("Wait for response from server on client " + timeOut + "ms,retry times is " + retryTimes + ",service's name is " + rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
