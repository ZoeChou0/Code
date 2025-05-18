package com.zsh.petsystem.controller;

import com.zsh.petsystem.dto.ServiceItemDetailDTO;
import com.zsh.petsystem.entity.ServiceItem;
import com.zsh.petsystem.service.ServiceItemService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
// import java.util.stream.Collectors; // 如果使用 DTO 转换会需要
import java.util.Map;

@RestController
@RequestMapping("/services")
@CrossOrigin
@Slf4j
public class ServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    /**
     * 获取所有已批准的、可用的服务项列表
     * 
     * @return 可用服务列表 (建议使用 DTO 过滤信息)
     */

    // 如果需要根据 ID 获取单个服务详情的公共接口，也需要注意 DTO 转换
    // @GetMapping("/{id}")
    // public ServiceItemDTO getServiceById(@PathVariable Long id) { ... }

    /**
     * 获取所有已批准的、可用的服务项列表 (支持筛选)
     * 访问路径: GET /services/active?keyword=美容&location=北京&minPrice=100...
     * 
     * @param params 包含所有查询参数的 Map
     * @return 包含详细信息的服务列表 DTO
     */
    @GetMapping("/active")
    public List<ServiceItemDetailDTO> getActiveServices(@RequestParam(required = false) Map<String, Object> params) {
        log.info("请求获取可用服务列表 (/active) with params: {}", params);
        // 调用更新后的 Service 方法
        return serviceItemService.getActiveServicesWithDetails(params);
        // 全局响应处理器会自动将其包装在 Result 对象中返回给前端
    }

    /**
     * 获取所有服务项 (需要管理员权限)
     * 
     * @return 服务列表 (考虑是否也返回 DTO)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ServiceItem> listAll() { // 或者返回 List<ServiceItemDetailDTO>
        log.info("管理员请求获取所有服务列表 (/all)");
        // return serviceItemService.getActiveServicesWithDetails(Map.of()); // 如果也想用DTO
        return serviceItemService.list(); // 当前保持不变
    }
}