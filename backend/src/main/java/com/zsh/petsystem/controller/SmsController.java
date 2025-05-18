// package com.zsh.petsystem.controller;

// import com.zsh.petsystem.service.SmsService;
// import com.zsh.petsystem.service.RedisService;
// import com.zsh.petsystem.util.RandomUtil;
// import jakarta.annotation.Resource;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/sms")
// @CrossOrigin
// public class SmsController {

// @Resource
// private SmsService smsService;

// @Resource
// private RedisService redisService;

// /**
// * 发送短信验证码
// */
// @GetMapping("/send")
// public String sendSms(@RequestParam String mobile) {
// // 生成随机验证码
// String code = RandomUtil.randomNumbers(6);
// // 存入缓存（有效期5分钟）
// redisService.set(mobile, code, 300L);
// // 发送短信
// boolean result = smsService.sendSms(mobile, code);
// return result ? "发送短信验证码成功" : "发送短信验证码失败";
// }
// }