package com.zsh.petsystem.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReservationDTO {
    private Long petId;
    private Long serviceId;
    private LocalDate reservationDate;
    private LocalDateTime reservationTime;

}