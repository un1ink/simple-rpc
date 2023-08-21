package com.un1ink.hello.server.serviceimpl;


import com.un1ink.annotation.RpcService;
import com.un1ink.hello.Hello;
import com.un1ink.hello.HelloService;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RpcService(group = "group1", version = "v1")
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}
