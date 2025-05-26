package com.zsh.petsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal; // 如果需要统计金额

@Data // Lombok 注解，自动生成 getter, setter, toString, equals, hashCode
@NoArgsConstructor // Lombok 注解，自动生成无参构造函数
@AllArgsConstructor // Lombok 注解，自动生成全参构造函数
public class ProviderDashboardStatsDTO {

  private Long pendingReservationsCount; // 待处理预约数量 (使用 Long 避免潜在的 int 溢出，虽然通常不会)
  private Long activeServicesCount; // 上架中（已批准）的服务数量
  private Long completedOrdersThisMonth; // 本月已完成订单数量 (示例，需要后端逻辑支持)
  private BigDecimal monthlyRevenue; // 本月收入 (示例，需要后端逻辑支持)

  // 你可以根据需要添加更多统计字段
  // 例如：
  // private Integer totalPetsServed;
  // private Double averageRating;
}