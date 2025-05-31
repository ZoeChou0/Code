package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zsh.petsystem.enums.EmailTemplateEnum;
import com.zsh.petsystem.service.CaptchaService;
import com.zsh.petsystem.util.EmailApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CaptachaServiceImpl implements CaptchaService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private EmailApi emailApi;

    private static final String CAPTCHA_KEY_PREFIX = "login:email:captcha:";

    @Override
    public boolean sendCaptcha(String email) {
        String key = CAPTCHA_KEY_PREFIX + email; // 使用常量前缀
        return sendMailCaptcha(key);
    }

    private boolean sendMailCaptcha(String key) {
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(key);

        String lastSendTimestampStr = (String) hashOps.get("lastSendTimestamp");
        if (StringUtils.isNotBlank(lastSendTimestampStr)) {
            long lastSendTime = Long.parseLong(lastSendTimestampStr);
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastSendTime) < 60 * 1000) {
                throw new RuntimeException("验证码发送太频繁，请稍后再试");
            }
        }
        String captcha = generateCaptcha(6);

        try {
            sendEmailWithCaptcha(key, captcha); // 修改了方法名以更清晰
        } catch (Exception e) {
            log.error("发送邮件验证码时捕获到异常 (key: {}): {}", key, e.getMessage(), e);
            return false;
        }

        hashOps.put("captcha", captcha);
        hashOps.put("lastSendTimestamp", String.valueOf(System.currentTimeMillis()));
        hashOps.expire(5, TimeUnit.MINUTES);
        log.info("验证码 {} 已发送至与 key {} 关联的邮箱，并存入Redis，有效期5分钟。", captcha, key);
        return true;
    }

    private void sendEmailWithCaptcha(String hashKey, String captcha) throws Exception {
        // hashKey 格式: login:email:captcha:user@example.com
        String[] parts = hashKey.split(":");
        if (parts.length < 4 || !parts[1].equals("email")) { // 确保是email类型的key
            log.error("无效的验证码Key格式或类型不匹配: {}", hashKey);
            throw new IllegalArgumentException("无效的验证码Key格式");
        }

        String receiverEmail = parts[3];
        String subject = EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.getSubject();
        String htmlContent = EmailTemplateEnum.VERIFICATION_CODE_EMAIL_HTML.buildContent(captcha);

        boolean emailSent = emailApi.sendHtmlEmail(subject, htmlContent, receiverEmail);
        if (!emailSent) {
            // EmailApi.sendHtmlEmail 在失败时应抛出异常，或者这里根据其返回值处理
            log.warn("EmailApi.sendHtmlEmail 返回 false，邮件可能未发送成功至 {}", receiverEmail);
            throw new RuntimeException("邮件服务提供商未能成功发送邮件。");
        }
        log.info("验证码邮件已尝试发送至: {}", receiverEmail);
    }

    @Override
    public boolean verifyCaptcha(String email, String code) {
        if (email == null || code == null || code.trim().isEmpty()) {
            return false;
        }
        String key = CAPTCHA_KEY_PREFIX + email;
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(key);
        String storedCaptcha = (String) hashOps.get("captcha");

        log.info("校验验证码: email={}, 提交的code={}, Redis中存储的code={}", email, code, storedCaptcha);

        if (storedCaptcha != null && storedCaptcha.equals(code)) {
            log.info("验证码 {} 对于邮箱 {} 校验成功。", code, email);
            return true;
        }
        log.warn("验证码 {} 对于邮箱 {} 校验失败。Redis中存储: {}", code, email, storedCaptcha);
        return false;
    }

    @Override
    public void clearCaptcha(String email) {
        if (email == null)
            return;
        String key = CAPTCHA_KEY_PREFIX + email;
        stringRedisTemplate.delete(key);
        log.info("已清理邮箱 {} 的验证码缓存 (key: {})", email, key);
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