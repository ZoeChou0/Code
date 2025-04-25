package com.zsh.petsystem.enums;

public enum EmailTemplateEnum {

    // 枚举项：验证码模板（可继续扩展其他模板）
    VERIFICATION_CODE_EMAIL_HTML("验证码邮件", "<p>您的验证码是：<b>{code}</b>，有效期5分钟，请勿泄露。</p>");

    private final String subject;
    private final String template;

    EmailTemplateEnum(String subject, String template) {
        this.subject = subject;
        this.template = template;
    }

    public String getSubject() {
        return subject;
    }

    // 占位符替换，将 {code} 替换成实际验证码
    public String buildContent(String code) {
        return template.replace("{code}", code);
    }
}