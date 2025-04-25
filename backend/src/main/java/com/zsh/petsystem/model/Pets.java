package com.zsh.petsystem.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("pets")
public class Pets {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String species; // 宠物品种
    private Integer age;
    private String gender;
    private String photoUrl;


    @TableField("user_id")
    private Long userId;

    //疫苗接种情况
    private String vaccinationInfo;

    //是否绝育
    private Boolean neutered;

    //过敏
    private String allergies;

    //已知健康问题
    private String medicalNotes;

    //性格
    private String temperament;


}
