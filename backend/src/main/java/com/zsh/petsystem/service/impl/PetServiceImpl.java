package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.mapper.PetMapper;
// 导入新的 DTO
import com.zsh.petsystem.dto.PetCreateDTO;
import com.zsh.petsystem.dto.PetUpdateDTO;
import com.zsh.petsystem.entity.Pets;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.PetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 建议添加事务注解

import java.util.List;
import java.util.Objects; // 导入 Objects

@Service
public class PetServiceImpl
        extends ServiceImpl<PetMapper, Pets>
        implements PetService {

    @Override
    @Transactional // 添加事务管理
    public Pets addPet(PetCreateDTO dto, Users user) {
        Pets pet = new Pets();
        // 将 DTO 的属性复制到实体
        pet.setName(dto.getName());
        pet.setSpecies(dto.getSpecies());
        pet.setGender(dto.getGender());
        pet.setAgeInYears(dto.getAgeInYears());
        pet.setAgeInMonths(dto.getAgeInMonths());
        pet.setWeight(dto.getWeight());
        pet.setNeutered(dto.getNeutered());
        pet.setPhotoUrl(dto.getPhotoUrl());
        pet.setDescription(dto.getDescription());
        pet.setVaccinationInfo(dto.getVaccinationInfo());
        pet.setAllergies(dto.getAllergies());
        pet.setMedicalNotes(dto.getMedicalNotes());
        pet.setTemperament(dto.getTemperament());
        pet.setEnergyLevel(dto.getEnergyLevel());
        pet.setFeedingSchedule(dto.getFeedingSchedule());
        pet.setFeedingInstructions(dto.getFeedingInstructions());
        pet.setPottyBreakFrequency(dto.getPottyBreakFrequency());
        pet.setPottyBreakInstructions(dto.getPottyBreakInstructions());
        pet.setAloneTimeTolerance(dto.getAloneTimeTolerance());

        // 设置 userId
        pet.setUserId(user.getId());

        // TODO: 在这里可以添加后端的数据验证逻辑，例如检查 species 和 gender 的值

        boolean saved = this.save(pet); // this.save() 返回 boolean
        if (!saved) {
            throw new RuntimeException("添加宠物失败");
        }
        // 返回保存后的实体，它现在应该包含由数据库生成的 id
        return pet;
    }

    @Override
    public List<Pets> getMyPets(Users user) {
        // 可以简化 LambdaQueryWrapper
        return this.lambdaQuery()
                .eq(Pets::getUserId, user.getId())
                .list();
    }

    @Override
    @Transactional // 添加事务管理
    public Pets updatePet(PetUpdateDTO dto, Long currentUserId) {
        // 1. 检查 DTO 和 ID
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("更新请求无效，缺少宠物 ID");
        }

        // 2. 获取现有宠物
        Pets existing = this.getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("宠物不存在 (ID: " + dto.getId() + ")");
        }

        // 3. **权限验证**: 确保当前用户是宠物的主人
        if (!Objects.equals(existing.getUserId(), currentUserId)) {
            // 可以抛出更具体的异常，例如 AccessDeniedException，或者由 Controller 处理返回 403
            throw new RuntimeException("无权修改不属于自己的宠物信息");
        }

        // 4. 更新实体的属性 (只更新 DTO 中包含的字段)
        // 注意：如果允许部分更新，需要检查 DTO 字段是否为 null
        // 如果要求必须传递所有字段，则可以直接设置
        existing.setName(dto.getName());
        existing.setSpecies(dto.getSpecies());
        existing.setGender(dto.getGender());
        existing.setAgeInYears(dto.getAgeInYears());
        existing.setAgeInMonths(dto.getAgeInMonths());
        existing.setWeight(dto.getWeight());
        existing.setNeutered(dto.getNeutered());
        existing.setPhotoUrl(dto.getPhotoUrl()); // 允许更新照片 URL
        existing.setDescription(dto.getDescription());
        existing.setVaccinationInfo(dto.getVaccinationInfo());
        existing.setAllergies(dto.getAllergies());
        existing.setMedicalNotes(dto.getMedicalNotes());
        existing.setTemperament(dto.getTemperament());
        existing.setEnergyLevel(dto.getEnergyLevel());
        existing.setFeedingSchedule(dto.getFeedingSchedule());
        existing.setFeedingInstructions(dto.getFeedingInstructions());
        existing.setPottyBreakFrequency(dto.getPottyBreakFrequency());
        existing.setPottyBreakInstructions(dto.getPottyBreakInstructions());
        existing.setAloneTimeTolerance(dto.getAloneTimeTolerance());

        // TODO: 在这里可以添加后端的数据验证逻辑

        // 5. 执行更新
        boolean updated = this.updateById(existing);
        if (!updated) {
            throw new RuntimeException("更新宠物信息失败");
        }

        // 6. 返回更新后的实体
        return existing;
    }

    @Override
    @Transactional // 添加事务管理
    public boolean deletePet(Long id, Long currentUserId) {
        // 1. 获取宠物信息
        Pets petToDelete = this.getById(id);

        // 2. 检查宠物是否存在
        if (petToDelete == null) {
            // 可以选择抛出异常或直接返回 false
            // throw new RuntimeException("未找到 ID 为 " + id + " 的宠物");
            return false;
        }

        // 3. **权限验证**: 确保当前用户是宠物的主人
        if (!Objects.equals(petToDelete.getUserId(), currentUserId)) {
            // throw new RuntimeException("无权删除不属于自己的宠物信息");
            return false; // 或者抛出异常，由 Controller 处理 403
        }

        // 4. 执行删除
        boolean removed = this.removeById(id);
        // TODO: 可能需要同时删除与该宠物相关的其他数据（例如预约、照片文件等）
        return removed;
    }
}