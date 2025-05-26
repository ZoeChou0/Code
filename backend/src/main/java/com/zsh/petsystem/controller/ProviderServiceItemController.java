package com.zsh.petsystem.controller;

import com.zsh.petsystem.annotation.CurrentUser;
import com.zsh.petsystem.common.Result; // <--- 确保导入 Result 类
import com.zsh.petsystem.dto.ServiceItemUpdateDTO;
import com.zsh.petsystem.entity.ServiceItem;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.ServiceItemService;
import lombok.extern.slf4j.Slf4j; // <--- 导入日志注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/provider/services")
@Slf4j
public class ProviderServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    @PostMapping("/add")
    // 返回值改为 ResponseEntity<Result<?>>
    public ResponseEntity<Result<?>> addService(@RequestBody ServiceItem item, // 建议使用 CreateDTO
            @CurrentUser Users currentProvider) {
        if (currentProvider == null) {
            // 使用 Result.failed 包装错误信息
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("无效的 Token 或用户未登录"));
        }
        if (!"provider".equalsIgnoreCase(currentProvider.getRole())) {
            // 使用 Result.failed 包装错误信息
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed("只有服务商才能添加服务项"));
        }

        // 后端设置关键属性
        item.setProviderId(currentProvider.getId());
        item.setReviewStatus("PENDING");
        item.setRejectionReason(null);
        item.setId(null);

        try {
            ServiceItem addedItem = serviceItemService.add(item);
            log.info("Provider {} added new service '{}' (ID: {})", currentProvider.getId(), addedItem.getName(),
                    addedItem.getId());
            // 使用 Result.success 包装成功返回的数据
            return ResponseEntity.ok(Result.success(addedItem, "服务添加成功，等待审核"));
        } catch (Exception e) {
            log.error("Error adding service for provider {}: {}", currentProvider.getId(), e.getMessage(), e);
            // 使用 Result.failed 包装服务器错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.failed("添加服务失败: " + e.getMessage()));
        }
    }

    @GetMapping("/my")
    // 返回值改为 ResponseEntity<Result<List<ServiceItem>>> 或 ResponseEntity<Result<?>>
    public ResponseEntity<Result<?>> myServices(@CurrentUser Users currentProvider) {
        if (currentProvider == null) {
            // 使用 Result.failed 包装错误信息
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("无效的 Token 或用户未登录"));
        }
        if (!"provider".equalsIgnoreCase(currentProvider.getRole())) {
            // 使用 Result.failed 包装错误信息
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed("只有服务商才能查看自己的服务项"));
        }

        try {
            List<ServiceItem> items = serviceItemService.lambdaQuery()
                    .eq(ServiceItem::getProviderId, currentProvider.getId())
                    .list();
            // TODO: DTO 转换以隐藏不必要信息
            // 使用 Result.success 包装成功返回的数据列表
            return ResponseEntity.ok(Result.success(items));
        } catch (Exception e) {
            log.error("Error fetching services for provider {}: {}", currentProvider.getId(), e.getMessage(), e);
            // 使用 Result.failed 包装服务器错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.failed("查询服务列表失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    // 返回值改为 ResponseEntity<Result<?>>
    public ResponseEntity<Result<?>> updateMyService(@PathVariable Long id,
            @RequestBody ServiceItemUpdateDTO updateDTO, // 建议扩展此 DTO
            @CurrentUser Users currentProvider) {
        // --- 权限检查 (保持不变，但使用 Result 包装错误返回) ---
        if (currentProvider == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("无效的 Token 或用户未登录"));
        }
        if (!"provider".equalsIgnoreCase(currentProvider.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed("只有服务商才能修改服务项"));
        }
        if (updateDTO.getId() == null) {
            updateDTO.setId(id);
        } else if (!Objects.equals(id, updateDTO.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.failed("路径 ID 与请求体中的 ID 不匹配"));
        }
        // --- 权限检查结束 ---

        try {
            ServiceItem updatedItem = serviceItemService.updateProviderServiceItem(updateDTO, currentProvider.getId());
            log.info("Provider {} updated service '{}' (ID: {})", currentProvider.getId(), updatedItem.getName(),
                    updatedItem.getId());
            // TODO: DTO 转换
            // 使用 Result.success 包装成功更新后的数据
            return ResponseEntity.ok(Result.success(updatedItem, "服务更新成功"));
        } catch (IllegalArgumentException e) { // 非法参数异常
            log.warn("Failed to update service {} (Bad Request): {}", id, e.getMessage());
            // 使用 Result.failed 包装业务逻辑错误信息
            return ResponseEntity.badRequest().body(Result.failed("更新失败: " + e.getMessage()));
        } catch (IllegalStateException e) { // 状态冲突异常 (例如存在预约)
            log.warn("Failed to update service {} (Conflict): {}", id, e.getMessage());
            // 使用 Result.failed 包装业务逻辑错误信息
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.failed("更新失败: " + e.getMessage()));
        } catch (RuntimeException e) { // 其他运行时异常 (如无权限、未找到)
            log.error("Error updating service {}: {}", id, e.getMessage(), e);
            // 根据 Service 层抛出的具体异常信息判断返回状态码，并用 Result.failed 包装
            if (e.getMessage() != null && (e.getMessage().contains("无权修改") || e.getMessage().contains("not allowed"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed("更新失败: " + e.getMessage()));
            } else if (e.getMessage() != null
                    && (e.getMessage().contains("不存在") || e.getMessage().contains("not found"))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.failed("更新失败: " + e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failed("更新服务项时发生内部错误"));
            }
        }
    }

    @DeleteMapping("/{id}")
    // 返回值改为 ResponseEntity<Result<?>>
    public ResponseEntity<Result<?>> deleteMyService(@PathVariable Long id, @CurrentUser Users currentProvider) {
        // --- 权限检查 (保持不变，但使用 Result 包装错误返回) ---
        if (currentProvider == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("无效的 Token 或用户未登录"));
        }
        if (!"provider".equalsIgnoreCase(currentProvider.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed("只有服务商才能删除服务项"));
        }
        // --- 权限检查结束 ---

        log.info("Provider {} attempting to delete service with ID: {}", currentProvider.getId(), id);
        try {
            boolean deleted = serviceItemService.deleteProviderServiceItem(id, currentProvider.getId());
            if (deleted) {
                log.info("Provider {} successfully deleted service with ID: {}", currentProvider.getId(), id);
                // 使用 Result.success 包装成功信息 (无数据)
                return ResponseEntity.ok(Result.success(null, "服务项删除成功"));
            } else {
                // 理论上 Service 层应该抛异常，这里作为后备
                log.error("Service deletion failed unexpectedly for service ID: {} by provider: {}", id,
                        currentProvider.getId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failed("删除服务项失败，未知原因"));
            }
        } catch (IllegalStateException e) { // 捕获 Service 层抛出的状态冲突异常 (如无法删除已批准的服务)
            log.warn("Failed to delete service {} (Conflict): {}", id, e.getMessage());
            // 使用 Result.failed 包装业务逻辑错误信息
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.failed("删除失败: " + e.getMessage()));
        } catch (RuntimeException e) { // 捕获 Service 层抛出的其他异常 (如未找到、无权限)
            log.error("Error deleting service {}: {}", id, e.getMessage(), e);
            // 根据 Service 层抛出的具体异常信息判断返回状态码，并用 Result.failed 包装
            if (e.getMessage() != null && (e.getMessage().contains("不存在") || e.getMessage().contains("not found"))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.failed("删除失败: " + e.getMessage()));
            } else if (e.getMessage() != null
                    && (e.getMessage().contains("无权删除") || e.getMessage().contains("not allowed"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed("删除失败: " + e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failed("删除服务项时发生内部错误"));
            }
        }
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<?> setServiceAvailability(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload, // 期望 { "available": true/false }
            @CurrentUser Users currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户未登录"));
        }
        Boolean available = payload.get("available");
        if (available == null) {
            return ResponseEntity.badRequest().body(Result.failed("缺少 'available' 参数"));
        }

        try {
            ServiceItem updatedService = serviceItemService.setProviderServiceAvailability(id, currentUser.getId(),
                    available);
            String message = available ? "服务已重新上架并通过审核" : "服务已下架"; // 假设下架后需要重新审核
            return ResponseEntity.ok(Result.success(updatedService, message));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Result.failed("操作失败: " + e.getMessage()));
        }
    }
}