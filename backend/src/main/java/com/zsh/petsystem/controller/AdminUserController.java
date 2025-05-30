// package com.zsh.petsystem.controller;

// import java.util.List;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import lombok.extern.slf4j.Slf4j;

// import com.zsh.petsystem.common.Result;
// import com.zsh.petsystem.entity.Users;
// import com.zsh.petsystem.service.UserService;

// @RestController
// @RequestMapping("/admin/users")
// @CrossOrigin
// @Slf4j
// @PreAuthorize("hasRole('ADMIN')")
// public class AdminUserController {

//   @Autowired
//   private UserService userService;

//   /**
//    * 获取所有用户列表 (管理员) - 直接返回列表
//    * 
//    * @return 用户列表 (密码已清除)
//    */
//   @GetMapping
//   public List<Users> getAllUsersForAdmin() {
//     log.info("管理员请求获取所有用户列表");
//     // 直接调用 Service 获取数据
//     List<Users> users = userService.getAllUsers();

//     // 清除密码字段 (这个逻辑保留)
//     List<Users> usersWithoutPassword = users.stream().map(user -> {
//       user.setPassword(null);
//       return user;
//     }).collect(Collectors.toList());

//     // 直接返回数据列表，GlobalResponseAdvice 会自动包装成 Result.success(data)
//     return usersWithoutPassword;
//     // 如果 userService.getAllUsers() 内部或数据库操作出错，GlobalExceptionHandler 会捕获并包装成
//     // Result.failed()
//   }

//   /**
//    * 获取待审核服务商列表 - 直接返回列表
//    * 
//    * @return 待审核服务商列表 (密码已清除)
//    */
//   @GetMapping("/providers/pending-review")
//   public List<Users> getPendingProviders() {
//     log.info("管理员请求获取待审核服务商列表");
//     List<Users> pendingProviders = userService.findPendingQualificationProviders();
//     // 清除密码
//     pendingProviders.forEach(p -> p.setPassword(null));
//     // 直接返回数据，由 Advice 包装
//     return pendingProviders;
//   }

//   /**
//    * 禁用用户 (管理员) - 返回 void
//    * 
//    * @param id 要禁用的用户 ID
//    */
//   @PutMapping("/{id}/ban")
//   public void banUser(@PathVariable("id") Long id) {
//     log.info("管理员请求禁用用户 ID: {}", id);
//     boolean success = userService.banUser(id);
//     if (!success) {
//       // 如果 Service 返回 false，表示业务逻辑失败，抛出异常让全局处理器处理
//       throw new RuntimeException("禁用失败，用户可能不存在或无法被禁用");
//     }
//     // 成功则方法正常结束，Advice 会包装成 Result.success(null, "用户已禁用") (如果需要 message 的话，可以在
//     // Advice 或 Exception Handler 中处理)
//     // 或者如果想包含默认成功消息，可以在Advice的beforeBodyWrite判断返回类型是否为void
//     log.info("用户 ID: {} 已禁用", id);
//   }

//   /**
//    * 解禁用户 (管理员) - 返回 void
//    * 
//    * @param id 要解禁的用户 ID
//    */
//   @PutMapping("/{id}/unban")
//   public void unbanUser(@PathVariable("id") Long id) {
//     log.info("管理员请求解禁用户 ID: {}", id);
//     boolean success = userService.unbanUser(id);
//     if (!success) {
//       // 抛出异常
//       throw new RuntimeException("解禁失败，用户可能不存在");
//     }
//     log.info("用户 ID: {} 已解禁", id);
//   }

//   /**
//    * 批准服务商资质 (管理员) - 返回 void
//    * 
//    * @param id 服务商用户 ID
//    */
//   @PutMapping("/providers/{id}/approve")
//   public void approveProviderQualification(@PathVariable("id") Long id) {
//     log.info("管理员请求批准服务商资质 ID: {}", id);
//     boolean success = userService.approveProviderQualification(id);
//     if (!success) {
//       // 抛出异常
//       throw new RuntimeException("批准资质失败，用户可能不是待审核的服务商");
//     }
//     log.info("服务商资质 ID: {} 已批准", id);
//   }

