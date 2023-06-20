package server;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 最大连接数限制处理器<br>
 * 作用：所有的请求数据都通过一条连接来传输数据，但是在微服务集群的模式下，服务提供者有可能会同时和上百个服务调用方建立连接，所以当服务提供方的负载压力达到一定阈值的条件下就应该减少外界新访问的连接<br>
 * 设计：对服务端的要有一个统一的连接数控制，比如最大连接限制为512，当前连接数超过512则超出的部分直接拒绝<br>
 * 编码位置：MainReactor负责客户端的连接请求，将请求转发给SubReactor。建立连接这部分的事件处理，应该由MainReactor部分处理，所以这块就可以自定义一个连接限制器来控制连接数目<br>
 */
@Slf4j
@ChannelHandler.Sharable
public class MaxConnectionLimitHandler extends ChannelInboundHandlerAdapter {

    private final int maxConnectionNum;
    private final AtomicInteger numConnection = new AtomicInteger(0);

    private final Set<Channel> childChannel = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // 这是在jdk1.8之后出现的对于AtomicLong的优化版本
    // 记录取消连接的个数
    // todo 有什么用?
    private final LongAdder numDroppedConnections = new LongAdder();

    private final AtomicBoolean loggingScheduled = new AtomicBoolean(false);

    public MaxConnectionLimitHandler(int maxConnectionNum) {
        this.maxConnectionNum = maxConnectionNum;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("connection limit handler");
        Channel channel = (Channel) msg;
        int conn = numConnection.incrementAndGet();

        if (conn > 0 && conn <= maxConnectionNum) {
            // 可以连接
            this.childChannel.add(channel);
            // 设置监听 待连接关闭时 释放numConn
            channel.closeFuture().addListener(future -> {
                childChannel.remove(channel);
                numConnection.decrementAndGet();
            });
            super.channelRead(ctx, msg);
        } else {
            // 连接失败
            numConnection.decrementAndGet();
            // 避免产生大量的time_wait连接
            // 当断开链接的时候，建议可以返回一些简单的数据包告知客户端，防止客户端的链接处于TIME_WAIT状态。
            channel.config().setOption(ChannelOption.SO_LINGER, 0);
            channel.unsafe().closeForcibly();

            numDroppedConnections.increment();

            // 这里加入一道cas可以减少一些并发请求的压力,定期地执行一些日志打印
            if (loggingScheduled.compareAndSet(false, true)) {
                ctx.executor().schedule(this::writeNumDroppedConnectionLog, 1, TimeUnit.SECONDS);
            }
        }

    }

    /**
     * 记录连接失败的日志
     */
    private void writeNumDroppedConnectionLog() {
        loggingScheduled.set(false);
        final long dropped = numDroppedConnections.sumThenReset();
        if (dropped > 0) {
            log.error("Dropped {} connection(s) to protect server,maxConnection is {}", dropped, maxConnectionNum);
        }
    }
}
