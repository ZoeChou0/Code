package com.zsh.petsystem.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
public class RegisterDataDTO {
  @NotBlank(message = "用户名不能为空")
  private String name;

  @NotBlank(message = "邮箱不能为空")
  @Email(message = "邮箱格式不正确")
  private String email;

  @NotBlank(message = "密码不能为空")
  @Size(min = 6, message = "密码长度不能少于6位")
  private String password;

  @NotBlank(message = "手机号不能为空")
  private String phone;

  @NotBlank(message = "请选择注册身份")
  private String role; // "user" or "provider"

  @NotBlank(message = "验证码不能为空")
  @Size(min = 6, max = 6, message = "验证码必须为6位")
  private String verificationCode;
}