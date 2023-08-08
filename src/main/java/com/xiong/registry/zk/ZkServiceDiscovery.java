package com.xiong.registry.zk;

import com.xiong.loadbalance.LoadBalance;
import com.xiong.extension.ExtensionLoader;
import com.xiong.enums.LoadBalanceEnum;
import com.xiong.enums.RpcErrorMessageEnum;
import com.xiong.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.util.CollectionUtils;
import com.xiong.registry.ServiceDiscovery;
import com.xiong.remoting.dto.RpcRequest;
import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;
    public ZkServiceDiscovery(){
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }


    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getRpcServiceName();
        CuratorFramework curatorFramework = CuratorUtils.getZkClient();
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
