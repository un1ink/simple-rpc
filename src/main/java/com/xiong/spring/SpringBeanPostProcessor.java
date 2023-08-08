package com.xiong.spring;

import com.xiong.annotation.Anno;
import com.xiong.annotation.RpcReference;
import com.xiong.annotation.RpcService;
import com.xiong.enums.RpcRequestTransportEnum;
import com.xiong.enums.ServiceRegistryEnum;
import com.xiong.extension.ExtensionLoader;
import com.xiong.provider.ServiceProvider;
import com.xiong.provider.impl.ZkServiceProviderImpl;
import com.xiong.config.RpcServiceConfig;
import com.xiong.factory.SingletonFactory;
import com.xiong.registry.ServiceRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import com.xiong.proxy.RpcClientProxy;
import com.xiong.remoting.transport.RpcRequestTransport;

import java.io.IOException;
import java.lang.reflect.Field;

@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {

        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension(RpcRequestTransportEnum.NETTY.getName());
        if(this.rpcClient == null){
            System.out.println("bad init!");
        }
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

        ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());


    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // get RpcService annotation
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // build RpcServiceProperties
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        System.out.println("Bean AfterInitialization :"+ beanName);

        for (Field declaredField : declaredFields) {
//            Anno anno = declaredField.getAnnotation(Anno.class);
//            if(anno != null){
//                System.out.println("this is new annotation.");
//            }

            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
//            if(beanName.equals("helloController") ){
//                System.out.println(declaredField);
//                System.out.println(rpcReference == null);
//            }
            if (rpcReference != null) {
//                System.out.println("获取RpcReference注解成功 :" + beanName);
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                if(rpcClient == null){
                    System.out.println("look this.");
                }
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
