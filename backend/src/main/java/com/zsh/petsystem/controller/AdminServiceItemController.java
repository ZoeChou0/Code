package com.zsh.petsystem.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsh.petsystem.dto.ServiceItemRejectionDTO;
import com.zsh.petsystem.model.Reservation;
import com.zsh.petsystem.model.ServiceItem;

import com.zsh.petsystem.service.ServiceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/admin/services")
public class AdminServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    // 获取所有服务项（可加分页）
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServiceItem>> getAll() {
        return ResponseEntity.ok(serviceItemService.list());
    }

    // 获取待审核服务项
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPending() {
        // ServiceImpl 提供了 list 方法，直接用 LambdaQueryWrapper 查询
        try {
            List<ServiceItem> pendingItems = serviceItemService.lambdaQuery()
                    .eq(ServiceItem::getReviewStatus, "PENDING") // 查询状态为 PENDING
                    .list();
            // TODO: DTO 转换，过滤敏感信息
            return ResponseEntity.ok(pendingItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取待审核列表失败: " + e.getMessage());
        }
    }

    // 启用服务项
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            boolean success = serviceItemService.approveServiceItem(id);
            if (success) {
                return ResponseEntity.ok("服务项已批准");
            } else {
                // 可能因为状态不对或者 ID 不存在导致失败
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("操作失败，服务项可能不存在或状态不正确");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("批准失败: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理批准请求时出错: " + e.getMessage());
        }
    }

    // 禁用服务项
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disable(@PathVariable Long id, @RequestBody ServiceItemRejectionDTO rejectionDTO) {
        // 检查驳回原因是否为空
        if (rejectionDTO == null || rejectionDTO.getReason() == null || rejectionDTO.getReason().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("必须提供驳回原因");
        }

        try {
            boolean success = serviceItemService.rejectServiceItem(id, rejectionDTO.getReason().trim());
            if (success) {
                return ResponseEntity.ok("服务项已拒绝");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("操作失败，服务项可能不存在或状态不正确");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("拒绝失败: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理拒绝请求时出错: " + e.getMessage());
        }

    }
}