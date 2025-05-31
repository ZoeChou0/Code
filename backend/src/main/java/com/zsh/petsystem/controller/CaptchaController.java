package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
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
     * 发送邮箱验证码 (如果此接口仍需保留)
     * 
     * @param email 用户邮箱
     * @return 发送结果 (Result 对象)
     */
    @PostMapping("/send")
    public Result<?> sendCaptcha(@RequestParam String email) {
        try {
            boolean success = captchaService.sendCaptcha(email);
            if (success) {
                return Result.success(null, "验证码发送成功");
            } else {
                return Result.failed("验证码发送失败");
            }
        } catch (RuntimeException e) {
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            // Log error e.g., log.error("Error in CaptchaController: ", e);
            return Result.failed("发送验证码时发生未知错误。");
        }
    }
}
