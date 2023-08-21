package com.un1ink.hello.server;

import com.un1ink.hello.server.serviceimpl.HelloServiceImpl;
import com.un1ink.config.RpcServiceConfig;
import com.un1ink.hello.HelloService;
import com.un1ink.remoting.transport.socket.SocketRpcServer;

public class SocketServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
