package com.xiong.hello.server;

import com.xiong.config.RpcServiceConfig;
import com.xiong.hello.HelloService;
import com.xiong.hello.server.serviceimpl.HelloServiceImpl;
import com.xiong.remoting.transport.socket.SocketRpcServer;


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
