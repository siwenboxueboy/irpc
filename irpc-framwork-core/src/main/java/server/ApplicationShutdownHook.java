package server;

import common.event.IRpcDestroyEvent;
import common.event.IRpcListenerLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationShutdownHook {

    /**
     * 注册一个shutdownHook的钩子，当jvm进程关闭的时候触发
     */
    public static void registryShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("[registryShutdownHook] ==== ");
            IRpcListenerLoader.sendSyncEvent(new IRpcDestroyEvent("destroy"));
            log.info("[Already destroy] ==== ");
        }));
    }
}
