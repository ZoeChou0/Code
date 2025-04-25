package com.zsh.petsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 支付宝支付请求参数
 */
@Data
public class AliPayReq {

    private String outTradeNo;   // 商户订单号
    private String subject;      // 订单标题
    private BigDecimal totalAmount;  // 金额（单位：元）
}