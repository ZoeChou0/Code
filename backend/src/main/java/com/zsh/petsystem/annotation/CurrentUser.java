package com.zsh.petsystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 应用于方法参数
@Retention(RetentionPolicy.RUNTIME) // 运行时可用
public @interface CurrentUser {
}