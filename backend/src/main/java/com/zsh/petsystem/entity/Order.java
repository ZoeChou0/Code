package com.zsh.petsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders") // 确认表名
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private Long reservationId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    @TableField("pay_time")
    private LocalDateTime payTime;
    @TableField("complete_time")
    private LocalDateTime completeTime;

    @TableField("alipay_trade_no")
    private String alipayTradeNo;

    @TableField("review_id")
    private Long reviewId;

}
