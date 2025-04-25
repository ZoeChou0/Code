package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.mapper.OrderMapper;
import com.zsh.petsystem.model.Order;
import com.zsh.petsystem.service.OrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public Order createOrder(OrderCreateDTO dto) {
        Order order = new Order();
        order.setReservationId(dto.getReservationId());
        order.setAmount(dto.getAmount());
        order.setStatus("待支付");
        order.setCreatedAt(LocalDateTime.now());

        this.save(order);
        return order;
    }
}