package common.exception;

import common.RpcInvocation;

public class MaxServiceLimitRequestException extends IRpcException {
    public MaxServiceLimitRequestException(RpcInvocation rpcInvocation) {
        super(rpcInvocation);
    }
}