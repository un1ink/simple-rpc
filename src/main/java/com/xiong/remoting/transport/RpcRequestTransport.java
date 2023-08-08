package com.xiong.remoting.transport;

import com.xiong.extension.SPI;
import com.xiong.remoting.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
