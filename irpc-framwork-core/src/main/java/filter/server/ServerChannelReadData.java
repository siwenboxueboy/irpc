package filter.server;

import common.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

@Data
public class ServerChannelReadData {
    private RpcProtocol rpcProtocol;

    private ChannelHandlerContext channelHandlerContext;
}
