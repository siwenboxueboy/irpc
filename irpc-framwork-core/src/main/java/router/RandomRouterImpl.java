package router;

import common.ChannelFutureWrapper;
import registy.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static common.cache.CommonClientCache.*;

public class RandomRouterImpl implements IRouter {

    /**
     * 设计思路：在客户端和服务提供者进行连接建立的环节会触发路由层的一个refreshRouterArr函数，生成对应先后顺序的随机数组，并且将其存放在一个map集合中。
     * */

    /**
     * 刷新路由数组
     *
     * @param selector
     */
    @Override
    public void refreshRouterArr(Selector selector) {
        //获取服务提供者的数目
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];

        //提前一次性生成调用先后顺序的随机数组
        //避免每次消息发送时都要生成随机数 jdk生成随机数函数random需要切到内核态，开销比较大，这样用空间换时间，减少调用时的调用开销
        int[] result = createRandomIndex(arr.length);

        //生成对应服务集群的每台机器的调用顺序
        for (int i = 0; i < result.length; i++) {
            arr[i] = channelFutureWrappers.get(result[i]);
        }

        // 放入缓存 后续服务调用的时候 就取这个缓存里面的序列值 从而确定每次调用提供者的调用顺序
        SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(), arr);
    }

    /**
     * 获取到请求的连接通道
     *
     * @param selector
     * @return
     */
    @Override
    public ChannelFutureWrapper select(Selector selector) {
        // 获取请求的连接通道
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getProviderServiceName());
    }

    /**
     * 更新权重信息
     *
     * @param url
     */
    @Override
    public void updateWeight(URL url) {
        //服务节点的权重
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(url.getServiceName());
        // 根据设置的权重值 设置每个提供者的调用次数
        Integer[] weightArr = createWeightArr(channelFutureWrappers);
        // 打乱上述设置的权重次序 从而达到根据权重的随机分配
        Integer[] finalArr = createRandomArr(weightArr);

        ChannelFutureWrapper[] finalChannelFutureWrappers = new ChannelFutureWrapper[finalArr.length];
        for (int j = 0; j < finalArr.length; j++) {
            finalChannelFutureWrappers[j] = channelFutureWrappers.get(j);
        }
        // 放入最终的路由缓存中 客户端调用服务时 从里面取调用
        SERVICE_ROUTER_MAP.put(url.getServiceName(),finalChannelFutureWrappers);

    }

    /**
     * weight是指权重，权重值约定好配置是100的整倍数
     *
     */
    private static Integer[] createWeightArr(List<ChannelFutureWrapper> channelFutureWrappers) {
        List<Integer> weightArr = new ArrayList<>();
        for (int k = 0; k < channelFutureWrappers.size(); k++) {
            Integer weight = channelFutureWrappers.get(k).getWeight();
            int c = weight / 100;
            for (int i = 0; i < c; i++) {
                weightArr.add(k);
            }
        }
        Integer[] arr = new Integer[weightArr.size()];
        return weightArr.toArray(arr);
    }

    /**
     * 将传入的数组打乱顺序
     *
     * @param arr
     * @return
     */
    private static Integer[] createRandomArr(Integer[] arr) {
        int total = arr.length;
        Random ra = new Random();
        for (int i = 0; i < total; i++) {
            int j = ra.nextInt(total);
            if (i == j) {
                continue;
            }
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }

    /**
     * 创建随机数组
     */
    private int[] createRandomIndex(int len) {
        int[] arrInt = new int[len];
        Random ra = new Random();
        for (int i = 0; i < arrInt.length; i++) {
            arrInt[i] = -1;
        }
        int index = 0;
        while (index < arrInt.length) {
            int num = ra.nextInt(len);
            //如果数组中不包含这个元素则赋值给数组
            if (!contains(arrInt, num)) {
                arrInt[index++] = num;
            }
        }
        return arrInt;
    }

    public boolean contains(int[] arr, int key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == key) {
                return true;
            }
        }
        return false;
    }

}
