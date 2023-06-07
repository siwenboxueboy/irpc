package common.cache;

import common.ChannelFutureWrapper;
import common.RpcInvocation;
import common.config.ClientConfig;
import registy.URL;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CommonClientCache {

    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue(100);
    public static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();
    public static ClientConfig CLIENT_CONFIG;
    //provider名称 --> 该服务有哪些集群URL 客户端向服务提供者接口连接时用
    public static List<String> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();
    public static Map<String, List<URL>> URL_MAP = new ConcurrentHashMap<>();
    // 当前消费者拥有的服务提供者的地址
    public static Set<String> SERVER_ADDRESS = new HashSet<>();
    //每次进行远程调用的时候都是从这里面去选择服务提供者
    // k: 服务接口名称  v：服务提供者列表
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();
}
