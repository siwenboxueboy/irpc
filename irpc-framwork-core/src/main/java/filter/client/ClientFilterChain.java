package filter.client;

import common.ChannelFutureWrapper;
import common.RpcInvocation;
import filter.IClientFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端模块的链路设计
 */
public class ClientFilterChain {
    private static List<IClientFilter> iClientFilterList = new ArrayList<>();

    public void addClientFilter(IClientFilter iClientFilter) {
        iClientFilterList.add(iClientFilter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        for (IClientFilter iClientFilter : iClientFilterList) {
            iClientFilter.doFilter(src, rpcInvocation);
        }
    }
}
