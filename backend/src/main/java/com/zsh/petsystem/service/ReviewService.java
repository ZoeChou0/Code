package com.zsh.petsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsh.petsystem.dto.ReviewDTO;
import com.zsh.petsystem.entity.Review;

import java.util.List;

public interface ReviewService extends IService<Review> {
    boolean submitReview(Long userId, ReviewDTO dto);

    List<Review> getReviewsByServiceItem(Long serviceItemId);

    Double getAverageRating(Long serviceItemId);

}
