package filter.server;

import common.RpcInvocation;
import filter.IServerFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerLogFilterImpl implements IServerFilter {
    /**
     * 执行核心过滤逻辑
     *
     * @param rpcInvocation
     */
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        log.info("{} do invoke -----> {}", rpcInvocation.getAttachments().get("c_app_name"), rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
