package common;

import java.util.concurrent.atomic.AtomicLong;

import static common.cache.CommonClientCache.SERVICE_ROUTER_MAP;
/**
 * 对该数组进行轮询即可，ChannelFutureRefWrapper类就是专门实现轮训效果，它的本质就是通过取模计算
 * */
public class ChannelFuturePollingRef {
    // 每次请求都会加1
    private AtomicLong referenceTimes = new AtomicLong(0);


    /**
     * 确定本次服务调用的服务提供者
     * */
    public ChannelFutureWrapper getChannelFutureWrapper(String serviceName){
        // 获取服务的服务调用顺序
        ChannelFutureWrapper[] arr = SERVICE_ROUTER_MAP.get(serviceName);
        long i = referenceTimes.getAndIncrement();
        int index = (int) (i % arr.length);
        return arr[index];
    }
}
