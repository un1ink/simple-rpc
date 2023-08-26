package com.un1ink.config;

import lombok.*;

/** Rpc服务配置信息 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    private String version = "";
    private String group = "";
    private Object service;

    public String getRpcServiceName(){
        return this.getServiceName() +"_" + this.getGroup() + "_" + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
