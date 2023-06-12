package filter;

import common.RpcInvocation;

/**
 * 请求抵达服务端之后需要经过的过滤器
 */
public interface IServerFilter extends IFilter {
    /**
     * 执行核心过滤逻辑
     *
     * @param rpcInvocation
     */
    void doFilter(RpcInvocation rpcInvocation);
}
