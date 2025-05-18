package com.zsh.petsystem.service.impl;

import com.alibaba.fastjson.JSON; // 确保导入 fastjson
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.config.AlipayConfig;
import com.zsh.petsystem.dto.AliPayReq;
import com.zsh.petsystem.entity.Order;
import com.zsh.petsystem.entity.Reservation;
import com.zsh.petsystem.service.AlipayService;
import com.zsh.petsystem.service.OrderService;
import com.zsh.petsystem.service.ReservationService; // 导入 ReservationService
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private OrderService orderService;

    @Resource // 注入 ReservationService
    private ReservationService reservationService;

    private static final String PC_PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";

    @Override
    public Result<?> initiatePcPayment(AliPayReq aliPayReq) {
        // ... (你之前的 initiatePcPayment 代码保持不变) ...
        String outTradeNo = aliPayReq.getOutTradeNo();
        if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
            return Result.failed("支付请求缺少订单号");
        }
        Order order;
        try {
            long orderIdLong = Long.parseLong(outTradeNo);
            order = orderService.getById(orderIdLong);
        } catch (NumberFormatException e) {
            log.error("无法将订单号 '{}' 解析为 Long", outTradeNo, e);
            return Result.failed("无效的订单号格式");
        } catch (Exception e) {
            log.error("查询订单信息时出错, 订单号={}", outTradeNo, e);
            return Result.failed("查询订单信息失败");
        }
        if (order == null) {
            return Result.failed("订单不存在 (ID: " + outTradeNo + ")");
        }
        if (!"待支付".equalsIgnoreCase(order.getStatus()) && !"pending".equalsIgnoreCase(order.getStatus())) {
            log.warn("订单 {} 状态为 {}，无法发起支付。", outTradeNo, order.getStatus());
            return Result.failed("订单状态不是待支付，无法发起支付");
        }
        if (order.getAmount() == null || order.getAmount().doubleValue() <= 0) {
            log.error("订单 {} 金额无效: {}", outTradeNo, order.getAmount());
            return Result.failed("订单金额无效，无法支付");
        }
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(alipayConfig.getReturnUrl());
        alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", order.getAmount().toString());
        bizContent.put("subject", "宠乐居服务订单 - " + order.getId());
        bizContent.put("product_code", PC_PRODUCT_CODE);
        alipayRequest.setBizContent(bizContent.toString());
        log.info("向支付宝发起支付请求, 订单号: {}, 内容: {}", outTradeNo, bizContent.toString());
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest);
            String formHtml = response.getBody();
            log.info("支付宝支付表单生成成功, 订单号: {}", outTradeNo);
            return Result.success(formHtml, "请求成功，请在新页面完成支付");
        } catch (AlipayApiException e) {
            log.error("请求支付宝支付接口失败, 订单号: {}, 错误码: {}, 错误信息: {}", outTradeNo, e.getErrCode(), e.getErrMsg(), e);
            return Result.failed("支付宝支付请求失败: " + e.getErrMsg());
        }
    }

    @Override
    @Transactional // 确保事务性
    public Result<?> processPaymentNotification(Map<String, String> params) {
        log.info(">>>> [AlipayService] processPaymentNotification started with params: {}", params);

        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no"); // 支付宝交易号
        String appId = params.get("app_id"); // 获取通知中的 app_id
        String totalAmountFromNotify = params.get("total_amount"); // 获取通知中的金额

        log.info(
                ">>>> [AlipayService] Extracted: out_trade_no: {}, trade_status: {}, trade_no: {}, app_id: {}, total_amount: {}",
                outTradeNo, tradeStatus, tradeNo, appId, totalAmountFromNotify);

        // 0. 基础参数校验
        if (outTradeNo == null || tradeStatus == null) {
            log.error(
                    ">>>> [AlipayService] Essential parameters (out_trade_no or trade_status) are missing from notification.");
            return Result.failed("failure");
        }

        // 1. 验签
        boolean signVerified = false;
        try {
            log.info(">>>> [AlipayService] Attempting to verify signature...");
            // 重要：确保 alipayConfig.getAlipayPublicKey() 获取到的是正确的【支付宝公钥】
            signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(), alipayConfig.getSignType());
            log.info(">>>> [AlipayService] Signature verification result: {}", signVerified);
        } catch (AlipayApiException e) {
            log.error(">>>> [AlipayService] AlipaySignature.rsaCheckV1 FAILED during signature verification.", e);
            return Result.failed("failure"); // 验签异常
        }

        if (!signVerified) {
            log.warn(">>>> [AlipayService] Alipay async notification signature VERIFICATION FAILED. Params: {}",
                    params);
            return Result.failed("failure"); // 验签失败
        }
        log.info(">>>> [AlipayService] Signature VERIFIED for out_trade_no: {}", outTradeNo);

        // 2. 校验 app_id 是否与配置一致
        if (!alipayConfig.getAppId().equals(appId)) {
            log.error(">>>> [AlipayService] app_id mismatch! Expected: {}, Received: {}", alipayConfig.getAppId(),
                    appId);
            return Result.failed("failure");
        }
        log.info(">>>> [AlipayService] app_id VERIFIED for out_trade_no: {}", outTradeNo);

        // 3. 获取订单信息
        Order order;
        try {
            order = orderService.getById(Long.parseLong(outTradeNo));
            log.info(">>>> [AlipayService] Fetched order by ID {}: {}", outTradeNo,
                    order != null ? "Found, Status: " + order.getStatus() : "NOT FOUND");
        } catch (NumberFormatException e) {
            log.error(">>>> [AlipayService] Error parsing out_trade_no '{}' to Long.", outTradeNo, e);
            return Result.failed("failure");
        } catch (Exception e) {
            log.error(">>>> [AlipayService] Error fetching order by ID {} from database.", outTradeNo, e);
            return Result.failed("failure"); // 数据库查询异常也应该让支付宝重试
        }

        if (order == null) {
            log.warn(
                    ">>>> [AlipayService] Order NOT FOUND for out_trade_no: {}. This might be an issue or an old/invalid notification.",
                    outTradeNo);
            // 即使订单不存在，但既然验签通过，也应该回复 "success" 给支付宝，避免它不断重试一个无效的通知。
            return Result.success("success");
        }

        // 4. 校验金额 (可选但推荐)
        // 注意：支付宝返回的金额是字符串，需要转换为 BigDecimal 比较
        if (totalAmountFromNotify != null && order.getAmount() != null) {
            if (order.getAmount().compareTo(new java.math.BigDecimal(totalAmountFromNotify)) != 0) {
                log.error(">>>> [AlipayService] Total amount mismatch for order {}. Expected: {}, Received: {}",
                        outTradeNo, order.getAmount(), totalAmountFromNotify);
                return Result.failed("failure"); // 金额不匹配，可能有问题
            }
            log.info(">>>> [AlipayService] Total amount VERIFIED for order: {}", outTradeNo);
        } else {
            log.warn(
                    ">>>> [AlipayService] Could not verify total amount for order {}. Either order amount or notify amount is null.",
                    outTradeNo);
            // 根据业务决定是否因为无法校验金额而失败，通常建议失败
            return Result.failed("failure");
        }

        // 5. 检查订单状态，防止重复处理
        log.info(">>>> [AlipayService] Current order status for {}: {}", outTradeNo, order.getStatus());
        if ("已支付".equalsIgnoreCase(order.getStatus()) || "paid".equalsIgnoreCase(order.getStatus())) {
            log.info(">>>> [AlipayService] Order {} already processed as PAID. Ignoring duplicate notification.",
                    outTradeNo);
            return Result.success("success");
        }
        if ("已取消".equalsIgnoreCase(order.getStatus()) || "cancelled".equalsIgnoreCase(order.getStatus())) {
            log.info(">>>> [AlipayService] Order {} is CANCELLED. Ignoring payment notification.", outTradeNo);
            return Result.success("success");
        }

        // 6. 根据交易状态处理业务逻辑
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            log.info(">>>> [AlipayService] Trade status is SUCCESS/FINISHED for order: {}. Preparing to update DB.",
                    outTradeNo);

            order.setStatus("已支付"); // 或使用枚举 OrderStatus.PAID.name()
            order.setPayTime(LocalDateTime.now());
            if (tradeNo != null) { // 保存支付宝交易号
                order.setAlipayTradeNo(tradeNo);
                log.info(">>>> [AlipayService] Setting alipayTradeNo: {} for order {}", tradeNo, outTradeNo);
            }

            boolean orderUpdated = false;
            try {
                orderUpdated = orderService.updateById(order);
            } catch (Exception e) {
                log.error(">>>> [AlipayService] EXCEPTION during orderService.updateById for order {}", outTradeNo, e);
                return Result.failed("failure"); // 数据库更新异常
            }

            log.info(">>>> [AlipayService] Order {} DB update result: {}", outTradeNo, orderUpdated);

            if (orderUpdated) {
                log.info(">>>> [AlipayService] Order {} successfully updated to '/待服务'.", outTradeNo);

                // 更新关联的预约状态
                if (order.getReservationId() != null) {
                    log.info(">>>> [AlipayService] Attempting to update reservation status for reservation ID: {}",
                            order.getReservationId());
                    Reservation reservation = reservationService.getById(order.getReservationId());
                    if (reservation != null) {
                        // 例子：如果预约是 AWAITING_PAYMENT，则更新为 PAID
                        if ("AWAITING_PAYMENT".equalsIgnoreCase(reservation.getStatus())) {
                            reservation.setStatus("PAID"); // 或其他合适的状态，如 CONFIRMED
                            try {
                                boolean reservationUpdated = reservationService.updateById(reservation);
                                log.info(">>>> [AlipayService] Reservation {} status update result: {}",
                                        reservation.getId(), reservationUpdated);
                            } catch (Exception e) {
                                log.error(
                                        ">>>> [AlipayService] EXCEPTION during reservationService.updateById for reservation {}",
                                        reservation.getId(), e);
                                // 即使预约更新失败，订单支付成功的事实不变，后续可以有补偿机制。
                                // 但为了支付宝不再重试，这里仍然可以考虑返回 success，关键看业务容忍度。
                                // 或者，如果预约更新非常关键，这里返回 failure 让支付宝重试，看能否解决。
                            }
                        } else {
                            log.warn(
                                    ">>>> [AlipayService] Reservation {} status is '{}', not AWAITING_PAYMENT. No status change made.",
                                    reservation.getId(), reservation.getStatus());
                        }
                    } else {
                        log.warn(">>>> [AlipayService] Could not find reservation with ID {} to update status.",
                                order.getReservationId());
                    }
                } else {
                    log.info(">>>> [AlipayService] Order {} has no associated reservationId.", outTradeNo);
                }

                // TODO: 在此触发其他业务，例如通知用户支付成功、通知服务商等

                log.info(
                        ">>>> [AlipayService] All processing for order {} successful. Returning 'success' to service caller.",
                        outTradeNo);
                return Result.success("success"); // 返回给 Controller，Controller 会把它转为纯文本 "success"
            } else {
                log.error(
                        ">>>> [AlipayService] FAILED to update order {} to '已支付' in DB. Database operation returned false.",
                        outTradeNo);
                return Result.failed("failure");
            }
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            log.info(">>>> [AlipayService] Trade for order {} is CLOSED. No DB update needed for status '已支付.",
                    outTradeNo);
            // 可选：如果需要，可以将订单状态更新为“已关闭”或“已取消”
            // order.setStatus("已取消"); // 或 "交易关闭"
            // orderService.updateById(order);
            return Result.success("success");
        } else {
            log.warn(
                    ">>>> [AlipayService] Order {} received an unhandled trade_status: {}. Telling Alipay we received it.",
                    outTradeNo, tradeStatus);
            return Result.success("success"); // 其他状态暂时也返回 success，避免支付宝不断重试
        }
    }

    @Override
    public Result<?> queryPaymentStatus(String outTradeNo, String tradeNo) {
        // ... (你之前的 queryPaymentStatus 代码保持不变) ...
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        if (outTradeNo != null && !outTradeNo.isEmpty()) {
            bizContent.put("out_trade_no", outTradeNo);
        } else if (tradeNo != null && !tradeNo.isEmpty()) {
            bizContent.put("trade_no", tradeNo);
        } else {
            return Result.failed("查询支付状态：订单号和支付宝交易号至少需要一个");
        }
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                log.info("支付宝支付状态查询成功: {}", response.getBody());
                return Result.success(JSON.parse(response.getBody()), "查询成功");
            } else {
                log.warn("支付宝支付状态查询失败: code={}, msg={}", response.getCode(), response.getMsg());
                return Result.failed(response.getMsg(), "查询失败 (错误码: " + response.getCode() + ")");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝支付状态查询请求异常: {}", e.getErrMsg(), e);
            return Result.failed("查询支付状态请求异常: " + e.getErrMsg());
        }
    }
}