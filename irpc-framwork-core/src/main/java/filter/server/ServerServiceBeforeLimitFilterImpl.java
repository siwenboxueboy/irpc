package filter.server;

import common.RpcInvocation;
import common.ServerServiceSemaphoreWrapper;
import common.annotation.SPI;
import common.exception.MaxServiceLimitRequestException;
import filter.IServerFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

import static common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;

@SPI("before")
@Slf4j
public class ServerServiceBeforeLimitFilterImpl implements IServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        //从缓存中提取semaphore对象
        Semaphore semaphore = serverServiceSemaphoreWrapper.getSemaphore();
        boolean tryResult = semaphore.tryAcquire();
        if (!tryResult) {
            log.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcInvocation.getTargetServiceName(), serverServiceSemaphoreWrapper.getMaxNums());
            MaxServiceLimitRequestException iRpcException = new MaxServiceLimitRequestException(rpcInvocation);
            rpcInvocation.setE(iRpcException);
            throw iRpcException;
        }
    }
}
