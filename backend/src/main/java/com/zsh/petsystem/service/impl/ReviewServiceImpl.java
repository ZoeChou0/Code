package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.dto.ReviewDTO;
import com.zsh.petsystem.mapper.ReservationMapper;
import com.zsh.petsystem.mapper.ReviewMapper;
import com.zsh.petsystem.model.Reservation;
import com.zsh.petsystem.model.Review;
import com.zsh.petsystem.service.ReservationService;
import com.zsh.petsystem.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class ReviewServiceImpl
        extends ServiceImpl<ReviewMapper, Review>
        implements ReviewService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public boolean submitReview(Long userId, ReviewDTO dto) {
        // 查询预约信息
        Reservation reservation = reservationMapper.selectById(dto.getReservationId());
        if (reservation == null || !reservation.getUserId().equals(userId)) {
            throw new RuntimeException("预约不存在或不属于当前用户");
        }

        if (!"已完成".equals(reservation.getStatus())) {
            throw new RuntimeException("仅已完成的服务可以评价");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setServiceItemId(reservation.getServiceId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());

        return this.save(review);
    }

    @Override
    public List<Review> getReviewsByServiceItem(Long serviceItemId) {
        return this.lambdaQuery().eq(Review::getServiceItemId, serviceItemId).list();
    }

    @Override
    public Double getAverageRating(Long serviceItemId) {
        List<Review> reviews = this.getReviewsByServiceItem(serviceItemId);
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

}