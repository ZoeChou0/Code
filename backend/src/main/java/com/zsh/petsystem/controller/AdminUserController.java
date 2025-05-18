// package com.zsh.petsystem.controller;

// import java.util.List;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.HttpStatusCode;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import lombok.extern.slf4j.Slf4j;

// import com.zsh.petsystem.common.Result;
// import com.zsh.petsystem.dto.ServiceItemRejectionDTO;
// import com.zsh.petsystem.model.ServiceItem;
// import com.zsh.petsystem.model.Users;
// import com.zsh.petsystem.service.ServiceItemService;
// import com.zsh.petsystem.service.UserService;

// @RestController
// @RequestMapping("/admin/users")
// @CrossOrigin
// @Slf4j
// @PreAuthorize("hasRole('ADMIN')")
// public class AdminUserController {

//   @Autowired
//   private UserService userService;

//   @GetMapping
//   public ResponseEntity<?> getAllUsersForAdmin() {
//     log.info("管理员请求获取所有用户列表");
//     try {
//       List<Users> users = userService.getAllUsers();

//       // 清除密码字段
//       List<Users> usersWithoutPassword = users.stream().map(user -> {
//         user.setPassword(null);
//         return user;
//       }).collect(Collectors.toList());

//       return ResponseEntity.ok(Result.success(usersWithoutPassword));
//     } catch (Exception e) {
//       log.error("管理员获取用户列表时出错", e);
//       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//           .body(Result.failed("获取用户列表失败: " + e.getMessage()));
//     }
//   }

//   @GetMapping("/providers/pending-review")
//   public ResponseEntity<?> getPendingProviders() {

//     try {
//       List<Users> pendingProviders = userService.findPendingQualificationProviders();
//       return ResponseEntity.ok(pendingProviders);
//     } catch (Exception e) {
//       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//           .body("获取待审核服务商列表失败: " + e.getMessage());
//     }
//   }

//   // @PutMapping("/providers/{id}/approve")
//   // public ResponseEntity<?> approveProvider(@PathVariable Long id) {
//   // try {
//   // boolean success = serviceItemService.approveServiceItem(id);
//   // if (success) {
//   // // 使用 Result 包装响应
//   // return ResponseEntity.ok(Result.success(null, "服务项已批准"));
//   // } else {
//   // return
//   // ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.failed("操作失败，服务项可能不存在或状态不正确"));
//   // }
//   // } catch (RuntimeException e) {
//   // return
//   // ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.failed("批准失败: " +
//   // e.getMessage()));
//   // } catch (Exception e) {
//   // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//   // .body(Result.failed("处理批准请求时出错: " + e.getMessage()));
//   // }
//   // }

//   // @PutMapping("/providers/{id}/reject")
//   // public ResponseEntity<?> disable(@PathVariable Long id, @RequestBody
//   // ServiceItemRejectionDTO rejectionDTO) {
//   // // 检查驳回原因是否为空
//   // if (rejectionDTO == null || rejectionDTO.getReason() == null ||
//   // rejectionDTO.getReason().trim().isEmpty()) {
//   // return
//   // ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.failed("必须提供驳回原因"));
//   // }

//   // try {
//   // boolean success = serviceItemService.rejectServiceItem(id,
//   // rejectionDTO.getReason().trim());
//   // if (success) {
//   // // 使用 Result 包装响应
//   // return ResponseEntity.ok(Result.success(null, "服务项已拒绝"));
//   // } else {
//   // return
//   // ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.failed("操作失败，服务项可能不存在或状态不正确"));
//   // }
//   // } catch (RuntimeException e) {
//   // return
//   // ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.failed("拒绝失败: " +
//   // e.getMessage()));
//   // } catch (Exception e) {
//   // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//   // .body(Result.failed("处理拒绝请求时出错: " + e.getMessage()));
//   // }
//   // }

