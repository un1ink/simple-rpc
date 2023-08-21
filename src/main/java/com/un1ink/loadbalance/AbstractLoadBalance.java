package com.un1ink.loadbalance;

import com.un1ink.remoting.dto.RpcRequest;
import com.un1ink.utils.CollectionUtil;

import java.util.List;

/**
 * 负载均衡抽象类模板
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        if(CollectionUtil.isEmpty(serviceUrlList)) {
            return null;
        }
        if(serviceUrlList.size() == 1){
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest);
}
