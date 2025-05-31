package com.zsh.petsystem.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataPointDTO {
  private String name; // 对应 ECharts 饼图的 name
  private Long value; // 对应 ECharts 饼图的 value (数量)
}