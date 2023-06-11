package common.event.listener;

import client.ConnectionHandler;
import cn.hutool.core.collection.CollUtil;
import common.ChannelFutureWrapper;
import common.event.IRpcUpdateEvent;
import common.event.URLChangeWrapper;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static common.cache.CommonClientCache.CONNECT_MAP;

/**
 * 客户端监听事件：zk的服务提供者节目的发生了变更
 * 客户端需要更新本地的一个目标服务列表，避免向无用的服务发送请求
 * 监听层级：对第二层，即监听服务名称下的服务提供者
 */
@Slf4j
public class ServiceUpdateListener implements IRpcListener<IRpcUpdateEvent> {

    /**
     * @param t
     */
    @Override
    public void callBack(Object t) {

        //获取到字节点的数据信息
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper) t;
        // 当前已存在的旧的URL
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(urlChangeWrapper.getServiceName());
        if (CollUtil.isEmpty(channelFutureWrappers)) {
            log.error("[ServiceUpdateListener] channelFutureWrappers is empty");
            return;
        } else {
            // 客户端监听到的最新的服务提供者的URL
            List<String> matchProviderUrl = urlChangeWrapper.getProviderUrl();
            // final开头的为结果集
            Set<String> finalUrl = new HashSet<>();
            List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();

            // 遍历旧的URL 检查是否还在
            // 移除没有用到的 老的 URL
            for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
                String oldServerAddress = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
                // 如果老的URL不存在 则不处理
                if (!matchProviderUrl.contains(oldServerAddress)) {
                    // todo 连接因为服务端已经下线了 客户端不用下线吗？
                    // todo 将引用置空？
                    channelFutureWrapper = null;
                    continue;
                } else {
                    // 如果存在的话 加入结果集
                    finalChannelFutureWrappers.add(channelFutureWrapper);
                    finalUrl.add(oldServerAddress);
                }
            }

            // 添加新的 URL
            // ChannelFutureWrapper其实是一个自定义的包装类，将netty建立好的ChannelFuture做了一些封装
            List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList<>();

            // 遍历新的URL 将结果集中没有的加入结果集
            for (String newProviderUrl : matchProviderUrl) {
                // 如果结果集中不存在 则进行添加
                if (!finalUrl.contains(newProviderUrl)){
                    ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                    String host = newProviderUrl.split(":")[0];
                    Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);
                    channelFutureWrapper.setPort(port);
                    channelFutureWrapper.setHost(host);
                    ChannelFuture channelFuture = null;
                    try {
                        // 如果有新的服务提供者则新建连接
                        channelFuture = ConnectionHandler.createChannelFuture(host,port);
                        channelFutureWrapper.setChannelFuture(channelFuture);

                        newChannelFutureWrapper.add(channelFutureWrapper);
                        finalUrl.add(newProviderUrl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            finalChannelFutureWrappers.addAll(newChannelFutureWrapper);
            //最终更新服务在这里
            CONNECT_MAP.put(urlChangeWrapper.getServiceName(),finalChannelFutureWrappers);
        }
    }

}
