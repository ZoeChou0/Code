package com.zsh.petsystem.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 单号工具类（规则：时间戳 + 随机数）（注意：一定概率会重复，数据库还会做一层唯一性校验，因此可以忽略）
 *
 * @author javgo.cn
 * @date 2024/1/13
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * 生成 6 位随机数
     */
    public static String getVerCode() {
        return RandomUtil.randomNumbers(6);
    }

    /**
     * 生成订单号
     */
    public static synchronized String getOrderNo() {
        return generateTimestamp() + RandomUtil.randomNumbers(3);
    }

    /**
     * 生成流水号
     */
    public static String getSerialNumber() {
        return generateTimestamp() + RandomUtil.randomNumbers(4);
    }

    /**
     * 生成时间戳
     */
    private static String generateTimestamp() {
        return DATE_FORMAT.format(new Date());
    }
}
