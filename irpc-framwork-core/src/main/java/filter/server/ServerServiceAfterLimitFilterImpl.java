package filter.server;

import common.RpcInvocation;
import common.ServerServiceSemaphoreWrapper;
import common.annotation.SPI;
import filter.IServerFilter;

import static common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;

@SPI("after")
public class ServerServiceAfterLimitFilterImpl implements IServerFilter {
    /**
     * 执行核心过滤逻辑
     *
     * @param rpcInvocation
     */
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        serverServiceSemaphoreWrapper.getSemaphore().release();
    }

}
