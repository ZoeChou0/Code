package com.zsh.petsystem.config;

import cn.jiguang.common.ClientConfig;
import cn.jpush.api.JPushClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JPushConfig {

  @Value("${jpush.app-key}")
  private String appKey;

  @Value("${jpush.master-secret}")
  private String masterSecret;

  @Bean
  public JPushClient jPushClient() {
    // 更多配置选项可以参考 JPush Java SDK 文档
    // ClientConfig clientConfig = ClientConfig.getInstance();
    // clientConfig.setApnsProduction(false); // APNs 是否生产环境，默认为 false (开发环境)
    // return new JPushClient(masterSecret, appKey, null, clientConfig);
    return new JPushClient(masterSecret, appKey);
  }
}