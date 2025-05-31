package com.zsh.petsystem.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("service_item") // 表名注解，正确
public class ServiceItem {

    @TableId(type = IdType.AUTO) // 主键，自增
    private Long id;

    private String name;

    private String description;

    private Double price; // 价格，如果需要高精度计算，可以考虑使用 BigDecimal

    private Integer duration; // 服务时长，单位：分钟

    private String category; // 服务类型/分类

    @TableField("review_status") // 明确指定列名（如果与属性名不完全一致或想更清晰）
    private String reviewStatus; // 审核状态: PENDING_APPROVAL, APPROVED, REJECTED

    @TableField("rejection_reason")
    private String rejectionReason; // 拒绝原因

    @TableField("provider_id")
    private Long providerId; // 服务商ID

    @TableField("daily_capacity")
    private Integer dailyCapacity; // 每日服务容量

    @TableField("required_vaccinations")
    private String requiredVaccinations; // 要求的疫苗列表

    @TableField("requires_neutered")
    private Boolean requiresNeutered; // 是否要求绝育 (数据库中可能是 TINYINT(1))

    @TableField("min_age")
    private Integer minAge; // 允许的最低年龄 (可选, 单位：岁)

    @TableField("max_age")
    private Integer maxAge; // 允许的最高年龄 (可选, 单位：岁)

    @TableField("temperament_requirements")
    private String temperamentRequirements; // 允许的性格要求

    @TableField("prohibited_breeds")
    private String prohibitedBreeds; // 不接受的品种列表

    @TableField(value = "created_at", fill = FieldFill.INSERT) // 创建时自动填充
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE) // 创建和更新时自动填充
    private LocalDateTime updatedAt;
}