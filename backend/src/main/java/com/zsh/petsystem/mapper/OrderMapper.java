package com.zsh.petsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsh.petsystem.entity.Order;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}