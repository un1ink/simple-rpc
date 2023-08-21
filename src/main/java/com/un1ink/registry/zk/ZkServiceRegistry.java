package com.un1ink.registry.zk;

import org.apache.curator.framework.CuratorFramework;
import com.un1ink.registry.ServiceRegistry;

import java.net.InetSocketAddress;

public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework curatorFramework = CuratorUtils.getZkClient();
        // todo 改为临时节点，连接断开时删除
        CuratorUtils.createPersistentNode(curatorFramework, servicePath);
    }
}
