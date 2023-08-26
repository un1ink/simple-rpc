package com.un1ink.spring;

import com.un1ink.annotation.RpcService;
import com.un1ink.registry.ServiceRegistry;
import com.un1ink.annotation.RpcReference;
import com.un1ink.enums.RpcRequestTransportEnum;
import com.un1ink.enums.ServiceRegistryEnum;
import com.un1ink.extension.ExtensionLoader;
import com.un1ink.provider.ServiceProvider;
import com.un1ink.provider.impl.ZkServiceProviderImpl;
import com.un1ink.config.RpcServiceConfig;
import com.un1ink.factory.SingletonFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import com.un1ink.proxy.RpcClientProxy;
import com.un1ink.remoting.transport.RpcRequestTransport;

import java.lang.reflect.Field;

@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    /** 加载服务 */
    public SpringBeanPostProcessor() {
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension(RpcRequestTransportEnum.NETTY.getName());
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());

    }

    /** 创建bean服务对象 */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    /** 根据RpcReference注解信息创建客户端代理对象 */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        System.out.println("Bean AfterInitialization :"+ beanName);

        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);

            if (rpcReference != null) {
                log.info("获取RpcReference注解成功 :" + beanName);
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }
}
