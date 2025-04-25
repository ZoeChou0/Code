package com.zsh.petsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.model.Order;

public interface OrderService extends IService<Order> {
    Order createOrder(OrderCreateDTO dto);
}