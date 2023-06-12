package filter.server;

import common.RpcInvocation;
import filter.IServerFilter;

import java.util.ArrayList;
import java.util.List;
/**
 * 服务端模块的过滤器链路
 * */
public class ServerFilterChain {
    private static List<IServerFilter> iServerFilters = new ArrayList<>();

    public void addServerFilter(IServerFilter iServerFilter) {
        iServerFilters.add(iServerFilter);
    }

    public void doFilter(RpcInvocation rpcInvocation) {
        for (IServerFilter iServerFilter : iServerFilters) {
            iServerFilter.doFilter(rpcInvocation);
        }
    }
}
