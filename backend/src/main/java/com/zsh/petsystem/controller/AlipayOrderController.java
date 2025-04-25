package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.service.AlipayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 支付宝支付订单表 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/alipayOrder")
public class AlipayOrderController {

    @Resource
    private AlipayOrderService alipayOrderService;


    @Operation(summary = "创建订单")
    @PostMapping("/createOrder")
    public Result<?> createOrder() {
        return alipayOrderService.createOrder();
    }

    @Operation(summary = "获取订单信息")
    @GetMapping("/getOrderInfo")
    public Result<?> getOrderInfo(String orderId) {
        return alipayOrderService.getOrderInfo(orderId);
    }
}
