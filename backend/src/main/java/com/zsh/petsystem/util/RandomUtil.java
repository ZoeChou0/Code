
package com.zsh.petsystem.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public class RandomUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Random random = new Random();

    /**
     * 生成指定长度的随机字符串（包含字母和数字）
     */
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(secureRandom.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 生成 6 位数字验证码
     */
    public static String randomNumbers(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成 UUID，不带 "-"
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成带时间戳的订单号：格式如 ORD20250420135923xxxx
     */
    public static String generateOrderNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "ORD" + timestamp + randomString(4);
    }

    public static int randomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min 不能大于 max");
        }
        return random.nextInt(max - min + 1) + min;
    }
}