//   /**
//    * 拒绝服务商资质 (管理员) - 返回 void
//    * 
//    * @param id 服务商用户 ID
//    */
//   @PutMapping("/providers/{id}/reject")
//   public void rejectProviderQualification(@PathVariable("id") Long id) {
//     log.info("管理员请求拒绝服务商资质 ID: {}", id);
//     // 暂时不处理拒绝原因
//     boolean success = userService.rejectProviderQualification(id);
//     if (!success) {
//       // 抛出异常
//       throw new RuntimeException("拒绝资质失败，用户可能不是待审核的服务商");
//     }
//     log.info("服务商资质 ID: {} 已拒绝", id);
//   }

//   // 获取服务商列表 (可按状态筛选) ---
//   @GetMapping("/providers")
//   public ResponseEntity<?> getProvidersByStatus(
//       @RequestParam(required = false) String qualificationStatus) {
//     try {
//       List<Users> providers;
//       if (qualificationStatus != null && !qualificationStatus.isEmpty()) {
//         // 根据传入的状态筛选，确保角色是 provider
//         providers = userService.lambdaQuery()
//             .eq(Users::getRole, "provider")
//             .eq(Users::getQualificationStatus, qualificationStatus.toUpperCase())
//             .list();
//       } else {
//         // 如果没有传入状态，获取所有角色为 provider 的用户
//         providers = userService.lambdaQuery().eq(Users::getRole, "provider").list();
//       }
//       // 移除密码等敏感信息
//       if (providers != null) {
//         providers.forEach(user -> user.setPassword(null));
//       }
//       return ResponseEntity.ok(Result.success(providers));
//     } catch (Exception e) {
//       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//           .body(Result.failed("获取服务商列表失败: " + e.getMessage()));
//     }
//   }

// }

package com.zsh.petsystem.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils; // 引入 BeanUtils
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody; // 用于接收拒绝原因
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsh.petsystem.dto.ProviderDTO; // 引入 ProviderDTO
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.UserService;
import java.util.Map; // 用于接收拒绝原因

