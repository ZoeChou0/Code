package com.zsh.petsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
// 导入新的 DTO
import com.zsh.petsystem.dto.PetCreateDTO;
import com.zsh.petsystem.dto.PetUpdateDTO;
import com.zsh.petsystem.entity.Pets;
import com.zsh.petsystem.entity.Users;

import java.util.List;

public interface PetService extends IService<Pets> {

    Pets addPet(PetCreateDTO dto, Users user);

    List<Pets> getMyPets(Users user);

    Pets updatePet(PetUpdateDTO dto, Long currentUserId);

    // 修改 deletePet，添加 currentUserId 用于权限验证
    boolean deletePet(Long id, Long currentUserId);
}