package com.zsh.petsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsh.petsystem.entity.Reservation;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

}