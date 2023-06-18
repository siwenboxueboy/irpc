package dispatcher;

import common.RpcInvocation;
import common.RpcProtocol;
import filter.server.ServerChannelReadData;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static common.cache.CommonServerCache.*;

/**
 * 请求分发器<br>
 * 内部存有一条阻塞队列<br>
 * 有一个线程对象专门负责将队列的数据读出，提供给业务线程池去执行<br>
 */
@NoArgsConstructor
public class ServerChannelDispatcher {
    private BlockingQueue<ServerChannelReadData> RPC_DATA_QUEUE;

    private ExecutorService executorService;

    public void init(int queueSize, int bizThreadNums) {
        RPC_DATA_QUEUE = new ArrayBlockingQueue<>(queueSize);
        executorService = new ThreadPoolExecutor(bizThreadNums, bizThreadNums, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(512));
    }

    public void add(ServerChannelReadData serverChannelReadData) {
        RPC_DATA_QUEUE.add(serverChannelReadData);
    }

    class ServerJobCoreHandle implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            while (true) {
                try {
                    ServerChannelReadData serverChannelReadData = RPC_DATA_QUEUE.take();
                    executorService.submit(() -> {
                        try {

                            RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                            RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);

                            SERVER_FILTER_CHAIN.doFilter(rpcInvocation);

                            // 这里的PROVIDER_CLASS_MAP就是一开始预先在启动时候存储的Bean集合
                            Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                            Method[] methods = aimObject.getClass().getDeclaredMethods();
                            Object result = null;
                            for (Method method : methods) {
                                if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                                    // 通过反射找到目标对象，然后执行目标方法并返回对应值
                                    if (method.getReturnType().equals(Void.TYPE)) {
                                        method.invoke(aimObject, rpcInvocation.getArgs());
                                    } else {
                                        result = method.invoke(aimObject, rpcInvocation.getArgs());
                                    }
                                    break;
                                }
                            }
                            // 请求完成，设置
                            rpcInvocation.setResponse(result);
                            // 序列化执行结果
                            RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                            serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    public void startDataConsume() {
        Thread thread = new Thread(new ServerJobCoreHandle());
        thread.start();
    }
}
