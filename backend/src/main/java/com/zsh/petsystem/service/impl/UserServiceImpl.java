// package com.zsh.petsystem.service.impl;

// import java.util.List;
// import java.util.Objects;
// import java.time.LocalDate;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.StringUtils;

// import com.zsh.petsystem.dto.UserUpdateProfileDTO;

// import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// import com.zsh.petsystem.mapper.UserMapper;
// import com.zsh.petsystem.model.Users;
// import com.zsh.petsystem.service.UserService;
// import com.zsh.petsystem.dto.UserUpdateProfileDTO;
// import org.springframework.security.crypto.password.PasswordEncoder;

// @Service
// public class UserServiceImpl
//     extends ServiceImpl<UserMapper, Users>
//     implements UserService {

//   private final PasswordEncoder passwordEncoder;

//   public UserServiceImpl(PasswordEncoder passwordEncoder) {
//     this.passwordEncoder = passwordEncoder;
//   }

//   @Override
//   public Users saveUser(Users user) {
//     this.save(user);
//     return user;
//   }

//   @Override
//   public List<Users> getAllUsers() {
//     return this.list();
//   }

//   @Override
//   public boolean existByEmail(String email) {
//     // 使用 LambdaQueryWrapper 进行类型安全的查询
//     LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
//     wrapper.eq(Users::getEmail, email);
//     // 使用 ServiceImpl 提供的 count 方法
//     return this.count(wrapper) > 0;
//   }

//   @Override
//   public boolean existByPhone(String phone) {
//     // 使用 LambdaQueryWrapper 进行类型安全的查询
//     LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
//     wrapper.eq(Users::getPhone, phone);
//     // 使用 ServiceImpl 提供的 count 方法
//     return this.count(wrapper) > 0;
//   }

//   @Override
//   public Users findByEmail(String email) {
//     // 使用 LambdaQueryWrapper 进行类型安全的查询
//     LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
//     wrapper.eq(Users::getEmail, email);
//     return this.getOne(wrapper);
//   }

//   @Override
//   public Users getById(Long id) {
//     return super.getById(id); // 调用 ServiceImpl 中的方法
//   }

//   @Override
//   public List<Users> findPendingQualificationProviders() {
//     LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
//     wrapper.eq(Users::getRole, "provider");
//     wrapper.eq(Users::getQualificationStatus, "PENDING_REVIEW");
//     return this.list(wrapper);
//   }

//   @Override
//   public Users updateUserProfile(Long userId, UserUpdateProfileDTO dto) {
//     Users existingUser = this.getById(userId);
//     if (existingUser == null) {
//       throw new RuntimeException("用户不存在"); // 或者返回 null 或其他错误处理
//     }

//     boolean changed = false;
//     if (dto.getName() != null && !dto.getName().equals(existingUser.getName())) {
//       existingUser.setName(dto.getName());
//       changed = true;
//     }
//     if (dto.getPhone() != null && !dto.getPhone().equals(existingUser.getPhone())) {
//       // TODO: 可以添加手机号格式验证，或检查手机号是否已被他人注册
//       existingUser.setPhone(dto.getPhone());
//       changed = true;
//     }

//     // 地址行1
//     if (dto.getAddressLine1() != null && !Objects.equals(dto.getAddressLine1(), existingUser.getAddressLine1())) {
//       existingUser.setAddressLine1(dto.getAddressLine1());
//       changed = true;
//     }
//     // 城市
//     if (dto.getCity() != null && !Objects.equals(dto.getCity(), existingUser.getCity())) {
//       existingUser.setCity(dto.getCity());
//       changed = true;
//     }
//     // 省/州
//     if (dto.getState() != null && !Objects.equals(dto.getState(), existingUser.getState())) {
//       existingUser.setState(dto.getState());
//       changed = true;
//     }
//     // 邮编
//     if (dto.getZipCode() != null && !Objects.equals(dto.getZipCode(), existingUser.getZipCode())) {
//       existingUser.setZipCode(dto.getZipCode());
//       changed = true;
//     }
//     // 生日
//     if (dto.getBirthday() != null && !Objects.equals(dto.getBirthday(), existingUser.getBirthday())) {
//       existingUser.setBirthday(dto.getBirthday());
//       changed = true;
//     }
//     // 头像URL
//     if (dto.getProfilePhotoUrl() != null
//         && !Objects.equals(dto.getProfilePhotoUrl(), existingUser.getProfilePhotoUrl())) {
//       existingUser.setProfilePhotoUrl(dto.getProfilePhotoUrl());
//       changed = true;
//     }

//     if (changed) {
//       this.updateById(existingUser);
//     }
//     // 返回更新后的用户信息（同样清除敏感信息）
//     existingUser.setPassword(null);
//     return existingUser;
//   }

//   @Override
//   @Transactional // 推荐添加事务注解
//   public void changePassword(Long userId, String currentPassword, String newPassword) {
//     // 1. 校验输入 (基本非空检查)
//     if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword)) {
//       throw new IllegalArgumentException("当前密码和新密码都不能为空");
//     }

//     // 2. 获取用户信息
//     Users user = this.getById(userId);
//     if (user == null) {
//       // 理论上，能调用此接口的用户应该是存在的，但以防万一
//       throw new RuntimeException("用户不存在");
//     }

