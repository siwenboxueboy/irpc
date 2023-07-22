package config;

import client.Client;
import client.ConnectionHandler;
import client.RpcReference;
import client.RpcReferenceWrapper;
import common.IRpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Field;

/**
 * 该类是如何进入到spring容器中的？通过spring boot 提供的spi机制加载的<br>
 * 为什么不直接加 @EnableAutoConfiguration 注解<br>
 * 用户如果使用的话，使用方法是：该实现会作为jar包依赖于使用方的lib中<br>
 * 而spring boot的包扫描路径是主类的子包路径，并不会包含该路径<br>
 * 但是spring boot 有SPI机制，jar包中实现了spring boot 的SPI，spring boot就会把它加载进spring context中处理
 * */
@Slf4j
public class IRpcClientAutoConfiguration implements BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {

    private static RpcReference rpcReference = null;
    private static Client client = null;
    private volatile boolean needInitClient = false;
    private volatile boolean hasInitClientConfig = false;
    /**
     * 阶段：在由bean Definition 初始化对象之后<br>
     * 职责：<br>
     * 1. 初始化rpc 客户端<br>
     * 2. 接入spring：将bean 实例化后的属性进行过滤，如果属性加了IRpcReference注解，则给其进行属性依赖注入。注入过程是利用动态代理，生成代理对象，详见RpcReference<br>
     * */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(IRpcReference.class)) {
                // 客户端只会初始化一次
                if (!hasInitClientConfig) {
                    client = new Client();
                    try {
                        rpcReference = client.initClientApplication();
                    } catch (Exception e) {
                        log.error("[IRpcClientAutoConfiguration] postProcessAfterInitialization has error ", e);
                        throw new RuntimeException(e);
                    }
                    hasInitClientConfig = true;
                }
                needInitClient = true;
                IRpcReference iRpcReference = field.getAnnotation(IRpcReference.class);
                try {
                    field.setAccessible(true);
                    Object refObj = field.get(bean);
                    RpcReferenceWrapper rpcReferenceWrapper = new RpcReferenceWrapper();
                    rpcReferenceWrapper.setAimClass(field.getType());
                    rpcReferenceWrapper.setGroup(iRpcReference.group());
                    rpcReferenceWrapper.setServiceToken(iRpcReference.serviceToken());
                    rpcReferenceWrapper.setUrl(iRpcReference.url());
                    rpcReferenceWrapper.setTimeOut(iRpcReference.timeOut());
                    //失败重试次数
                    rpcReferenceWrapper.setRetry(iRpcReference.retry());
                    rpcReferenceWrapper.setAsync(iRpcReference.async());
                    refObj = rpcReference.get(rpcReferenceWrapper);
                    field.set(bean, refObj);
                    client.doSubscribeService(field.getType());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return bean;
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (needInitClient && client != null) {
            log.info(" ================== [{}] started success ================== ", client.getClientConfig().getApplicationName());
            ConnectionHandler.setBootstrap(client.getBootstrap());
            client.doConnectServer();
            client.startClient();
        }
    }
}
