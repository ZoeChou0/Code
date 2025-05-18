package com.zsh.petsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderId;
    private Long userId;
    private Long appointmentId;
    private BigDecimal amount;
    private String status; // 例如：待支付、已支付
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}