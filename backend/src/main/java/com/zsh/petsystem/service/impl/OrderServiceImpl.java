package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.dto.OrderFromReservationDTO;
import com.zsh.petsystem.dto.OrderViewDTO;
import com.zsh.petsystem.entity.Order;
import com.zsh.petsystem.entity.Reservation;
import com.zsh.petsystem.mapper.OrderMapper;
import com.zsh.petsystem.service.OrderService;
import com.zsh.petsystem.service.PetService;
import com.zsh.petsystem.service.ReservationService;
import com.zsh.petsystem.service.ServiceItemService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PetService petService;

    @Autowired
    private ServiceItemService serviceItemService;

    @Override
    public Order createOrder(OrderCreateDTO dto) {
        if (dto.getReservationId() == null) {
            throw new IllegalArgumentException("从DTO创建订单需要 reservationId");
        }
        Reservation reservation = reservationService.getById(dto.getReservationId());
        if (reservation == null) {
            throw new RuntimeException("关联的预约不存在");
        }
        if (reservation.getUserId() == null) {
            throw new RuntimeException("关联的预约缺少用户信息");
        }

        Order order = new Order();
        order.setUserId(reservation.getUserId()); // <--- **从预约中获取并设置用户ID**
        order.setReservationId(dto.getReservationId());
        order.setAmount(dto.getAmount());
        order.setStatus("待支付");
        order.setCreatedAt(LocalDateTime.now());
        this.save(order);
        log.info("直接通过DTO创建订单成功，预约ID: {}, 用户ID: {}", dto.getReservationId(), reservation.getUserId());
        return order;
    }

    @Override
    @Transactional // **重要：确保原子性**
    public Order createOrderFromReservation(OrderFromReservationDTO dto, Long userId) {
        if (dto == null || dto.getReservationId() == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        Long reservationId = dto.getReservationId();
        log.info("尝试从预约创建订单，预约ID: {}, 用户ID: {}", reservationId, userId);

        // 1. 获取预约信息
        Reservation reservation = reservationService.getById(reservationId);

        // 2. 执行各种校验
        if (reservation == null) {
            log.error("尝试为不存在的预约创建订单，预约ID: {}", reservationId);
            throw new RuntimeException("预约记录不存在");
        }
        if (!Objects.equals(reservation.getUserId(), userId)) {
            log.error("用户ID不匹配。用户 {} 尝试为用户 {} 的预约创建订单", userId, reservation.getUserId());
            throw new SecurityException("无权为他人预约创建订单");
        }
        if (reservation.getOrderId() != null) {
            log.warn("预约 {} 已有关联订单 {}，阻止创建重复订单", reservationId, reservation.getOrderId());
            throw new IllegalStateException("该预约已有对应订单，请勿重复创建");
        }
        // 检查预约状态是否允许创建订单 (例如，必须是已确认状态)
        // ** 根据你的业务流程调整允许的状态列表 **
        List<String> allowedStatus = List.of("CONFIRMED", "PENDING_CONFIRMATION"); // 示例
        if (reservation.getStatus() == null || !allowedStatus.contains(reservation.getStatus().toUpperCase())) {
            log.error("无法创建订单，预约 {} 的状态为 {}，不在允许的状态列表中: {}", reservationId, reservation.getStatus(), allowedStatus);
            throw new IllegalStateException("当前预约状态 (" + reservation.getStatus() + ") 不允许创建订单");
        }
        // 检查金额是否有效
        if (reservation.getAmount() == null || reservation.getAmount().doubleValue() <= 0) {
            log.error("无法创建订单，预约 {} 的金额无效: {}", reservationId, reservation.getAmount());
            throw new IllegalStateException("预约金额无效，无法创建订单");
        }

        // 3. 创建新订单对象
        Order order = new Order();
        order.setUserId(userId);
        order.setReservationId(reservationId);
        order.setAmount(reservation.getAmount()); // 从预约记录获取金额
        order.setStatus("待支付"); // 订单初始状态
        order.setCreatedAt(LocalDateTime.now()); // 手动设置创建时间，如果Order模型没有自动填充配置

        boolean orderSaved = this.save(order);
        if (!orderSaved || order.getId() == null) {
            log.error("保存订单到数据库失败，预约ID: {}", reservationId);
            throw new RuntimeException("订单创建数据库操作失败");
        }
        log.info("订单创建成功。订单ID: {}, 关联预约ID: {}", order.getId(), reservationId);

        // 4. **关键步骤：更新预约记录**
        reservation.setOrderId(order.getId()); // 将新订单ID关联到预约
        reservation.setStatus("AWAITING_PAYMENT"); // 更新预约状态为等待支付
        // updatedAt 由MyBatis-Plus自动填充

        boolean reservationUpdated = reservationService.updateById(reservation);
        if (!reservationUpdated) {
            log.error("更新预约记录 {} 失败（关联订单ID {}），事务将回滚。", reservationId, order.getId());
            // @Transactional 会处理回滚
            throw new RuntimeException("更新预约信息失败，订单创建已回滚");
        }
        log.info("预约记录 {} 已成功关联订单ID {} 并更新状态为 AWAITING_PAYMENT", reservationId, order.getId());

        // 5. 返回创建的订单对象
        return order;
    }

    @Override
    @Transactional(readOnly = true) // 查询操作，标记为只读事务，可能提升性能
    public List<OrderViewDTO> getUserOrdersWithDetails(Long userId) {
        // 1. 查询该用户的基本订单列表
        List<Order> orders = this.lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt) // 按创建时间降序
                .list();

        // 2. 准备用于存储结果的 DTO 列表
        List<OrderViewDTO> orderViewDTOs = new ArrayList<>();

        // 3. 遍历订单，查询并填充附加信息
        for (Order order : orders) {
            OrderViewDTO dto = new OrderViewDTO();
            BeanUtils.copyProperties(order, dto); // 复制基础属性 (order -> dto)

            // 尝试获取关联信息
            if (order.getReservationId() != null) {
                // 使用 Optional 处理可能为 null 的情况，避免空指针
                Optional<Reservation> reservationOpt = Optional
                        .ofNullable(reservationService.getById(order.getReservationId()));

                if (reservationOpt.isPresent()) {
                    Reservation reservation = reservationOpt.get();

                    // 获取宠物名称
                    Optional.ofNullable(reservation.getPetId())
                            .flatMap(petId -> Optional.ofNullable(petService.getById(petId)))
                            .ifPresent(pet -> dto.setPetName(pet.getName()));

                    // 获取服务名称
                    Optional.ofNullable(reservation.getServiceId())
                            .flatMap(serviceId -> Optional.ofNullable(serviceItemService.getById(serviceId)))
                            .ifPresent(service -> dto.setServiceName(service.getName()));

                    // 获取预约的服务开始时间 (并格式化为字符串)
                    Optional.ofNullable(reservation.getServiceStartTime())
                            .ifPresent(startTime -> dto.setReservationServiceStartTime(
                                    startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // 转换为标准字符串格式
                            ));
                } else {
                    log.warn("订单 {} 关联的预约 ID {} 未找到对应的预约记录。", order.getId(), order.getReservationId());
                }
            } else {
                log.warn("订单 {} 没有关联的预约 ID。", order.getId());
                // 根据业务逻辑，这里可能需要填充默认值或标记为特定类型订单
                dto.setPetName("N/A");
                dto.setServiceName("N/A");
            }

            orderViewDTOs.add(dto);
        }

        return orderViewDTOs;
    }

    @Override
    @Transactional
    public void cancelUserOrder(Long orderId, Long userId) { // 返回 void，失败时抛异常
        log.info("Service: User {} attempting to cancel order ID: {}", userId, orderId);
        Order order = this.getById(orderId);

        if (order == null) {
            log.warn("Service: Attempt to cancel non-existent order ID: {}", orderId);
            throw new RuntimeException("订单不存在，无法取消。");
        }
        if (!order.getUserId().equals(userId)) {
            log.warn("Service: User {} attempted to cancel order {} owned by user {}.", userId, orderId,
                    order.getUserId());
            throw new SecurityException("您无权取消此订单。");
        }
        String currentStatus = order.getStatus();
        log.info("Service: Current status of order {} is '{}'", orderId, currentStatus);
        if (!("pending".equalsIgnoreCase(currentStatus) || "待支付".equalsIgnoreCase(currentStatus))) {
            log.warn("Service: Order {} is in status '{}' and cannot be cancelled by user.", orderId, currentStatus);
            throw new IllegalStateException("当前订单状态不允许取消。");
        }
        order.setStatus("已取消");
        boolean updated = this.updateById(order);
        if (!updated) {
            log.error("Service: Failed to update order {} status to '已取消' in DB.", orderId);
            throw new RuntimeException("数据库操作失败，取消订单失败。");
        }
        log.info("Service: Order ID: {} has been successfully cancelled by user {}.", orderId, userId);

    }
}