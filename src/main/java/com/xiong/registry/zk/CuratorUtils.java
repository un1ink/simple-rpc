package com.xiong.registry.zk;

import com.xiong.utils.PropertiesFileUtil;
import com.xiong.enums.RpcConfigEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CuratorUtils {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;

    public static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc-myself";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<String, List<String>>();
    public static final Set<String> RESGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    private static CuratorFramework zkClient;

    CuratorUtils(){}

    public static void createPersistentNode(CuratorFramework zkClient, String path){
        try {
            if(RESGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.info("The node already exist. The node is : [{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully, The node is [{}]", path);
            }
            RESGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}]", path);
        }
    }

    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName){
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        } else {
            List<String>  result = null;
            String servicePath = ZK_REGISTER_ROOT_PATH +"/"+ rpcServiceName;
            try {
                result = zkClient.getChildren().forPath(servicePath);
                SERVICE_ADDRESS_MAP.put(servicePath, result);
                registerWatcher(rpcServiceName, zkClient);
            } catch (Exception e) {
                log.error("get children nodes for path [{}] fail", servicePath);
            }
            return result;

        }
    }

    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) {
        try {
            String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
            PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
            PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildremCacheEvent) -> {
                List<String> serviceAddress = curatorFramework.getChildren().forPath(servicePath);
                SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddress);
            };
            pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
            pathChildrenCache.start();
        } catch (Exception e) {
            log.error("pathChildrenCache start error!");
        }

    }

    public static CuratorFramework getZkClient(){
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = (properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null)
                ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue())
                : DEFAULT_ZOOKEEPER_ADDRESS;
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy).build();
        zkClient.start();
        try {
            if(!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return zkClient;

    }

    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        RESGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if(p.endsWith(inetSocketAddress.toString())) {
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered services on the server are cleared : [{}]", RESGISTERED_PATH_SET.toString());
    }

}
