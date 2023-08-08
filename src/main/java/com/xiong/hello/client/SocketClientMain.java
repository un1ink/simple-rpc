package com.xiong.hello.client;

import com.xiong.config.RpcServiceConfig;
import com.xiong.hello.Hello;
import com.xiong.hello.HelloService;
import com.xiong.proxy.RpcClientProxy;
import com.xiong.remoting.transport.RpcRequestTransport;
import com.xiong.remoting.transport.socket.SocketRpcClient;


public class SocketClientMain {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceConfig);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
