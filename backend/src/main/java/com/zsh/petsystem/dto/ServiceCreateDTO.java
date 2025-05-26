package com.zsh.petsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
// 如果你的 ServiceItem 中有枚举类型，例如服务类型，也需要在这里考虑
// import com.zsh.petsystem.entity.ServiceItem.ServiceType; // 假设 ServiceItem 中有此枚举

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Data
public class ServiceCreateDTO {

  @NotBlank(message = "服务名称不能为空")
  @Size(max = 100, message = "服务名称长度不能超过100个字符")
  private String name;

  @Size(max = 1000, message = "服务描述长度不能超过1000个字符")
  private String description;

  @NotNull(message = "价格不能为空")
  @DecimalMin(value = "0.01", message = "价格必须大于0")
  private BigDecimal price;

  @NotNull(message = "服务时长不能为空")
  @Min(value = 1, message = "服务时长至少为1分钟")
  private Integer duration; // 服务时长（分钟）

  @Min(value = 0, message = "每日可预约次数不能为负数")
  private Integer dailyCapacity; // 每日可预约次数 (0 可能表示无限制，或根据业务逻辑处理)

  // 以下是服务要求的相关字段，根据 ServiceItem 实体类来定义
  // 这些字段在创建时可能是可选的，或者有默认值
  @Size(max = 255, message = "疫苗要求长度不能超过255个字符")
  private String requiredVaccinations;

  private Boolean requiresNeutered; // 是否要求宠物已绝育

  @Min(value = 0, message = "最小年龄不能为负数")
  private Integer minAge; // 最小年龄要求（岁）

  @Min(value = 0, message = "最大年龄不能为负数")
  private Integer maxAge; // 最大年龄要求（岁）

  @Size(max = 255, message = "性格要求长度不能超过255个字符")
  private String temperamentRequirements;

  @Size(max = 255, message = "不接受品种说明长度不能超过255个字符")
  private String prohibitedBreeds;

  // 假设 ServiceItem.java 中的字段与 Pet.java 类似
  // private String species; // 允许的服务宠物类型，例如 "Dog", "Cat", 或 "Dog,Cat"
  // private String energyLevel; // 例如 "High", "Moderate", "Low"

  // 注意：providerId 通常由后端根据当前登录的服务商自动设置，不由 DTO 传入。
  // reviewStatus, rejectionReason, createdAt, updatedAt 等字段也通常由后端处理。
}