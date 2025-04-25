package com.zsh.petsystem.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启用状态枚举
 *
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StatusType {

    ENABLE(0, "启用"),
    DISABLE(1, "禁用"),
    DELETED(-1, "已删除");

    private final Integer code;
    private final String des;

    public static StatusType of(Integer code) {
        for (StatusType statusType : StatusType.values()) {
            if (statusType.code.equals(code)) {
                return statusType;
            }
        }
        return null;
    }

}
