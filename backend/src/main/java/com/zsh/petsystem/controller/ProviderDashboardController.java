package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.ProviderDashboardStatsDTO; // 你需要创建这个 DTO
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.OrderService;
import com.zsh.petsystem.service.ReservationService;
import com.zsh.petsystem.service.ServiceItemService;
import com.zsh.petsystem.annotation.CurrentUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/provider/dashboard")
@CrossOrigin
@PreAuthorize("hasRole('PROVIDER')")
public class ProviderDashboardController {

  @Autowired
  private ServiceItemService serviceItemService;

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private OrderService orderService; // 如果需要统计订单/收入

  @GetMapping("/stats")
  public ResponseEntity<?> getDashboardStats(@CurrentUser Users currentUser) {
    if (currentUser == null || currentUser.getId() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户未登录"));
    }
    Long providerId = currentUser.getId();

    // 实现统计逻辑
    long pendingReservationsCount = reservationService.countPendingReservationsForProvider(providerId);
    long activeServicesCount = serviceItemService.countActiveServicesForProvider(providerId);
    // long completedOrdersThisMonth =
    // orderService.countCompletedOrdersThisMonthForProvider(providerId);
    // BigDecimal monthlyRevenue =
    // orderService.getMonthlyRevenueForProvider(providerId);

    ProviderDashboardStatsDTO stats = new ProviderDashboardStatsDTO();
    stats.setPendingReservationsCount(pendingReservationsCount);
    stats.setActiveServicesCount(activeServicesCount);
    // stats.setCompletedOrdersThisMonth(completedOrdersThisMonth);
    // stats.setMonthlyRevenue(monthlyRevenue);

    return ResponseEntity.ok(Result.success(stats));
  }
}