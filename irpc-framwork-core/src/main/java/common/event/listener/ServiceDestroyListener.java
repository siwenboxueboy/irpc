package common.event.listener;

import common.event.IRpcDestroyEvent;
import registy.URL;

import static common.cache.CommonServerCache.PROVIDER_URL_SET;
import static common.cache.CommonServerCache.REGISTRY_SERVICE;

/**
 * 服务注销 监听器
 * */
public class ServiceDestroyListener implements IRpcListener<IRpcDestroyEvent> {

    @Override
    public void callBack(Object t) {
        for (URL url : PROVIDER_URL_SET) {
            REGISTRY_SERVICE.unRegister(url);
        }
    }
}
