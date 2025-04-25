package com.zsh.petsystem.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reservationId; // 对应预约记录 ID
    private BigDecimal amount; // 金额
    private String status;     // 状态：待支付、已支付、失败等

    private LocalDateTime createdAt;
}
