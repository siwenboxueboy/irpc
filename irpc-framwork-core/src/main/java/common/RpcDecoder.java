package common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * RPC解码器
 * 在实现解码的过程中需要考虑是否会有粘包拆包的问题，而且还要设置请求数据包体积最大值
 */
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * 协议的开头部分的标准长度
     */
    public final int BASE_LENGTH = 2 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {

//        必须大于等于标准长度才算是正常的包
        if (byteBuf.readableBytes() >= BASE_LENGTH) {
            // 防止收到一些体积过大的数据包
            // todo why?
            if (byteBuf.readableBytes() > 1000) {
                byteBuf.skipBytes(byteBuf.readableBytes());
            }

            int beginReader;
            // todo 这里为啥要while true？
            while (true) {
                // 初始的位置在这里 用于如果帧不完整到达时，重置下一次的读取位置
                beginReader = byteBuf.readerIndex();
                byteBuf.markReaderIndex();

                if (byteBuf.readShort() == MAGIC_NUMBER) {
                    break;
                } else {
                    // 非魔数
                    ctx.close(); // 非法数据包
                    return;
                }
            }

            // 对应了RpcProtocol的contentLength字段
            int length = byteBuf.readInt();

            // 说明剩余的数据包不是完整的，这里需要重置下索引
            if (byteBuf.readableBytes() < length) {
                // 重置读取位置
                byteBuf.readerIndex(beginReader);
                // 现在不获取
                return;
            }
            // 直到帧完整到达
            byte[] data = new byte[length];
            byteBuf.readBytes(data);
            RpcProtocol rpcProtocol = new RpcProtocol(data);
            out.add(rpcProtocol);
        }


    }
}
