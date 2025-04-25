package com.zsh.petsystem.dto; 

import lombok.Data;

@Data
public class ServiceItemUpdateDTO {

    private Long id;

    // 服务商修改字段 
    private String name;
    private String description;
    private Double price;
    private Integer duration; // 服务时长

}