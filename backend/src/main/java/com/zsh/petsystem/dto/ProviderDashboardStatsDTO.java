// package com.zsh.petsystem.dto;

// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// import java.math.BigDecimal; // 如果需要统计金额

// @Data // Lombok 注解，自动生成 getter, setter, toString, equals, hashCode
// @NoArgsConstructor // Lombok 注解，自动生成无参构造函数
// @AllArgsConstructor // Lombok 注解，自动生成全参构造函数
// public class ProviderDashboardStatsDTO {

//   private Long pendingReservationsCount; // 待处理预约数量 (使用 Long 避免潜在的 int 溢出，虽然通常不会)
//   private Long activeServicesCount; // 上架中（已批准）的服务数量
//   private Long completedOrdersThisMonth; // 本月已完成订单数量 (示例，需要后端逻辑支持)
//   private BigDecimal monthlyRevenue; // 本月收入 (示例，需要后端逻辑支持)

//   // 你可以根据需要添加更多统计字段
//   // 例如：
//   // private Integer totalPetsServed;
//   // private Double averageRating;
// }

package com.zsh.petsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List; // For recent reviews and chart data
import java.util.Map; // For distribution data

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDashboardStatsDTO { // Renamed from ProviderStatisticsDTO for consistency if extending
  // Existing stats (from your current DTO)
  private Long pendingReservationsCount;
  private Long activeServicesCount;

  // New Stats from your template
  private Long monthlyOrders; // 本月订单
  private Double orderTrend; // 订单趋势 (e.g., percentage change from last month)
  private BigDecimal monthlyIncome; // 本月收入
  private Double incomeTrend; // 收入趋势
  private Double averageRating; // 平均评分
  private Long activeCustomers; // 活跃客户
  private Double customerTrend; // 客户趋势

  // Data for charts (example structure, can be more specific)
  // private Map<String, List<IncomeChartDataPoint>> incomeChartData; // Key:
  // "week", "month", "year"
  // For simplicity, you might have the controller return specific period data
  // based on a query param
  private List<ChartDataPoint> incomeTrendData; // e.g., [{label: "周一", value: 3200}, ...]
  private List<ChartDataPoint> serviceTypeDistributionData; // e.g., [{name: "宠物美容", value: 45}, ...]

  // Data for reviews
  private Map<Integer, Long> ratingDistribution; // Key: 1-5 (star), Value: count
  private Long totalReviews;
  private List<RecentReviewDTO> recentReviews; // A new DTO for recent reviews
}

// Helper DTOs for chart data and reviews
@Data
@NoArgsConstructor
@AllArgsConstructor
class ChartDataPoint {
  private String label; // For X-axis or legend name
  private Number value; // For Y-axis or pie value
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class RecentReviewDTO {
  private Long id;
  private String userName;
  private Integer rating;
  private String date; // Formatted date string
  private String comment;
  // Potentially petName, serviceName
}