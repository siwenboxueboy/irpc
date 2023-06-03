package proxy.jdk;


import proxy.ProxyFactory;

import java.lang.reflect.Proxy;

public class JDKProxyFactory implements ProxyFactory {

    /**
     *
     * 这个代理工厂的内部我注入了一个叫做JDK的代理处理器，JDKClientInvocationHandler，
     * 它的核心任务就是将需要调用的方法名称、服务名称，参数统统都封装好到RpcInvocation当中，
     * 然后塞入到一个队列里，并且等待服务端的数据返回。
     * @param clazz
     * @param <T>
     * @return
     */
    @Override
    public <T> T getProxy(final Class clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},new JDKClientInvocationHandler(clazz));
    }
}
