package com.zsh.petsystem.dto;

import lombok.Data;

@Data
public class PetUpdateDTO {
    private Long id;
    private String name;
    private String species;
    private Integer age;
    private String gender;
    private String photoUrl;

}
