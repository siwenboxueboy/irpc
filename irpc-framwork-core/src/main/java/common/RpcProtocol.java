package common;

import lombok.Data;

import java.io.Serializable;

import static common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * 自定义协议体
 * 采用固定协议传输的方式去解决网络中的拆包和粘包问题
 */
@Data
public class RpcProtocol implements Serializable {

    public RpcProtocol(byte[] content) {
        this.content = content;
        this.contentLength = content.length;
    }

    private static final long serialVersionUID = 7111945433171539393L;

    // 魔法数，主要是在做服务通讯的时候定义的一个安全检测，确认当前请求的协议是否合法。
    private short magicNumber = MAGIC_NUMBER;
    // 协议传输核心数据的长度。这里将长度单独拎出来设置有个好处，当服务端的接收能力有限，可以对该字段进行赋值。
    // 当读取到的网络数据包中的contentLength字段已经超过预期值的话，就不会去读取content字段
    private int contentLength;
    //这个字段其实是RpcInvocation类的字节数组，在RpcInvocation中包含了更多的调用信息，详情见下方介绍
    // 核心的传输数据，这里核心的传输数据主要是请求的服务名称，请求服务的方法名称，请求参数内容。
    // 为了方便后期扩展，这些核心的请求数据我都统一封装到了RpcInvocation对象当中。
    private byte[] content;

}
