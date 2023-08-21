package com.un1ink.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** rpc传输方式 */
@AllArgsConstructor
@Getter
public enum RpcRequestTransportEnum {

    NETTY("netty"),
    SOCKET("socket");

    private final String name;
}
