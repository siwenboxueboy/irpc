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

    /**
     * 阶段：对象初始化阶段，属性填充之后<br>
     * 职责：<br>
     * 1. 初始化rpc server<br>
     * 2. 接入spring：从spring factory拿到服务接口组装Provider信息，放入本地缓存，后续暴露给注册中心<br>
     * */
    @Override
    public void afterPropertiesSet() throws Exception {
        Server server = null;
        // IRpcService作用：1. 用于标记rpc 服务提供者接口，2. 同时也可以注入spring
        // 拿到标记了IRpcService注解的 spring bean
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(IRpcService.class);
        if (beanMap.size() == 0) {
            //说明当前应用内部不需要对外暴露服务
            return;
        }
        printBanner();
        long begin = System.currentTimeMillis();
        // 初始化网络服务
        server = new Server();
        // 1. 配置加载 2. SPI加载
        server.initServerConfig();
        // 初始化发布-订阅模型
        IRpcListenerLoader iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        for (String beanName : beanMap.keySet()) {
            Object bean = beanMap.get(beanName);
            IRpcService iRpcService = bean.getClass().getAnnotation(IRpcService.class);
            ServiceWrapper dataServiceServiceWrapper = new ServiceWrapper(bean, iRpcService.group());
            dataServiceServiceWrapper.setServiceToken(iRpcService.serviceToken());
            dataServiceServiceWrapper.setLimit(iRpcService.limit());
            // 1. SPI加载注册中心配置，实例化注册中心 2. 服务接口组装Provider信息，放入本地缓存，后续暴露给注册中心
            server.exportService(dataServiceServiceWrapper);
            log.info(">>>>>>>>>>>>>>> [irpc] {} export success! >>>>>>>>>>>>>>> ", beanName);
        }
        long end = System.currentTimeMillis();
        ApplicationShutdownHook.registryShutdownHook();
        // 1. 启动netty相关基础组件 2. 向注册中心注册相关服务接口 3. 启动接收rpc请求线程
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
