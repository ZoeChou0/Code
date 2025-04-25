package com.zsh.petsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.model.AlipayOrder;
import org.springframework.transaction.annotation.Transactional;

public interface AlipayOrderService extends IService<AlipayOrder> {

    /**
     * 创建支付宝订单
     */
    @Transactional
    Result<?> createOrder();

    /**
     * 支付宝回调
     *
     * @param orderId 订单的唯一标识符
     */
    @Transactional
    Result<?> getOrderInfo(String orderId);

    /**
     * 支付成功回调
     *
     * @param orderId   订单ID
     * @param payMethod 支付方式
     */
    @Transactional
    Result<?> paySuccess(String orderId, Integer payMethod);

    /**
     * 根据订单ID处理支付成功后的业务逻辑
     *
     * @param orderId   订单ID
     * @param payMethod 支付方式
     */
    @Transactional
    void paySuccessByOrderId(String orderId, Integer payMethod);
}
