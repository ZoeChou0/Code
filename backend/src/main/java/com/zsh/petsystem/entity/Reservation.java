package com.zsh.petsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.FieldFill;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

// @Data
// @TableName("reservation") // 确保表名与数据库一致
// public class Reservation {

//     @TableId(type = IdType.AUTO)
//     private Long id;

//     // --- 核心 ID ---
//     @TableField("user_id")
//     private Long userId; // 预订的用户

//     @TableField("pet_id")
//     private Long petId; // 接受服务的宠物

//     @TableField("service_id")
//     private Long serviceId; // 预订的服务

//     @TableField("provider_id") // 服务商 ID
//     private Long providerId;

//     @TableField("order_id") // 关联对应的订单
//     private Long orderId;

//     @TableField("reservation_date")
//     private LocalDate reservationDate; // 服务的日期

//     @TableField("service_start_time") // 具体的开始时间
//     private LocalDateTime serviceStartTime;

//     @TableField("service_end_time") // 指定的结束时间
//     private LocalDateTime serviceEndTime;

//     // @TableField("check_in_time") // 实际签到时间
//     // private LocalDateTime checkInTime;

//     // @TableField("check_out_time") //实际签出时间
//     // private LocalDateTime checkOutTime;

//     private String status; // PENDING_CONFIRMATION(待确认), CONFIRMED(已确认), AWAITING_PAYMENT(待支付), PAID(已支付),
//                            // IN_PROGRESS(进行中), COMPLETED(已完成), CANCELLED_USER(用户取消),
//                            // CANCELLED_PROVIDER(服务商取消), REJECTED(已拒绝)

//     @TableField("amount") // 存储预订时的价格
//     private BigDecimal amount;

//     @TableField("user_notes") // 用户预订时的备注
//     private String userNotes;

//     @TableField("provider_notes") // 服务商的备注
//     private String providerNotes;

//     @TableField("cancellation_reason") // 如果取消，记录原因
//     private String cancellationReason;

//     @TableField("rejection_reason")
//     private String rejectionReason;

//     // --- 时间戳 ---
//     @TableField(value = "created_at", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT) // 使用填充策略
//     private LocalDateTime createdAt;

//     @TableField(value = "updated_at", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE) // 使用填充策略
//     private LocalDateTime updatedAt;

//     // --- 标志位 (Provider Confirmed 可能被 Status 替代) ---
//     // private Boolean providerConfirmed; // 考虑用状态值如 PENDING_CONFIRMATION /
//     // CONFIRMED 替代

// }

@Data
@TableName("reservation") // 对应数据库表名
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId; // 预约用户ID

    @TableField("pet_id")
    private Long petId; // 宠物ID

    @TableField("service_id")
    private Long serviceId; // 服务项ID

    @TableField("provider_id")
    private Long providerId; // 服务商ID

    @TableField("order_id") // 关联订单ID
    private Long orderId;

    @TableField("reservation_start_date") // 新增: 预约开始日期
    private LocalDate reservationStartDate;

    @TableField("reservation_end_date") // 新增: 预约结束日期
    private LocalDate reservationEndDate;

    @TableField("service_start_time") // 实际服务开始时间
    private LocalDateTime serviceStartTime;

    @TableField("service_end_time") // 实际服务结束时间
    private LocalDateTime serviceEndTime;

    private String status; // 状态: PENDING_CONFIRMATION(待确认), CONFIRMED(已确认), AWAITING_PAYMENT(待支付),
                           // PAID(已支付/待服务) 等

    @TableField("amount") // 预约金额
    private BigDecimal amount;

    @TableField("user_notes") // 用户备注
    private String userNotes;

    @TableField("provider_notes") // 服务商备注
    private String providerNotes;

    @TableField("cancellation_reason") // 取消原因
    private String cancellationReason;

    @TableField("rejection_reason") // 拒绝原因
    private String rejectionReason;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}