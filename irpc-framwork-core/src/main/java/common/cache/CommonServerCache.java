package common.cache;

import cn.hutool.core.map.MapUtil;
import registy.RegistryService;
import registy.URL;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonServerCache {
    // k：暴露的服务接口名称 v：服务接口实现实例对象，用于客户端调用
    public static final Map<String, Object> PROVIDER_CLASS_MAP = MapUtil.newHashMap();

    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();

    public static RegistryService REGISTRY_SERVICE;
}
