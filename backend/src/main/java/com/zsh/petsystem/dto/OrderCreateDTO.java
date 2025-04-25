package com.zsh.petsystem.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateDTO {
    private Long userId;
    private Long reservationId;
    private BigDecimal amount;
}
