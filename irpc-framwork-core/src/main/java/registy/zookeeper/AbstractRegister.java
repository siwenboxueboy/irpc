package registy.zookeeper;

import registy.RegistryService;
import registy.URL;

import java.util.List;
import java.util.Map;

import static common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * 本类行为：
 * 1. 对一些注册数据做统一的处理，假设日后需要考虑支持多种类型的注册中心，例如redis 、 etcd之类的话，所有基础的记录操作都可以统一放在抽象类里实现;
 * 2. 同时为了考虑到，后续留给子类可以做更多的拓展行为，我在抽象层也封装了一些扩展函数，诸如doAfterSubscribe之类的
 */
public abstract class AbstractRegister implements RegistryService {

    /**
     * 注册url
     * <p>
     * 将irpc服务写入注册中心节点
     * 当出现网络抖动的时候需要进行适当的重试做法
     * 注册服务url的时候需要写入持久化文件中
     *
     * @param url
     */
    @Override
    public void register(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    /**
     * 服务下线
     * <p>
     * 持久化节点是无法进行服务下线操作的
     * 下线的服务必须保证url是完整匹配的
     * 移除持久化文件中的一些内容信息
     *
     * @param url
     */
    @Override
    public void unRegister(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    /**
     * 消费方订阅服务
     *
     * @param url
     */
    @Override
    public void subscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.add(url);
    }

    /**
     * 执行取消订阅内部的逻辑
     *
     * @param url
     */
    @Override
    public void doUnSubscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.remove(url.getServiceName());
    }

    /**
     * 留给子类扩展
     *
     * @param url
     */
    public abstract void doAfterSubscribe(URL url);

    /**
     * 留给子类扩展
     *
     * @param url
     */
    public abstract void doBeforeSubscribe(URL url);

    /**
     * 留给子类扩展
     *
     * @param serviceName
     * @return
     */
    public abstract List<String> getProviderIps(String serviceName);

    /**
     * 获取服务的权重信息
     *
     * @param serviceName
     * @return <ip:port --> urlString>,<ip:port --> urlString>,<ip:port --> urlString>,<ip:port --> urlString>
     */
    public abstract Map<String, String> getServiceWeightMap(String serviceName);
}
