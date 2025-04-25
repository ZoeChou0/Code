package com.zsh.petsystem.service;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.AliPayReq;

import java.util.Map;

/**
 * 支付宝支付服务类（支付宝支付流程中的主要操作方法）
 */
public interface AlipayService {

    /**
     * 发起支付宝电脑网站支付请求
     *
     * @param aliPayReq 支付请求参数
     * @return Result 返回支付结果，包含支付表单或错误信息
     */
    Result<?> initiatePcPayment(AliPayReq aliPayReq);

    /**
     * 处理支付宝支付结果的异步通知
     *
     * @param params 从支付宝回调接收到的参数集合
     * @return Result 返回处理结果，成功或失败
     */
    Result<?> processPaymentNotification(Map<String, String> params);

    /**
     * 查询支付宝交易的支付状态
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo    支付宝交易号
     * @return Result 返回查询结果，包含交易状态或错误信息
     */
    Result<?> queryPaymentStatus(String outTradeNo, String tradeNo);
}
