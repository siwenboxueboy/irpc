package filter.client;

import common.ChannelFutureWrapper;
import common.RpcInvocation;
import filter.IClientFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static common.cache.CommonClientCache.CLIENT_CONFIG;

/**
 * 客户端调用日志过滤器
 * */
@Slf4j
public class ClientLogFilterImpl implements IClientFilter {

    /**
     * 执行过滤链
     *
     * @param src
     * @param rpcInvocation
     * @return
     */
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        // 记录当前客户端程序调用了哪个具体的service方法
        rpcInvocation.getAttachments().put("c_app_name",CLIENT_CONFIG.getApplicationName());
        log.info(rpcInvocation.getAttachments().get("c_app_name")+" do invoke -----> "+rpcInvocation.getTargetServiceName());
    }
}
