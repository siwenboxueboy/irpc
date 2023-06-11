package common.cache;

import cn.hutool.core.map.MapUtil;
import registy.RegistryService;
import registy.URL;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonServerCache {

    /**
     * 重要缓存<br>
     * k：服务接口名称 v：服务接口实现的实例对象<br>
     * 作用：存放服务提供者服务实例对象<br>
     * 实例对象用于客户端rpc调用
     * */
    public static final Map<String, Object> PROVIDER_CLASS_MAP = MapUtil.newHashMap();
    /**
     * 重要缓存<br>
     * 存放内容：当前服务提供者提供了那几个服务接口
     * 作用：存放服务提供者服务实例对象<br>
     * 实例对象用于客户端rpc调用
     * */
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();

    public static RegistryService REGISTRY_SERVICE;
}
