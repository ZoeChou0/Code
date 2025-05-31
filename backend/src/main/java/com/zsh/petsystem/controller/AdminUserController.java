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
import org.springframework.web.bind.annotation.PathVariable;
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

  @GetMapping
  public List<ProviderDTO> getUsersForAdminListView() { // 方法名可以更清晰，例如 getUsersForAdminListView
    log.info("管理员请求获取普通用户列表");
    // List<Users> users = userService.getAllUsers(); // 旧的调用
    List<Users> users = userService.getAllRegularUsers(); // <<--- 修改为调用新方法

    // DTO转换保持不变，ProviderDTO中特定于服务商的字段（如qualificationStatus）对于普通用户会是null或默认值
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

  @GetMapping("/providers/{id}")
  public ProviderDTO getProviderDetailsById(@PathVariable Long id) {
    log.info("管理员请求获取服务商详情，ID: {}", id);
    Users user = userService.getById(id);

    if (user == null) {
      // GlobalExceptionHandler 会捕获这个异常并返回合适的HTTP状态码和Result结构
      throw new RuntimeException("未找到ID为 " + id + " 的用户。");
    }
    if (!"provider".equalsIgnoreCase(user.getRole())) {
      throw new RuntimeException("用户ID " + id + " 对应的用户不是服务商角色。");
    }
    return convertToProviderDTO(user); // GlobalResponseAdvice 会将此DTO包装在Result.success中
  }
}
