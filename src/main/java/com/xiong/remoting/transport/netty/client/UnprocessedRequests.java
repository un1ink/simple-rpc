package com.xiong.remoting.transport.netty.client;

import com.xiong.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESS_REQUEST_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future){
        UNPROCESS_REQUEST_FUTURES.put(requestId, future);
    }

    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future = UNPROCESS_REQUEST_FUTURES.remove(rpcResponse.getRequestId());
        if(future != null){
            future.complete(rpcResponse);
        }else{
            throw new IllegalStateException();
        }
    }
}
