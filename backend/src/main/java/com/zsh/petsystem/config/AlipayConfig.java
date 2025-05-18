package com.zsh.petsystem.config;

import com.aliyuncs.http.FormatType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    private String appId;
    private String alipayPublicKey;
    private String merchantPrivateKey;
    // 网关
    private String gatewayUrl;
    // 同步通知
    private String returnUrl;
    // 异步通知
    private String notifyUrl;

    private String format = FormatType.JSON.toString();

    private String charset = "UTF-8";

    private String signType = "RSA2";

}
