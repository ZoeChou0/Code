package com.zsh.petsystem.service;

import com.zsh.petsystem.dto.NotificationViewDTO;
import com.zsh.petsystem.entity.Notifications;
import com.baomidou.mybatisplus.extension.service.IService;
// import com.baomidou.mybatisplus.core.metadata.IPage; // 如果使用 MyBatis-Plus 的 IPage

import java.util.Map;

public interface NotificationService extends IService<Notifications> {

  Notifications saveNotification(Notifications notification);

  /**
   * 获取指定用户的通知列表（分页）。
   * 由于不再区分已读/未读，readStatus 参数可以移除或忽略。
   */
  Map<String, Object> getPaginatedUserNotifications(Long userId, int page, int size);

  // 移除了 markNotificationAsRead
  // 移除了 markAllUserNotificationsAsRead

  NotificationViewDTO getNotificationViewById(Long notificationId);
}