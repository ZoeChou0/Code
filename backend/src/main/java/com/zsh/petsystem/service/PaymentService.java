package com.zsh.petsystem.service;

import com.zsh.petsystem.dto.OrderCreateDTO;
import java.util.Map;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zsh.petsystem.model.Order;
import com.zsh.petsystem.model.Payment;

public interface PaymentService extends IService<Payment> {

    Payment createPayment(OrderCreateDTO orderCreateDTO);
    boolean markAsPaid(Long paymentId);
    boolean updatePaymentStatus(Long orderIs, String status);
}