package com.zsh.petsystem.service;

import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.entity.Order;
import com.zsh.petsystem.entity.Payment;

import java.util.Map;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PaymentService extends IService<Payment> {

    Payment createPayment(OrderCreateDTO orderCreateDTO);

    boolean markAsPaid(Long paymentId);

    boolean updatePaymentStatus(Long orderIs, String status);
}