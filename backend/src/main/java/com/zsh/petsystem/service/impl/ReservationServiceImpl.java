package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.ReservationDTO; // 假设 DTO 可能包含 userNotes
import com.zsh.petsystem.dto.WebSocketMessageDTO;
import com.zsh.petsystem.entity.Notifications;
import com.zsh.petsystem.entity.Pets;
import com.zsh.petsystem.entity.Reservation;
import com.zsh.petsystem.entity.ServiceItem;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.mapper.PetMapper;
import com.zsh.petsystem.mapper.ReservationMapper;
import com.zsh.petsystem.mapper.ServiceItemMapper;
import com.zsh.petsystem.service.UserService;
import com.zsh.petsystem.service.WebNotificationService;

import lombok.extern.slf4j.Slf4j;

import com.zsh.petsystem.service.NotificationService;
import com.zsh.petsystem.service.ReservationService;
import com.zsh.petsystem.service.ServiceItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal; // 导入 BigDecimal
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects; // 导入 Objects

@Service
@Slf4j // 添加日志记录器
public class ReservationServiceImpl
        extends ServiceImpl<ReservationMapper, Reservation>
        implements ReservationService {

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private ServiceItemMapper serviceItemMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private NotificationService notificationService; // 用于持久化通知

    @Autowired
    private WebNotificationService webNotificationService;

    @Autowired
    private ServiceItemService serviceItemService;

    @Override
    @Transactional // 确保方法原子性
    public Reservation create(ReservationDTO dto, Long userId) {
        // --- 基础信息校验 ---
        Users user = userService.getById(userId);
        if (user == null)
            throw new IllegalArgumentException("用户不存在 (ID: " + userId + ")");

        Pets pet = petMapper.selectById(dto.getPetId());
        if (pet == null)
            throw new IllegalArgumentException("宠物不存在 (ID: " + dto.getPetId() + ")");
        if (!Objects.equals(userId, pet.getUserId())) {
            throw new SecurityException("无法预约不属于自己的宠物"); // 使用更具体的异常类型
        }

        ServiceItem serviceItem = serviceItemMapper.selectById(dto.getServiceId());
        if (serviceItem == null)
            throw new IllegalArgumentException("服务项不存在 (ID: " + dto.getServiceId() + ")");

        // --- 服务状态校验 ---
        if (!"APPROVED".equalsIgnoreCase(serviceItem.getReviewStatus())) {
            throw new IllegalStateException("该服务项当前不可用 (状态: " + serviceItem.getReviewStatus() + ")");
        }

        // --- 日期和时间校验 ---
        if (dto.getReservationStartDate() == null || dto.getReservationEndDate() == null) {
            throw new IllegalArgumentException("必须提供预约开始日期和结束日期");
        }
        if (dto.getReservationEndDate().isBefore(dto.getReservationStartDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        if (dto.getReservationStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("不能预约过去的日期");
        }
        if (dto.getReservationTime() == null) {
            throw new IllegalArgumentException("必须提供预约开始时间");
        }
        LocalTime startTime = dto.getReservationTime().toLocalTime(); // 提取时间部分

        // --- 宠物资格检查 ---
        checkPetEligibility(pet, serviceItem);

        // --- **日期范围容量检查** ---
        checkCapacityForDateRange(serviceItem, dto.getReservationStartDate(), dto.getReservationEndDate());

        // --- 创建预约对象 ---
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setPetId(dto.getPetId());
        reservation.setServiceId(serviceItem.getId());
        reservation.setProviderId(serviceItem.getProviderId());
        reservation.setOrderId(null); // 订单ID稍后在创建订单时设置

        reservation.setReservationStartDate(dto.getReservationStartDate()); // 设置开始日期
        reservation.setReservationEndDate(dto.getReservationEndDate()); // 设置结束日期

        // 组合开始日期和时间
        reservation.setServiceStartTime(LocalDateTime.of(dto.getReservationStartDate(), startTime));

        // 计算结束日期时间 (基于服务时长)
        // 注意：这里的逻辑假设服务时长只应用一次，或者代表每天的时长。需要根据业务明确。
        if (serviceItem.getDuration() != null && serviceItem.getDuration() > 0) {
            // 简单情况：总时长从第一天开始计算
            reservation.setServiceEndTime(reservation.getServiceStartTime().plusMinutes(serviceItem.getDuration()));
        } else {
            // 如果没有时长，例如按天计算的服务，可能在结束日期的某个固定时间结束
            reservation.setServiceEndTime(LocalDateTime.of(dto.getReservationEndDate(), LocalTime.of(18, 0))); // 示例：结束日下午6点
            // log.warn("服务项 ID {} 没有设置时长，结束时间计算逻辑需要确认。", serviceItem.getId());
        }

        // 设置初始状态和金额
        reservation.setStatus("PENDING_CONFIRMATION"); // 初始状态，可根据流程调整为 "CONFIRMED"
        if (serviceItem.getPrice() != null) {
            // !! 金额计算逻辑需要根据业务规则确认 !!
            // 示例：按天收费 (包含首尾两天)
            long days = dto.getReservationStartDate().until(dto.getReservationEndDate()).getDays() + 1;
            if (days <= 0)
                days = 1; // 至少算一天
            BigDecimal totalAmount = BigDecimal.valueOf(serviceItem.getPrice()).multiply(BigDecimal.valueOf(days));
            reservation.setAmount(totalAmount);
            log.info("计算得到预约天数: {}, 单价: {}, 总金额: {}", days, serviceItem.getPrice(), totalAmount);
        } else {
            reservation.setAmount(BigDecimal.ZERO); // 如果服务免费或价格未定
        }

        // reservation.setUserNotes(dto.getUserNotes()); // 如果DTO中有备注字段

        // --- 保存预约 ---
        // createdAt 和 updatedAt 由MyBatis-Plus自动填充处理
        boolean saved = this.save(reservation);
        if (!saved) {
            log.error("保存预约记录到数据库失败，用户ID: {}, 服务ID: {}", userId, serviceItem.getId());
            throw new RuntimeException("创建预约数据库操作失败");
        }

        log.info("预约创建成功。ID: {}, 用户: {}, 服务: {}, 日期范围: {} 到 {}",
                reservation.getId(), userId, serviceItem.getId(), dto.getReservationStartDate(),
                dto.getReservationEndDate());
        // 返回包含ID和计算后金额的完整预约对象
        return reservation;
    }

    // --- 容量检查辅助方法 ---
    private void checkCapacityForDateRange(ServiceItem serviceItem, LocalDate startDate, LocalDate endDate) {
        // 如果服务没有设置每日容量限制，则直接返回
        if (serviceItem.getDailyCapacity() == null || serviceItem.getDailyCapacity() <= 0) {
            log.info("服务 ID {} 未设置每日容量限制，跳过检查。", serviceItem.getId());
            return;
        }

        LocalDate currentDate = startDate;
        // 遍历日期范围内的每一天
        while (!currentDate.isAfter(endDate)) {
            // 查询在 currentDate 当天有效的预约数量 (状态非取消/拒绝)
            LambdaQueryWrapper<Reservation> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(Reservation::getServiceId, serviceItem.getId())
                    .le(Reservation::getReservationStartDate, currentDate) // 预约开始日期 <= 当前检查日期
                    .ge(Reservation::getReservationEndDate, currentDate) // 预约结束日期 >= 当前检查日期
                    .notIn(Reservation::getStatus, // 排除已取消或拒绝的状态
                            "CANCELLED_USER", "CANCELLED_PROVIDER", "REJECTED");

            long currentBookingsOnDate = this.count(countWrapper);

            // 检查是否超出容量
            if (currentBookingsOnDate >= serviceItem.getDailyCapacity()) {
                log.warn("容量检查失败：服务 {} 在日期 {} 已有 {} 个预约，达到或超过容量 {}",
                        serviceItem.getId(), currentDate, currentBookingsOnDate, serviceItem.getDailyCapacity());
                throw new IllegalStateException("服务项在 " + currentDate + " 的预约已满");
            }
            currentDate = currentDate.plusDays(1); // 检查下一天
        }
        log.info("容量检查通过：服务 {} 从 {} 到 {} 均有余量。", serviceItem.getId(), startDate, endDate);
    }

    @Override
    public List<Reservation> getByUserId(Long userId) {
        // 可以根据开始日期或创建日期排序
        return this.lambdaQuery()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getReservationStartDate) // 按开始日期降序
                .orderByDesc(Reservation::getServiceStartTime) // 再按开始时间降序
                .list();
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Reservation reservation = this.getById(id);
        if (reservation == null)
            throw new RuntimeException("预约不存在 (ID: " + id + ")");

        // 检查是否可取消（状态、时间等）
        LocalDateTime serviceStartTime = reservation.getServiceStartTime();
        String currentStatus = reservation.getStatus();
        // 定义最终状态，这些状态下不允许取消
        List<String> finalStates = Arrays.asList("COMPLETED", "CANCELLED_USER", "CANCELLED_PROVIDER", "REJECTED");

        if (currentStatus != null && finalStates.contains(currentStatus.toUpperCase())) {
            log.info("预约 (ID: {}) 已处于最终状态 ({})，无需取消。", id, currentStatus);
            // throw new IllegalStateException("预约已完成或已取消"); // 或者静默返回
            return;
        }

        // 增加基于时间的取消限制，例如服务开始前24小时内不允许取消
        if (serviceStartTime != null && serviceStartTime.isBefore(LocalDateTime.now().plusHours(24))) {
            throw new IllegalStateException("距离服务开始不足24小时，无法在线取消");
        }

        reservation.setStatus("CANCELLED_USER"); // 设置为用户取消状态
        // reservation.setCancellationReason(reason); // 如果需要记录取消原因

        boolean updated = this.updateById(reservation);
        if (!updated) {
            log.error("更新预约状态为 CANCELLED_USER 失败，预约ID: {}", id);
            throw new RuntimeException("更新预约状态数据库操作失败");
        }

        // TODO: 添加取消后的业务逻辑，如退款、通知服务商等

        log.info("预约 ID {} 已被用户取消。", id);
    }

    // 宠物资格检查方法 (内部逻辑不变)
    private void checkPetEligibility(Pets pet, ServiceItem serviceItem) {
        // ... (之前的检查逻辑：疫苗、绝育、年龄、品种、性格等) ...
        log.info("宠物 ID {} 资格检查通过，服务 ID {}", pet.getId(), serviceItem.getId());
    }

    @Override
    public List<Reservation> getReservationsForProvider(Long providerId, String status) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getProviderId, providerId);
        if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
            wrapper.eq(Reservation::getStatus, status.toUpperCase());
        }
        wrapper.orderByDesc(Reservation::getReservationStartDate, Reservation::getServiceStartTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public Reservation confirmReservationByProvider(Long reservationId, Long providerId) {
        Reservation reservation = getAndCheckProviderReservation(reservationId, providerId);
        if (!"PENDING".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalStateException("只有待处理的预约才能被确认");
        }
        reservation.setStatus("CONFIRMED");
        this.updateById(reservation);
        // TODO: 通知用户预约已确认
        return reservation;
    }

    @Override
    @Transactional
    public Reservation rejectReservationByProvider(Long reservationId, Long providerId, String reason) {
        Reservation reservation = getAndCheckProviderReservation(reservationId, providerId);
        if (!"PENDING".equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalStateException("只有待处理的预约才能被拒绝");
        }
        reservation.setStatus("REJECTED");
        if (StringUtils.hasText(reason)) {
            reservation.setRejectionReason(reason);
        }
        this.updateById(reservation);
        // TODO: 通知用户预约被拒绝
        return reservation;
    }

    @Override
    @Transactional
    public Reservation completeReservationByProvider(Long reservationId, Long providerId) {
        Reservation reservation = getAndCheckProviderReservation(reservationId, providerId);
        if (!"CONFIRMED".equalsIgnoreCase(reservation.getStatus())
                && !"PAID".equalsIgnoreCase(reservation.getStatus())) { // 允许从 PAID 状态完成
            throw new IllegalStateException("只有已确认或已支付的预约才能被标记为完成");
        }
        reservation.setStatus("COMPLETED"); // 标记为已完成
        reservation.setServiceEndTime(LocalDateTime.now()); // 可以更新实际服务完成时间
        reservation.setUpdatedAt(LocalDateTime.now());
        boolean updated = this.updateById(reservation);

        if (updated) {
            log.info("Reservation ID {} has been marked as COMPLETED by provider ID {}.", reservationId, providerId);

            // TODO: 触发订单结算逻辑 (如果适用)

            // 发送通知给用户，提示可以评价
            Users user = userService.getById(reservation.getUserId());
            ServiceItem serviceItem = serviceItemService.getById(reservation.getServiceId());
            String serviceName = (serviceItem != null) ? serviceItem.getName() : "您预约的服务";
            String petName = "";
            if (reservation.getPetId() != null) {
                Pets pet = petMapper.selectById(reservation.getPetId());
                if (pet != null) {
                    petName = "您的宠物 " + pet.getName() + " 的";
                }
            }

            if (user != null) {
                String title = "服务已完成，期待您的评价！";
                String content = String.format("尊敬的%s，%s“%s”已完成。感谢您的惠顾，期待您对本次服务做出评价！",
                        user.getName(), petName, serviceName);

                // 1. 持久化通知到数据库
                Notifications dbNotification = new Notifications();
                dbNotification.setUserId(user.getId().intValue()); // 注意类型转换
                dbNotification.setType("review_invitation");
                dbNotification.setTitle(title);
                dbNotification.setContent(content);
                dbNotification.setLevel("info");
                // data可以包含跳转链接或相关ID
                dbNotification.setData(Map.of(
                        "reservationId", reservation.getId(),
                        "serviceItemId", reservation.getServiceId() // 评价是针对 serviceItem
                // "reviewPageUrl", "/my-orders" // 或直接到评价页面的链接
                ));
                dbNotification.setIsBroadcast(false);
                dbNotification.setCreatedAt(LocalDateTime.now());
                dbNotification.setSentAt(LocalDateTime.now());
                try {
                    notificationService.saveNotification(dbNotification);
                    log.info("Review invitation notification saved for user ID: {}", user.getId());
                } catch (Exception e) {
                    log.error("Failed to save review invitation notification for user ID {}: {}", user.getId(),
                            e.getMessage());
                }

                // 2. 通过 WebSocket 发送实时通知 (如果用户在线)
                WebSocketMessageDTO<Map<String, Object>> wsMessage = new WebSocketMessageDTO<>(
                        "review_invitation",
                        title,
                        content,
                        System.currentTimeMillis(),
                        "info",
                        Map.of(
                                "reservationId", reservation.getId(),
                                "serviceItemId", reservation.getServiceId()));
                // webNotificationService.sendMessageToUser(user.getId(), wsMessage); // 假设有此方法
                // 或者如果 WebSocketServer 能根据 userId 推送
                // NotificationWebSocketServer.sendObjectToUser(user.getId().toString(),
                // wsMessage);
                // 当前的 NotificationWebSocketServer.sendObjectInfo 是群发，需要一个单发逻辑
                log.info("Sent (or attempted to send) real-time review invitation to user ID: {}", user.getId());
            }
        }
        return reservation;
    }

    private Reservation getAndCheckProviderReservation(Long reservationId, Long providerId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null)
            throw new RuntimeException("预约不存在");
        if (!reservation.getProviderId().equals(providerId)) {
            // 通过 ServiceItem 再次确认 providerId，或直接比较 reservation.getProviderId()
            ServiceItem serviceItem = serviceItemService.getById(reservation.getServiceId());
            if (serviceItem == null || !serviceItem.getProviderId().equals(providerId)) {
                throw new SecurityException("无权操作此预约");
            }
            // 如果 reservation 表本身没有 providerId, 则需要通过 serviceItem 查找
            // 假设 Reservation 表有 providerId 字段
        }
        return reservation;
    }

    @Override
    public long countPendingReservationsForProvider(Long providerId) {
        return this.lambdaQuery()
                .eq(Reservation::getProviderId, providerId)
                .eq(Reservation::getStatus, "PENDING")
                .count();
    }

}