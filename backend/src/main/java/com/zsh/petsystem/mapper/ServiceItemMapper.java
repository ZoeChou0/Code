package com.zsh.petsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsh.petsystem.dto.ServiceItemDetailDTO;
import com.zsh.petsystem.entity.ServiceItem;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ServiceItemMapper extends BaseMapper<ServiceItem> {
  List<ServiceItemDetailDTO> findActiveServiceDetailsFiltered(@Param("params") Map<String, Object> params);
}