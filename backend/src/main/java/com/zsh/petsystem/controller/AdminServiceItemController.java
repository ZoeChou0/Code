// package com.zsh.petsystem.controller;

// import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// import com.zsh.petsystem.dto.ServiceItemRejectionDTO;
// import com.zsh.petsystem.model.Reservation;
// import com.zsh.petsystem.model.ServiceItem;

// import com.zsh.petsystem.service.ServiceItemService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize;

// import java.util.List;

// @RestController
// @RequestMapping("/admin/services")
// public class AdminServiceItemController {

//     @Autowired
//     private ServiceItemService serviceItemService;

//     // 获取所有服务项（可加分页）
//     @GetMapping("/all")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<List<ServiceItem>> getAll() {
//         return ResponseEntity.ok(serviceItemService.list());
//     }

//     // 获取待审核服务项
//     @GetMapping("/pending")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<?> getPending() {
//         // ServiceImpl 提供了 list 方法，直接用 LambdaQueryWrapper 查询
//         try {
//             List<ServiceItem> pendingItems = serviceItemService.lambdaQuery()
//                     .eq(ServiceItem::getReviewStatus, "PENDING") // 查询状态为 PENDING
//                     .list();
//             // TODO: DTO 转换，过滤敏感信息
//             return ResponseEntity.ok(pendingItems);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取待审核列表失败: " + e.getMessage());
//         }
//     }

//     // 启用服务项
//     @PutMapping("/{id}/approve")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<?> approve(@PathVariable Long id) {
//         try {
//             boolean success = serviceItemService.approveServiceItem(id);
//             if (success) {
//                 return ResponseEntity.ok("服务项已批准");
//             } else {
//                 // 可能因为状态不对或者 ID 不存在导致失败
//                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("操作失败，服务项可能不存在或状态不正确");
//             }
//         } catch (RuntimeException e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("批准失败: " + e.getMessage());
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理批准请求时出错: " + e.getMessage());
//         }
//     }

//     // 禁用服务项
//     @PutMapping("/{id}/reject")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<?> disable(@PathVariable Long id, @RequestBody ServiceItemRejectionDTO rejectionDTO) {
//         // 检查驳回原因是否为空
//         if (rejectionDTO == null || rejectionDTO.getReason() == null || rejectionDTO.getReason().trim().isEmpty()) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("必须提供驳回原因");
//         }

//         try {
//             boolean success = serviceItemService.rejectServiceItem(id, rejectionDTO.getReason().trim());
//             if (success) {
//                 return ResponseEntity.ok("服务项已拒绝");
//             } else {
//                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("操作失败，服务项可能不存在或状态不正确");
//             }
//         } catch (RuntimeException e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("拒绝失败: " + e.getMessage());
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理拒绝请求时出错: " + e.getMessage());
//         }

//     }
// }

package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result; // 不再需要直接使用
import com.zsh.petsystem.dto.ServiceItemRejectionDTO;
import com.zsh.petsystem.entity.ServiceItem;
// import com.zsh.petsystem.model.Users; // 此 Controller 不直接操作 Users
import com.zsh.petsystem.service.ServiceItemService;
// import com.zsh.petsystem.service.UserService; // 此 Controller 不直接操作 UserService
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus; // 不再需要
// import org.springframework.http.ResponseEntity; // 不再需要
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.stream.Collectors; // 如果使用 DTO 会需要

@RestController
@RequestMapping("/admin/services") // Controller 基础路径
@CrossOrigin
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // 对整个控制器应用管理员权限
public class AdminServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    /**
     * (管理员) 获取所有服务项列表
     * 
     * @return 服务项列表 (强烈建议使用 DTO 过滤敏感信息)
     */
    @GetMapping("/all")
    public List<ServiceItem> getAllServices() { // 返回 List<ServiceItem> 或 List<ServiceItemDTO>
        log.info("管理员请求获取所有服务列表 (/all)");
        List<ServiceItem> allItems = serviceItemService.list();

        // ---------------------------------------------------------------
        // TODO: [重要] DTO 数据转换!
        // 返回给前端前，应转换为 DTO，移除 providerId, rejectionReason 等字段
        // List<ServiceItemAdminDTO> dtos =
        // allItems.stream().map(this::convertToDto).collect(Collectors.toList());
        // return dtos;
        // ---------------------------------------------------------------

        log.info("找到 {} 个服务项 (所有状态)", allItems.size());
        // 直接返回列表，由 GlobalResponseAdvice 自动包装
        return allItems;
        // 如果 serviceItemService.list() 抛出异常，由 GlobalExceptionHandler 处理
    }

    /**
     * (管理员) 获取待审核服务项列表
     * 
     * @return 待审核服务项列表 (建议使用 DTO)
     */
    @GetMapping("/pending")
    public List<ServiceItem> getPendingServices() { // 返回 List<ServiceItem> 或 List<ServiceItemDTO>
        log.info("管理员请求获取待审核服务列表 (/pending)");
        List<ServiceItem> pendingItems = serviceItemService.lambdaQuery()
                .eq(ServiceItem::getReviewStatus, "PENDING")
                .list();

        // TODO: DTO 数据转换 (同上)

        log.info("找到 {} 个待审核服务项", pendingItems.size());
        return pendingItems; // 由 Advice 包装
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
            // 抛出异常，让全局处理器处理
            throw new RuntimeException("批准失败，服务项可能不存在或状态不正确");
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
        // 检查原因
        if (rejectionDTO == null || rejectionDTO.getReason() == null || rejectionDTO.getReason().trim().isEmpty()) {
            // 抛出业务异常，可以被全局处理器捕获并返回 400 Bad Request (如果配置了特定异常处理)
            throw new IllegalArgumentException("必须提供驳回原因");
        }

        boolean success = serviceItemService.rejectServiceItem(id, rejectionDTO.getReason().trim());
        if (!success) {
            // 抛出异常
            throw new RuntimeException("拒绝失败，服务项可能不存在或状态不正确");
        }
        log.info("服务项 ID: {} 已拒绝", id);
    }
}