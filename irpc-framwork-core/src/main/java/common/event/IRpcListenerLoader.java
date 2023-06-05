package common.event;

import cn.hutool.core.collection.CollUtil;
import common.event.listener.ServiceUpdateListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class IRpcListenerLoader {

    // 注册的监听器集合
    private static List<IRpcListener> iRpcListenerList = new ArrayList<>();

    // 线程池 用于监听器异步执行 静态线程池 初始化时就创建好了
    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    public static void registerListener(IRpcListener iRpcListener) {
        iRpcListenerList.add(iRpcListener);
    }

    public void init() {
        // 初始化时将处理事件的监听器加入
        registerListener(new ServiceUpdateListener());
    }

    /**
     * 获取接口上的泛型T
     *
     * @param o 接口
     */
    public static Class<?> getInterfaceT(Object o) {
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    public static void sendEvent(IRpcEvent iRpcEvent) {

        if (CollUtil.isEmpty(iRpcListenerList)) {
            return;
        }
        for (IRpcListener<?> iRpcListener : iRpcListenerList) {
            Class<?> type = getInterfaceT(iRpcListener);
            if (type.equals(iRpcEvent.getClass())) {
                eventThreadPool.execute(() -> {
                    try {
                        iRpcListener.callBack(iRpcEvent.getData());
                    } catch (Exception e) {
                        // 异常发生只打印日志，但是不至于使得整个进程崩溃停止
                        e.printStackTrace();
                    }
                });
            }
        }
    }


}
