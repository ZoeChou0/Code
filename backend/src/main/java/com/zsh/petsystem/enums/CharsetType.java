package com.zsh.petsystem.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CharsetType {

    UTF_8("UTF-8", "八位 USC 转换格式"),
    ISO_8859_1("ISO-8869-1", "拉丁字母表 No.1"),
    GBK("GBK", "国标 2312 的扩展"),
    GB2312("GB2312", "简体中文汉字编码标准"),
    ASCII("ASCII", "美国标准信息交换码");

    private final String charset;
    private final String description;

    public static CharsetType of(String charset) {
        for (CharsetType charsetType : values()) {
            if (charsetType.getCharset().equals(charset)) {
                return charsetType;
            }
        }
        return null;
    }
}
