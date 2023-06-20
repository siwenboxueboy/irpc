package common.exception;

import common.RpcInvocation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IRpcException extends RuntimeException{
    private RpcInvocation rpcInvocation;
}
