package com.zsh.petsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@TableName("users")
public class Users {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String password;

    private String email;

    private String phone;

    private String role;

    private String qualificationStatus;

    private String addressLine1;
    private String city;
    private String state; // 省
    private String zipCode; // 邮政编码
    private LocalDate birthday; // 生日 (只包含年月日)
    private String profilePhotoUrl; // 头像图片

    private String status;
}