package com.xiong.registry;

import com.xiong.extension.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceRegistry {
    /*
    * regist service
    * @param rpcServiceName     rpc service name
    * @param InetSocketAddress  service address
    *
    * */
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
