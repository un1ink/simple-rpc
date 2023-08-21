package com.un1ink.registry.zk;

import com.un1ink.loadbalance.LoadBalance;
import com.un1ink.extension.ExtensionLoader;
import com.un1ink.enums.LoadBalanceEnum;
import com.un1ink.enums.RpcErrorMessageEnum;
import com.un1ink.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.util.CollectionUtils;
import com.un1ink.registry.ServiceDiscovery;
import com.un1ink.remoting.dto.RpcRequest;
import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;
    public ZkServiceDiscovery(){
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getRpcServiceName();
        CuratorFramework curatorFramework = CuratorUtils.getZkClient();
        // todo zkClient 可以设置为成员变量，便于监听状态
        List<String> serviceList = CuratorUtils.getChildrenNodes(curatorFramework, serviceName);
        if(CollectionUtils.isEmpty(serviceList)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, serviceName);
        }
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceList, rpcRequest);
        log.info("Successfully found the service address : [{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
