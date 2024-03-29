package com.un1ink.provider.impl;

import com.un1ink.registry.ServiceRegistry;
import com.un1ink.extension.ExtensionLoader;
import com.un1ink.config.RpcServiceConfig;
import com.un1ink.enums.RpcErrorMessageEnum;
import com.un1ink.enums.ServiceRegistryEnum;
import com.un1ink.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import com.un1ink.provider.ServiceProvider;
import com.un1ink.remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
    }

    /** 添加服务信息至缓存中 */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)) {
            return ;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service : {} and interfaces : {}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }


    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if(service == null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    /** 服务端发布服务信息，注册到本地缓存和注册中心 */
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));

        } catch (Exception e){
            log.error("occur exception when getHostAddress", e);
        }
    }
}
