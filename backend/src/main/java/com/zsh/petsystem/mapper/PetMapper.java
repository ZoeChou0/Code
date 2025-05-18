package com.zsh.petsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsh.petsystem.entity.Pets;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper extends BaseMapper<Pets> {
}
