package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.NotificationViewDTO;
import com.zsh.petsystem.entity.Notifications;
import com.zsh.petsystem.mapper.NotificationMapper;
import com.zsh.petsystem.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notifications>
    implements NotificationService {

  @Autowired
  private NotificationMapper notificationMapper;

  // private final ObjectMapper objectMapper = new ObjectMapper(); // 如果 data_json
  // 是字符串

  @Override
  @Transactional
  public Notifications saveNotification(Notifications notification) {
    if (notification.getCreatedAt() == null) {
      notification.setCreatedAt(LocalDateTime.now());
    }
    this.save(notification); // 直接保存到 notifications 表
    return notification;
  }

  @Override
  public Map<String, Object> getPaginatedUserNotifications(Long userId, int pageNum, int pageSize) {
    Page<Object> pageConfig = new Page<>(pageNum, pageSize);

    IPage<NotificationViewDTO> notificationDtoPage = notificationMapper.findUserNotifications(pageConfig, userId,
        userId.intValue());

    List<NotificationViewDTO> dtoList = notificationDtoPage.getRecords().stream().map(dto -> {
      // 如果 data 是 Map<String, Object> 类型，并且是从实体直接映射的，这里可能不需要手动解析
      // 如果在DTO中 data_json 是 String, parsedData 是 Map, 则在此处解析
      // if (dto.getDataJson() != null && !dto.getDataJson().isEmpty()) {
      // try {
      // dto.setParsedData(objectMapper.readValue(dto.getDataJson(), new
      // TypeReference<Map<String, Object>>() {}));
      // } catch (IOException e) {
      // log.error("解析通知data_json失败, id: {}", dto.getId(), e);
      // dto.setParsedData(null);
      // }
      // }
      // 由于 NotificationViewDTO 现在直接有 Map<String, Object> data 字段，并且实体 Notifications 的
      // dataJson 字段
      // 使用了 JacksonTypeHandler，数据应该已经转换好了。
      // 如果实体中 dataJson 是 String，DTO 中 data 是 Map，那么 service 层是转换的好地方。
      // 但我们之前的DTO定义中，NotificationViewDTO的data字段就是Map，假设mapper查询能直接填充。
      return dto;
    }).collect(Collectors.toList());

    Map<String, Object> result = new java.util.HashMap<>();
    result.put("items", dtoList);
    result.put("total", notificationDtoPage.getTotal());
    result.put("currentPage", notificationDtoPage.getCurrent());
    result.put("pageSize", notificationDtoPage.getSize());
    result.put("totalPages", notificationDtoPage.getPages());
    return result;
  }

  // 移除了 markNotificationAsRead 和 markAllUserNotificationsAsRead 方法

  @Override
  public NotificationViewDTO getNotificationViewById(Long notificationId) {
    Notifications notification = notificationMapper.selectById(notificationId);
    if (notification == null) {
      return null;
    }
    NotificationViewDTO dto = new NotificationViewDTO();
    BeanUtils.copyProperties(notification, dto);
    dto.setUserId(notification.getUserId() != null ? notification.getUserId().longValue() : null);

    if (notification.getData() != null) {
      dto.setData(notification.getData());
    }
    // isRead 和 readAt 已从 DTO 移除
    return dto;
  }
}