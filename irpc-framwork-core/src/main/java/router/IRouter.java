package router;

import common.ChannelFutureWrapper;
import registy.URL;

/**
 * 路由层的抽象
 * */
public interface IRouter {

    /**
     * 缺陷：
     * 假设目标机器的性能不一，如何对机器进行权重分配？
     * 每次都要执行jdk自带的Random函数，在高并发情况下对CPU的消耗会比较高。
     * 如何基于路由策略做ABTest？
     * 解决：
     * 提供路由层，自定义路由策略解决上述问题
     * 随机调用的思路设计的路由实现类。随机调用自定义实现，不消耗性能版本。
     * */

    /**
     * 刷新路由数组
     *
     * @param selector
     */
    void refreshRouterArr(Selector selector);

    /**
     * 获取到请求到连接通道
     *
     * @return
     */
    ChannelFutureWrapper select(Selector selector);

    /**
     * 更新权重信息
     *
     * @param url
     */
    void updateWeight(URL url);
}
