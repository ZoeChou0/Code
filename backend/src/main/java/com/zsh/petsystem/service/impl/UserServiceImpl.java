package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.UserUpdateProfileDTO;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.mapper.UserMapper;
import com.zsh.petsystem.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl
    extends ServiceImpl<UserMapper, Users>
    implements UserService {

  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;

  }

  @Override
  public Users saveUser(Users user) {
    boolean saved = this.save(user);
    if (saved && user.getId() != null) {

      log.info("New user ID: {} saved.", user.getId());
    }
    return user;
  }

  @Override
  public List<Users> getAllUsers() {
    return this.list();
  }

  @Override
  public boolean existByEmail(String email) {
    return this.count(
        new LambdaQueryWrapper<Users>()
            .eq(Users::getEmail, email)) > 0;
  }

  @Override
  public boolean existByPhone(String phone) {
    return this.count(
        new LambdaQueryWrapper<Users>()
            .eq(Users::getPhone, phone)) > 0;
  }

  @Override
  public Users findByEmail(String email) {
    return this.getOne(
        new LambdaQueryWrapper<Users>()
            .eq(Users::getEmail, email));
  }

  @Override
  public Users getById(Long id) {
    if (id == null) {
      return null;
    }
    // 移除了所有 Redis 缓存逻辑
    log.info("Fetching user ID: {} from DB.", id);
    Users userFromDb = super.getById(id); // 直接调用 MybatisPlus 的 getById
    return userFromDb;
  }

  @Override
  public List<Users> findPendingQualificationProviders() {
    return this.list(
        new LambdaQueryWrapper<Users>()
            .eq(Users::getRole, "provider")
            .eq(Users::getQualificationStatus, "PENDING_REVIEW"));
  }

  @Override
  public Users updateUserProfile(Long userId, UserUpdateProfileDTO dto) {
    Users existing = this.getById(userId); // 此处 getById 已不走缓存
    if (existing == null) {
      throw new RuntimeException("用户不存在");
    }

    boolean changed = false;
    if (dto.getName() != null && !dto.getName().equals(existing.getName())) {
      existing.setName(dto.getName());
      changed = true;
    }
    if (dto.getPhone() != null && !dto.getPhone().equals(existing.getPhone())) {
      existing.setPhone(dto.getPhone());
      changed = true;
    }
    if (dto.getAddressLine1() != null
        && !Objects.equals(dto.getAddressLine1(), existing.getAddressLine1())) {
      existing.setAddressLine1(dto.getAddressLine1());
      changed = true;
    }
    if (dto.getCity() != null
        && !Objects.equals(dto.getCity(), existing.getCity())) {
      existing.setCity(dto.getCity());
      changed = true;
    }
    if (dto.getState() != null
        && !Objects.equals(dto.getState(), existing.getState())) {
      existing.setState(dto.getState());
      changed = true;
    }
    if (dto.getZipCode() != null
        && !Objects.equals(dto.getZipCode(), existing.getZipCode())) {
      existing.setZipCode(dto.getZipCode());
      changed = true;
    }
    if (dto.getBirthday() != null
        && !Objects.equals(dto.getBirthday(), existing.getBirthday())) {
      existing.setBirthday(dto.getBirthday());
      changed = true;
    }
    if (dto.getProfilePhotoUrl() != null
        && !Objects.equals(dto.getProfilePhotoUrl(), existing.getProfilePhotoUrl())) {
      existing.setProfilePhotoUrl(dto.getProfilePhotoUrl());
      changed = true;
    }

    if (changed) {
      boolean updated = this.updateById(existing); // 更新数据库
      if (updated) {
        // 移除了删除缓存的逻辑
        log.info("User ID: {} updated in DB.", userId);
      }
    }
    // 返回时去掉密码字段
    existing.setPassword(null);
    return existing;
  }

  @Override
  @Transactional
  public void changePassword(Long userId, String currentPassword, String newPassword) {
    if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword)) {
      throw new IllegalArgumentException("当前密码和新密码都不能为空");
    }

    Users user = this.getById(userId); // 此处 getById 已不走缓存
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new RuntimeException("当前密码不正确");
    }

    if (passwordEncoder.matches(newPassword, user.getPassword())) {
      // 新旧密码相同，无需更新
      return;
    }

    String hashed = passwordEncoder.encode(newPassword);
    user.setPassword(hashed);
    if (!this.updateById(user)) {
      throw new RuntimeException("更新密码时数据库操作失败");
    }
  }

  @Override
  public Users findUserByIdentifier(String identifier) {
    if (identifier == null || identifier.trim().isEmpty()) {
      return null;
    }
    // Use LambdaQueryWrapper with OR conditions
    LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper
        .eq(Users::getName, identifier) // Check username
        .or() // OR
        .eq(Users::getEmail, identifier) // Check email
        .or() // OR
        .eq(Users::getPhone, identifier); // Check phone
    return this.getOne(queryWrapper);
  }

  @Override
  @Transactional // 添加事务
  public boolean banUser(Long userId) {
    Users user = this.getById(userId); // 此处 getById 已不走缓存
    if (user == null) {
      log.warn("尝试禁用不存在的用户: ID {}", userId);
      return false;
    }
    if ("admin".equalsIgnoreCase(user.getRole())) {
      log.warn("尝试禁用管理员用户: ID {}, Email {}", userId, user.getEmail());
      return false;
    }
    if ("banned".equalsIgnoreCase(user.getStatus())) {
      log.info("用户 ID {} 已经是禁用状态", userId);
      return true;
    }

    user.setStatus("banned");
    log.info("正在禁用用户: ID {}, Email {}", userId, user.getEmail());
    return this.updateById(user);
  }

  @Override
  @Transactional // 添加事务
  public boolean unbanUser(Long userId) {
    Users user = this.getById(userId); // 此处 getById 已不走缓存
    if (user == null) {
      log.warn("尝试解禁不存在的用户: ID {}", userId);
      return false;
    }
    if ("active".equalsIgnoreCase(user.getStatus()) || user.getStatus() == null) {
      log.info("用户 ID {} 已经是激活状态或状态为空", userId);
      return true;
    }

    user.setStatus("active");
    log.info("正在解禁用户: ID {}, Email {}", userId, user.getEmail());
    return this.updateById(user);
  }

  @Override
  @Transactional
  public boolean approveProviderQualification(Long providerId) {
    Users provider = this.getById(providerId); // 此处 getById 已不走缓存
    if (provider == null || !"provider".equalsIgnoreCase(provider.getRole())) {
      log.warn("尝试批准非服务商或不存在的用户资质: ID {}", providerId);
      return false;
    }
    if (!"PENDING_REVIEW".equalsIgnoreCase(provider.getQualificationStatus())) {
      log.warn("尝试批准非待审核状态的服务商: ID {}, 当前状态 {}", providerId, provider.getQualificationStatus());
      return false;
    }

    provider.setQualificationStatus("APPROVED");
    log.info("正在批准服务商资质: ID {}, Email {}", providerId, provider.getEmail());
    return this.updateById(provider);
  }

  @Override
  @Transactional
  public boolean rejectProviderQualification(Long providerId) {
    Users provider = this.getById(providerId); // 此处 getById 已不走缓存
    if (provider == null || !"provider".equalsIgnoreCase(provider.getRole())) {
      log.warn("尝试拒绝非服务商或不存在的用户资质: ID {}", providerId);
      return false;
    }
    if (!"PENDING_REVIEW".equalsIgnoreCase(provider.getQualificationStatus())) {
      log.warn("尝试拒绝非待审核状态的服务商: ID {}, 当前状态 {}", providerId, provider.getQualificationStatus());
      return false;
    }

    provider.setQualificationStatus("REJECTED");
    log.info("正在拒绝服务商资质: ID {}, Email {}", providerId, provider.getEmail());
    return this.updateById(provider);
  }

  @Override
  public List<Users> getAllRegularUsers() {
    log.info("Fetching all regular users (role='user')");
    return this.lambdaQuery().eq(Users::getRole, "user").list(); // <<--- 实现只查询 role 为 'user' 的用户
  }
}