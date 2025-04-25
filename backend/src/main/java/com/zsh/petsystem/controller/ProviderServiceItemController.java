package com.zsh.petsystem.controller;

import com.zsh.petsystem.annotation.CurrentUser;
import com.zsh.petsystem.dto.ServiceItemUpdateDTO;

import com.zsh.petsystem.model.ServiceItem;
import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.service.ServiceItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/provider/services")
public class ProviderServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    @PostMapping("/add")
    public ResponseEntity<?> addService(@RequestBody ServiceItem item,
            @CurrentUser Users currentProvider) {
        if (currentProvider == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
        }
        // 检查是否为服务商
        if (!"provider".equalsIgnoreCase(currentProvider.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只有服务商才能添加服务项");
        }
        item.setProviderId(currentProvider.getId());

        item.setReviewStatus("PENDING");
        item.setRejectionReason(null);
        try {
            ServiceItem addedItem = serviceItemService.add(item);
            return ResponseEntity.ok(addedItem);
        } catch (Exception e) {
            // 处理添加过程中可能出现的异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("添加服务失败: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> myServices(@CurrentUser Users currentProvider) {
        if (currentProvider == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
        }

        if (!"provider".equalsIgnoreCase(currentProvider.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只有服务商才能查看自己的服务项");
        }

        // 3. 查询服务列表
        try {
            List<ServiceItem> items = serviceItemService.lambdaQuery()
                    // 直接使用注入的 provider 对象的 ID
                    .eq(ServiceItem::getProviderId, currentProvider.getId())
                    .list();
            // TODO: DTO 转换，过滤不需要返回给服务商的字段 (例如审核细节)
            return ResponseEntity.ok(items);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询服务列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}") // 使用 PUT 请求方法
    public ResponseEntity<?> updateMyService(@PathVariable Long id,
            @RequestBody ServiceItemUpdateDTO updateDTO,
            @CurrentUser Users currentProvider) {
        // 1. 检查用户是否登录
        if (currentProvider == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
        }

        if (!"provider".equalsIgnoreCase(currentProvider.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只有服务商才能修改服务项");
        }

        // 3. 校验路径 ID 和 DTO 中的 ID 是否一致 (如果 DTO 中有 ID)
        if (updateDTO.getId() == null) {
            updateDTO.setId(id); // 如果 DTO 没传 ID，从路径获取
        } else if (!Objects.equals(id, updateDTO.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("路径 ID 与请求体中的 ID 不匹配");
        }

        // 4. 调用 Service 层执行更新 (包含权限和冲突检查)
        try {
            ServiceItem updatedItem = serviceItemService.updateProviderServiceItem(updateDTO, currentProvider.getId());
            // TODO: DTO 转换，过滤信息
            return ResponseEntity.ok(updatedItem); // 返回更新后的完整对象
        } catch (IllegalArgumentException e) { // 非法参数异常
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("更新失败: " + e.getMessage());
        } catch (IllegalStateException e) { // 状态冲突异常 (例如存在预约)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("更新失败: " + e.getMessage()); // 409 Conflict
        } catch (RuntimeException e) { // 其他运行时异常 (如无权限、未找到)
            if (e.getMessage().contains("无权修改")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("更新失败: " + e.getMessage()); // 403 Forbidden
            } else if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("更新失败: " + e.getMessage()); // 404 Not Found
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新服务项时发生错误: " + e.getMessage()); // 500
            }
        }
    }
}