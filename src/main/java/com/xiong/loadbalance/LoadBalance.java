package com.xiong.loadbalance;

import com.xiong.extension.SPI;
import com.xiong.remoting.dto.RpcRequest;

import java.util.List;

@SPI
public interface LoadBalance {

    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
