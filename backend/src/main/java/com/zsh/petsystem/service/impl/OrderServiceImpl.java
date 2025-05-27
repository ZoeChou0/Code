// package com.zsh.petsystem.service.impl;

// import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// import com.baomidou.mybatisplus.core.metadata.IPage;
// import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// import com.zsh.petsystem.dto.OrderAdminViewDTO;
// import com.zsh.petsystem.dto.OrderCreateDTO;
// import com.zsh.petsystem.dto.OrderFromReservationDTO;
// import com.zsh.petsystem.dto.OrderViewDTO;
// import com.zsh.petsystem.entity.Order;
// import com.zsh.petsystem.entity.Pets;
// import com.zsh.petsystem.entity.Reservation;
// import com.zsh.petsystem.entity.ServiceItem;
// import com.zsh.petsystem.entity.Users;
// import com.zsh.petsystem.mapper.OrderMapper;
// import com.zsh.petsystem.service.OrderService;
// import com.zsh.petsystem.service.PetService;
// import com.zsh.petsystem.service.ReservationService;
// import com.zsh.petsystem.service.ServiceItemService;

// import lombok.extern.slf4j.Slf4j;

// import org.springframework.beans.BeanUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Page;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.StringUtils;

// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Objects;
// import java.util.Optional;
// import java.util.stream.Collectors;

// @Service
// @Slf4j
// public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

//     @Autowired
//     private ReservationService reservationService;

//     @Autowired
//     private PetService petService;

//     @Autowired
//     private ServiceItemService serviceItemService;

//     @Override
//     public Order createOrder(OrderCreateDTO dto) {
//         if (dto.getReservationId() == null) {
//             throw new IllegalArgumentException("从DTO创建订单需要 reservationId");
//         }
//         Reservation reservation = reservationService.getById(dto.getReservationId());
//         if (reservation == null) {
//             throw new RuntimeException("关联的预约不存在");
//         }
//         if (reservation.getUserId() == null) {
//             throw new RuntimeException("关联的预约缺少用户信息");
//         }

//         Order order = new Order();
//         order.setUserId(reservation.getUserId()); // <--- **从预约中获取并设置用户ID**
//         order.setReservationId(dto.getReservationId());
//         order.setAmount(dto.getAmount());
//         order.setStatus("待支付");
//         order.setCreatedAt(LocalDateTime.now());
//         this.save(order);
//         log.info("直接通过DTO创建订单成功，预约ID: {}, 用户ID: {}", dto.getReservationId(), reservation.getUserId());
//         return order;
//     }

//     @Override
//     @Transactional // **重要：确保原子性**
//     public Order createOrderFromReservation(OrderFromReservationDTO dto, Long userId) {
//         if (dto == null || dto.getReservationId() == null) {
//             throw new IllegalArgumentException("预约ID不能为空");
//         }
//         Long reservationId = dto.getReservationId();
//         log.info("尝试从预约创建订单，预约ID: {}, 用户ID: {}", reservationId, userId);

//         // 1. 获取预约信息
//         Reservation reservation = reservationService.getById(reservationId);

//         // 2. 执行各种校验
//         if (reservation == null) {
//             log.error("尝试为不存在的预约创建订单，预约ID: {}", reservationId);
//             throw new RuntimeException("预约记录不存在");
//         }
//         if (!Objects.equals(reservation.getUserId(), userId)) {
//             log.error("用户ID不匹配。用户 {} 尝试为用户 {} 的预约创建订单", userId, reservation.getUserId());
//             throw new SecurityException("无权为他人预约创建订单");
//         }
//         if (reservation.getOrderId() != null) {
//             log.warn("预约 {} 已有关联订单 {}，阻止创建重复订单", reservationId, reservation.getOrderId());
//             throw new IllegalStateException("该预约已有对应订单，请勿重复创建");
//         }
//         // 检查预约状态是否允许创建订单 (例如，必须是已确认状态)
//         // ** 根据你的业务流程调整允许的状态列表 **
//         List<String> allowedStatus = List.of("CONFIRMED", "PENDING_CONFIRMATION"); // 示例
//         if (reservation.getStatus() == null || !allowedStatus.contains(reservation.getStatus().toUpperCase())) {
//             log.error("无法创建订单，预约 {} 的状态为 {}，不在允许的状态列表中: {}", reservationId, reservation.getStatus(), allowedStatus);
//             throw new IllegalStateException("当前预约状态 (" + reservation.getStatus() + ") 不允许创建订单");
//         }
//         // 检查金额是否有效
//         if (reservation.getAmount() == null || reservation.getAmount().doubleValue() <= 0) {
//             log.error("无法创建订单，预约 {} 的金额无效: {}", reservationId, reservation.getAmount());
//             throw new IllegalStateException("预约金额无效，无法创建订单");
//         }

