package com.xiong.registry;

import com.xiong.extension.SPI;
import com.xiong.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
