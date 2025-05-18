package com.zsh.petsystem.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsh.petsystem.dto.UserUpdateProfileDTO;
import com.zsh.petsystem.entity.Users;

public interface UserService extends IService<Users> {

  Users saveUser(Users users);

  List<Users> getAllUsers();

  boolean existByEmail(String email);

  boolean existByPhone(String phone);

  Users findByEmail(String email);

  Users getById(Long id);

  // 资质审核服务商
  List<Users> findPendingQualificationProviders();

  Users updateUserProfile(Long userId, UserUpdateProfileDTO dto);

  void changePassword(Long userId, String currentPassword, String newPassword);

  Users findUserByIdentifier(String identifier);

  boolean banUser(Long userId);

  boolean unbanUser(Long userId);

  boolean approveProviderQualification(Long providerId);

  boolean rejectProviderQualification(Long providerId);
}