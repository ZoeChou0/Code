package com.zsh.petsystem.dto;

import lombok.Data;

@Data
public class PetCreateDTO {
  // 包含所有前端需要提交的字段，除了 id 和 userId
  // @NotBlank
  private String name;
  // @NotBlank @Pattern(regexp = "^(Dog|Cat)$")
  private String species;
  // @NotBlank @Pattern(regexp = "^(Male|Female)$")
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