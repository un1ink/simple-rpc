package com.un1ink.hello.client;

import com.un1ink.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"com.un1ink"})
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        clientTest();
    }

    public static void clientTest() throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}
