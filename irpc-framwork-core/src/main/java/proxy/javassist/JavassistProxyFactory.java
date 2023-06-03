package proxy.javassist;

import proxy.ProxyFactory;

/**
 * 通过javassist的技术来实现代理模式
 */
public class JavassistProxyFactory implements ProxyFactory {

    /**
     * @param clazz
     * @param <T>
     * @return
     */
    @Override
    public <T> T getProxy(Class clazz) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(), clazz, new JavassistInvocationHandler(clazz));
    }
}
