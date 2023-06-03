package proxy.jdk;

import common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static common.cache.CommonClientCache.RESP_MAP;
import static common.cache.CommonClientCache.SEND_QUEUE;

public class JDKClientInvocationHandler implements InvocationHandler {

    private final static Object OBJECT = new Object();

    private Class<?> clazz;

    public JDKClientInvocationHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     *
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 组装远程调用对象
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(clazz.getName());
        //这里面注入了一个uuid，对每一次的请求都做单独区分，方便我们在客户端接收数据的时候进行识别
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        // 将其放入map，方便后面响应获取
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);

        //这里就是将请求的参数放入到发送队列中，待异步IO线程处理
        SEND_QUEUE.add(rpcInvocation);

        // 客户端请求超时的一个判断依据
        long beginTime = System.currentTimeMillis();
        // 获取客户端响应
        while (System.currentTimeMillis() - beginTime < 3 * 1000) {
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                return ((RpcInvocation) object).getResponse();
            }
        }
        throw new TimeoutException("client wait server's response timeout!");
    }
}
