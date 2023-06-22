package config;

import common.IRpcService;
import common.event.IRpcListenerLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import server.ApplicationShutdownHook;
import server.Server;
import server.ServiceWrapper;

import java.util.Map;

@Slf4j
public class IRpcServerAutoConfiguration implements InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Server server = null;
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(IRpcService.class);
        if (beanMap.size() == 0) {
            //说明当前应用内部不需要对外暴露服务
            return;
        }
        printBanner();
        long begin = System.currentTimeMillis();
        server = new Server();
        server.initServerConfig();
        IRpcListenerLoader iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        for (String beanName : beanMap.keySet()) {
            Object bean = beanMap.get(beanName);
            IRpcService iRpcService = bean.getClass().getAnnotation(IRpcService.class);
            ServiceWrapper dataServiceServiceWrapper = new ServiceWrapper(bean, iRpcService.group());
            dataServiceServiceWrapper.setServiceToken(iRpcService.serviceToken());
            dataServiceServiceWrapper.setLimit(iRpcService.limit());
            server.exportService(dataServiceServiceWrapper);
            log.info(">>>>>>>>>>>>>>> [irpc] {} export success! >>>>>>>>>>>>>>> ", beanName);
        }
        long end = System.currentTimeMillis();
        ApplicationShutdownHook.registryShutdownHook();
        server.startApplication();
        log.info(" ================== [{}] started success in {}s ================== ", server.getServerConfig().getApplicationName(), ((double) end - (double) begin) / 1000);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void printBanner() {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("|||---------- IRpc Starting Now! ----------|||");
        System.out.println("==============================================");
        System.out.println("version: 1.8.0");
        System.out.println();
    }
}
