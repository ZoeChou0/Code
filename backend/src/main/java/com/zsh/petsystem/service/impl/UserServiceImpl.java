package com.zsh.petsystem.service.impl;

import java.util.List;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.mapper.UserMapper;
import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.service.UserService;

public class UserServiceImpl
    extends ServiceImpl<UserMapper, Users>
    implements UserService {

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
    // 使用 LambdaQueryWrapper 进行类型安全的查询
    LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Users::getEmail, email);
    // 使用 ServiceImpl 提供的 count 方法
    return this.count(wrapper) > 0;
  }

  @Override
  public boolean existByPhone(String phone) {
    // 使用 LambdaQueryWrapper 进行类型安全的查询
    LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Users::getPhone, phone);
    // 使用 ServiceImpl 提供的 count 方法
    return this.count(wrapper) > 0;
  }

  @Override
  public Users findByEmail(String email) {
    // 使用 LambdaQueryWrapper 进行类型安全的查询
    LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Users::getEmail, email);
    return this.getOne(wrapper);
  }

  @Override
  public Users getById(Long id) {
    return super.getById(id); // 调用 ServiceImpl 中的方法
  }

  @Override
  public List<Users> findPendingQualificationProviders() {
    LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Users::getRole, "provider");
    wrapper.eq(Users::getQualificationStatus, "PENDING_REVIEW");
    return this.list(wrapper);
  }
}
