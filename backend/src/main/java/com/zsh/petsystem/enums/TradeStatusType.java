package com.zsh.petsystem.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单交易状态
 *
 */
@Getter
@AllArgsConstructor
public enum TradeStatusType {

    WAIT_BUYER_PAY(1, "WAIT_BUYER_PAY", "交易创建，等待买家付款"),
    TRADE_CLOSED(2, "TRADE_CLOSED", "未付款交易超时关闭，或支付完成后全额退款"),
    TRADE_SUCCESS(3, "TRADE_SUCCESS", "交易支付成功"),
    TRADE_FINISHED(4, "TRADE_FINISHED", "交易结束，不可退款"),
    TRADE_FAILED(5, "TRADE_FAILED", "支付失败");

    private final Integer code;
    private final String status;
    private final String description;

    public static TradeStatusType of(String status) {
        for (TradeStatusType tradeStatus : values()) {
            if (tradeStatus.getStatus().equals(status)) {
                return tradeStatus;
            }
        }
        return null;
    }
}
