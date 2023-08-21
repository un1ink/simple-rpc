package com.un1ink.remoting.transport;

import com.un1ink.extension.SPI;
import com.un1ink.remoting.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
