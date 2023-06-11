package server;

import com.alibaba.fastjson.JSON;
import common.RpcInvocation;
import common.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static common.cache.CommonServerCache.PROVIDER_CLASS_MAP;
import static common.cache.CommonServerCache.SERVER_SERIALIZE_FACTORY;

/**
 * RpcDecoder解码之后的处理
 * 服务端解析客户端的请求
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InvocationTargetException, IllegalAccessException {
        //服务端接收数据的时候统一以RpcProtocol协议的格式接收，具体的发送逻辑见文章下方客户端发送部分
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        // 反序列化请求
        RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
        //这里的PROVIDER_CLASS_MAP就是一开始预先在启动时候存储的Bean集合
        Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
        Method[] methods = aimObject.getClass().getDeclaredMethods();
        Object result = null;
        for (Method method : methods) {
            if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                // 通过反射找到目标对象，然后执行目标方法并返回对应值
                if (method.getReturnType().equals(Void.TYPE)) {
                    method.invoke(aimObject, rpcInvocation.getArgs());
                } else {
                    result = method.invoke(aimObject, rpcInvocation.getArgs());
                }
                break;
            }
        }
        // 请求完成，设置
        rpcInvocation.setResponse(result);
        // 序列化执行结果
        RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
        // 回复给客户端结果
        ctx.writeAndFlush(respRpcProtocol);
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
