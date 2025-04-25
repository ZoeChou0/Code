package com.zsh.petsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.config.AlipayConfig;
import com.zsh.petsystem.dto.AliPayReq;
import com.zsh.petsystem.enums.PayMethod;
import com.zsh.petsystem.enums.TradeStatusType;
import com.zsh.petsystem.model.AlipayOrder;
import com.zsh.petsystem.model.Order;
import com.zsh.petsystem.model.Reservation;
import com.zsh.petsystem.service.AlipayOrderService;
import com.zsh.petsystem.service.AlipayService;
import com.zsh.petsystem.service.OrderService;
import com.zsh.petsystem.service.ReservationService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付宝支付服务实现类
 * 
 */
@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private AlipayOrderService alipayOrderService;

    @Resource
    private OrderService orderService;
    @Resource
    private ReservationService reservationService;
    /**
     * 电脑网站支付产品编号(固定值)
     */
    private static final String PC_PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";

    /**
     * 交易结算信息
     */
    private static final String TRADE_SETTLE_INFO = "trade_settle_info";

    @Override
    public Result<?> initiatePcPayment(AliPayReq aliPayReq) {
        // 支付条件前置检查
        String outTradeNo = aliPayReq.getOutTradeNo();
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            return Result.failed("支付请求缺少订单号");
        }

        // 查询？
        Order order;
        Reservation reservation = null;

        try {
            long orderId = Long.parseLong(outTradeNo);
            order = orderService.getById(orderId);
            if (order != null && order.getReservationId() != null) {
                reservation = reservationService.getById(order.getReservationId());
            }
        } catch (NumberFormatException e) {
            log.error("无法解析订单号为 Long: {}", outTradeNo);
            return Result.failed("无效的订单号格式");
        } catch (Exception e) {
            log.error("查询订单或预约信息时出错, outTradeNo={}", outTradeNo, e);
            return Result.failed("查询订单信息失败");
        }

        // 2. 检查订单和预约是否存在
        if (order == null) {
            return Result.failed("订单不存在 (ID: " + outTradeNo + ")");
        }
        if (reservation == null) {
            return Result.failed("关联的预约不存在 (Order ID: " + outTradeNo + ")");
        }

        // 3. 检查订单状态是否为 '待支付'
        if (!"待支付".equals(order.getStatus())) { // 确认你的状态字符串
            return Result.failed("订单状态不是待支付，无法发起支付 (当前状态: " + order.getStatus() + ")");
        }

        // 4. 检查服务商确认
        if (!Boolean.TRUE.equals(reservation.getProviderConfirmed())) {
            return Result.failed("服务商尚未确认该预约，请稍后或联系服务商");
        }
        log.warn("支付检查跳过：服务商确认状态检查未实现 (Reservation ID: {})", reservation.getId());

        // 5. 检查距离服务开始时间是否大于 24 小时
        LocalDateTime serviceStartTime = reservation.getReservationTime();
        LocalDateTime now = LocalDateTime.now();
        if (serviceStartTime == null || !serviceStartTime.isAfter(now.plusHours(24))) {
            // ChronoUnit.HOURS.between(now, serviceStartTime) < 24
            return Result.failed("距离服务开始不足24小时，无法在线支付，请联系服务商");
        }

        log.info("支付条件检查通过，订单号: {}", outTradeNo);

        try {
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            // 设置同步/异步通知地址 (可以封装成私有方法)
            setNotifyAndReturnUrl(alipayRequest);
            // 构造业务参数 (可以封装成私有方法)
            JSONObject bizContent = constructBizContent(aliPayReq, PC_PRODUCT_CODE); // PC_PRODUCT_CODE 需要定义
            alipayRequest.setBizContent(bizContent.toJSONString());

            String formHtml = alipayClient.pageExecute(alipayRequest).getBody();
            return Result.success(formHtml, "支付宝 PC 端支付请求成功"); // 使用 Result 包装
        } catch (Exception e) {
            log.error("支付宝 PC 端支付请求失败", e);
            return Result.failed("支付宝 PC 端支付请求失败: " + e.getMessage()); // 使用 Result 包装
        }

    }

    @Override
    public Result<?> processPaymentNotification(Map<String, String> params) {
        String result = "failure";
        boolean signVerified = false;
        try {
            // 1. 验证签名
            signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(), alipayConfig.getSignType());
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return Result.failed("支付宝支付结果通知签名验证失败");
        }
        if (signVerified) {
            // 2. 验证交易状态
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 3. 更新订单状态
                result = "success";
                AlipayOrder alipayOrder = BeanUtil.mapToBean(params, AlipayOrder.class, true, null);
                alipayOrder.setOrderId(params.get("out_trade_no"));
                alipayOrder.setTradeStatus(TradeStatusType.TRADE_SUCCESS.getCode());
                alipayOrder.setPayMethod(PayMethod.ALIPAY.getCode());
                log.info("支付宝支付结果通知参数：{}", JSON.toJSONString(alipayOrder));
                QueryWrapper<AlipayOrder> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_id", alipayOrder.getOrderId());
                alipayOrderService.update(alipayOrder, queryWrapper);
                log.info("支付宝订单交易成功，交易状态：{}", tradeStatus);
                // 4.执行回调
                alipayOrderService.paySuccessByOrderId(alipayOrder.getOrderId(), alipayOrder.getPayMethod());
            } else {
                log.error("支付宝订单交易失败，交易状态：{}", tradeStatus);
            }
        } else {
            log.error("支付宝支付结果通知签名验证失败");
        }
        return result.equals("success") ? Result.success("支付宝支付结果通知处理成功") : Result.failed("支付宝支付结果通知处理失败");
    }

    @Override
    public Result<?> queryPaymentStatus(String outTradeNo, String tradeNo) {
        // 1. 创建支付宝支付查询请求
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
        // 2. 设置支付宝支付请求参数
        JSONObject bizContent = new JSONObject();
        if (StringUtils.isNotEmpty(outTradeNo)) {
            // 商户订单号
            bizContent.put("out_trade_no", outTradeNo);
        }
        if (StringUtils.isNotEmpty(tradeNo)) {
            // 支付宝交易号
            bizContent.put("trade_no", tradeNo);
        }
        // 交易结算信息
        String[] queryOptions = new String[] { TRADE_SETTLE_INFO };
        bizContent.put("query_options", queryOptions);
        alipayRequest.setBizContent(bizContent.toJSONString());
        // 3. 发起支付宝支付查询请求
        AlipayTradeQueryResponse alipayResponse = null;
        try {
            alipayResponse = alipayClient.execute(alipayRequest);
        } catch (AlipayApiException e) {
            log.error("支付宝支付查询请求失败", e);
            return Result.failed("支付宝支付查询请求失败");
        }
        // 4. 处理支付宝支付查询结果（支付状态见 TradeStatusType）
        if (alipayResponse.isSuccess()) {
            log.info("支付宝支付查询请求成功");
            // 5.执行回调
            alipayOrderService.paySuccessByOrderId(outTradeNo, PayMethod.ALIPAY.getCode());
            return Result.success(alipayResponse.getTradeStatus(), "支付宝支付查询请求成功");
        } else {
            log.error("支付宝支付查询请求失败");
            return Result.failed(alipayResponse.getTradeStatus(), "支付宝支付查询请求失败");
        }
    }

    /**
     * 发起支付宝支付请求（电脑网站支付和手机网站支付）
     * 
     * @param aliPayReq      支付请求参数
     * @param productCode    产品编号
     * @param failMessage    失败提示信息
     * @param successMessage 成功提示信息
     * @return Result 返回支付结果，包含支付表单或错误信息
     */
    private Result<?> initiatePayment(AliPayReq aliPayReq, String productCode, String failMessage,
            String successMessage) {
        // 1. 创建支付宝支付请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        // 2. 设置支付宝支付同步通知页面和异步通知地址
        setNotifyAndReturnUrl(alipayRequest);

        // 3. 设置支付宝支付请求参数
        JSONObject bizContent = constructBizContent(aliPayReq, productCode);
        alipayRequest.setBizContent(bizContent.toJSONString());

        String formHtml = null;
        try {
            formHtml = alipayClient.pageExecute(alipayRequest).getBody();
            return Result.success(formHtml, successMessage);
        } catch (Exception e) {
            log.error(failMessage, e);
            return Result.failed(formHtml, failMessage);
        }
    }

    /**
     * 设置支付宝支付同步通知页面和异步通知地址
     * 
     * @param alipayRequest 支付宝支付请求
     */
    private void setNotifyAndReturnUrl(AlipayTradePagePayRequest alipayRequest) {
        // 设置同步通知页面
        if (StringUtils.isNotEmpty(alipayConfig.getReturnUrl())) {
            alipayRequest.setReturnUrl(alipayConfig.getReturnUrl());
        }
        // 设置异步通知地址
        if (StringUtils.isNotEmpty(alipayConfig.getNotifyUrl())) {
            alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
    }

    /**
     * 构造支付宝支付请求参数
     *
     * @param aliPayReq   支付请求参数
     * @param productCode 产品编号
     * @return JSONObject 支付宝支付请求参数
     */
    private JSONObject constructBizContent(AliPayReq aliPayReq, String productCode) {
        JSONObject bizContent = new JSONObject();
        // 订单标题（不可以使用特殊字符）
        bizContent.put("subject", aliPayReq.getSubject());
        // 商户订单号（由商家自定义的唯一订单号）
        bizContent.put("out_trade_no", aliPayReq.getOutTradeNo());
        // 订单总金额(元)，最小值为0.01
        bizContent.put("total_amount", aliPayReq.getTotalAmount());
        // 销售产品码，与支付宝签约的产品码名称（固定值）
        bizContent.put("product_code", productCode);
        return bizContent;
    }
}