//         // 3. 创建新订单对象
//         Order order = new Order();
//         order.setUserId(userId);
//         order.setReservationId(reservationId);
//         order.setAmount(reservation.getAmount()); // 从预约记录获取金额
//         order.setStatus("待支付"); // 订单初始状态
//         order.setCreatedAt(LocalDateTime.now()); // 手动设置创建时间，如果Order模型没有自动填充配置

//         boolean orderSaved = this.save(order);
//         if (!orderSaved || order.getId() == null) {
//             log.error("保存订单到数据库失败，预约ID: {}", reservationId);
//             throw new RuntimeException("订单创建数据库操作失败");
//         }
//         log.info("订单创建成功。订单ID: {}, 关联预约ID: {}", order.getId(), reservationId);

//         // 4. **关键步骤：更新预约记录**
//         reservation.setOrderId(order.getId()); // 将新订单ID关联到预约
//         reservation.setStatus("AWAITING_PAYMENT"); // 更新预约状态为等待支付
//         // updatedAt 由MyBatis-Plus自动填充

//         boolean reservationUpdated = reservationService.updateById(reservation);
//         if (!reservationUpdated) {
//             log.error("更新预约记录 {} 失败（关联订单ID {}），事务将回滚。", reservationId, order.getId());
//             // @Transactional 会处理回滚
//             throw new RuntimeException("更新预约信息失败，订单创建已回滚");
//         }
//         log.info("预约记录 {} 已成功关联订单ID {} 并更新状态为 AWAITING_PAYMENT", reservationId, order.getId());

//         // 5. 返回创建的订单对象
//         return order;
//     }

//     @Override
//     @Transactional(readOnly = true) // 查询操作，标记为只读事务，可能提升性能
//     public List<OrderViewDTO> getUserOrdersWithDetails(Long userId) {
//         // 1. 查询该用户的基本订单列表
//         List<Order> orders = this.lambdaQuery()
//                 .eq(Order::getUserId, userId)
//                 .orderByDesc(Order::getCreatedAt) // 按创建时间降序
//                 .list();

//         // 2. 准备用于存储结果的 DTO 列表
//         List<OrderViewDTO> orderViewDTOs = new ArrayList<>();

//         // 3. 遍历订单，查询并填充附加信息
//         for (Order order : orders) {
//             OrderViewDTO dto = new OrderViewDTO();
//             BeanUtils.copyProperties(order, dto); // 复制基础属性 (order -> dto)

//             // 尝试获取关联信息
//             if (order.getReservationId() != null) {
//                 // 使用 Optional 处理可能为 null 的情况，避免空指针
//                 Optional<Reservation> reservationOpt = Optional
//                         .ofNullable(reservationService.getById(order.getReservationId()));

//                 if (reservationOpt.isPresent()) {
//                     Reservation reservation = reservationOpt.get();

//                     // 获取宠物名称
//                     Optional.ofNullable(reservation.getPetId())
//                             .flatMap(petId -> Optional.ofNullable(petService.getById(petId)))
//                             .ifPresent(pet -> dto.setPetName(pet.getName()));

//                     // 获取服务名称
//                     Optional.ofNullable(reservation.getServiceId())
//                             .flatMap(serviceId -> Optional.ofNullable(serviceItemService.getById(serviceId)))
//                             .ifPresent(service -> dto.setServiceName(service.getName()));

//                     // 获取预约的服务开始时间 (并格式化为字符串)
//                     Optional.ofNullable(reservation.getServiceStartTime())
//                             .ifPresent(startTime -> dto.setReservationServiceStartTime(
//                                     startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // 转换为标准字符串格式
//                             ));
//                 } else {
//                     log.warn("订单 {} 关联的预约 ID {} 未找到对应的预约记录。", order.getId(), order.getReservationId());
//                 }
//             } else {
//                 log.warn("订单 {} 没有关联的预约 ID。", order.getId());
//                 // 根据业务逻辑，这里可能需要填充默认值或标记为特定类型订单
//                 dto.setPetName("N/A");
//                 dto.setServiceName("N/A");
//             }

//             orderViewDTOs.add(dto);
//         }

//         return orderViewDTOs;
//     }

