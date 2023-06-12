package proxy;

import client.RpcReferenceWrapper;

public interface ProxyFactory {
    <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable;
}
