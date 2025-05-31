package com.zsh.petsystem.service;

public interface CaptchaService {

    boolean sendCaptcha(String email);

    /**
     * 校验邮箱验证码
     * 
     * @param email 邮箱
     * @param code  用户提交的验证码
     * @return true 如果验证码正确, false 否则
     */
    boolean verifyCaptcha(String email, String code);

    /**
     * 清理已使用的或已过期的验证码
     * 
     * @param email 邮箱
     */
    void clearCaptcha(String email);
}