//     @Override
//     @Transactional
//     public void cancelUserOrder(Long orderId, Long userId) { // 返回 void，失败时抛异常
//         log.info("Service: User {} attempting to cancel order ID: {}", userId, orderId);
//         Order order = this.getById(orderId);

//         if (order == null) {
//             log.warn("Service: Attempt to cancel non-existent order ID: {}", orderId);
//             throw new RuntimeException("订单不存在，无法取消。");
//         }
//         if (!order.getUserId().equals(userId)) {
//             log.warn("Service: User {} attempted to cancel order {} owned by user {}.", userId, orderId,
//                     order.getUserId());
//             throw new SecurityException("您无权取消此订单。");
//         }
//         String currentStatus = order.getStatus();
//         log.info("Service: Current status of order {} is '{}'", orderId, currentStatus);
//         if (!("pending".equalsIgnoreCase(currentStatus) || "待支付".equalsIgnoreCase(currentStatus))) {
//             log.warn("Service: Order {} is in status '{}' and cannot be cancelled by user.", orderId, currentStatus);
//             throw new IllegalStateException("当前订单状态不允许取消。");
//         }
//         order.setStatus("已取消");
//         boolean updated = this.updateById(order);
//         if (!updated) {
//             log.error("Service: Failed to update order {} status to '已取消' in DB.", orderId);
//             throw new RuntimeException("数据库操作失败，取消订单失败。");
//         }
//         log.info("Service: Order ID: {} has been successfully cancelled by user {}.", orderId, userId);

//     }

//     @Override // 确保添加 @Override 注解
//     @Transactional // 通常这类操作需要事务管理
//     public void completeOrderByProvider(Long orderId, Long providerId) {
//         log.info("服务商 {} 尝试完成订单 ID: {}", providerId, orderId);

//         if (orderId == null || providerId == null) {
//             throw new IllegalArgumentException("订单ID和服务商ID都不能为空");
//         }

//         Order order = this.getById(orderId);

//         // 1. 校验订单是否存在
//         if (order == null) {
//             log.warn("尝试完成不存在的订单，订单ID: {}", orderId);
//             throw new RuntimeException("订单不存在，无法完成。");
//         }

//         // 2. 校验服务商是否有权操作此订单
//         // 这通常需要通过订单关联的 Reservation，再关联到 ServiceItem，再找到 ServiceItem 的 providerId
//         Reservation reservation = reservationService.getById(order.getReservationId());
//         if (reservation == null) {
//             log.error("订单 {} 关联的预约不存在，无法验证服务商权限。", orderId);
//             throw new RuntimeException("关联预约信息缺失，操作失败。");
//         }
//         ServiceItem serviceItem = serviceItemService.getById(reservation.getServiceId());
//         if (serviceItem == null) {
//             log.error("订单 {} 关联的服务项不存在，无法验证服务商权限。", orderId);
//             throw new RuntimeException("关联服务项信息缺失，操作失败。");
//         }

//         if (!Objects.equals(serviceItem.getProviderId(), providerId)) {
//             log.warn("服务商 {} 无权完成订单 {}，该订单属于服务商 {}", providerId, orderId, serviceItem.getProviderId());
//             throw new SecurityException("您无权完成此订单。");
//         }

//         // 3. 检查订单当前状态是否允许被服务商完成
//         // 例如，订单可能需要是 "已支付" (PAID) 或 "服务中" (SERVICE_IN_PROGRESS) 状态
//         String currentStatus = order.getStatus();
//         // 示例：假设订单必须是 "已支付" 或 "服务中" 才能被完成
//         if (!("已支付".equalsIgnoreCase(currentStatus) || "服务中".equalsIgnoreCase(currentStatus) /* 根据你的实际状态调整 */ )) {
//             log.warn("订单 {} 当前状态为 '{}'，服务商无法将其标记为完成。", orderId, currentStatus);
//             throw new IllegalStateException("当前订单状态不允许标记为完成。");
//         }

//         // 4. 更新订单状态为 "已完成" (或你定义的其他完成状态)
//         order.setStatus("已完成"); // 假设 "已完成" 是你的状态值
//         // 或者如果 Order 实体有 paymentTime 或 completedTime 字段，也可以在这里设置

//         boolean updated = this.updateById(order);

//         if (!updated) {
//             log.error("数据库更新订单 {} 状态为 '已完成' 失败。", orderId);
//             throw new RuntimeException("数据库操作失败，完成订单失败。");
//         }

//         // 5. (可选) 触发后续操作，例如通知用户、计算服务商结算等

