package com.zsh.petsystem.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsh.petsystem.dto.ServiceCreateDTO;
import com.zsh.petsystem.dto.ServiceItemDetailDTO;
import com.zsh.petsystem.dto.ServiceItemUpdateDTO;
import com.zsh.petsystem.entity.ServiceItem;

public interface ServiceItemService extends IService<ServiceItem> {

    ServiceItem add(ServiceItem item);

    ServiceItem update(ServiceItem item);

    void delete(Long id);

    boolean approveServiceItem(Long serviceItemId);// 批准服务

    boolean rejectServiceItem(Long serviceItemId, String reason);// 拒绝服务申请

    boolean deleteProviderServiceItem(Long serviceItemId, Long providerId);

    ServiceItem updateProviderServiceItem(ServiceItemUpdateDTO updateDTO, Long providerId);

    List<ServiceItemDetailDTO> getActiveServicesWithDetails(Map<String, Object> params);

    ServiceItem addProviderService(ServiceCreateDTO dto, Long providerId);

    ServiceItem setProviderServiceAvailability(Long serviceItemId, Long providerId, boolean available);

    long countActiveServicesForProvider(Long providerId); // 新增统计
}