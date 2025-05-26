package com.zsh.petsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.zsh.petsystem.dto.ReservationDTO;
import com.zsh.petsystem.entity.Reservation;

import java.util.List;

public interface ReservationService extends IService<Reservation> {
    Reservation create(ReservationDTO dto, Long userId);

    List<Reservation> getByUserId(Long userId);

    void cancel(Long id);

    List<Reservation> getReservationsForProvider(Long providerId, String status);

    Reservation confirmReservationByProvider(Long reservationId, Long providerId);

    Reservation rejectReservationByProvider(Long reservationId, Long providerId, String reason);

    Reservation completeReservationByProvider(Long reservationId, Long providerId);

    long countPendingReservationsForProvider(Long providerId); // 新增统计
}