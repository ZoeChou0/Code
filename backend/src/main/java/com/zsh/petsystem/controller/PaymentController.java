package com.zsh.petsystem.controller;

import com.zsh.petsystem.entity.Payment;
import com.zsh.petsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@CrossOrigin
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Payment payment) {
        try {
            boolean success = paymentService.save(payment);
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body(payment);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建支付记录失败");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建支付记录时出错: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Payment>> getAll() {
        return ResponseEntity.ok(paymentService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Payment payment = paymentService.getById(id);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到支付记录"); // 404 Not Found
        }
    }

    @PutMapping("{id}/pay")
    public ResponseEntity<?> markAsPaid(@PathVariable Long id) {
        try {
            // 调用你在 Service 中定义的 markAsPaid 方法
            boolean updated = paymentService.markAsPaid(id);
            if (updated) {
                return ResponseEntity.ok("订单状态已更新为已支付");
            } else {
                // markAsPaid 内部应该抛异常，或者这里根据 Service 返回判断
                // 如果 Service 返回 false 表示未找到或已经是已支付
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("订单不存在或状态无法更新"); // 400 Bad Request
            }
        } catch (RuntimeException e) {
            // 捕获 Service 抛出的异常，例如 "支付记录不存在"
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("更新失败: " + e.getMessage()); // 404 Not Found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("标记为已支付时出错: " + e.getMessage());
        }
    }
}