//     // 3. 验证当前密码是否正确
//     if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
//       throw new RuntimeException("当前密码不正确");
//     }

//     // 4. (可选) 可以在这里添加新密码复杂度校验

//     // 5. 如果新旧密码相同，可以提示用户无需修改或直接返回成功
//     if (passwordEncoder.matches(newPassword, user.getPassword())) {
//       // 可以选择抛出异常提示用户新旧密码不能一样，或者静默成功
//       // throw new RuntimeException("新密码不能与当前密码相同");
//       return; // 或者直接返回，表示操作完成（虽然没修改）
//     }

//     // 6. 对新密码进行哈希加密
//     String hashedNewPassword = passwordEncoder.encode(newPassword);

//     // 7. 更新用户密码
//     user.setPassword(hashedNewPassword);
//     boolean updated = this.updateById(user);

//     // 8. 检查更新是否成功
//     if (!updated) {
//       // 记录日志或抛出更具体的异常
//       throw new RuntimeException("更新密码时数据库操作失败");
//     }
//   }
// }

// src/main/java/com/zsh/petsystem/service/impl/UserServiceImpl.java
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
    this.save(user);
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
    return super.getById(id);
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
    Users existing = this.getById(userId);
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
      this.updateById(existing);
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

    Users user = this.getById(userId);
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
    // Use getOne - assumes identifiers (name, email, phone) are unique across users
    // If duplicates are possible, use list() and handle accordingly
    return this.getOne(queryWrapper);
  }

  @Override
  @Transactional // 添加事务
  public boolean banUser(Long userId) {
    Users user = this.getById(userId);
    if (user == null) {
      // 或者抛出异常 throw new RuntimeException("用户不存在");
      log.warn("尝试禁用不存在的用户: ID {}", userId);
      return false;
    }
    // 防止禁用管理员自己或其他管理员 (可选逻辑)
    if ("admin".equalsIgnoreCase(user.getRole())) {
      log.warn("尝试禁用管理员用户: ID {}, Email {}", userId, user.getEmail());
      // 或者抛出异常 throw new IllegalArgumentException("不能禁用管理员账户");
      return false;
    }
    // 检查是否已经是禁用状态
    if ("banned".equalsIgnoreCase(user.getStatus())) {
      log.info("用户 ID {} 已经是禁用状态", userId);
      return true; // 可以认为操作是成功的（幂等性）
    }

    user.setStatus("banned"); // 设置状态为禁用
    log.info("正在禁用用户: ID {}, Email {}", userId, user.getEmail());
    return this.updateById(user); // 更新数据库
  }

  @Override
  @Transactional // 添加事务
  public boolean unbanUser(Long userId) {
    Users user = this.getById(userId);
    if (user == null) {
      log.warn("尝试解禁不存在的用户: ID {}", userId);
      return false;
    }
    // 检查是否已经是激活状态
    if ("active".equalsIgnoreCase(user.getStatus()) || user.getStatus() == null) {
      log.info("用户 ID {} 已经是激活状态或状态为空", userId);
      return true; // 幂等性
    }

    user.setStatus("active"); // 设置状态为激活
    log.info("正在解禁用户: ID {}, Email {}", userId, user.getEmail());
    return this.updateById(user); // 更新数据库
  }

  @Override
  @Transactional
  public boolean approveProviderQualification(Long providerId) {
    Users provider = this.getById(providerId);
    if (provider == null || !"provider".equalsIgnoreCase(provider.getRole())) {
      log.warn("尝试批准非服务商或不存在的用户资质: ID {}", providerId);
      return false; // 或抛出异常
    }
    // 最好检查当前状态是否为 PENDING_REVIEW
    if (!"PENDING_REVIEW".equalsIgnoreCase(provider.getQualificationStatus())) {
      log.warn("尝试批准非待审核状态的服务商: ID {}, 当前状态 {}", providerId, provider.getQualificationStatus());
      return false; // 或者根据业务逻辑决定是否允许从其他状态批准
    }

    provider.setQualificationStatus("APPROVED");
    log.info("正在批准服务商资质: ID {}, Email {}", providerId, provider.getEmail());
    // TODO: 添加邮件通知服务商审核通过
    return this.updateById(provider);
  }

  @Override
  @Transactional
  public boolean rejectProviderQualification(Long providerId) {
    Users provider = this.getById(providerId);
    if (provider == null || !"provider".equalsIgnoreCase(provider.getRole())) {
      log.warn("尝试拒绝非服务商或不存在的用户资质: ID {}", providerId);
      return false;
    }
    // 最好检查当前状态是否为 PENDING_REVIEW
    if (!"PENDING_REVIEW".equalsIgnoreCase(provider.getQualificationStatus())) {
      log.warn("尝试拒绝非待审核状态的服务商: ID {}, 当前状态 {}", providerId, provider.getQualificationStatus());
      return false;
    }

    provider.setQualificationStatus("REJECTED");
    // provider.setQualificationRejectionReason(reason); // 如果需要记录原因，需要先在 Users
    // 模型和数据库加字段
    log.info("正在拒绝服务商资质: ID {}, Email {}", providerId, provider.getEmail());
    // TODO: 添加邮件通知服务商审核被拒
    return this.updateById(provider);
  }

}