package com.zsh.petsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.zsh.petsystem.dto.ReservationDTO;
import com.zsh.petsystem.model.Reservation;

import java.util.List;

public interface ReservationService extends IService<Reservation> {
    Reservation create(ReservationDTO dto, Long userId);
    List<Reservation> getByUserId(Long userId);
    void cancel(Long id);
}