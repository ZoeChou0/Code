package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.ServiceCreateDTO;
import com.zsh.petsystem.dto.ServiceItemDetailDTO;
import com.zsh.petsystem.dto.ServiceItemUpdateDTO;
import com.zsh.petsystem.entity.Reservation;
import com.zsh.petsystem.entity.ServiceItem;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.mapper.ReservationMapper;
import com.zsh.petsystem.mapper.ServiceItemMapper;
import com.zsh.petsystem.service.ServiceItemService;
import com.zsh.petsystem.service.UserService;
import com.zsh.petsystem.util.EmailApi;
import com.zsh.petsystem.service.RedisService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ServiceItemServiceImpl
        extends ServiceImpl<ServiceItemMapper, ServiceItem>
        implements ServiceItemService {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailApi emailApi;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ServiceItemMapper serviceItemMapper;

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
            log.warn("尝试批准不存在的服务项 ID: {}", serviceItemId);
            throw new RuntimeException("服务项不存在");
        }

        log.info("正在处理批准服务项 ID: {}, 当前状态: {}", serviceItemId, item.getReviewStatus());

        // 检查状态是否允许批准，主要检查 PENDING_APPROVAL，也允许从 PENDING 或 REJECTED 批准
        if ("PENDING_APPROVAL".equalsIgnoreCase(item.getReviewStatus()) ||
                "PENDING".equalsIgnoreCase(item.getReviewStatus()) || // 保留以防万一
                "REJECTED".equalsIgnoreCase(item.getReviewStatus())) {

            item.setReviewStatus("APPROVED");
            item.setRejectionReason(null); // 批准时清除拒绝原因
            // 不需要手动设置 updatedAt，MyMetaObjectHandler 会处理
            // item.setUpdatedAt(LocalDateTime.now());

            boolean updated = this.updateById(item);
            log.info("尝试更新服务项 ID: {} 到 APPROVED 状态，数据库 updateById 返回: {}", serviceItemId, updated);

            if (updated) {
                log.info("服务项 ID: {} 成功更新状态为 APPROVED。", serviceItemId);
                // 解注释并确保 notifyProvider 方法及其依赖（userService, emailApi）已正确注入和工作
                // notifyProvider(item, "服务已经通过审核",
                // String.format("恭喜！您发布的服务 '%s' 已经通过审核。", item.getName()));
            } else {
                log.error("服务项 ID: {} 更新状态为 APPROVED 失败 (updateById 返回 false)。当前数据库状态可能未改变。", serviceItemId);
            }
            return updated;
        }

        log.warn("服务项 ID {} 的状态为 '{}' (期望 PENDING_APPROVAL, PENDING, 或 REJECTED)，不满足批准条件。", serviceItemId,
                item.getReviewStatus());
        return false;
    }

    @Override
    @Transactional
    public boolean rejectServiceItem(Long serviceItemId, String reason) {
        ServiceItem item = this.getById(serviceItemId);
        if (item == null) {
            log.warn("尝试拒绝不存在的服务项 ID: {}", serviceItemId);
            throw new RuntimeException("服务项不存在");
        }
        log.info("正在处理拒绝服务项 ID: {}, 当前状态: {}, 拒绝原因: {}", serviceItemId, item.getReviewStatus(), reason);

        // 通常只允许拒绝处于待审核状态的服务
        if ("PENDING_APPROVAL".equalsIgnoreCase(item.getReviewStatus()) ||
                "PENDING".equalsIgnoreCase(item.getReviewStatus())) {

            item.setReviewStatus("REJECTED");
            item.setRejectionReason(reason);
            // 不需要手动设置 updatedAt

            boolean updated = this.updateById(item);
            log.info("尝试更新服务项 ID: {} 到 REJECTED 状态，数据库 updateById 返回: {}", serviceItemId, updated);

            if (updated) {
                log.info("服务项 ID: {} 成功更新状态为 REJECTED。", serviceItemId);
                // 解注释并确保 notifyProvider 方法及其依赖已正确注入和工作
                // notifyProvider(item, "您的服务项审核未通过",
                // String.format("抱歉，您发布的服务 '%s' 未能通过审核。原因：%s", item.getName(), reason));
            } else {
                log.error("服务项 ID: {} 更新状态为 REJECTED 失败 (updateById 返回 false)。当前数据库状态可能未改变。", serviceItemId);
            }
            return updated;
        }
        log.warn("服务项 ID {} 的状态为 '{}' (期望 PENDING_APPROVAL 或 PENDING)，不满足拒绝条件。", serviceItemId, item.getReviewStatus());
        return false;
    }

    // private void notifyProvider(ServiceItem item, String subject, String content)
    // {
    // if (item.getProviderId() != null) {
    // Users provider = userService.getById(item.getProviderId());
    // if (provider != null && provider.getEmail() != null) {
    // try {
    // emailApi.sendEmail(provider.getEmail(), subject, content);
    // System.out.println("邮件已发送至: " + provider.getEmail());
    // } catch (Exception e) {
    // System.err.println("发送邮件给服务商失败 (User ID: " + provider.getId() + "): " +
    // e.getMessage());
    // }
    // } else {
    // System.err.println("无法发送通知：未找到服务商信息或邮箱为空 (Provider ID: " +
    // item.getProviderId() + ")");
    // }
    // } else {
    // System.err.println("无法发送通知：服务项缺少 Provider ID (ServiceItem ID: " +
    // item.getId() + ")");
    // }
    // }

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
                // 状态不是已取消 (需要包含所有代表取消的状态)
                .notIn(Reservation::getStatus, "已取消", "CANCELLED_USER", "CANCELLED_PROVIDER")
                // **修改**: 使用 serviceStartTime 检查预约时间是否在当前时间之后
                .gt(Reservation::getServiceStartTime, LocalDateTime.now());

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
        if (updateDTO.getCategory() != null && !updateDTO.getCategory().equals(existingItem.getCategory())) {
            existingItem.setCategory(updateDTO.getCategory());
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

    @Override // 添加 @Override 注解
    @Transactional(readOnly = true) // 查询操作建议添加只读事务
    public List<ServiceItemDetailDTO> getActiveServicesWithDetails(Map<String, Object> params) {
        // 参数预处理 (例如，将前端传来的 priceRange 数组转换为 minPrice/maxPrice Map 条目)
        if (params.containsKey("priceRange") && params.get("priceRange") instanceof List) {
            try {
                List<?> range = (List<?>) params.get("priceRange");
                if (range.size() == 2) {
                    Number min = (Number) range.get(0);
                    Number max = (Number) range.get(1);
                    if (min != null && min.doubleValue() > 0) {
                        params.put("minPrice", min.doubleValue());
                    }
                    if (max != null && max.doubleValue() < 1000) { // 假设1000是上限标记
                        params.put("maxPrice", max.doubleValue());
                    }
                }
            } catch (ClassCastException | IndexOutOfBoundsException e) {
                // 处理类型转换或索引越界错误，打印日志或忽略
                log.warn("处理 priceRange 参数时出错: {}", e.getMessage());
            }
            params.remove("priceRange"); // 移除原始键，避免混淆
        }
        return serviceItemMapper.findActiveServiceDetailsFiltered(params);

    }

    @Override
    @Transactional
    public ServiceItem setProviderServiceAvailability(Long serviceItemId, Long providerId, boolean available) {
        ServiceItem item = this.getById(serviceItemId);
        if (item == null)
            throw new RuntimeException("服务项不存在");
        if (!item.getProviderId().equals(providerId))
            throw new SecurityException("无权操作此服务项");

        // 如果是下架，可以直接改为一个例如 "UNAVAILABLE" 或 "INACTIVE" 的状态
        // 如果是重新上架，通常需要管理员重新审核，所以可能设为 "PENDING_APPROVAL"
        if (available) {
            if (!"APPROVED".equals(item.getReviewStatus())) { // 假设只有 APPROVED 的才能操作上架，或者下架后重新上架需要审核
                item.setReviewStatus("PENDING_APPROVAL"); // 或直接 APPROVED，取决于业务
                log.info("服务项 {} 由服务商 {} 申请重新上架，状态更改为 PENDING_APPROVAL", serviceItemId, providerId);
            } else {
                // 如果已经是 APPROVED 且是想从一个非活跃状态恢复 (假设我们添加一个 active 字段)
                // item.setActive(true);
                // 这里简化处理：如果已经是 APPROVED，则不改变 reviewStatus
                log.info("服务项 {} 已是 APPROVED 状态，无需操作上架审核状态", serviceItemId);
            }
        } else { // 下架
            // 注意：下架不应影响 PENDING_APPROVAL 或 REJECTED 的状态
            // 考虑增加一个新的布尔字段如 `isListed` 或 `isActiveByProvider`
            // 此处简化：如果已经是 APPROVED，可以改为一个自定义的 "SUSPENDED_BY_PROVIDER" 状态
            // 或者，更简单的逻辑是，服务商不能直接“上架”，只能提交审核，下架后可能也需要重新审核才能上架
            // 暂时我们假设服务商只能控制一个额外的 "active" 状态，或者下架就是标记为 "PENDING_APPROVAL" 等待管理员重新审核
            // 为简单起见，如果想下架，则改为 PENDING_APPROVAL，让管理员决定是否批准。
            // 或者，更合理的做法是，服务项一旦 APPROVED，服务商只能主动将其设为 “UNAVAILABLE_BY_PROVIDER”
            // 而不能直接改回 PENDING_APPROVAL 或 REJECTED。
            // 此处示例为修改为 PENDING_APPROVAL 等待管理员操作
            if ("APPROVED".equals(item.getReviewStatus())) {
                item.setReviewStatus("PENDING_APPROVAL"); // 下架后需要重新审核
                log.info("服务项 {} 由服务商 {} 下架，状态更改为 PENDING_APPROVAL", serviceItemId, providerId);
            } else {
                log.info("服务项 {} 当前状态为 {}，服务商下架操作不改变其审核状态。", serviceItemId, item.getReviewStatus());
                // 或者抛出异常，不允许操作非 APPROVED 的服务
                // throw new IllegalStateException("只有已批准的服务才能被下架");
            }
            // 实际下架操作可能更复杂，比如需要检查是否有未完成的预约等。
        }
        // 这里只是修改 reviewStatus，实际业务中“下架”可能意味着修改一个独立的布尔字段 `published` 或 `active`
        // 并且下架后，该服务不应再出现在普通用户的搜索结果中。
        // 简单的做法是，如果服务商想下架，就将 reviewStatus 改为非 APPROVED 的状态，例如 "UNLISTED_BY_PROVIDER"
        // 这里我们假设下架就是让它变为非 APPROVED 状态，例如 PENDING_APPROVAL (需要管理员再次审核才能上架)
        // 或者，如果 reviewStatus 有 "UNAVAILABLE" 值
        // item.setReviewStatus(available ? "APPROVED" : "UNAVAILABLE"); // 假设服务商可以直接切换

        this.updateById(item);
        return item;
    }

    @Override
    public long countActiveServicesForProvider(Long providerId) {
        return this.lambdaQuery()
                .eq(ServiceItem::getProviderId, providerId)
                .eq(ServiceItem::getReviewStatus, "APPROVED") // 只统计已批准的
                .count();
    }

    @Override
    @Transactional // 推荐为创建操作添加事务管理
    public ServiceItem addProviderService(ServiceCreateDTO dto, Long providerId) {
        if (dto == null || providerId == null) {
            throw new IllegalArgumentException("服务创建信息和提供者ID不能为空");
        }

        ServiceItem serviceItem = new ServiceItem();

        BeanUtils.copyProperties(dto, serviceItem);

        serviceItem.setCategory(dto.getCategory());

        // 设置由后端逻辑决定的字段
        serviceItem.setProviderId(providerId);
        serviceItem.setReviewStatus("PENDING_APPROVAL"); // 新服务通常需要审核
        serviceItem.setCreatedAt(LocalDateTime.now()); // 设置创建时间
        serviceItem.setUpdatedAt(LocalDateTime.now()); // 设置更新时间

        // 校验 dailyCapacity 是否为 null，如果是，则可能需要设置为0或一个默认值
        if (serviceItem.getDailyCapacity() == null) {
            serviceItem.setDailyCapacity(0); // 或者一个你业务逻辑中的默认值，比如Integer.MAX_VALUE代表无限
        }

        // 如果 requiresNeutered 是 Boolean 类型，且 DTO 中是可选的，这里可能需要处理 null
        if (serviceItem.getRequiresNeutered() == null) {
            serviceItem.setRequiresNeutered(false); // 或根据业务逻辑设为 true/null
        }

        // 保存到数据库
        boolean saved = this.save(serviceItem); // this.save() 是 ServiceImpl 提供的方法

        if (!saved || serviceItem.getId() == null) {
            // 可以记录日志
            // log.error("服务商 {} 创建服务项 {} 失败，数据库保存返回 false 或 ID 为空", providerId,
            // dto.getName());
            throw new RuntimeException("创建服务项数据库操作失败");
        }

        // log.info("服务商 {} 成功创建服务项: {}, ID: {}", providerId, serviceItem.getName(),
        // serviceItem.getId());
        return serviceItem; // 返回包含新生成 ID 的实体对象
    }
}
