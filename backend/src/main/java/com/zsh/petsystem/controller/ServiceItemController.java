package com.zsh.petsystem.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsh.petsystem.mapper.PaymentMapper;
import com.zsh.petsystem.util.AuthUtil;
import com.zsh.petsystem.model.ServiceItem;
import com.zsh.petsystem.service.ServiceItemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@CrossOrigin
public class ServiceItemController {

    @Autowired
    private ServiceItemService service;

    @GetMapping("/active")
    public ResponseEntity<?> getActiveServices() {
        try {
            List<ServiceItem> activeItems = service.lambdaQuery()
                    // 查询 reviewStatus 为 "APPROVED" 的服务项
                    .eq(ServiceItem::getReviewStatus, "APPROVED")
                    .list();
            // TODO: DTO 转换，过滤不需要给普通用户看的信息
            return ResponseEntity.ok(activeItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取可用服务列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有服务项 (可选接口，根据需要决定是否保留或加权限)
     * 
     * @return 所有服务项列表
     */
    @GetMapping("/all")
    public ResponseEntity<?> listAll() {
        // TODO: 考虑此接口的必要性，以及是否需要权限控制
        try {
            List<ServiceItem> allItems = service.list();
            // TODO: DTO 转换，过滤敏感信息 (如 providerId, rejectionReason 等)
            return ResponseEntity.ok(allItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取所有服务列表失败: " + e.getMessage());
        }
    }
}