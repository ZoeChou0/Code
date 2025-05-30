package com.zsh.petsystem.controller;

import com.zsh.petsystem.dto.OrderAdminViewDTO;
import com.zsh.petsystem.service.OrderService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage; // 导入 IPage

@RestController
@RequestMapping("/admin/orders")
@CrossOrigin
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // 对整个控制器应用管理员权限
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public IPage<OrderAdminViewDTO> list(
            @RequestParam(name = "page", defaultValue = "1") int pageNum,
            @RequestParam(name = "size", defaultValue = "10") int pageSize,
            @RequestParam(name = "status", required = false) String status) {
        return orderService.getAdminOrdersPage(pageNum, pageSize, status);
    }
}
