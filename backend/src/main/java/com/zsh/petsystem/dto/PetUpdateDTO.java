package com.zsh.petsystem.dto;

import lombok.Data;
// import jakarta.validation.constraints.*;

@Data
public class PetUpdateDTO {
    // @NotNull // ID 在更新时是必须的
    private Long id;

    // 其他所有允许用户修改的字段
    // @Size(max = 100)
    private String name;
    // @Pattern(regexp = "^(Dog|Cat)$")
    private String species;
    // @Pattern(regexp = "^(Male|Female)$")
    private String gender;
    // @Min(0)
    private Integer ageInYears;
    private Integer ageInMonths;
    private Double weight;
    private Boolean neutered;
    private String photoUrl;
    private String description;
    private String vaccinationInfo;
    private String allergies;
    private String medicalNotes;
    private String temperament;
    // @Pattern(regexp = "^(High|Moderate|Low)$")
    private String energyLevel;
    private String feedingSchedule;
    private String feedingInstructions;
    private String pottyBreakFrequency;
    private String pottyBreakInstructions;
    private String aloneTimeTolerance;

}