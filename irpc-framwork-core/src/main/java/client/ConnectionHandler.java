package client;

import cn.hutool.core.collection.CollUtil;
import common.ChannelFutureWrapper;
import common.RpcInvocation;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import registy.URL;
import registy.zookeeper.ProviderNodeInfo;
import router.Selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static common.cache.CommonClientCache.*;

/**
 * 职责： 当注册中心的节点新增或者移除或者权重变化的时候，这个类主要负责对内存中的url做变更
 * 将连接的建立，断开，按照服务名筛选等功能都封装在了一起，按照单一职责的设计原则，将与连接有关的功能都统一封装在了一起。
 */
@Slf4j
public class ConnectionHandler {

    /**
     * 核心的连接处理器
     * 专门用于负责和服务端构建连接通信
     */
    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap) {
        ConnectionHandler.bootstrap = bootstrap;
    }

    /**
     * 构建单个连接通道 元操作，既要处理连接，还要统一将连接进行内存存储管理
     * 添加某个服务接口的某个服务提供者
     *
     * @param providerIp
     * @return
     * @throws InterruptedException
     */
    public static void connect(String providerServiceName, String providerIp) throws InterruptedException {
        if (bootstrap == null) {
            // RuntimeException及其子类：这一类异常其实是程序设计的错误，通过修正程序设计是可以避免的。例如，数组越界异常、数值异常等。
            // 非RuntimeException及其子类：这一类异常的发生通常由外部因素导致，是不可预知和避免的。例如，IO异常、类型寻找异常等。
            // 比如jackson的read 要try-catch 这类就时由外部的不确定的json串导致的异常，是不可预知和避免的，要显式的捕获并处理
            throw new RuntimeException("bootstrap can not be null");
        }
        // 格式错误类型的信息
        if (!providerIp.contains(":")) {
            return;
        }
        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];
        Integer port = Integer.parseInt(providerAddress[1]);
        // 到底这个channelFuture里面是什么
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        String providerURLInfo = URL_MAP.get(providerServiceName).get(providerIp);
        ProviderNodeInfo providerNodeInfo = URL.buildURLFromUrlStr(providerURLInfo);
        log.info("providerURLInfo is : {}", providerURLInfo);
        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);
        channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
        channelFutureWrapper.setGroup(providerNodeInfo.getGroup());
        // 该消费者记录了服务提供者的地址
        SERVER_ADDRESS.add(providerIp);

        // 该消费者 更新该服务接口的 服务提供者列表
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CollUtil.isEmpty(channelFutureWrappers)) {
            channelFutureWrappers = new ArrayList<>();
        }
        channelFutureWrappers.add(channelFutureWrapper);
        CONNECT_MAP.put(providerServiceName, channelFutureWrappers);
        // 路由层生成对某个服务不同服务提供者的调用顺序
        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        IROUTER.refreshRouterArr(selector);
    }


    /**
     * 构建ChannelFuture
     *
     * @param ip
     * @param port
     * @return
     * @throws InterruptedException
     */
    public static ChannelFuture createChannelFuture(String ip, Integer port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        return channelFuture;
    }

    /**
     * 断开连接 移除某个服务接口的某个服务提供者
     *
     * @param providerServiceName
     * @param providerIp
     */
    public static void disConnect(String providerServiceName, String providerIp) {
        SERVER_ADDRESS.remove(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CollUtil.isNotEmpty(channelFutureWrappers)) {
            Iterator<ChannelFutureWrapper> iterator = channelFutureWrappers.iterator();
            while (iterator.hasNext()) {
                ChannelFutureWrapper channelFutureWrapper = iterator.next();
                if (providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort())) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 默认走随机策略获取ChannelFuture
     *
     * @param rpcInvocation
     * @return
     */
    public static ChannelFuture getChannelFuture(RpcInvocation rpcInvocation) {
        String providerServiceName = rpcInvocation.getTargetServiceName();
        // 判断已经定义的路由规则是否已经生成
        ChannelFutureWrapper[] channelFutureWrappers = SERVICE_ROUTER_MAP.get(providerServiceName);
        if (channelFutureWrappers == null || channelFutureWrappers.length == 0) {
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }

        // 责任链 客户端在获取到目标方的channel集合之后需要进行筛选过滤，最终才会发起真正的请求
        CLIENT_FILTER_CHAIN.doFilter(Arrays.asList(channelFutureWrappers),rpcInvocation);

        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        selector.setChannelFutureWrappers(channelFutureWrappers);
        ChannelFuture channelFuture = IROUTER.select(selector).getChannelFuture();
        return channelFuture;
    }

}
