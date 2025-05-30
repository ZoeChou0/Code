package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 引入 LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.ReviewDTO;
import com.zsh.petsystem.entity.Order; // 引入 Order 实体
import com.zsh.petsystem.entity.Reservation;
import com.zsh.petsystem.entity.Review;
import com.zsh.petsystem.mapper.ReservationMapper;
import com.zsh.petsystem.mapper.ReviewMapper;
import com.zsh.petsystem.service.OrderService; // 引入 OrderService
import com.zsh.petsystem.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ReviewServiceImpl
        extends ServiceImpl<ReviewMapper, Review>
        implements ReviewService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired // 注入 OrderService
    private OrderService orderService;

    // 统一订单状态常量，与数据库和 OrderServiceImpl 中更新订单状态时使用的值一致
    public static final String ORDER_STATUS_COMPLETED = "completed";

    @Override
    @Transactional
    public boolean submitReview(Long userId, ReviewDTO dto) {
        log.info("User {} attempting to submit review for reservationId: {}", userId, dto.getReservationId());

        // 1. 查询预约信息
        Reservation reservation = reservationMapper.selectById(dto.getReservationId());

        // 2. 校验预约是否存在且属于当前用户
        if (reservation == null) {
            log.warn("Review submission failed: Reservation not found for ID: {}", dto.getReservationId());
            throw new RuntimeException("预约不存在，无法评价");
        }
        if (!reservation.getUserId().equals(userId)) {
            log.warn("Review submission failed: User {} does not own reservation ID: {}", userId,
                    dto.getReservationId());
            throw new RuntimeException("您只能评价自己的预约");
        }

        // 3. 查询关联的订单信息
        if (reservation.getOrderId() == null) {
            log.warn("Review submission failed: Reservation ID {} is not associated with any order.",
                    dto.getReservationId());
            throw new RuntimeException("此预约没有对应的订单信息，无法评价");
        }
        Order order = orderService.getById(reservation.getOrderId());
        if (order == null) {
            log.warn("Review submission failed: Order not found for order ID: {} (associated with reservation ID: {})",
                    reservation.getOrderId(), dto.getReservationId());
            throw new RuntimeException("关联的订单不存在，无法评价");
        }

        // 4. 校验订单状态是否为“已完成”
        log.info("Order ID: {} (from reservation ID: {}) current status is '{}'",
                order.getId(), dto.getReservationId(), order.getStatus());
        if (!ORDER_STATUS_COMPLETED.equalsIgnoreCase(order.getStatus())) { // 使用订单状态进行判断
            log.warn("Review submission failed: Order ID {} status is '{}', not '{}'. Cannot submit review.",
                    order.getId(), order.getStatus(), ORDER_STATUS_COMPLETED);
            throw new RuntimeException("仅已完成订单的服务可以评价"); // 修改提示信息
        }

        // 5. （可选）检查是否已评价过此预约/服务
        long existingReviewCount = this.lambdaQuery()
                .eq(Review::getUserId, userId)
                // 评价仍然是针对 service_item_id
                .eq(Review::getServiceItemId, reservation.getServiceId())
                .count();
        if (existingReviewCount > 0) {
            log.warn("User {} has already reviewed service item ID {} (from reservation ID: {})",
                    userId, reservation.getServiceId(), dto.getReservationId());
            throw new RuntimeException("您已经评价过此服务，请勿重复提交");
        }

        // 6. 创建并保存评价对象
        Review review = new Review();
        review.setUserId(userId);
        review.setServiceItemId(reservation.getServiceId()); // 评价是针对服务项的
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        // review.setCreatedAt(LocalDateTime.now()); // 通常由 MybatisPlus 自动填充

        boolean saved = this.save(review);
        if (saved) {
            log.info(
                    "Review successfully submitted by userId: {} for serviceItemId: {} (via reservationId: {}) (Review ID: {})",
                    userId, reservation.getServiceId(), dto.getReservationId(), review.getId());
        } else {
            log.error("Failed to save review for userId: {} and reservationId: {}", userId, dto.getReservationId());
            throw new RuntimeException("评价提交失败，请稍后再试");
        }
        return saved;
    }

    @Override
    public List<Review> getReviewsByServiceItem(Long serviceItemId) {
        return this.lambdaQuery().eq(Review::getServiceItemId, serviceItemId)
                .orderByDesc(Review::getCreatedAt)
                .list();
    }

    @Override
    public Double getAverageRating(Long serviceItemId) {
        List<Review> reviews = this.getReviewsByServiceItem(serviceItemId);
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    @Override
    public Review findMyReviewForReservation(Long reservationId, Long userId) {
        log.info("Finding review for reservationId: {} and userId: {}", reservationId, userId);
        // 首先，我们需要通过 reservationId 找到 serviceItemId，因为 Review 表是按 serviceItemId 存储的
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            log.warn("Cannot find review: Reservation with ID {} not found.", reservationId);
            return null; // 或抛出异常
        }
        // 校验这个预约是否属于该用户 (可选，但推荐)
        if (!reservation.getUserId().equals(userId)) {
            log.warn("User {} attempted to find review for reservation {} not belonging to them.", userId,
                    reservationId);
            return null; // 或抛出权限异常
        }

        // 根据 userId 和 serviceItemId 查找评价
        // 假设一个用户对一个服务项（通过一次预约）只能评价一次
        return this.lambdaQuery()
                .eq(Review::getUserId, userId)
                .eq(Review::getServiceItemId, reservation.getServiceId())
                .one(); // one() 会返回单个记录，如果找到多个会报错，如果没找到返回null
    }
}