//         log.info("订单 ID: {} 已被服务商 {} 成功标记为完成。", orderId, providerId);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public IPage<OrderAdminViewDTO> getAdminOrdersPage(Page<Order> page, String status) {
//         log.info("管理员请求订单列表，页码: {}, 每页数量: {}, 状态筛选: {}", page.getCurrent(), page.getSize(), status);

//         // 调用 Mapper 进行自定义分页查询 (需要在 OrderMapper.xml 和 OrderMapper.java 中定义)
//         // 这里我们先用一个简化的方式，先分页查询 Order，再逐个填充 DTO
//         // 性能更好的方式是直接在 Mapper XML 中写 JOIN 查询并返回 DTO
//         LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
//         if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
//             wrapper.eq(Order::getStatus, status.toUpperCase());
//         }
//         wrapper.orderByDesc(Order::getCreatedAt); // 按创建时间降序

//         IPage<Order> orderPage = this.page(page, wrapper); // MyBatis-Plus 自带的分页查询

//         // 将 IPage<Order> 转换为 IPage<OrderAdminViewDTO>
//         List<OrderAdminViewDTO> dtoList = orderPage.getRecords().stream().map(order -> {
//             OrderAdminViewDTO dto = new OrderAdminViewDTO();
//             BeanUtils.copyProperties(order, dto); // 复制基础订单属性

//             // 填充关联信息
//             if (order.getReservationId() != null) {
//                 Reservation reservation = reservationService.getById(order.getReservationId());
//                 if (reservation != null) {
//                     dto.setPetId(reservation.getPetId());
//                     dto.setServiceId(reservation.getServiceId());
//                     dto.setReservationServiceStartTime(reservation.getServiceStartTime()); // 或 reservationStartDate

//                     if (reservation.getPetId() != null) {
//                         Pets pet = petService.getById(reservation.getPetId());
//                         if (pet != null) dto.setPetName(pet.getName());
//                     }
//                     if (reservation.getServiceId() != null) {
//                         com.zsh.petsystem.entity.ServiceItem service = serviceItemService.getById(reservation.getServiceId());
//                         if (service != null) dto.setServiceName(service.getName());
//                     }
//                 }
//             }
//             if (order.getUserId() != null) {
//                 Users user = userService.getById(order.getUserId());
//                 if (user != null) dto.setUserName(user.getName());
//             }
//             return dto;
//         }).collect(Collectors.toList());

//         // 创建一个新的 Page 对象来承载 DTO 列表和分页信息
//         Page<OrderAdminViewDTO> dtoPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
//         dtoPage.setRecords(dtoList);

//         log.info("管理员订单列表查询完成，返回 {} 条记录，总计 {} 条。", dtoList.size(), dtoPage.getTotal());
//         return dtoPage;
//     }
// }

