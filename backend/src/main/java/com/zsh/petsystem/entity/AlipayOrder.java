package com.zsh.petsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("alipay_order")
@Schema(name = "AlipayOrder", description = "支付宝支付订单表")
public class AlipayOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @Schema(description = "订单编号")
    private String orderId;

    @Schema(description = "订单标题/商品标题/交易标题")
    private String subject;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "交易状态，见 TradeStatusType")
    private Integer tradeStatus;

    @Schema(description = "商户订单号")
    private String outTradeNo;

    @Schema(description = "支付方式，见 PayMethod")
    private Integer payMethod;

    @Schema(description = "产品码")
    private String productCode;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "支付宝交易号")
    private String tradeNo;

    @Schema(description = "买家支付宝账号")
    private String buyerId;

    @Schema(description = "交易付款时间")
    private Timestamp gmtPayment;

    @Schema(description = "用户在交易中支付的金额")
    private BigDecimal buyerPayAmount;

    @Schema(description = "创建时间")
    private Timestamp createTime;
}