package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.entity.Reservation;
import com.zsh.petsystem.entity.ServiceItem;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.entity.Pets;
import com.zsh.petsystem.service.ReservationService;
import com.zsh.petsystem.service.UserService; // 用于获取用户信息
import com.zsh.petsystem.service.PetService; // 用于获取宠物信息
import com.zsh.petsystem.service.ServiceItemService; // 用于获取服务信息
import com.zsh.petsystem.dto.ReservationProviderViewDTO; // 你需要创建这个 DTO
import com.zsh.petsystem.annotation.CurrentUser;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/provider/reservations") // 服务商预约管理基础路径
@CrossOrigin
@PreAuthorize("hasRole('PROVIDER')")
public class ProviderReservationController {

  @Autowired
  private ReservationService reservationService;
  @Autowired
  private UserService userService; // 用于获取预约用户名称
  @Autowired
  private PetService petService; // 用于获取宠物名称
  @Autowired
  private ServiceItemService serviceItemService; // 用于获取服务名称

  // 辅助方法：将 Reservation 转换为 ReservationProviderViewDTO
  private ReservationProviderViewDTO convertToProviderViewDto(Reservation reservation) {
    ReservationProviderViewDTO dto = new ReservationProviderViewDTO();
    BeanUtils.copyProperties(reservation, dto);

    if (reservation.getUserId() != null) {
      Users user = userService.getById(reservation.getUserId());
      if (user != null) {
        dto.setUserName(user.getName());
        dto.setUserPhone(user.getPhone()); // 可以选择是否暴露电话
      }
    }
    if (reservation.getPetId() != null) {
      Pets pet = petService.getById(reservation.getPetId());
      if (pet != null) {
        dto.setPetName(pet.getName());
      }
    }
    if (reservation.getServiceId() != null) {
      ServiceItem service = serviceItemService.getById(reservation.getServiceId());
      if (service != null) {
        dto.setServiceName(service.getName());
      }
    }
    return dto;
  }

  @GetMapping
  public ResponseEntity<?> getMyIncomingReservations(
      @CurrentUser Users currentUser,
      @RequestParam(required = false) String status) {
    if (currentUser == null || currentUser.getId() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户未登录"));
    }
    List<Reservation> reservations = reservationService.getReservationsForProvider(currentUser.getId(), status);

    // 转换为包含更多信息的 DTO
    List<ReservationProviderViewDTO> dtoList = reservations.stream()
        .map(this::convertToProviderViewDto)
        .collect(Collectors.toList());

    return ResponseEntity.ok(Result.success(dtoList));
  }

  @PutMapping("/{id}/confirm")
  public ResponseEntity<?> confirmReservation(@PathVariable Long id, @CurrentUser Users currentUser) {
    if (currentUser == null || currentUser.getId() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户未登录"));
    }
    try {
      Reservation updatedReservation = reservationService.confirmReservationByProvider(id, currentUser.getId());
      return ResponseEntity.ok(Result.success(convertToProviderViewDto(updatedReservation), "预约已确认"));
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed(e.getMessage()));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Result.failed(e.getMessage()));
    }
  }

  @PutMapping("/{id}/reject")
  public ResponseEntity<?> rejectReservation(
      @PathVariable Long id,
      @RequestBody(required = false) Map<String, String> payload, // 允许前端不传拒绝理由
      @CurrentUser Users currentUser) {
    if (currentUser == null || currentUser.getId() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户未登录"));
    }
    String reason = payload != null ? payload.get("rejectionReason") : null;
    try {
      Reservation updatedReservation = reservationService.rejectReservationByProvider(id, currentUser.getId(), reason);
      return ResponseEntity.ok(Result.success(convertToProviderViewDto(updatedReservation), "预约已拒绝"));
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed(e.getMessage()));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Result.failed(e.getMessage()));
    }
  }

  // 服务商标记服务完成
  @PutMapping("/{id}/complete")
  public ResponseEntity<?> completeReservation(@PathVariable Long id, @CurrentUser Users currentUser) {
    if (currentUser == null || currentUser.getId() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户未登录"));
    }
    try {
      Reservation updatedReservation = reservationService.completeReservationByProvider(id, currentUser.getId());
      return ResponseEntity.ok(Result.success(convertToProviderViewDto(updatedReservation), "服务已标记为完成"));
    } catch (SecurityException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed(e.getMessage()));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Result.failed(e.getMessage()));
    }
  }
}