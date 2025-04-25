package com.zsh.petsystem.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

  @Value("${spring.sms.access-key-id}")
  private String accessKeyId;

  @Value("${spring.sms.access-key-secret}")
  private String accessKeySecret;

  @Value("${spring.sms.sign-name}")
  private String signName;

  @Value("${spring.sms.template-code}")
  private String templateCode;

  /**
   * 发送短信验证码
   */
  public boolean sendSms(String mobile, String code) {
    try {
      DefaultProfile profile = DefaultProfile.getProfile(
          "cn-hangzhou",
          accessKeyId,
          accessKeySecret);
      IAcsClient client = new DefaultAcsClient(profile);

      CommonRequest request = new CommonRequest();
      request.setSysMethod(MethodType.POST);
      request.setSysDomain("dysmsapi.aliyuncs.com");
      request.setSysVersion("2017-05-25");
      request.setSysAction("SendSms");
      request.putQueryParameter("PhoneNumbers", mobile);
      request.putQueryParameter("SignName", signName);
      request.putQueryParameter("TemplateCode", templateCode);
      request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");

      CommonResponse response = client.getCommonResponse(request);
      log.info("短信发送结果：{}", response.getData());
      return true;
    } catch (ClientException e) {
      log.error("短信发送失败", e);
      return false;
    }
  }
}