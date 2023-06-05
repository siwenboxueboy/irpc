package common;

import io.netty.channel.ChannelFuture;

public class ChannelFutureWrapper {

    private String host;
    private Integer port;
    private ChannelFuture channelFuture;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }
}
