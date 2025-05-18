package com.zsh.petsystem.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.extern.slf4j.Slf4j; // 确保导入 @Slf4j
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j // 确保有 @Slf4j 注解
public class AlipayClientConfig {

    @Bean
    public AlipayClient alipayClient(AlipayConfig alipayConfig) {
        // --- 检查并打印关键配置 ---
        log.info("Creating AlipayClient with config:");
        log.info("  Gateway URL: {}", alipayConfig.getGatewayUrl());
        log.info("  App ID: {}", alipayConfig.getAppId());

        // --- 修改这里：使用 getMerchantPrivateKey() ---
        String privateKey = alipayConfig.getMerchantPrivateKey(); // <--- **修改这里**

        if (privateKey != null && !privateKey.isEmpty()) {
            log.info("  Merchant Private Key Length: {}", privateKey.length()); // 日志也更新一下名字
            // log.info(" Merchant Private Key (First 10 chars): {}",
            // privateKey.substring(0, Math.min(privateKey.length(), 10)));
        } else {
            log.error("  Merchant Private Key is NULL or EMPTY in AlipayConfig!"); // 日志也更新一下名字
        }
        log.info("  Alipay Public Key Length: {}",
                alipayConfig.getAlipayPublicKey() != null ? alipayConfig.getAlipayPublicKey().length() : "NULL");
        log.info("  Sign Type: {}", alipayConfig.getSignType());
        log.info("  Charset: {}", alipayConfig.getCharset());
        // --- 打印结束 ---

        // --- 修改这里：传递正确的私钥变量 ---
        return new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                privateKey, // <--- 使用从 getMerchantPrivateKey() 获取的变量
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType());
    }
}