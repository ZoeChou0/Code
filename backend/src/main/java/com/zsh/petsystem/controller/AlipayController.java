package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.config.AlipayConfig;
import com.zsh.petsystem.dto.AliPayReq;
import com.zsh.petsystem.service.AlipayService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付控制器
 *
 * @author javgo.cn
 * @date 2024/1/13
 */
@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private AlipayService alipayService;

    @Operation(summary = "支付宝电脑网站支付接口")
    @GetMapping("/pcPayment")
    public void pcPayment(AliPayReq aliPayReq, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=" + alipayConfig.getCharset());
        Result<?> result = alipayService.initiatePcPayment(aliPayReq);
        response.getWriter().write(result.getData().toString());
        response.getWriter().flush();
        response.getWriter().close();
    }


    @Operation(summary = "支付宝手机网站支付接口")
    @GetMapping("/mobilePayment")
    public void mobilePayment(AliPayReq aliPayReq, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=" + alipayConfig.getCharset());
        Result<?> result = alipayService.initiateMobilePayment(aliPayReq);
        response.getWriter().write(result.getData().toString());
        response.getWriter().flush();
        response.getWriter().close();
    }


    @Operation(summary = "支付宝支付通知")
    @PostMapping("/notify")
    @ResponseBody
    public Result<?> processPaymentNotification(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        requestParams.keySet().forEach(r -> params.put(r, request.getParameter(r)));
        return alipayService.processPaymentNotification(params);
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/queryPaymentStatus")
    @ResponseBody
    public Result<?> queryPaymentStatus(String outTradeNo, String tradeNo) {
        return alipayService.queryPaymentStatus(outTradeNo, tradeNo);
    }
}
