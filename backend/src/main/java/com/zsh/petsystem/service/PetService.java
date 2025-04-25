package com.zsh.petsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsh.petsystem.dto.PetUpdateDTO;
import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.model.Pets;

import java.util.List;

public interface PetService extends IService<Pets> {
    Pets addPet(Pets pet, Users user);
    List<Pets> getMyPets(Users user);
    Pets updatePet(PetUpdateDTO dto);
    void deletePet(Long id);
}