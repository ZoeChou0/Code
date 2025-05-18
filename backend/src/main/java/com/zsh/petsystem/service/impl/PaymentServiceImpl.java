package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.entity.Payment;
import com.zsh.petsystem.mapper.PaymentMapper;
import com.zsh.petsystem.service.PaymentService;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl
        extends ServiceImpl<PaymentMapper, Payment>
        implements PaymentService {

    @Override
    public Payment createPayment(OrderCreateDTO dto) {
        Payment payment = new Payment();

        payment.setUserId(dto.getUserId());
        payment.setAppointmentId(dto.getReservationId());
        payment.setAmount(dto.getAmount());
        payment.setStatus("待支付");
        payment.setCreatedAt(LocalDateTime.now());

        this.save(payment);
        return payment;
    }

    @Override
    public boolean markAsPaid(Long paymentId) {
        Payment payment = this.getById(paymentId);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }

        payment.setStatus("已支付/待服务");
        payment.setPaidAt(LocalDateTime.now());
        return this.updateById(payment);
    }

    @Override
    public boolean updatePaymentStatus(Long orderId, String status) {
        LambdaUpdateWrapper<Payment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId).set(Payment::getStatus, status);
        return this.update(wrapper);

    }

}
