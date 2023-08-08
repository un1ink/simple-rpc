package com.xiong.hello.client;

import com.xiong.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"com.xiong"})
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        clientTest();
//        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
//        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
//        helloController.test();
//        while(true){
//            Thread.sleep(5000);
//
//            break;
//
//        }

    }

    public static void clientTest() throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();

    }
}