package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.OrderAdminViewDTO;
import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.dto.OrderFromReservationDTO;
import com.zsh.petsystem.dto.OrderViewDTO;
import com.zsh.petsystem.entity.Order;
import com.zsh.petsystem.entity.Pets;
import com.zsh.petsystem.entity.Reservation;
import com.zsh.petsystem.entity.ServiceItem;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.mapper.OrderMapper;
import com.zsh.petsystem.service.OrderService;
import com.zsh.petsystem.service.PetService;
import com.zsh.petsystem.service.ReservationService;
import com.zsh.petsystem.service.ServiceItemService;
import com.zsh.petsystem.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PetService petService;

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private UserService userService;

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
        order.setUserId(reservation.getUserId());
        order.setReservationId(dto.getReservationId());
        order.setAmount(dto.getAmount());
        order.setStatus("待支付");
        order.setCreatedAt(LocalDateTime.now());
        this.save(order);
        log.info("直接通过DTO创建订单成功，预约ID: {}, 用户ID: {}", dto.getReservationId(), reservation.getUserId());
        return order;
    }

    @Override
    @Transactional
    public Order createOrderFromReservation(OrderFromReservationDTO dto, Long userId) {
        if (dto == null || dto.getReservationId() == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        Long reservationId = dto.getReservationId();
        log.info("尝试从预约创建订单，预约ID: {}, 用户ID: {}", reservationId, userId);

        Reservation reservation = reservationService.getById(reservationId);
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
        List<String> allowedStatus = List.of("CONFIRMED", "PENDING_CONFIRMATION");
        if (reservation.getStatus() == null || !allowedStatus.contains(reservation.getStatus().toUpperCase())) {
            log.error("无法创建订单，预约 {} 的状态为 {}，不在允许的状态列表中: {}", reservationId, reservation.getStatus(), allowedStatus);
            throw new IllegalStateException("当前预约状态 (" + reservation.getStatus() + ") 不允许创建订单");
        }
        if (reservation.getAmount() == null || reservation.getAmount().doubleValue() <= 0) {
            log.error("无法创建订单，预约 {} 的金额无效: {}", reservationId, reservation.getAmount());
            throw new IllegalStateException("预约金额无效，无法创建订单");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setReservationId(reservationId);
        order.setAmount(reservation.getAmount());
        order.setStatus("待支付");
        order.setCreatedAt(LocalDateTime.now());

        boolean orderSaved = this.save(order);
        if (!orderSaved || order.getId() == null) {
            log.error("保存订单到数据库失败，预约ID: {}", reservationId);
            throw new RuntimeException("订单创建数据库操作失败");
        }
        log.info("订单创建成功。订单ID: {}, 关联预约ID: {}", order.getId(), reservationId);

        reservation.setOrderId(order.getId());
        reservation.setStatus("AWAITING_PAYMENT");
        boolean reservationUpdated = reservationService.updateById(reservation);
        if (!reservationUpdated) {
            log.error("更新预约记录 {} 失败（关联订单ID {}），事务将回滚。", reservationId, order.getId());
            throw new RuntimeException("更新预约信息失败，订单创建已回滚");
        }
        log.info("预约记录 {} 已成功关联订单ID {} 并更新状态为 AWAITING_PAYMENT", reservationId, order.getId());

        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderViewDTO> getUserOrdersWithDetails(Long userId) {
        List<Order> orders = this.lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt)
                .list();
        List<OrderViewDTO> orderViewDTOs = new ArrayList<>();

        for (Order order : orders) {
            OrderViewDTO dto = new OrderViewDTO();
            BeanUtils.copyProperties(order, dto);
            if (order.getReservationId() != null) {
                Optional<Reservation> reservationOpt = Optional
                        .ofNullable(reservationService.getById(order.getReservationId()));
                if (reservationOpt.isPresent()) {
                    Reservation reservation = reservationOpt.get();
                    Optional.ofNullable(reservation.getPetId())
                            .flatMap(petId -> Optional.ofNullable(petService.getById(petId)))
                            .ifPresent(pet -> dto.setPetName(pet.getName()));
                    Optional.ofNullable(reservation.getServiceId())
                            .flatMap(serviceId -> Optional.ofNullable(serviceItemService.getById(serviceId)))
                            .ifPresent(serviceItem -> dto.setServiceName(serviceItem.getName()));
                    Optional.ofNullable(reservation.getServiceStartTime())
                            .ifPresent(startTime -> dto.setReservationServiceStartTime(
                                    startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
                }
            }
            orderViewDTOs.add(dto);
        }

        return orderViewDTOs;
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
        if (!("pending".equalsIgnoreCase(currentStatus) || "待支付".equalsIgnoreCase(currentStatus))) {
            throw new IllegalStateException("当前订单状态不允许取消。");
        }
        order.setStatus("已取消");
        if (!this.updateById(order))
            throw new RuntimeException("数据库操作失败，取消订单失败。");
    }

    @Override
    @Transactional
    public void completeOrderByProvider(Long orderId, Long providerId) {
        if (orderId == null || providerId == null)
            throw new IllegalArgumentException("订单ID和服务商ID都不能为空");
        Order order = this.getById(orderId);
        if (order == null)
            throw new RuntimeException("订单不存在，无法完成。");
        Reservation reservation = reservationService.getById(order.getReservationId());
        if (reservation == null)
            throw new RuntimeException("关联预约信息缺失，操作失败。");
        ServiceItem serviceItem = serviceItemService.getById(reservation.getServiceId());
        if (!Objects.equals(serviceItem.getProviderId(), providerId))
            throw new SecurityException("您无权完成此订单。");
        String currentStatus = order.getStatus();
        if (!("已支付".equalsIgnoreCase(currentStatus) || "服务中".equalsIgnoreCase(currentStatus))) {
            throw new IllegalStateException("当前订单状态不允许标记为完成。");
        }
        order.setStatus("已完成");
        if (!this.updateById(order))
            throw new RuntimeException("数据库操作失败，完成订单失败。");
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<OrderAdminViewDTO> getAdminOrdersPage(int pageNum, int pageSize, String status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
            wrapper.eq(Order::getStatus, status.toUpperCase());
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        IPage<Order> orderPage = this.page(page, wrapper);
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
        Page<OrderAdminViewDTO> dtoPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
}
