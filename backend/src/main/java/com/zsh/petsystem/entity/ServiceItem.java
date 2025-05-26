package com.zsh.petsystem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("service_item")
public class ServiceItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Double price;

    private Integer duration;// 时长

    private String reviewStatus; // PENDING, APPROVED, REJECTED
    private String rejectionReason;

    @TableField("provider_id")
    private Long providerId;

    @TableField("daily_capacity")
    private Integer dailyCapacity; // 每日服务容量

    // @TableField("current_bookings")
    // private Integer currentBookings; // 当前已预约数量

    // 要求的疫苗列表
    private String requiredVaccinations;

    // 是否要求绝育
    private Boolean requiresNeutered;

    // 允许的最低年龄 (可选)
    private Integer minAge;

    // 允许的最高年龄 (可选)
    private Integer maxAge;

    // 允许的性格要求 (例如 "仅限友好", "不接受攻击性")
    private String temperamentRequirements;

    // 不接受的品种列表 (例如 "比特犬,罗威纳")
    private String prohibitedBreeds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}