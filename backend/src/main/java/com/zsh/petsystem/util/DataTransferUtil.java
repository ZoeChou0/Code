package com.zsh.petsystem.util;

import org.springframework.beans.BeanUtils;

/**
 * 通用的数据传输工具类
 */
public class DataTransferUtil {

    /**
     * 对象属性浅拷贝（用于 DTO 转换）
     *
     * @param source      原对象
     * @param targetClass 目标类
     * @param <S>         原类型
     * @param <T>         目标类型
     * @return T 新对象
     */
    public static <S, T> T shallowCopy(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("数据拷贝失败", e);
        }
    }
}