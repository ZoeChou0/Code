package com.zsh.petsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsh.petsystem.entity.Payment;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
