package common.cache;

import filter.client.ClientFilterChain;
import common.ChannelFuturePollingRef;
import common.ChannelFutureWrapper;
import common.RpcInvocation;
import common.config.ClientConfig;
import registy.URL;
import router.IRouter;
import serialize.SerializeFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CommonClientCache {
    // ===========================proxy=======================
    /**
     * 重要缓存<br>
     * 存放rpc请求的阻塞队列
     * 作用：可以异步处理rpc请求
     * */
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue(100);
    /**
     * 重要缓存<br>
     * k：rpc请求时的唯一uuid v：rpc请求<br>
     * 作用：存放已经请求的rpc调用，但是还未有返回值返回<br>
     * 原理：rpc请求有响应时，根据uuid对应原请求，然后设置原请求的响应结构，最后原代理轮询得到结果<br>
     * */
    public static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();
    public static ClientConfig CLIENT_CONFIG;
    // ===========================client subscribe=======================
    /**
     * 重要缓存<br>
     * 客户端订阅服务接口列表 --> 记录客户端订阅了哪些服务接口<br>
     * 客户端订阅、取消订阅服务接口时维护<br>
     * 作用：客户端启动时，拿出这些客户端已经订阅了的服务接口，与这些接口的服务提供者建立网络连接<br>
     * */
    public static List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();

    /**
     * 存储信息：客户端所订阅的服务提供者的zk缓存信息<br>
     * 作用：客户端订阅时拿到 在客户端连接服务提供者时仅作展示，目前作用不明<br>
     * 格式：com.sise.test.service -> <<ip:host,urlString>,<ip:host,urlString>,<ip:host,urlString>><br>
     * */
    public static Map<String, Map<String,String>> URL_MAP = new ConcurrentHashMap<>();

    // ===========================consumer connect provider=======================
    /**
     * 当前客户端已经连接的服务提供者的地址<br>
     * 客户端连接，取消连接服务提供者时维护<br>
     * 作用：不明<br>
     * */
    public static Set<String> SERVER_ADDRESS = new HashSet<>();

    /**
     * 重要缓存<br>
     * 当前客户端已经连接的服务提供者的连接信息<br>
     * 维护：客户端成功连接了服务提供者时进行添加，服务提供者下线时，或者服务提供者发生权重信息更改时进行修改<br>
     * 作用：每次进行远程调用的时候都是从这里面去选择服务提供者<br>
     * k: 服务接口名称  v：服务提供者列表<br>
     * */
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();

    // ===========================client router=======================
    /**
     * 重要缓存<br>
     * 路由缓存<br>
     * 作用：暂存由具体路由策略生成的路由数组<br>
     * 维护：具体的路由策略的路由生成方法生成<br>
     * */
    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();
    // 客户端调用的时候从这里取下一个服务提供者的连接
    /**
     * 重要缓存<br>
     * 对上述路由缓存的封装<br>
     * 作用：确定本次服务调用的服务提供者<br>
     *
     * */
    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();

    /**
     * 重要缓存<br>
     * 全局定义的路由<br>
     * 生命周期：客户端初始化时生成<br>
     * 维护路由：在消费者连接提供者后路由刷新<br>
     * 维护权重：客户端主动监听提供者权重变化信息，监听到信息后进行更新路由权重<br>
     * 作用：统一路由行为，维护路由及权重<br>
     *
     * */
    public static IRouter IROUTER;

    // ===========================serialize=======================
    /**
     * 重要缓存<br>
     * 全局定义的客户端序列化工厂<br>
     *
     * */
    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;
    // ===========================filter=======================
    /**
     * 过滤链的实例
     * */
    public static ClientFilterChain CLIENT_FILTER_CHAIN;
}
