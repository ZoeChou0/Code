package com.zsh.petsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsh.petsystem.dto.ServiceItemAdminViewDTO;
import com.zsh.petsystem.dto.ServiceItemRejectionDTO;
import com.zsh.petsystem.entity.ServiceItem;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.ServiceItemService;
import com.zsh.petsystem.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/services") // Controller 基础路径
@CrossOrigin
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // 对整个控制器应用管理员权限
public class AdminServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private UserService userService;

    private ServiceItemAdminViewDTO convertToDto(ServiceItem item) {
        ServiceItemAdminViewDTO dto = new ServiceItemAdminViewDTO();
        BeanUtils.copyProperties(item, dto);
        if (item.getProviderId() != null) {
            Users provider = userService.getById(item.getProviderId());
            if (provider != null) {
                dto.setProviderName(provider.getName());
            } else {
                dto.setProviderName("未知服务商 (ID: " + item.getProviderId() + ")");
            }
        }
        return dto;
    }

    @GetMapping("/all")
    public List<ServiceItemAdminViewDTO> getAllServiceItems( // 直接返回 List<DTO>，让 Advice 包装
            @RequestParam(required = false) String reviewStatus) {
        log.info("管理员请求获取服务列表，状态: {}", StringUtils.hasText(reviewStatus) ? reviewStatus : "全部");
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(reviewStatus) && !"ALL".equalsIgnoreCase(reviewStatus)) {
            wrapper.eq(ServiceItem::getReviewStatus, reviewStatus.toUpperCase());
        }
        // 确保 ServiceItem 类中存在 getCreatedAt 方法
        wrapper.orderByDesc(ServiceItem::getCreatedAt); // 错误发生在这里

        List<ServiceItem> items = serviceItemService.list(wrapper);

        List<ServiceItemAdminViewDTO> dtoList = items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("找到 {} 个服务项 (筛选后)", dtoList.size());
        return dtoList; // 由 Advice 包装 Result.success()
    }

    /**
     * (管理员) 获取待审核服务项列表
     *
     * @return 待审核服务项列表 (DTO 列表)
     */
    @GetMapping("/pending")
    public List<ServiceItemAdminViewDTO> getPendingServices() { // 返回 DTO 列表
        log.info("管理员请求获取待审核服务列表 (/pending)");
        List<ServiceItem> pendingItems = serviceItemService.lambdaQuery()
                .eq(ServiceItem::getReviewStatus, "PENDING_APPROVAL") // 确保状态值正确
                .orderByDesc(ServiceItem::getCreatedAt) // 按创建时间排序
                .list();

        List<ServiceItemAdminViewDTO> dtoList = pendingItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("找到 {} 个待审核服务项", dtoList.size());
        return dtoList; // 由 Advice 包装
    }

    /**
     * (管理员) 批准服务项 - 返回 void
     *
     * @param id 服务项 ID
     */
    @PutMapping("/{id}/approve")
    public void approveService(@PathVariable Long id) { // 返回 void
        log.info("管理员请求批准服务项 ID: {}", id);
        boolean success = serviceItemService.approveServiceItem(id);
        if (!success) {
            // 抛出异常，让全局处理器处理或特定异常处理器处理
            throw new RuntimeException("批准失败，服务项ID: " + id + " 可能不存在或状态不正确");
        }
        log.info("服务项 ID: {} 已批准", id);
        // 成功则正常结束，由 Advice 包装 Result.success(null)
    }

    /**
     * (管理员) 拒绝服务项 - 返回 void
     *
     * @param id           服务项 ID
     * @param rejectionDTO 包含拒绝原因的 DTO
     */
    @PutMapping("/{id}/reject")
    public void rejectService(@PathVariable Long id, @RequestBody ServiceItemRejectionDTO rejectionDTO) { // 返回 void
        log.info("管理员请求拒绝服务项 ID: {}，原因: {}", id, rejectionDTO.getReason());
        if (rejectionDTO == null || !StringUtils.hasText(rejectionDTO.getReason())) {
            throw new IllegalArgumentException("必须提供驳回原因");
        }

        boolean success = serviceItemService.rejectServiceItem(id, rejectionDTO.getReason().trim());
        if (!success) {
            throw new RuntimeException("拒绝失败，服务项ID: " + id + " 可能不存在或状态不正确");
        }
        log.info("服务项 ID: {} 已拒绝", id);
    }
}