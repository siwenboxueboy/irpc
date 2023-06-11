package common;

import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChannelFutureWrapper {

    private String host;
    private Integer port;
    private ChannelFuture channelFuture;
    // 权重 权重值约定好配置是100的整倍数 权重越大则该提供者可用性更好
    private Integer weight;

    public ChannelFutureWrapper() {
    }

    public ChannelFutureWrapper(String host, Integer port, Integer weight) {
        this.host = host;
        this.port = port;
        this.weight = weight;
    }
}
