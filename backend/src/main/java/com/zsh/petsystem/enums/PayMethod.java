package com.zsh.petsystem.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 * 
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PayMethod {

    ALIPAY(1, "支付宝"),
    WECHAT(2, "微信"),
    UNIONPAY(3, "银联"),
    APPLEPAY(4, "Apple Pay"),
    CREDITCARD(5, "信用卡"),
    CASH(6, "现金"),
    OTHER(7, "其他");

    private final Integer code;
    private final String description;

    public static PayMethod of(Integer code) {
        for (PayMethod payMethod : values()) {
            if (payMethod.getCode().equals(code)) {
                return payMethod;
            }
        }
        return null;
    }
}
