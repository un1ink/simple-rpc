package com.un1ink.registry;

import com.un1ink.extension.SPI;
import com.un1ink.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
