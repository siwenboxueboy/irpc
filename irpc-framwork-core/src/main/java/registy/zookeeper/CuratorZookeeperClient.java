package registy.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

public class CuratorZookeeperClient extends AbstractZookeeperClient {

    /**
     * Zookeeper开源的客户端框架
     */
    private CuratorFramework client;

    public CuratorZookeeperClient(String zkAddress) {
        this(zkAddress, null, null);
    }

    public CuratorZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetryTimes) {
        super(zkAddress, baseSleepTimes, maxRetryTimes);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(super.getBaseSleepTimes(), super.getMaxRetryTimes());
        if (client == null) {
            client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
            client.start();
        }
    }

    /**
     * @param address
     * @param data
     */
    @Override
    public void updateNodeData(String address, String data) {
        try {
            client.setData().forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    @Override
    public CuratorFramework getClient() {
        return client;
    }

    /**
     * 拉取节点的数据
     *
     * @param address
     * @return
     */
    @Override
    public String getNodeData(String address) {
        byte[] result = new byte[0];
        try {
            result = client.getData().forPath(address);
            if (result != null) {
                return new String(result);
            }
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定目录下的字节点数据
     *
     * @param path
     * @return
     */
    @Override
    public List<String> getChildrenData(String path) {

        try {
            List<String> childrenData = client.getChildren().forPath(path);
            return childrenData;
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建持久化类型节点数据信息
     *
     * @param address
     * @param data
     */
    @Override
    public void createPersistentData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param address
     * @param data
     */
    @Override
    public void createPersistentWithSeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建有序且临时类型节点数据信息
     *
     * @param address
     * @param data
     */
    @Override
    public void createTemporarySeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建临时节点数据类型信息
     *
     * @param address
     * @param data
     */
    @Override
    public void createTemporaryData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(address, data.getBytes());
        } catch (KeeperException.NoChildrenForEphemeralsException e) {
            try {
                client.setData().forPath(address, data.getBytes());
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * 设置某个节点的数值
     *
     * @param address
     * @param data
     */
    @Override
    public void setTemporaryData(String address, String data) {
        try {
            client.setData().forPath(address, data.getBytes());
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * 断开zk的客户端链接
     */
    @Override
    public void destroy() {
        client.close();
    }

    /**
     * 展示节点下边的数据
     *
     * @param address
     */
    @Override
    public List<String> listNode(String address) {
        try {
            return client.getChildren().forPath(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 删除节点下边的数据
     *
     * @param address
     * @return
     */
    @Override
    public boolean deleteNode(String address) {
        try {
            client.delete().forPath(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断是否存在其他节点
     *
     * @param address
     * @return
     */
    @Override
    public boolean existNode(String address) {
        try {
            Stat stat = client.checkExists().forPath(address);
            return stat != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 监听path路径下某个节点的数据变化
     *
     * @param path
     * @param watcher
     */
    @Override
    public void watchNodeData(String path, Watcher watcher) {
        try {
            client.getData().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听子节点下的数据变化
     *
     * @param path
     * @param watcher
     */
    @Override
    public void watchChildNodeData(String path, Watcher watcher) {
        try {
            client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
