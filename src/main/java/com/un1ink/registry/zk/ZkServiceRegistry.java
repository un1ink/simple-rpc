package com.un1ink.registry.zk;

import org.apache.curator.framework.CuratorFramework;
import com.un1ink.registry.ServiceRegistry;

import java.net.InetSocketAddress;

public class ZkServiceRegistry implements ServiceRegistry {

    private final CuratorFramework curatorFramework;

    public ZkServiceRegistry(){
        this.curatorFramework = CuratorUtils.getZkClient();
    }
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorUtils.createEphemeralNode(curatorFramework, servicePath);
    }
}
