package com.un1ink.hello.client;

import com.un1ink.annotation.RpcReference;
import com.un1ink.hello.Hello;
import com.un1ink.hello.HelloService;
import org.springframework.stereotype.Component;


@Component
public class HelloController {

    @RpcReference(version = "v1", group = "group1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = this.helloService.hello(new Hello("Hello!", "This is a message from client."));
    }
}

