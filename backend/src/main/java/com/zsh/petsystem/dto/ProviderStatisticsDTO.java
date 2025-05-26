package com.zsh.petsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProviderStatisticsDTO {
  // 基础统计数据
  private Integer totalOrders; // 总订单数
  private Integer completedOrders; // 已完成订单数
  private Integer pendingOrders; // 待支付订单数
  private Integer cancelledOrders; // 已取消订单数
  private BigDecimal totalRevenue; // 总收入
  private BigDecimal averageRating; // 平均评分

  // 服务项目统计
  private Integer totalServices; // 服务项目总数
  private Integer activeServices; // 已上架服务数
  private Integer pendingReviewServices; // 待审核服务数

  // 时间维度统计（最近30天）
  private Integer last30DaysOrders; // 最近30天订单数
  private BigDecimal last30DaysRevenue; // 最近30天收入
  private Double last30DaysRating; // 最近30天平均评分

  // 评价统计
  private Integer totalReviews; // 总评价数
  private Integer fiveStarReviews; // 五星评价数
  private Integer fourStarReviews; // 四星评价数
  private Integer threeStarReviews; // 三星评价数
  private Integer twoStarReviews; // 二星评价数
  private Integer oneStarReviews; // 一星评价数
}