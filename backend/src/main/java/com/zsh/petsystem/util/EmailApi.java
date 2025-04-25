package com.zsh.petsystem.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.util.Objects;

@Slf4j
@Component
public class EmailApi {
    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from; // 发件人

    /**
     * ✅ 新增统一的发送邮件方法（默认使用纯文本方式）
     */
    public boolean sendEmail(String to, String subject, String content) {
        return sendGeneralEmail(subject, content, to);
    }

    /**
     * 发送纯文本的邮件
     */
    @SneakyThrows
    public boolean sendGeneralEmail(String subject, String content, String... to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
        return true;
    }

    /**
     * 发送HTML的邮件
     */
    @SneakyThrows
    public boolean sendHtmlEmail(String subject, String content, String... to) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(mimeMessage);
        log.info("发送邮件成功");
        return true;
    }

    /**
     * 发送带附件的邮件
     */
    @SneakyThrows
    public boolean sendAttachmentsEmail(String subject, String content, String[] to, String[] filePaths) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        if (filePaths != null) {
            for (String filePath : filePaths) {
                FileSystemResource file = new FileSystemResource(new File(filePath));
                helper.addAttachment(Objects.requireNonNull(file.getFilename()), file);
            }
        }

        mailSender.send(mimeMessage);
        return true;
    }

    /**
     * 发送带静态资源的邮件（嵌入图片等）
     */
    @SneakyThrows
    public boolean sendInlineResourceEmail(String subject, String content, String to, String rscPath, String rscId) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);

        String contentHtml = "<html><body>这是邮件的内容，包含一个图片：<img src='cid:" + rscId + "'>" + content + "</body></html>";
        helper.setText(contentHtml, true);

        FileSystemResource res = new FileSystemResource(new File(rscPath));
        helper.addInline(rscId, res);

        mailSender.send(mimeMessage);
        return true;
    }
}