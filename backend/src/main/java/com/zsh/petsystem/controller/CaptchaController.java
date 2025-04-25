package com.zsh.petsystem.controller;

import com.zsh.petsystem.service.CaptchaService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    /**
     * 发送邮箱验证码
     * 
     * @param email 用户邮箱
     * @return 发送结果
     */

    @PostMapping("/send")
    public String sendCaptcha(@RequestParam String code, @RequestParam String email) {
        boolean success = captchaService.sendCaptcha(email);
        return success ? "验证码发送成功" : "验证码发送失败";
    }

}
