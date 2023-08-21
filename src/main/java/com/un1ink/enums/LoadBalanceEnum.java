package com.un1ink.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 负载均衡信息 */
@AllArgsConstructor
@Getter
public enum LoadBalanceEnum {

    LOADBALANCE("loadBalance");

    private final String name;
}
