package registy.zookeeper;

import com.alibaba.fastjson.JSON;
import common.event.*;
import interfaces.DataService;
import org.apache.zookeeper.Watcher;
import registy.RegistryService;
import registy.URL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.cache.CommonClientCache.CLIENT_CONFIG;
import static common.cache.CommonServerCache.SERVER_CONFIG;

/**
 * 服务端 注册 取消
 * 客户端 订阅 取消订阅
 * 订阅范围：
 */
public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private AbstractZookeeperClient zkClient;

    private String ROOT = "/irpc";

    /**
     * newInstance 时必须得用无参构造
     * */
    public ZookeeperRegister() {
        String registryAddr = CLIENT_CONFIG!= null ? CLIENT_CONFIG.getRegisterAddr() : SERVER_CONFIG.getRegisterAddr();
        this.zkClient = new CuratorZookeeperClient(registryAddr);
    }
    public ZookeeperRegister(String address) {
        this.zkClient = new CuratorZookeeperClient(address);
    }

    /**
     * 留给子类扩展
     *
     * @param url
     */
    @Override
    public void doAfterSubscribe(URL url) {
        // 客户端订阅之后做的事情
        // 监听是否有新的服务注册
        String servicePath = url.getParameters().get("servicePath");
        String newServerNodePath = ROOT + "/" + servicePath;
        watchChildNodeData(newServerNodePath);


        // 某个服务权重的更新
        String providerIpStrJson = url.getParameters().get("providerIps");
        List<String> providerIpList = JSON.parseObject(providerIpStrJson, List.class);
        for (String providerIp : providerIpList) {
            this.watchNodeDataChange(ROOT + "/" + servicePath + "/" + providerIp);
        }
    }

    /**
     * 订阅服务节点内部的数据变化
     * 用于处理节点权重的更新
     * @param newServerNodePath
     */
    public void watchNodeDataChange(String newServerNodePath) {
        zkClient.watchNodeData(newServerNodePath, watchedEvent -> {
            String path = watchedEvent.getPath();
            String nodeData = zkClient.getNodeData(path);
            nodeData = nodeData.replace(";","/");
            ProviderNodeInfo providerNodeInfo = URL.buildURLFromUrlStr(nodeData);
            IRpcEvent iRpcEvent = new IRpcNodeChangeEvent(providerNodeInfo);
            IRpcListenerLoader.sendEvent(iRpcEvent);
            watchNodeDataChange(newServerNodePath);
        });
    }

    /**
     * 订阅服务孩子节点的数据变化
     * 用于监听处理某个服务接口的某个服务提供者的上下线
     * */
    public void watchChildNodeData(String newServerNodePath) {
        Watcher watcher = watchedEvent -> {
            System.out.println(watchedEvent);
            String path = watchedEvent.getPath();
            List<String> childrenDataList = zkClient.getChildrenData(path);
            URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
            urlChangeWrapper.setProviderUrl(childrenDataList);
            urlChangeWrapper.setServiceName(path.split("/")[2]);
            //自定义的一套事件监听组件
            // 当zookeeper的某个节点发生数据变动的时候，就会发送一个变更事件，然后由对应的监听器去捕获这些数据并做处理
            IRpcEvent iRpcEvent = new IRpcUpdateEvent(urlChangeWrapper);
            IRpcListenerLoader.sendEvent(iRpcEvent);

            //收到回调之后在注册一次监听，这样能保证一直都收到消息
            // 因为zk节点的消息通知其实是只具有一次性的功效，所以可能会出现第一次修改节点之后发送一次通知，之后再次修改节点不再会发送节点变更通知操作
            watchChildNodeData(path);
        };
        zkClient.watchChildNodeData(newServerNodePath, watcher);

    }

    /**
     * 留给子类扩展
     *
     * @param url
     */
    @Override
    public void doBeforeSubscribe(URL url) {

    }

    /**
     * 留给子类扩展
     *
     * @param serviceName
     * @return
     */
    @Override
    public List<String> getProviderIps(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        return nodeDataList;
    }

    /**
     * 获取服务的权重信息
     *
     * @param serviceName
     * @return <ip:port --> urlString>,<ip:port --> urlString>,<ip:port --> urlString>,<ip:port --> urlString>
     */
    @Override
    public Map<String, String> getServiceWeightMap(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        Map<String, String> result = new HashMap<>();
        for (String ipAndHost : nodeDataList) {
            String childData = this.zkClient.getNodeData(ROOT + "/" + serviceName + "/provider/" + ipAndHost);
            result.put(ipAndHost, childData);
        }
        return result;
    }

    /**
     * 注册url
     * <p>
     * 将irpc服务写入注册中心节点
     * 当出现网络抖动的时候需要进行适当的重试做法
     * 注册服务url的时候需要写入持久化文件中
     * 服务注册的范围：服务类的提供者及其IP端口
     *
     * @param url
     */
    @Override
    public void register(URL url) {
        if (!this.zkClient.existNode(ROOT)) {
            // 创建持久化节点
            zkClient.createPersistentData(ROOT, "");
        }
        // zk对应叶子节点路径要存储的值
        String urlStr = URL.buildProviderUrlStr(url);
        // zk的路径
        String providerPath = getProviderPath(url);
        if (!zkClient.existNode(providerPath)) {
            zkClient.createTemporaryData(providerPath, urlStr);
        } else {
            zkClient.deleteNode(providerPath);
            zkClient.createTemporaryData(providerPath, urlStr);
        }
        super.register(url);
    }

    private String getProviderPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters().get("host") + ":" + url.getParameters().get("port");
    }

    private String getConsumerPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName() + ":" + url.getParameters().get("host") + ":";
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
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);
    }

    /**
     * 消费方订阅服务
     *
     * @param url
     */
    @Override
    public void subscribe(URL url) {
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildConsumerUrlStr(url);
        String consumerPath = getConsumerPath(url);
        if (!zkClient.existNode(consumerPath)) {
            zkClient.createTemporarySeqData(consumerPath, urlStr);
        } else {
            zkClient.deleteNode(consumerPath);
            zkClient.createTemporarySeqData(consumerPath, urlStr);
        }
        super.subscribe(url);
    }

    /**
     * 执行取消订阅内部的逻辑
     *
     * @param url
     */
    @Override
    public void doUnSubscribe(URL url) {
        this.zkClient.deleteNode(getConsumerPath(url));
        super.doUnSubscribe(url);
    }

    public static void main(String[] args) throws InterruptedException {
        ZookeeperRegister zookeeperRegister = new ZookeeperRegister("localhost:2181");
        List<String> urls = zookeeperRegister.getProviderIps(DataService.class.getName());
        System.out.println(urls);
        Thread.sleep(2000000);
    }

}
