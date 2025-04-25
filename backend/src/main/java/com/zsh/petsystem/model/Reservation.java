package com.zsh.petsystem.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reservation")
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户
    @TableField("user_id")
    private Long userId;

    // 宠物
    @TableField("pet_id")
    private Long petId;

    // 服务
    @TableField("service_id")
    private Long serviceId;

    // 预约日期
    @TableField("reservation_date")
    private LocalDate reservationDate;

    // 预约时间段
    @TableField("reservation_time")
    private LocalDateTime reservationTime;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    //服务商确认
    private Boolean providerConfirmed;
}