@RestController
@RequestMapping("/admin/users")
@CrossOrigin
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  @Autowired
  private UserService userService;

  // 辅助方法：将 Users 实体转换为 ProviderDTO
  private ProviderDTO convertToProviderDTO(Users user) {
    if (user == null)
      return null;
    ProviderDTO dto = new ProviderDTO();
    BeanUtils.copyProperties(user, dto); // 复制属性
    // ProviderDTO 通常不包含密码字段，所以不需要 user.setPassword(null)
    return dto;
  }

  @GetMapping // 获取所有用户 (包括非服务商)
  public List<ProviderDTO> getAllUsersForAdmin() {
    log.info("管理员请求获取所有用户列表");
    List<Users> users = userService.getAllUsers();
    return users.stream()
        .map(this::convertToProviderDTO)
        .collect(Collectors.toList());
  }

  // 获取服务商列表 (可按状态筛选)
  @GetMapping("/providers")
  public List<ProviderDTO> getServiceProviders(
      @RequestParam(required = false) String qualificationStatus) {
    log.info("管理员请求获取服务商列表，筛选状态: {}", qualificationStatus == null ? "ALL" : qualificationStatus);
    LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Users::getRole, "provider"); // 确保只查询服务商
    if (qualificationStatus != null && !qualificationStatus.isEmpty() && !"ALL".equalsIgnoreCase(qualificationStatus)) {
      queryWrapper.eq(Users::getQualificationStatus, qualificationStatus.toUpperCase());
    }
    List<Users> providers = userService.list(queryWrapper);
    return providers.stream()
        .map(this::convertToProviderDTO)
        .collect(Collectors.toList());
  }

  // 注意：为了与 admin.ts 中的 getPendingProviders 等明确路径的函数对应，
  // 下面的特定路径接口可以保留，或者让前端统一调用 /providers?qualificationStatus=XXX

  // 路径与 admin.ts 中的 getPendingProviders() 匹配
  @GetMapping("/providers/pending")
  public List<ProviderDTO> getPendingProvidersList() {
    log.info("管理员请求获取待审核服务商列表 (/providers/pending)");
    List<Users> pendingProviders = userService.findPendingQualificationProviders();
    return pendingProviders.stream().map(this::convertToProviderDTO).collect(Collectors.toList());
  }

  // 路径与 admin.ts 中的 getApprovedProviders() 匹配
  @GetMapping("/providers/approved")
  public List<ProviderDTO> getApprovedProvidersList() {
    log.info("管理员请求获取已批准服务商列表 (/providers/approved)");
    List<Users> approvedProviders = userService.lambdaQuery()
        .eq(Users::getRole, "provider")
        .eq(Users::getQualificationStatus, "APPROVED")
        .list();
    return approvedProviders.stream().map(this::convertToProviderDTO).collect(Collectors.toList());
  }

  // 路径与 admin.ts 中的 getRejectedProviders() 匹配
  @GetMapping("/providers/rejected")
  public List<ProviderDTO> getRejectedProvidersList() {
    log.info("管理员请求获取已拒绝服务商列表 (/providers/rejected)");
    List<Users> rejectedProviders = userService.lambdaQuery()
        .eq(Users::getRole, "provider")
        .eq(Users::getQualificationStatus, "REJECTED")
        .list();
    return rejectedProviders.stream().map(this::convertToProviderDTO).collect(Collectors.toList());
  }

  @PutMapping("/{id}/ban")
  public void banUser(@PathVariable("id") Long id) {
    log.info("管理员请求禁用用户 ID: {}", id);
    boolean success = userService.banUser(id);
    if (!success) {
      throw new RuntimeException("禁用失败，用户可能不存在或无法被禁用");
    }
    log.info("用户 ID: {} 已禁用", id);
  }

  @PutMapping("/{id}/unban")
  public void unbanUser(@PathVariable("id") Long id) {
    log.info("管理员请求解禁用户 ID: {}", id);
    boolean success = userService.unbanUser(id);
    if (!success) {
      throw new RuntimeException("解禁失败，用户可能不存在");
    }
    log.info("用户 ID: {} 已解禁", id);
  }

  @PutMapping("/providers/{id}/approve")
  public void approveProviderQualification(@PathVariable("id") Long id) {
    log.info("管理员请求批准服务商资质 ID: {}", id);
    boolean success = userService.approveProviderQualification(id);
    if (!success) {
      throw new RuntimeException("批准资质失败，用户可能不是待审核的服务商或操作失败");
    }
    log.info("服务商资质 ID: {} 已批准", id);
  }

  // 修改以接收请求体中的 reason
  @PutMapping("/providers/{id}/reject")
  public void rejectProviderQualification(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
    String reason = payload.get("reason");
    log.info("管理员请求拒绝服务商资质 ID: {}，原因: {}", id, reason);
    if (reason == null || reason.trim().isEmpty()) {
      throw new IllegalArgumentException("拒绝原因不能为空");
    }
    // 您需要在 UserService 和 UserServiceImpl 中修改 rejectProviderQualification 方法以接收并处理
    // reason
    // 假设 UserServiceImpl.rejectProviderQualification 已修改为:
    // boolean rejectProviderQualification(Long providerId, String reason);
    // boolean success = userService.rejectProviderQualification(id, reason);

    // 当前 userService.rejectProviderQualification(id) 不接收 reason，我们先保持原样，但您应该去修改它
    boolean success = userService.rejectProviderQualification(id); // 您应扩展此方法以记录 reason
    if (!success) {
      throw new RuntimeException("拒绝资质失败，用户可能不是待审核的服务商或操作失败");
    }
    log.info("服务商资质 ID: {} 已拒绝，原因: {}", id, reason);
  }
}
