package filter.client;

import common.ChannelFutureWrapper;
import common.RpcInvocation;
import common.utils.CommonUtils;
import filter.IClientFilter;

import java.util.List;

/**
 * 基于分组的过滤链路<br>
 * 方便ABTest
 */
public class GroupFilterImpl implements IClientFilter {

    /**
     * 执行过滤链
     *
     * @param src
     * @param rpcInvocation
     * @return
     */
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String group = String.valueOf(rpcInvocation.getAttachments().get("group"));
        for (ChannelFutureWrapper channelFutureWrapper : src) {
            if (!channelFutureWrapper.getGroup().equals(group)) {
                src.remove(channelFutureWrapper);
            }
        }
        if (CommonUtils.isEmptyList(src)) {
            throw new RuntimeException("no provider match for group " + group);
        }
    }
}
