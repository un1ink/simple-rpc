package com.xiong.hello.server;

import com.xiong.annotation.RpcScan;
import com.xiong.config.RpcServiceConfig;
import com.xiong.hello.HelloService;
import com.xiong.hello.server.serviceimpl.HelloServiceImpl;
import com.xiong.hello.server.serviceimpl.HelloServiceImpl2;
import com.xiong.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


@RpcScan(basePackage = {"com.xiong"})
public class NettyServerMain {
    public static void main(String[] args) {
        // Register service via annotation
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        // Register service manually
        HelloService helloService2 = new HelloServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test1").version("version1").service(helloService2).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}