//   @PutMapping("/{id}/ban") // 使用 PUT 请求修改状态
//   public ResponseEntity<?> banUser(@PathVariable("id") Long id) {
//     log.info("管理员请求禁用用户 ID: {}", id);
//     try {
//       boolean success = userService.banUser(id);
//       if (success) {
//         return ResponseEntity.ok(Result.success(null, "用户已禁用"));
//       } else {
//         // Service 层返回 false 可能因为用户不存在或不能被禁用
//         return ResponseEntity.badRequest().body(Result.failed("操作失败，用户可能不存在或无法被禁用"));
//       }
//     } catch (Exception e) {
//       log.error("禁用用户 ID {} 时出错", id, e);
//       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//           .body(Result.failed("禁用用户时发生错误: " + e.getMessage()));
//     }
//   }

//   /**
//    * 解禁用户 (管理员)
//    * 
//    * @param id 要解禁的用户 ID
//    * @return 操作结果
//    */
//   @PutMapping("/{id}/unban") // 使用 PUT 请求修改状态
//   public ResponseEntity<?> unbanUser(@PathVariable("id") Long id) {
//     log.info("管理员请求解禁用户 ID: {}", id);
//     try {
//       boolean success = userService.unbanUser(id);
//       if (success) {
//         return ResponseEntity.ok(Result.success(null, "用户已解禁"));
//       } else {
//         // Service 层返回 false 可能因为用户不存在
//         return ResponseEntity.badRequest().body(Result.failed("操作失败，用户可能不存在"));
//       }
//     } catch (Exception e) {
//       log.error("解禁用户 ID {} 时出错", id, e);
//       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//           .body(Result.failed("解禁用户时发生错误: " + e.getMessage()));
//     }
//   }

//   /**
//    * 批准服务商资质 (管理员)
//    * 
//    * @param id 服务商用户 ID
//    * @return 操作结果
//    */
//   @PutMapping("/providers/{id}/approve") // 注意路径前缀
//   public ResponseEntity<?> approveProviderQualification(@PathVariable("id") Long id) {
//     log.info("管理员请求批准服务商资质 ID: {}", id);
//     try {
//       boolean success = userService.approveProviderQualification(id);
//       if (success) {
//         return ResponseEntity.ok(Result.success(null, "服务商资质已批准"));
//       } else {
//         return ResponseEntity.badRequest().body(Result.failed("操作失败，用户可能不是待审核的服务商"));
//       }
//     } catch (Exception e) {
//       log.error("批准服务商资质 ID {} 时出错", id, e);
//       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//           .body(Result.failed("批准服务商资质时发生错误: " + e.getMessage()));
//     }
//   }

//   /**
//    * 拒绝服务商资质 (管理员)
//    * 
//    * @param id 服务商用户 ID
//    * @return 操作结果
//    */
//   @PutMapping("/providers/{id}/reject") // 注意路径前缀
//   public ResponseEntity<?> rejectProviderQualification(@PathVariable("id") Long id) {
//     // 如果需要拒绝原因，这里需要 @RequestBody DTO reasonDto
//     log.info("管理员请求拒绝服务商资质 ID: {}", id);
//     try {
//       // boolean success = userService.rejectProviderQualification(id,
//       // reasonDto.getReason()); // 如果需要原因
//       boolean success = userService.rejectProviderQualification(id);
//       if (success) {
//         return ResponseEntity.ok(Result.success(null, "服务商资质已拒绝"));
//       } else {
//         return ResponseEntity.badRequest().body(Result.failed("操作失败，用户可能不是待审核的服务商"));
//       }
//     } catch (Exception e) {
//       log.error("拒绝服务商资质 ID {} 时出错", id, e);
//       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//           .body(Result.failed("拒绝服务商资质时发生错误: " + e.getMessage()));
//     }
//   }

// }

package com.zsh.petsystem.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.UserService;

