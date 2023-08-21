package com.un1ink.hello.client;

import com.un1ink.config.RpcServiceConfig;
import com.un1ink.proxy.RpcClientProxy;
import com.un1ink.remoting.transport.RpcRequestTransport;
import com.un1ink.hello.Hello;
import com.un1ink.hello.HelloService;
import com.un1ink.remoting.transport.socket.SocketRpcClient;

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
