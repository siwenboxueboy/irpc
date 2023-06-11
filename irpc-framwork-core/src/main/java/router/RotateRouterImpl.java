package router;

import common.ChannelFutureWrapper;
import registy.URL;

import java.util.List;

import static common.cache.CommonClientCache.*;

/**
 * 轮询策略
 * 更新某个服务的客户端调用顺序
 */
public class RotateRouterImpl implements IRouter {

    /**
     * 刷新路由数组
     *
     * @param selector
     */
    @Override
    public void refreshRouterArr(Selector selector) {
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        for (int i=0;i<channelFutureWrappers.size();i++) {
            arr[i]=channelFutureWrappers.get(i);
        }
        SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(),arr);
    }

    /**
     * 获取到请求到连接通道
     *
     * @param selector
     * @return
     */
    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getProviderServiceName());
    }

    /**
     * 更新权重信息
     *
     * @param url
     */
    @Override
    public void updateWeight(URL url) {

    }
}
