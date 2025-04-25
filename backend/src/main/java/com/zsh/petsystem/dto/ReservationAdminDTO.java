package com.zsh.petsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationAdminDTO {
    private Long id;
    private String userName;
    private String petName;
    private String serviceName;
    private LocalDateTime reservationTime;
    private String status;
}