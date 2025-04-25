package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.mapper.PetMapper;
import com.zsh.petsystem.model.Pets;
import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.dto.PetUpdateDTO;

import com.zsh.petsystem.service.PetService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class PetServiceImpl
        extends ServiceImpl<PetMapper, Pets>
        implements PetService {

    @Override
    public Pets addPet(Pets pet, Users user) {
        pet.setUserId(user.getId());
        this.save(pet);
        return pet;
    }

    @Override
    public List<Pets> getMyPets(Users user) {
        QueryWrapper<Pets> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        return this.list(queryWrapper);
    }

    @Override
    public Pets updatePet(PetUpdateDTO dto) {
        Pets existing = this.getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("宠物不存在");
        }

        if (dto.getName() != null)
            existing.setName(dto.getName());
        if (dto.getSpecies() != null)
            existing.setSpecies(dto.getSpecies());
        if (dto.getAge() != null)
            existing.setAge(dto.getAge());
        if (dto.getGender() != null)
            existing.setGender(dto.getGender());
        if (dto.getPhotoUrl() != null)
            existing.setPhotoUrl(dto.getPhotoUrl());

        this.updateById(existing);
        return existing;

    }

    @Override
    public void deletePet(Long id) {
        this.removeById(id);
    }
}