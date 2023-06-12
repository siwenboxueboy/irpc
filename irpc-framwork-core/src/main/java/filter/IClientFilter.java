package filter;

import common.ChannelFutureWrapper;
import common.RpcInvocation;

import java.util.List;

/**
 * 在客户端发起调用过程中需要经过的过滤器
 */
public interface IClientFilter extends IFilter {
    /**
     * 执行过滤链
     *
     * @param src
     * @param rpcInvocation
     * @return
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);
}
