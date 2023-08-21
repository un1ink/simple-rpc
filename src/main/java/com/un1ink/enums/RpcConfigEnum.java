package com.un1ink.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 配置zk相关信息 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
    /** 配置文件名 */
    RPC_CONFIG_PATH("rpc.properties"),
    /** 配置文件中zk地址的键 */
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
