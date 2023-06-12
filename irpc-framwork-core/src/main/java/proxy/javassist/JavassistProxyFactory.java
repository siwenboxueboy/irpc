package proxy.javassist;

import client.RpcReferenceWrapper;
import proxy.ProxyFactory;

/**
 * 通过javassist的技术来实现代理模式
 */
public class JavassistProxyFactory implements ProxyFactory {

    /**
     * @param rpcReferenceWrapper
     * @param <T>
     * @return
     */
    @Override
    public <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(), rpcReferenceWrapper.getAimClass(), new JavassistInvocationHandler(rpcReferenceWrapper));
    }
}
