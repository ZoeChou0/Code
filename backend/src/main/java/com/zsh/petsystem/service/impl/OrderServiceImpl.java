package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.config.OrderStatusConstants; // 引入状态常量
import com.zsh.petsystem.dto.OrderAdminViewDTO;
import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.dto.OrderFromReservationDTO;
import com.zsh.petsystem.dto.OrderViewDTO;
import com.zsh.petsystem.entity.Order;
import com.zsh.petsystem.entity.Pets;
import com.zsh.petsystem.entity.Reservation;
import com.zsh.petsystem.entity.Review; // 引入 Review 实体
import com.zsh.petsystem.entity.ServiceItem;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.mapper.OrderMapper;
import com.zsh.petsystem.service.OrderService;
import com.zsh.petsystem.service.PetService;
import com.zsh.petsystem.service.ReservationService;
import com.zsh.petsystem.service.ReviewService; // 引入 ReviewService
import com.zsh.petsystem.service.ServiceItemService;
import com.zsh.petsystem.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // 引入 @Lazy
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final ReservationService reservationService;
    private final PetService petService;
    private final ServiceItemService serviceItemService;
    private final UserService userService;
    private final ReviewService reviewService; // 用于填充订单的评价信息

    @Autowired
    public OrderServiceImpl(
            ReservationService reservationService,
            PetService petService,
            ServiceItemService serviceItemService,
            UserService userService,
            @Lazy ReviewService reviewService) { // 对 ReviewService 使用 @Lazy
        this.reservationService = reservationService;
        this.petService = petService;
        this.serviceItemService = serviceItemService;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @Override
    @Transactional
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

        // 检查该预约是否已经有订单
        if (reservation.getOrderId() != null) {
            Order existingOrder = this.getById(reservation.getOrderId());
            if (existingOrder != null) {
                log.warn("Reservation ID: {} already has an associated order ID: {}. Returning existing order.",
                        dto.getReservationId(), existingOrder.getId());
                return existingOrder; // 或者抛出异常，取决于业务逻辑
            }
        }

        Order order = new Order();
        order.setUserId(reservation.getUserId());
        order.setReservationId(dto.getReservationId());
        order.setAmount(dto.getAmount()); // 金额应从 dto 或 reservation 获取
        order.setStatus(OrderStatusConstants.PENDING_PAYMENT);
        // createdAt 和 updatedAt 通常由 MyMetaObjectHandler 自动填充
        // order.setCreatedAt(LocalDateTime.now());

        this.save(order); // 保存订单

        // 回填 orderId 到 reservation 并更新预约状态
        reservation.setOrderId(order.getId());
        reservation.setStatus(OrderStatusConstants.AWAITING_PAYMENT);
        reservationService.updateById(reservation);

        log.info("Order created via DTO. Order ID: {}, Reservation ID: {}, User ID: {}", order.getId(),
                dto.getReservationId(), reservation.getUserId());
        return order;
    }

    @Override
    @Transactional
    public Order createOrderFromReservation(OrderFromReservationDTO dto, Long userId) {
        if (dto == null || dto.getReservationId() == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        Long reservationId = dto.getReservationId();
        log.info("Attempting to create order from reservation. Reservation ID: {}, User ID: {}", reservationId, userId);

        Reservation reservation = reservationService.getById(reservationId);
        if (reservation == null) {
            log.error("Attempt to create order for non-existent reservation. Reservation ID: {}", reservationId);
            throw new RuntimeException("预约记录不存在");
        }
        if (!Objects.equals(reservation.getUserId(), userId)) {
            log.error("User ID mismatch. User {} attempting to create order for reservation of user {}", userId,
                    reservation.getUserId());
            throw new SecurityException("无权为他人预约创建订单");
        }
        if (reservation.getOrderId() != null) {
            Order existingOrder = this.getById(reservation.getOrderId());
            if (existingOrder != null) {
                log.warn("Reservation {} already has an associated order {}. Preventing duplicate order creation.",
                        reservationId, reservation.getOrderId());
                throw new IllegalStateException("该预约已有对应订单，请勿重复创建");
            }
        }

        List<String> allowedReservationStatus = List.of(OrderStatusConstants.CONFIRMED,
                OrderStatusConstants.PENDING_CONFIRMATION);
        if (reservation.getStatus() == null
                || !allowedReservationStatus.contains(reservation.getStatus().toUpperCase())) {
            log.error("Cannot create order. Reservation {} status is {}, not in allowed list: {}", reservationId,
                    reservation.getStatus(), allowedReservationStatus);
            throw new IllegalStateException("当前预约状态 (" + reservation.getStatus() + ") 不允许创建订单");
        }
        if (reservation.getAmount() == null || reservation.getAmount().doubleValue() <= 0) {
            log.error("Cannot create order. Reservation {} has invalid amount: {}", reservationId,
                    reservation.getAmount());
            throw new IllegalStateException("预约金额无效，无法创建订单");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setReservationId(reservationId);
        order.setAmount(reservation.getAmount());
        order.setStatus(OrderStatusConstants.PENDING_PAYMENT);
        // order.setCreatedAt(LocalDateTime.now()); // MyMetaObjectHandler 会处理

        boolean orderSaved = this.save(order);
        if (!orderSaved || order.getId() == null) {
            log.error("Failed to save order to database. Reservation ID: {}", reservationId);
            throw new RuntimeException("订单创建数据库操作失败");
        }
        log.info("Order created successfully. Order ID: {}, Associated Reservation ID: {}", order.getId(),
                reservationId);

        reservation.setOrderId(order.getId());
        reservation.setStatus(OrderStatusConstants.AWAITING_PAYMENT);
        // reservation.setUpdatedAt(LocalDateTime.now()); // MyMetaObjectHandler 会处理
        boolean reservationUpdated = reservationService.updateById(reservation);
        if (!reservationUpdated) {
            log.error("Failed to update reservation {} with order ID {}. Transaction will be rolled back.",
                    reservationId, order.getId());
            throw new RuntimeException("更新预约信息失败，订单创建已回滚");
        }
        log.info("Reservation {} successfully associated with order ID {} and status updated to AWAITING_PAYMENT",
                reservationId, order.getId());

        return order;
    }

    private void populateOrderViewDTODetails(OrderViewDTO dto, Reservation reservation, Long userIdForReviewContext) {
        if (reservation == null)
            return;

        dto.setReservationId(reservation.getId());
        dto.setPetId(reservation.getPetId());
        dto.setServiceId(reservation.getServiceId());

        Optional.ofNullable(reservation.getPetId())
                .flatMap(petId -> Optional.ofNullable(petService.getById(petId)))
                .ifPresent(pet -> dto.setPetName(pet.getName()));
        Optional.ofNullable(reservation.getServiceId())
                .flatMap(serviceId -> Optional.ofNullable(serviceItemService.getById(serviceId)))
                .ifPresent(serviceItem -> dto.setServiceName(serviceItem.getName()));
        Optional.ofNullable(reservation.getServiceStartTime())
                .ifPresent(startTime -> dto.setReservationServiceStartTime(
                        startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        // 填充评价信息 (reviewId)
        if (reviewService != null && userIdForReviewContext != null && reservation.getId() != null) {
            Review review = reviewService.findMyReviewForReservation(reservation.getId(), userIdForReviewContext);
            if (review != null) {
                dto.setReviewId(review.getId());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderViewDTO> getUserOrdersWithDetails(Long userId) {
        List<Order> orders = this.lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt)
                .list();

        return orders.stream().map(order -> {
            OrderViewDTO dto = new OrderViewDTO();
            BeanUtils.copyProperties(order, dto); // 基础订单属性
            if (order.getReservationId() != null) {
                Reservation reservation = reservationService.getById(order.getReservationId());
                populateOrderViewDTODetails(dto, reservation, userId); // 传入 userId 以便查询该用户对此预约的评价
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderViewDTO> getProviderOrdersWithDetails(Long providerUserId) {
        log.info("Fetching orders for provider user ID: {}", providerUserId);

        List<Long> serviceItemIds = serviceItemService.lambdaQuery()
                .eq(ServiceItem::getProviderId, providerUserId)
                .list().stream().map(ServiceItem::getId).collect(Collectors.toList());

        if (serviceItemIds.isEmpty())
            return Collections.emptyList();

        List<Long> reservationIds = reservationService.lambdaQuery()
                .in(Reservation::getServiceId, serviceItemIds)
                .list().stream().map(Reservation::getId).collect(Collectors.toList());

        if (reservationIds.isEmpty())
            return Collections.emptyList();

        List<Order> orders = this.lambdaQuery()
                .in(Order::getReservationId, reservationIds)
                .orderByDesc(Order::getCreatedAt).list();

        return orders.stream().map(order -> {
            OrderViewDTO dto = new OrderViewDTO();
            BeanUtils.copyProperties(order, dto);
            if (order.getReservationId() != null) {
                Reservation reservation = reservationService.getById(order.getReservationId());
                // 对于服务商视图，评价状态是相对于订单的客户(userId)
                populateOrderViewDTODetails(dto, reservation, order.getUserId());

                if (reservation != null && reservation.getUserId() != null) {
                    Users customer = userService.getById(reservation.getUserId());
                    if (customer != null) {
                        dto.setUserName(customer.getName()); // 客户名称
                    }
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelUserOrder(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null)
            throw new RuntimeException("订单不存在，无法取消。");
        if (!order.getUserId().equals(userId))
            throw new SecurityException("您无权取消此订单。");

        String currentStatus = order.getStatus();
        if (!OrderStatusConstants.PENDING_PAYMENT.equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException("当前订单状态 (" + currentStatus + ") 不允许取消。");
        }
        order.setStatus(OrderStatusConstants.CANCELLED);
        // order.setUpdatedAt(LocalDateTime.now()); // MyMetaObjectHandler 会处理
        boolean updated = this.updateById(order);
        if (!updated)
            throw new RuntimeException("数据库操作失败，取消订单失败。");

        log.info("Order ID: {} cancelled by user ID: {}", orderId, userId);

        if (order.getReservationId() != null) {
            Reservation reservation = reservationService.getById(order.getReservationId());
            if (reservation != null &&
                    (OrderStatusConstants.AWAITING_PAYMENT.equalsIgnoreCase(reservation.getStatus()) ||
                            OrderStatusConstants.PENDING_CONFIRMATION.equalsIgnoreCase(reservation.getStatus()) ||
                            OrderStatusConstants.CONFIRMED.equalsIgnoreCase(reservation.getStatus()))) {
                reservation.setStatus(OrderStatusConstants.CANCELLED_USER);
                // reservation.setUpdatedAt(LocalDateTime.now()); // MyMetaObjectHandler 会处理
                reservationService.updateById(reservation);
                log.info("Associated reservation ID {} status updated to CANCELLED_USER due to order cancellation.",
                        reservation.getId());
            }
        }
    }

    @Override
    @Transactional
    public void completeOrderByProvider(Long orderId, Long providerId) {
        Order order = this.getById(orderId);
        if (order == null)
            throw new RuntimeException("订单不存在，无法完成。");
        if (order.getReservationId() == null)
            throw new RuntimeException("订单缺少关联的预约信息，无法完成。");

        Reservation reservation = reservationService.getById(order.getReservationId());
        if (reservation == null)
            throw new RuntimeException("关联预约信息缺失，操作失败。");
        if (reservation.getServiceId() == null)
            throw new RuntimeException("预约缺少关联的服务项信息，操作失败。");

        ServiceItem serviceItem = serviceItemService.getById(reservation.getServiceId());
        if (serviceItem == null)
            throw new RuntimeException("关联服务项信息缺失，操作失败。");

        if (!Objects.equals(serviceItem.getProviderId(), providerId)) {
            throw new SecurityException("您无权完成此订单。");
        }

        String currentStatus = order.getStatus();
        if (!OrderStatusConstants.PAID.equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException(
                    "订单状态为 '" + currentStatus + "'，不能标记为完成。必须是 '" + OrderStatusConstants.PAID + "' 状态。");
        }

        order.setStatus(OrderStatusConstants.COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
        // order.setUpdatedAt(LocalDateTime.now()); // MyMetaObjectHandler 会处理
        boolean orderUpdated = this.updateById(order);

        if (orderUpdated) {
            log.info("Order ID {} marked as COMPLETED by provider user ID {}.", orderId, providerId);
            // 同步更新预约状态
            reservation.setStatus(OrderStatusConstants.COMPLETED); // 使用统一常量
            reservation.setServiceEndTime(LocalDateTime.now()); // 更新服务实际结束时间
            // reservation.setUpdatedAt(LocalDateTime.now()); // MyMetaObjectHandler 会处理
            reservationService.updateById(reservation);
            log.info("Associated reservation ID {} status updated to COMPLETED.", reservation.getId());
        } else {
            throw new RuntimeException("数据库操作失败，完成订单失败。");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<OrderAdminViewDTO> getAdminOrdersPage(int pageNum, int pageSize,
            String status /* , String keyword */) {
        Page<Order> pageRequest = new Page<>(pageNum, pageSize); // MyBatis-Plus Page 对象
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
            wrapper.eq(Order::getStatus, status); // 假设 status 是常量值
        }
        // TODO: 实现 keyword 搜索逻辑
        wrapper.orderByDesc(Order::getCreatedAt);
        IPage<Order> orderPage = this.page(pageRequest, wrapper); // 使用 this.page

        List<OrderAdminViewDTO> dtoList = orderPage.getRecords().stream().map(order -> {
            OrderAdminViewDTO dto = new OrderAdminViewDTO();
            BeanUtils.copyProperties(order, dto);
            if (order.getReservationId() != null) {
                Reservation reservation = reservationService.getById(order.getReservationId());
                if (reservation != null) {
                    dto.setPetId(reservation.getPetId());
                    dto.setServiceId(reservation.getServiceId());
                    dto.setReservationServiceStartTime(reservation.getServiceStartTime());
                    if (reservation.getPetId() != null) {
                        Pets pet = petService.getById(reservation.getPetId());
                        if (pet != null)
                            dto.setPetName(pet.getName());
                    }
                    if (reservation.getServiceId() != null) {
                        ServiceItem service = serviceItemService.getById(reservation.getServiceId());
                        if (service != null)
                            dto.setServiceName(service.getName());
                    }
                }
            }
            if (order.getUserId() != null) {
                Users user = userService.getById(order.getUserId());
                if (user != null)
                    dto.setUserName(user.getName());
            }
            return dto;
        }).collect(Collectors.toList());

        // 创建一个新的 Page 对象来承载 DTO 列表和分页信息
        Page<OrderAdminViewDTO> dtoPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        dtoPage.setRecords(dtoList);
        dtoPage.setPages(orderPage.getPages());
        return dtoPage;
    }
}