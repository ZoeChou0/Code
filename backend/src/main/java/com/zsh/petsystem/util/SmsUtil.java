package com.zsh.petsystem.util;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsUtil {

    // 替换为你的阿里云信息
    private static final String REGION_ID = "cn-hangzhou";
    private static final String ACCESS_KEY_ID = "你的AccessKeyId";
    private static final String ACCESS_KEY_SECRET = "你的AccessKeySecret";
    private static final String SIGN_NAME = "宠乐居";
    private static final String TEMPLATE_CODE = "SMS_123456789"; // 替换为你的模板Code

    public boolean sendSms(String phone, String code) {
        try {
            DefaultProfile profile = DefaultProfile.getProfile(REGION_ID, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
            IAcsClient client = new DefaultAcsClient(profile);

            CommonRequest request = new CommonRequest();
            request.setSysMethod(com.aliyuncs.http.MethodType.POST);
            request.setSysDomain("dysmsapi.aliyuncs.com");
            request.setSysVersion("2017-05-25");
            request.setSysAction("SendSms");

            request.putQueryParameter("PhoneNumbers", phone);
            request.putQueryParameter("SignName", SIGN_NAME);
            request.putQueryParameter("TemplateCode", TEMPLATE_CODE);
            request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");

            CommonResponse response = client.getCommonResponse(request);
            log.info("短信响应结果: {}", response.getData());
            return response.getHttpResponse().isSuccess();
        } catch (Exception e) {
            log.error("短信发送失败", e);
            return false;
        }
    }
}