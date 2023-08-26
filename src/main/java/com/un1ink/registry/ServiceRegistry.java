package com.un1ink.registry;

import com.un1ink.extension.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceRegistry {

    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
