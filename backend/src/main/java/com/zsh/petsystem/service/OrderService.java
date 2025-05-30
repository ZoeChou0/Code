// package com.zsh.petsystem.service;

// import java.util.List;

// import org.springframework.data.domain.Page;

// import com.baomidou.mybatisplus.core.metadata.IPage;
// import com.baomidou.mybatisplus.extension.service.IService;
// import com.zsh.petsystem.dto.OrderAdminViewDTO;
// import com.zsh.petsystem.dto.OrderCreateDTO;
// import com.zsh.petsystem.dto.OrderFromReservationDTO;
// import com.zsh.petsystem.dto.OrderViewDTO;
// import com.zsh.petsystem.entity.Order;

// public interface OrderService extends IService<Order> {
//     Order createOrder(OrderCreateDTO dto);

//     Order createOrderFromReservation(OrderFromReservationDTO dto, Long userId);

//     /**
//      * 获取指定用户的订单列表，并包含服务名称、宠物名称等详细信息
//      * 
//      * @param userId 用户ID
//      * @return 包含详细信息的订单列表 DTO
//      */
//     List<OrderViewDTO> getUserOrdersWithDetails(Long userId);

//     void cancelUserOrder(Long orderId, Long userId);

//     /**
//      * 服务商标记订单为已完成
//      * 
//      * @param orderId        订单ID
//      * @param providerUserId 当前服务商的用户ID (用于权限验证)
//      * @throws RuntimeException (或更具体的业务异常) 如果操作失败
//      */
//     void completeOrderByProvider(Long orderId, Long providerUserId);

//     IPage<OrderAdminViewDTO> getAdminOrdersPage(com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> pageRequest, String status);
// }

package com.zsh.petsystem.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zsh.petsystem.dto.OrderAdminViewDTO;
import com.zsh.petsystem.dto.OrderCreateDTO;
import com.zsh.petsystem.dto.OrderFromReservationDTO;
import com.zsh.petsystem.dto.OrderViewDTO;
import com.zsh.petsystem.entity.Order;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {
    Order createOrder(OrderCreateDTO dto);

    Order createOrderFromReservation(OrderFromReservationDTO dto, Long userId);

    /**
     * 获取指定用户的订单列表，并包含服务名称、宠物名称等详细信息
     */
    List<OrderViewDTO> getUserOrdersWithDetails(Long userId);

    /**
     * 用户取消自己的订单
     */
    void cancelUserOrder(Long orderId, Long userId);

    /**
     * 服务商标记订单为已完成
     */
    void completeOrderByProvider(Long orderId, Long providerUserId);

    /**
     * 管理员分页查看订单
     *
     * @param pageNum  当前页码 (从1开始)
     * @param pageSize 每页条数
     * @param status   订单状态筛选, 传null或"ALL"则不过滤
     * @return 分页后的订单视图DTO
     */
    IPage<OrderAdminViewDTO> getAdminOrdersPage(int pageNum, int pageSize, String status);

    List<OrderViewDTO> getProviderOrdersWithDetails(Long providerId);
}
