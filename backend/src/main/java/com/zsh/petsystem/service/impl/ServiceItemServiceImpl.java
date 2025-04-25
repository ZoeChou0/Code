package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.ServiceItemUpdateDTO;
import com.zsh.petsystem.mapper.ReservationMapper;
import com.zsh.petsystem.mapper.ServiceItemMapper;
import com.zsh.petsystem.model.Reservation;
import com.zsh.petsystem.model.ServiceItem;
import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.service.ServiceItemService;
import com.zsh.petsystem.service.UserService;
import com.zsh.petsystem.util.EmailApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ServiceItemServiceImpl
        extends ServiceImpl<ServiceItemMapper, ServiceItem>
        implements ServiceItemService {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailApi emailApi;

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public ServiceItem add(ServiceItem item) {
        item.setReviewStatus("PENDING");
        item.setRejectionReason((null));
        this.save(item);
        return item;
    }

    @Override
    public ServiceItem update(ServiceItem item) {
        this.updateById(item);
        return item;
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    @Override
    @Transactional
    public boolean approveServiceItem(Long serviceItemId) {
        ServiceItem item = this.getById(serviceItemId);
        if (item == null) {
            throw new RuntimeException("服务项不存在");
        }
        if ("PENDING".equals(item.getReviewStatus()) || "REJECTED".equals(item.getReviewStatus())) {
            item.setReviewStatus("APPROVED");
            item.setRejectionReason(null);
            boolean updated = this.updateById(item);
            if (updated) {
                notifyProvider(item, "服务已经通过审核",
                        String.format("恭喜！您发布的服务 '%s' 已经通过审核。", item.getName()));
            }
            return updated;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean rejectServiceItem(Long serviceItemId, String reason) {
        ServiceItem item = this.getById(serviceItemId);
        if (item == null) {
            throw new RuntimeException("服务项不存在");
        }
        if ("PENDING".equals(item.getReviewStatus())) {
            item.setReviewStatus("REJECTED");
            item.setRejectionReason(reason);
            boolean updated = this.updateById(item);

            if (updated) {
                notifyProvider(item, "您的服务项审核未通过",
                        String.format("抱歉，您发布的服务 '%s' 未能通过审核。原因：%s", item.getName(), reason));
            }
            return updated;
        }
        return false;
    }

    private void notifyProvider(ServiceItem item, String subject, String content) {
        if (item.getProviderId() != null) {
            Users provider = userService.getById(item.getProviderId());
            if (provider != null && provider.getEmail() != null) {
                try {
                    emailApi.sendEmail(provider.getEmail(), subject, content);
                    System.out.println("邮件已发送至: " + provider.getEmail());
                } catch (Exception e) {
                    System.err.println("发送邮件给服务商失败 (User ID: " + provider.getId() + "): " + e.getMessage());
                }
            } else {
                System.err.println("无法发送通知：未找到服务商信息或邮箱为空 (Provider ID: " + item.getProviderId() + ")");
            }
        } else {
            System.err.println("无法发送通知：服务项缺少 Provider ID (ServiceItem ID: " + item.getId() + ")");
        }
    }

    @Override
    @Transactional
    public boolean deleteProviderServiceItem(Long serviceItemId, Long providerId) {
        // 1. 查找服务项
        ServiceItem item = this.getById(serviceItemId);
        if (item == null) {
            throw new RuntimeException("要删除的服务项不存在 (ID: " + serviceItemId + ")");
        }

        // 2. 验证所有权
        if (!Objects.equals(item.getProviderId(), providerId)) {
            throw new RuntimeException("无权删除不属于自己的服务项");
        }

        // 3. 检查状态
        if ("APPROVED".equals(item.getReviewStatus())) {
            throw new IllegalStateException("无法删除已批准的服务项，请先联系管理员进行下架处理。");
            // 或者返回 false; return false;
        }

        // 4. 执行删除 (如果所有检查通过)
        return this.removeById(serviceItemId);
    }

    @Override
    @Transactional
    public ServiceItem updateProviderServiceItem(ServiceItemUpdateDTO updateDTO, Long providerId) {
        // 1. 验证 DTO 和 ID
        if (updateDTO == null || updateDTO.getId() == null) {
            throw new IllegalArgumentException("更新请求无效，缺少必要信息");
        }
        Long serviceItemId = updateDTO.getId();

        // 2. 查找要更新的服务项
        ServiceItem existingItem = this.getById(serviceItemId);
        if (existingItem == null) {
            throw new RuntimeException("要更新的服务项不存在 (ID: " + serviceItemId + ")");
        }

        // 3. 验证所有权
        if (!Objects.equals(existingItem.getProviderId(), providerId)) {
            throw new RuntimeException("无权修改不属于自己的服务项"); // 或 AccessDeniedException
        }

        // 4.检查是否存在未来的、未取消的预约
        LambdaQueryWrapper<Reservation> conflictCheckWrapper = new LambdaQueryWrapper<>();
        conflictCheckWrapper
                .eq(Reservation::getServiceId, serviceItemId) // 匹配服务项 ID
                .ne(Reservation::getStatus, "已取消") // 状态不是 "已取消" (确切字符串)
                .gt(Reservation::getReservationTime, LocalDateTime.now()); // 预约时间在当前时间之后

        // 使用 reservationMapper 查询是否存在冲突的预约
        Long conflictingReservationsCount = reservationMapper.selectCount(conflictCheckWrapper);

        if (conflictingReservationsCount > 0) {
            // 如果存在冲突预约，则禁止修改
            throw new IllegalStateException(String.format(
                    "无法修改服务项 '%s'，因为它存在 %d 个未来的有效预约。请先处理这些预约。",
                    existingItem.getName(), conflictingReservationsCount));
        }

        // 5. 执行更新 (如果无冲突)
        boolean changed = false; // 标记是否有实际更改
        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingItem.getName())) {
            existingItem.setName(updateDTO.getName());
            changed = true;
        }
        if (updateDTO.getDescription() != null && !updateDTO.getDescription().equals(existingItem.getDescription())) {
            existingItem.setDescription(updateDTO.getDescription());
            changed = true;
        }
        if (updateDTO.getPrice() != null && !updateDTO.getPrice().equals(existingItem.getPrice())) {
            existingItem.setPrice(updateDTO.getPrice());
            changed = true;
        }
        if (updateDTO.getDuration() != null && !updateDTO.getDuration().equals(existingItem.getDuration())) {
            existingItem.setDuration(updateDTO.getDuration());
            changed = true;
        }

        // 如果有字段被修改，则执行数据库更新
        if (changed) {
            // 更新后可能需要重新提交审核，根据业务逻辑决定是否重置状态
            // existingItem.setReviewStatus("PENDING");
            // existingItem.setRejectionReason(null);
            boolean updated = this.updateById(existingItem);
            if (!updated) {
                throw new RuntimeException("更新服务项到数据库时失败");
            }
        }

        // 返回更新后的（或未改变的）服务项信息
        return existingItem;
    }

}
