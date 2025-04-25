package com.zsh.petsystem.model;

import com.baomidou.mybatisplus.annotation.IdType; 
import com.baomidou.mybatisplus.annotation.TableId; 
import com.baomidou.mybatisplus.annotation.TableName; 
import lombok.Data;
import lombok.NoArgsConstructor; 

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
}