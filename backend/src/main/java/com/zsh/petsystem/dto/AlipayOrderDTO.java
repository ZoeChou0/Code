package com.zsh.petsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Schema(description = "支付宝订单 DTO")
public class AlipayOrderDTO {

    @Schema(description = "订单编号")
    private String orderId;

    @Schema(description = "订单标题/商品标题/交易标题")
    private String subject;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "交易状态，见 TradeStatusType")
    private Integer tradeStatus;

    @Schema(description = "支付方式，见 PayMethod")
    private Integer payMethod;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "创建时间")
    private Timestamp createTime;
}