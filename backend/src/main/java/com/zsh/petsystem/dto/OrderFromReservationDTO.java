package com.zsh.petsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor // 无参构造
@AllArgsConstructor // 全参构造
public class OrderFromReservationDTO {
  private Long reservationId; // 只需要预约ID
}