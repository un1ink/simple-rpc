package com.xiong.hello.client;

import com.xiong.annotation.Anno;
import com.xiong.annotation.RpcReference;
import com.xiong.hello.Hello;
import com.xiong.hello.HelloService;
import org.springframework.stereotype.Component;


@Component
public class HelloController {

    @Anno
    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = this.helloService.hello(new Hello("111", "222"));
    }
}

