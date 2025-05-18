package com.zsh.petsystem.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.dto.OrderFromReservationDTO;
import com.zsh.petsystem.dto.OrderViewDTO;
import com.zsh.petsystem.entity.Order;

public interface OrderService extends IService<Order> {
    Order createOrder(OrderCreateDTO dto);

    Order createOrderFromReservation(OrderFromReservationDTO dto, Long userId);

    /**
     * 获取指定用户的订单列表，并包含服务名称、宠物名称等详细信息
     * 
     * @param userId 用户ID
     * @return 包含详细信息的订单列表 DTO
     */
    List<OrderViewDTO> getUserOrdersWithDetails(Long userId);

    void cancelUserOrder(Long orderId, Long userId);

    /**
     * 服务商标记订单为已完成
     * 
     * @param orderId        订单ID
     * @param providerUserId 当前服务商的用户ID (用于权限验证)
     * @throws RuntimeException (或更具体的业务异常) 如果操作失败
     */
    void completeOrderByProvider(Long orderId, Long providerUserId);
}