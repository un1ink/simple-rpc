package com.un1ink.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
    SUCCESS(200, "Request success."),
    FAIL(500, "Request failed.");
    private final int code;

    private final String message;
}
