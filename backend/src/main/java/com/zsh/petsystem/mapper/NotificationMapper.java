package com.zsh.petsystem.mapper;

import com.zsh.petsystem.dto.NotificationViewDTO;
import com.zsh.petsystem.entity.Notifications;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
// import org.apache.ibatis.annotations.Select; // 如果之前有用
// import org.apache.ibatis.annotations.Update; // 如果之前有用

@Mapper
public interface NotificationMapper extends BaseMapper<Notifications> {

  /**
   * 根据用户ID分页查询通知视图。
   * 查询用户专属通知 (user_id = #{userId}) 和所有广播通知 (is_broadcast = TRUE AND user_id IS
   * NULL)。
   * 
   * @param page                        分页对象
   * @param userId                      用户ID
   * @param targetUserIdForNotification 用于查询 user_id = ? 的条件
   * @return 分页的通知视图DTO
   */
  IPage<NotificationViewDTO> findUserNotifications(@Param("page") Page<Object> page, @Param("userId") Long userId,
      @Param("targetUserIdForNotification") Integer targetUserIdForNotification);

}