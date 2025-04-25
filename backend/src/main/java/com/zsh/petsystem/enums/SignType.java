package com.zsh.petsystem.enums;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SignType {
    RSA("RSA", "RSA签名"),
    RSA2("RSA2", "RSA签名的SHA-256升级版"),
    MD5("MD5", "消息摘要算法5"),
    HMAC_SHA256("HMAC-SHA256", "带有SHA-256的密钥哈希消息认证码"),
    DSA("DSA", "数字签名算法");


    private final String type;
    private final String description;


    public static SignType of(String type) {
        for (SignType signType : SignType.values()) {
            if (signType.getType().equalsIgnoreCase(type)) {
                return signType;
            }
        }
        return null;
    }
}
