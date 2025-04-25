package com.zsh.petsystem.controller;

import com.zsh.petsystem.annotation.CurrentUser; // 导入 @CurrentUser
import com.zsh.petsystem.dto.ReservationDTO;
import com.zsh.petsystem.model.Reservation;
import com.zsh.petsystem.service.ReservationService;
// 移除 HttpServletRequest 和 JwtUtil 的导入
// import jakarta.servlet.http.HttpServletRequest;
// import com.zhoucodes.petsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // 导入 HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects; // 导入 Objects 用于比较

@RestController
@RequestMapping("/reservations")
@CrossOrigin
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    /**
     * 创建预约
     * @param dto 预约数据传输对象
     * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
     * @return ResponseEntity
     */
    @PostMapping("/add")
    // 使用 @CurrentUser 替换 HttpServletRequest 和手动解析
    public ResponseEntity<?> create(@RequestBody ReservationDTO dto, @CurrentUser Long currentUserId) {
        // 1. 检查用户是否已登录
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
        }

        // 2. 调用 Service 创建预约
        try {
            // reservationService.create 现在接收 currentUserId
            Reservation newReservation = reservationService.create(dto, currentUserId);
            return ResponseEntity.ok(newReservation); // 返回创建的预约信息
        } catch (RuntimeException e) {
            // 捕获 Service 层可能抛出的异常
            // 例如: "用户不存在", "宠物不存在", "服务项不存在", "该服务项暂未启用" 等
            // 根据具体错误信息返回相应的状态码
            if (e.getMessage().contains("不存在") || e.getMessage().contains("未启用")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("创建预约失败: " + e.getMessage()); // 400 Bad Request
            } else {
                // 其他未知运行时异常
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建预约时发生错误: " + e.getMessage()); // 500 Internal Server Error
            }
        }
    }

    /**
     * 获取当前用户的预约列表
     * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
     * @return ResponseEntity 包含预约列表
     */
    @GetMapping("/my")
    // 使用 @CurrentUser 替换 HttpServletRequest 和手动解析
    public ResponseEntity<?> list(@CurrentUser Long currentUserId) {
        // 1. 检查用户是否已登录
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
        }

        // 2. 调用 Service 获取列表
        try {
            List<Reservation> reservations = reservationService.getByUserId(currentUserId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
             // 处理可能的异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取预约列表失败: " + e.getMessage());
        }
    }

    /**
     * 取消预约
     * @param id 要取消的预约的 ID
     * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
     * @return ResponseEntity
     */
    @PostMapping("/{id}/cancel")
    // 增加 @CurrentUser 用于授权检查
    public ResponseEntity<?> cancel(@PathVariable Long id, @CurrentUser Long currentUserId) {
        // 1. 检查用户是否已登录
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
        }

        // 2. 授权检查：检查该预约是否属于当前用户
        try {
            Reservation reservation = reservationService.getById(id); // ReservationService 需要有 getById 方法 (IService 提供了)

            // 检查预约是否存在
            if (reservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到 ID 为 " + id + " 的预约"); // 404 Not Found
            }

            // 检查是否是该用户的预约
            if (!Objects.equals(reservation.getUserId(), currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权取消不属于自己的预约"); // 403 Forbidden
            }

            // 3. 授权通过，执行取消操作
            reservationService.cancel(id);
            return ResponseEntity.ok("预约已取消");

        } catch (RuntimeException e) {
             // 捕获 Service 层 cancel 或 getById 可能抛出的异常
             if (e.getMessage().contains("预约不存在")) { // Service 层 cancel 方法可能会再次检查
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body("取消预约失败: " + e.getMessage());
             } else {
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("取消预约时发生错误: " + e.getMessage());
             }
        }
    }
}