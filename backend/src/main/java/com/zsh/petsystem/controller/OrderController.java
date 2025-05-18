package com.zsh.petsystem.controller;

import com.zsh.petsystem.annotation.CurrentUser;
import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.dto.OrderFromReservationDTO;
import com.zsh.petsystem.service.OrderService;

import lombok.extern.slf4j.Slf4j;

import com.zsh.petsystem.dto.OrderViewDTO;
import com.zsh.petsystem.entity.Order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderCreateDTO dto) {
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    @PostMapping("/from-reservation")
    @PreAuthorize("isAuthenticated()") // 要求用户必须登录才能调用
    public Order createOrderFromReservation(
            @RequestBody OrderFromReservationDTO dto, // 请求体包含 reservationId
            @CurrentUser Long currentUserId) { // 通过注解注入当前用户ID
        // 让全局响应处理器 (GlobalResponseAdvice) 自动包装成 Result 对象
        return orderService.createOrderFromReservation(dto, currentUserId);
    }

    /**
     * 获取当前登录用户的订单列表 (包含详情)
     * 
     * @param currentUserId 通过 @CurrentUser 注解自动注入
     * @return 用户订单列表 (DTO 格式)
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    // --- 修改返回类型为 List<OrderViewDTO> ---
    public List<OrderViewDTO> getMyOrders(@CurrentUser Long currentUserId) {
        // --- 调用 Service 层的新方法 ---
        return orderService.getUserOrdersWithDetails(currentUserId);
        // 全局响应处理器会自动包装成 Result<List<OrderViewDTO>>
    }

    /**
     * 用户取消订单
     *
     * @param orderId       要取消的订单ID (从路径中获取)
     * @param currentUserId 当前登录用户的ID (通过注解注入)
     * @return 操作结果
     */
    @PostMapping("/cancel/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> cancelMyOrder(@PathVariable Long orderId, @CurrentUser Long currentUserId) { // 返回 Result<?>
        log.info("User {} attempting to cancel order ID: {}", currentUserId, orderId);
        try {
            // 修改 OrderService.cancelUserOrder 方法，使其在成功时不返回 boolean，
            // 而是在失败时抛出具体的业务异常。
            orderService.cancelUserOrder(orderId, currentUserId);
            log.info("Order ID: {} cancelled successfully by user {}", orderId, currentUserId);
            return Result.success(null, "订单取消成功"); // <--- 明确返回成功的 Result
        } catch (SecurityException e) {
            log.warn("SecurityException while cancelling order ID: {} by user {}: {}", orderId, currentUserId,
                    e.getMessage());
            return Result.failed(e.getMessage()); // 假设 Result.failed 会设置 code 为非 200
        } catch (IllegalStateException e) {
            log.warn("IllegalStateException while cancelling order ID: {} by user {}: {}", orderId, currentUserId,
                    e.getMessage());
            return Result.failed(e.getMessage());
        } catch (RuntimeException e) { // 其他运行时异常，应该由全局异常处理器处理以返回标准错误结构
            log.error("Error cancelling order ID: {} by user {}: {}", orderId, currentUserId, e.getMessage(), e);
            // 理想情况下，这类异常会被 @ControllerAdvice 捕获并统一处理成 Result.failed(...)
            // 为确保前端能收到 Result 结构，这里也返回一个
            return Result.failed("取消订单时发生内部服务器错误。");
        }
    }
}