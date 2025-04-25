package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zsh.petsystem.enums.EmailTemplateEnum;
import com.zsh.petsystem.service.CaptchaService;
import com.zsh.petsystem.util.EmailApi;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class CaptachaServiceImpl implements CaptchaService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private EmailApi emailApi;

    @Override
    public boolean sendCaptcha(String email) {
        String key = "login:email:captcha:" + email;
        return sendMailCaptcha(key);
    }

    private boolean sendMailCaptcha(String key) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(key);

        String lastSendTimestamp = hashOps.get("lastSendTimestamp");
        if (StringUtils.isNotBlank(lastSendTimestamp)) {
            long lastSendTime = Long.parseLong(lastSendTimestamp);
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastSendTime) > 60 * 1000) {
                throw new RuntimeException("验证码发送太频繁，请稍后再试");
            }
        }
        String captcha = generateCaptcha(6);

        try {
            sendCaptcha(captcha);
        } catch (Exception e) {
            return false;
        }

        hashOps.put("captcha", captcha);
        hashOps.put("lastSendTimestamp", String.valueOf(System.currentTimeMillis()));
        hashOps.expire(5, TimeUnit.MINUTES);

        return true;
    }

    private void sendCaptcha(String hashKey, String captcha) throws Exception {
        String[] parts = hashKey.split(":");
        if (parts.length < 4) {
            throw new IllegalArgumentException("无效的验证码");
        }

        String type = parts[1];
        String receiver = parts[3];

        switch (type) {
            case "email":
                String subject = EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.getSubject();
                String htmlContent = EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.buildContent(captcha);
                if (!emailApi.sendHtmlEmail(subject, htmlContent, receiver)) {
                    throw new RuntimeException("发送失败");
                }
                break;
            case "phone":

                break;
            default:
                break;

        }

    }

    private String generateCaptcha(int len) {
        String chars = "0123456789";
        StringBuilder captcha = new StringBuilder(len);
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            captcha.append(chars.charAt(random.nextInt(chars.length())));
        }
        return captcha.toString();

    }

}