@RestController
@RequestMapping("/admin/users")
@CrossOrigin
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  @Autowired
  private UserService userService;

  /**
   * 获取所有用户列表 (管理员) - 直接返回列表
   * 
   * @return 用户列表 (密码已清除)
   */
  @GetMapping
  public List<Users> getAllUsersForAdmin() {
    log.info("管理员请求获取所有用户列表");
    // 直接调用 Service 获取数据
    List<Users> users = userService.getAllUsers();

    // 清除密码字段 (这个逻辑保留)
    List<Users> usersWithoutPassword = users.stream().map(user -> {
      user.setPassword(null);
      return user;
    }).collect(Collectors.toList());

    // 直接返回数据列表，GlobalResponseAdvice 会自动包装成 Result.success(data)
    return usersWithoutPassword;
    // 如果 userService.getAllUsers() 内部或数据库操作出错，GlobalExceptionHandler 会捕获并包装成
    // Result.failed()
  }

  /**
   * 获取待审核服务商列表 - 直接返回列表
   * 
   * @return 待审核服务商列表 (密码已清除)
   */
  @GetMapping("/providers/pending-review")
  public List<Users> getPendingProviders() {
    log.info("管理员请求获取待审核服务商列表");
    List<Users> pendingProviders = userService.findPendingQualificationProviders();
    // 清除密码
    pendingProviders.forEach(p -> p.setPassword(null));
    // 直接返回数据，由 Advice 包装
    return pendingProviders;
  }

  /**
   * 禁用用户 (管理员) - 返回 void
   * 
   * @param id 要禁用的用户 ID
   */
  @PutMapping("/{id}/ban")
  public void banUser(@PathVariable("id") Long id) {
    log.info("管理员请求禁用用户 ID: {}", id);
    boolean success = userService.banUser(id);
    if (!success) {
      // 如果 Service 返回 false，表示业务逻辑失败，抛出异常让全局处理器处理
      throw new RuntimeException("禁用失败，用户可能不存在或无法被禁用");
    }
    // 成功则方法正常结束，Advice 会包装成 Result.success(null, "用户已禁用") (如果需要 message 的话，可以在
    // Advice 或 Exception Handler 中处理)
    // 或者如果想包含默认成功消息，可以在Advice的beforeBodyWrite判断返回类型是否为void
    log.info("用户 ID: {} 已禁用", id);
  }

  /**
   * 解禁用户 (管理员) - 返回 void
   * 
   * @param id 要解禁的用户 ID
   */
  @PutMapping("/{id}/unban")
  public void unbanUser(@PathVariable("id") Long id) {
    log.info("管理员请求解禁用户 ID: {}", id);
    boolean success = userService.unbanUser(id);
    if (!success) {
      // 抛出异常
      throw new RuntimeException("解禁失败，用户可能不存在");
    }
    log.info("用户 ID: {} 已解禁", id);
  }

  /**
   * 批准服务商资质 (管理员) - 返回 void
   * 
   * @param id 服务商用户 ID
   */
  @PutMapping("/providers/{id}/approve")
  public void approveProviderQualification(@PathVariable("id") Long id) {
    log.info("管理员请求批准服务商资质 ID: {}", id);
    boolean success = userService.approveProviderQualification(id);
    if (!success) {
      // 抛出异常
      throw new RuntimeException("批准资质失败，用户可能不是待审核的服务商");
    }
    log.info("服务商资质 ID: {} 已批准", id);
  }

  /**
   * 拒绝服务商资质 (管理员) - 返回 void
   * 
   * @param id 服务商用户 ID
   */
  @PutMapping("/providers/{id}/reject")
  public void rejectProviderQualification(@PathVariable("id") Long id) {
    log.info("管理员请求拒绝服务商资质 ID: {}", id);
    // 暂时不处理拒绝原因
    boolean success = userService.rejectProviderQualification(id);
    if (!success) {
      // 抛出异常
      throw new RuntimeException("拒绝资质失败，用户可能不是待审核的服务商");
    }
    log.info("服务商资质 ID: {} 已拒绝", id);
  }

}
