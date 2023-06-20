package server;

import common.RpcProtocol;
import filter.server.ServerChannelReadData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.InvocationTargetException;

import static common.cache.CommonServerCache.SERVER_CHANNEL_DISPATCHER;

/**
 * RpcDecoder解码之后的处理
 * 服务端解析客户端的请求
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InvocationTargetException, IllegalAccessException {
        //服务端接收数据的时候统一以RpcProtocol协议的格式接收，具体的发送逻辑见文章下方客户端发送部分
        ServerChannelReadData serverChannelReadData = new ServerChannelReadData();
        serverChannelReadData.setRpcProtocol((RpcProtocol) msg);
        serverChannelReadData.setChannelHandlerContext(ctx);
        //放入channel分发器
        SERVER_CHANNEL_DISPATCHER.add(serverChannelReadData);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
