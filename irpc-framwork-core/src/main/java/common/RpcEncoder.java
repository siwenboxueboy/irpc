package common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static common.constants.RpcConstants.DEFAULT_DECODE_CHAR;

/**
 * RPC编码器
 * 当数据发送之前，会通过这个模块
 * 序列化处理
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {

    /**
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out) throws Exception {
        out.writeShort(msg.getMagicNumber());
        out.writeInt(msg.getContentLength());
        out.writeBytes(msg.getContent());
        // out.writeBytes(DEFAULT_DECODE_CHAR.getBytes());
    }

}
