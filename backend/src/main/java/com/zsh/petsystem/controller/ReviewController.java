package com.zsh.petsystem.controller;

import com.zsh.petsystem.annotation.CurrentUser; // 导入 @CurrentUser
import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.ReviewDTO;
import com.zsh.petsystem.entity.Review;
import com.zsh.petsystem.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // 导入 HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List; // 确保 List 被导入

@RestController
@RequestMapping("/reviews")
@CrossOrigin
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 提交评价
     * 
     * @param dto           评价数据传输对象
     * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
     * @return ResponseEntity
     */
    @PostMapping("/submit")
    // 使用 @CurrentUser 替换 @RequestHeader 和手动解析
    public ResponseEntity<?> submit(@RequestBody ReviewDTO dto, @CurrentUser Long currentUserId) {
        // 1. 检查用户是否已登录 (Token 是否有效)
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录"); // 401 Unauthorized
        }

        // 2. 调用 Service 层提交评价
        try {
            // reviewService.submitReview 现在接收 currentUserId
            boolean success = reviewService.submitReview(currentUserId, dto); // 假设 submitReview 返回 boolean
            if (success) {
                return ResponseEntity.ok("评价提交成功");
            } else {
                // 如果 service 层返回 false 但没有抛异常 (虽然不太可能根据现有实现)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("评价提交失败");
            }
        } catch (RuntimeException e) {
            // 捕获 Service 层可能抛出的异常 (例如 "预约不存在...", "仅已完成...")
            // 根据异常信息返回更具体的错误状态码
            if (e.getMessage().contains("预约不存在") || e.getMessage().contains("不属于当前用户")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("评价提交失败: " + e.getMessage()); // 403 Forbidden 或
                                                                                                      // 404 Not Found
            } else if (e.getMessage().contains("仅已完成的服务可以评价")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("评价提交失败: " + e.getMessage()); // 400 Bad
                                                                                                        // Request
            } else {
                // 其他未知运行时异常
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("评价提交时发生错误: " + e.getMessage()); // 500
                                                                                                                     // Internal
                                                                                                                     // Server
                                                                                                                     // Error
            }
        }
    }

    /**
     * 根据服务项 ID 获取评价列表 (公开接口，无需修改)
     * 
     * @param serviceItemId 服务项 ID
     * @return ResponseEntity 包含评价列表
     */
    @GetMapping("/service/{serviceItemId}")
    public ResponseEntity<?> list(@PathVariable Long serviceItemId) {
        // 调用 reviewService.getReviewsByServiceItem
        // 注意：原始代码这里直接返回了 service 的结果，最好也包装一下
        try {
            List<Review> reviews = reviewService.getReviewsByServiceItem(serviceItemId); // 假设 Review 在这里需要导入
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            // 处理可能的异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取评价列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据服务项 ID 获取平均评分 (公开接口，无需修改)
     * 
     * @param serviceItemId 服务项 ID
     * @return ResponseEntity 包含平均分
     */
    @GetMapping("/service/{serviceItemId}/average")
    public ResponseEntity<?> average(@PathVariable Long serviceItemId) {
        // 调用 reviewService.getAverageRating
        // 同样，最好包装一下结果并处理异常
        try {
            Double averageRating = reviewService.getAverageRating(serviceItemId);
            // 可以考虑将结果包装在一个 Map 或 DTO 中，而不是直接返回 Double
            // return ResponseEntity.ok(Map.of("averageRating", averageRating));
            return ResponseEntity.ok(averageRating); // 保持和原来一致，直接返回 Double
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取平均评分失败: " + e.getMessage());
        }
    }

    /**
     * 根据预约ID获取用户对该预约的评价 (如果存在)
     * 
     * @param reservationId 预约ID
     * @param currentUserId 当前登录用户ID
     * @return ResponseEntity 包含评价信息或空结果
     */
    @GetMapping("/by-reservation/{reservationId}")
    @PreAuthorize("isAuthenticated()") // 需要用户登录
    public ResponseEntity<?> getReviewByReservationId(
            @PathVariable Long reservationId,
            @CurrentUser Long currentUserId) {
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户未认证"));
        }
        // ReviewService 中需要一个方法来根据 reservationId 和 userId 查找评价
        // 注意：Review 实体目前是通过 serviceItemId 和 userId 关联的。
        // 我们需要 ReviewService.findMyReviewForReservation(Long reservationId, Long
        // userId)
        Review review = reviewService.findMyReviewForReservation(reservationId, currentUserId);

        if (review != null) {
            return ResponseEntity.ok(Result.success(review));
        } else {
            // 没有找到评价，这不一定是错误，只是表示用户尚未评价
            return ResponseEntity.ok(Result.success(null, "尚未对该服务进行评价"));
        }
    }
}