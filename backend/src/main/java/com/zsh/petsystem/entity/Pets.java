package com.zsh.petsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pets")
public class Pets {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    // --- Basic Info ---
    private String name; // Pet's name

    // @Pattern(regexp = "^(Dog|Cat)$", message = "Species must be Dog or Cat") //
    // Example validation
    private String species; // Modify: Keep as String, enforce "Dog" or "Cat" in validation/frontend

    // @Pattern(regexp = "^(Male|Female)$", message = "Gender must be Male or
    // Female") // Example validation
    private String gender; // Modify: Keep as String, enforce "Male" or "Female"
    private Integer ageInYears;
    // @Min(0) // Example validation
    private Integer ageInMonths; // Modify: Store age in months for better precision

    private Double weight; // Add: Pet's weight (e.g., in kg or lbs - be consistent)
    // @Size(max = 5) // Example for weight unit if stored separately
    // private String weightUnit; // Optional: Add a field for the unit (kg/lbs)

    private Boolean neutered; // Keep: Is the pet neutered?

    private String photoUrl; // Keep: URL for the pet's photo

    // --- Detailed Info ---
    private String description; // Add: General introduction/description

    // @Size(max = 500)
    private String vaccinationInfo; // Keep: Details about vaccinations

    // @Size(max = 500)
    private String allergies; // Keep: Known allergies

    // @Size(max = 1000)
    private String medicalNotes; // Keep: Known health issues/medical history

    // @Size(max = 500)
    private String temperament; // Keep: General temperament description

    // --- Care Info (based on screenshot) ---
    // @Size(max = 50) // Example
    private String energyLevel; // Add: e.g., "High", "Moderate", "Low"

    // @Size(max = 100) // Example
    private String feedingSchedule; // Add: e.g., "Morning", "Twice a day", "Free feeding"

    // @Size(max = 1000)
    private String feedingInstructions; // Add: Special instructions for feeding

    // @Size(max = 100) // Example
    private String pottyBreakFrequency; // Add: e.g., "Every 2 hours", "Every 4 hours"

    // @Size(max = 1000)
    private String pottyBreakInstructions; // Add: Special instructions for potty breaks

    // @Size(max = 100) // Example
    private String aloneTimeTolerance; // Add: e.g., "1 hour or less", "1-4 hours", "4-8 hours"

}