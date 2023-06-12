package common.cache;

import cn.hutool.core.map.MapUtil;
import common.config.ServerConfig;
import filter.server.ServerFilterChain;
import registy.RegistryService;
import registy.URL;
import serialize.SerializeFactory;
import server.ServiceWrapper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommonServerCache {

    /**
     * 重要缓存<br>
     * k：服务接口名称 v：服务接口实现的实例对象<br>
     * 作用：存放服务提供者服务实例对象<br>
     * 实例对象用于客户端rpc调用
     */
    public static final Map<String, Object> PROVIDER_CLASS_MAP = MapUtil.newHashMap();
    /**
     * 重要缓存<br>
     * 存放内容：当前服务提供者提供了那几个服务接口
     * 作用：存放服务提供者服务实例对象<br>
     * 实例对象用于客户端rpc调用
     */
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();

    public static RegistryService REGISTRY_SERVICE;

    // ===========================serialize=======================
    /**
     * 重要缓存<br>
     * 全局定义的客户端序列化工厂<br>
     */
    public static SerializeFactory SERVER_SERIALIZE_FACTORY;

    public static ServerConfig SERVER_CONFIG;

    // ===========================filter=======================
    /**
     * 过滤链的实例
     */
    public static ServerFilterChain SERVER_FILTER_CHAIN;

    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();
}
