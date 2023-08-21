package com.un1ink.loadbalance;

import com.un1ink.extension.SPI;
import com.un1ink.remoting.dto.RpcRequest;

import java.util.List;

@SPI
public interface LoadBalance {

    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
