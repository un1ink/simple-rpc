package com.xiong.registry.zk;

import org.apache.curator.framework.CuratorFramework;
import com.xiong.registry.ServiceRegistry;

import java.net.InetSocketAddress;

public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework curatorFramework = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(curatorFramework, servicePath);
    }
}
