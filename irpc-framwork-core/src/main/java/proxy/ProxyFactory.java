package proxy;

public interface ProxyFactory {
    <T> T getProxy(Class clazz) throws Throwable;
}
