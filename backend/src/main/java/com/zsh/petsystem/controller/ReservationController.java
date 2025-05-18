// package com.zsh.petsystem.controller;

// import com.zsh.petsystem.annotation.CurrentUser; // 导入 @CurrentUser
// import com.zsh.petsystem.dto.ReservationDTO;
// import com.zsh.petsystem.model.Reservation;
// import com.zsh.petsystem.service.ReservationService;
// // 移除 HttpServletRequest 和 JwtUtil 的导入
// // import jakarta.servlet.http.HttpServletRequest;
// // import com.zhoucodes.petsystem.util.JwtUtil;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus; // 导入 HttpStatus
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Objects; // 导入 Objects 用于比较

// @RestController
// @RequestMapping("/reservations")
// @CrossOrigin
// public class ReservationController {

//     @Autowired
//     private ReservationService reservationService;

//     /**
//      * 创建预约
//      * @param dto 预约数据传输对象
//      * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
//      * @return ResponseEntity
//      */
//     @PostMapping("/add")
//     // 使用 @CurrentUser 替换 HttpServletRequest 和手动解析
//     public ResponseEntity<?> create(@RequestBody ReservationDTO dto, @CurrentUser Long currentUserId) {
//         // 1. 检查用户是否已登录
//         if (currentUserId == null) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
//         }

//         // 2. 调用 Service 创建预约
//         try {
//             // reservationService.create 现在接收 currentUserId
//             Reservation newReservation = reservationService.create(dto, currentUserId);
//             return ResponseEntity.ok(newReservation); // 返回创建的预约信息
//         } catch (RuntimeException e) {
//             // 捕获 Service 层可能抛出的异常
//             // 例如: "用户不存在", "宠物不存在", "服务项不存在", "该服务项暂未启用" 等
//             // 根据具体错误信息返回相应的状态码
//             if (e.getMessage().contains("不存在") || e.getMessage().contains("未启用")) {
//                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("创建预约失败: " + e.getMessage()); // 400 Bad Request
//             } else {
//                 // 其他未知运行时异常
//                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建预约时发生错误: " + e.getMessage()); // 500 Internal Server Error
//             }
//         }
//     }

//     /**
//      * 获取当前用户的预约列表
//      * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
//      * @return ResponseEntity 包含预约列表
//      */
//     @GetMapping("/my")
//     // 使用 @CurrentUser 替换 HttpServletRequest 和手动解析
//     public ResponseEntity<?> list(@CurrentUser Long currentUserId) {
//         // 1. 检查用户是否已登录
//         if (currentUserId == null) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
//         }

//         // 2. 调用 Service 获取列表
//         try {
//             List<Reservation> reservations = reservationService.getByUserId(currentUserId);
//             return ResponseEntity.ok(reservations);
//         } catch (Exception e) {
//              // 处理可能的异常
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取预约列表失败: " + e.getMessage());
//         }
//     }

//     /**
//      * 取消预约
//      * @param id 要取消的预约的 ID
//      * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
//      * @return ResponseEntity
//      */
//     @PostMapping("/{id}/cancel")
//     // 增加 @CurrentUser 用于授权检查
//     public ResponseEntity<?> cancel(@PathVariable Long id, @CurrentUser Long currentUserId) {
//         // 1. 检查用户是否已登录
//         if (currentUserId == null) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
//         }

//         // 2. 授权检查：检查该预约是否属于当前用户
//         try {
//             Reservation reservation = reservationService.getById(id); // ReservationService 需要有 getById 方法 (IService 提供了)

//             // 检查预约是否存在
//             if (reservation == null) {
//                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到 ID 为 " + id + " 的预约"); // 404 Not Found
//             }

//             // 检查是否是该用户的预约
//             if (!Objects.equals(reservation.getUserId(), currentUserId)) {
//                 return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权取消不属于自己的预约"); // 403 Forbidden
//             }

//             // 3. 授权通过，执行取消操作
//             reservationService.cancel(id);
//             return ResponseEntity.ok("预约已取消");

//         } catch (RuntimeException e) {
//              // 捕获 Service 层 cancel 或 getById 可能抛出的异常
//              if (e.getMessage().contains("预约不存在")) { // Service 层 cancel 方法可能会再次检查
//                  return ResponseEntity.status(HttpStatus.NOT_FOUND).body("取消预约失败: " + e.getMessage());
//              } else {
//                  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("取消预约时发生错误: " + e.getMessage());
//              }
//         }
//     }
// }

package com.zsh.petsystem.controller;

import com.zsh.petsystem.annotation.CurrentUser;
import com.zsh.petsystem.common.Result; // <--- 确保导入 Result 类
import com.zsh.petsystem.dto.ReservationDTO;
import com.zsh.petsystem.entity.Reservation;
import com.zsh.petsystem.service.ReservationService;
import lombok.extern.slf4j.Slf4j; // <--- 添加日志注解导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/reservations")
@CrossOrigin // 如果全局配置了 CORS，这里可能不是必需的
@Slf4j // <--- 添加日志注解
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    /**
     * 创建预约
     * 
     * @param dto           预约数据传输对象
     * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
     * @return ResponseEntity<Result<?>>
     */
    @PostMapping("/add")
    public ResponseEntity<Result<?>> create(@RequestBody ReservationDTO dto, @CurrentUser Long currentUserId) {
        // 1. 检查用户是否已登录
        if (currentUserId == null) {
            log.warn("Attempt to create reservation without authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("无效的 Token 或用户未登录"));
        }

        // 2. 调用 Service 创建预约
        try {
            log.info("User {} attempting to create reservation with DTO: {}", currentUserId, dto);
            Reservation newReservation = reservationService.create(dto, currentUserId);
            log.info("Reservation created successfully for user {}: ID {}", currentUserId, newReservation.getId());
            // 返回 Result 包装的成功数据
            return ResponseEntity.ok(Result.success(newReservation, "预约创建成功"));
        } catch (IllegalArgumentException | IllegalStateException e) { // 捕获预期的业务逻辑异常
            log.warn("Failed to create reservation for user {}: {}", currentUserId, e.getMessage());
            // 返回 Result 包装的失败信息 (Bad Request)
            return ResponseEntity.badRequest().body(Result.failed("创建预约失败: " + e.getMessage()));
        } catch (RuntimeException e) { // 捕获 Service 层可能抛出的其他运行时异常
            log.error("Runtime error creating reservation for user {}: {}", currentUserId, e.getMessage(), e);
            // 返回 Result 包装的失败信息 (Bad Request 通常也适用)
            return ResponseEntity.badRequest().body(Result.failed("创建预约失败: " + e.getMessage()));
        } catch (Exception e) { // 捕获其他所有意外异常
            log.error("Unexpected error creating reservation for user {}: {}", currentUserId, e.getMessage(), e);
            // 返回 Result 包装的服务器错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failed("创建预约时发生服务器内部错误，请稍后重试"));
        }
    }

    /**
     * 获取当前用户的预约列表
     * 
     * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
     * @return ResponseEntity<Result<List<Reservation>>>
     */
    @GetMapping("/my")
    public ResponseEntity<Result<List<Reservation>>> list(@CurrentUser Long currentUserId) {
        // 1. 检查用户是否已登录
        if (currentUserId == null) {
            log.warn("Attempt to list reservations without authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("无效的 Token 或用户未登录"));
        }

        // 2. 调用 Service 获取列表
        try {
            log.info("Fetching reservations for user {}", currentUserId);
            List<Reservation> reservations = reservationService.getByUserId(currentUserId);
            log.info("Found {} reservations for user {}", reservations.size(), currentUserId);
            // 返回 Result 包装的成功数据
            return ResponseEntity.ok(Result.success(reservations));
        } catch (Exception e) {
            log.error("Error fetching reservations for user {}: {}", currentUserId, e.getMessage(), e);
            // 返回 Result 包装的服务器错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.failed("获取预约列表失败: " + e.getMessage()));
        }
    }

    /**
     * 取消预约
     * 
     * @param id            要取消的预约的 ID
     * @param currentUserId 当前登录用户的 ID (通过 @CurrentUser 注入)
     * @return ResponseEntity<Result<?>>
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Result<?>> cancel(@PathVariable Long id, @CurrentUser Long currentUserId) {
        // 1. 检查用户是否已登录
        if (currentUserId == null) {
            log.warn("Attempt to cancel reservation {} without authentication.", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("无效的 Token 或用户未登录"));
        }

        // 2. 授权检查并执行取消操作
        try {
            log.info("User {} attempting to cancel reservation {}", currentUserId, id);
            Reservation reservation = reservationService.getById(id);

            // 检查预约是否存在
            if (reservation == null) {
                log.warn("Reservation {} not found for cancellation attempt by user {}", id, currentUserId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.failed("未找到 ID 为 " + id + " 的预约"));
            }

            // 检查是否是该用户的预约 (或者管理员也可以取消?) - 根据业务逻辑决定
            // if (!Objects.equals(reservation.getUserId(), currentUserId) &&
            // !userStore.isAdmin()) { // 示例：允许管理员
            if (!Objects.equals(reservation.getUserId(), currentUserId)) {
                log.warn("User {} attempted to cancel reservation {} owned by user {}", currentUserId, id,
                        reservation.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed("无权取消不属于自己的预约"));
            }

            // 3. 授权通过，执行取消操作 (Service 层会处理具体逻辑和状态检查)
            reservationService.cancel(id /* , cancellationReason */); // Service 层应处理状态检查，如果已经是取消或完成状态，可能抛异常或静默处理
            log.info("Reservation {} cancelled successfully by user {}", id, currentUserId);
            // 返回 Result 包装的成功信息 (无数据)
            return ResponseEntity.ok(Result.success(null, "预约已取消"));

        } catch (IllegalStateException e) { // 捕获 Service 层可能抛出的状态不允许取消的异常
            log.warn("Failed to cancel reservation {} for user {}: {}", id, currentUserId, e.getMessage());
            return ResponseEntity.badRequest().body(Result.failed("取消预约失败: " + e.getMessage())); // 400 Bad Request
        } catch (RuntimeException e) { // 捕获 Service 层 cancel 或 getById 可能抛出的其他运行时异常
            log.error("Runtime error cancelling reservation {} for user {}: {}", id, currentUserId, e.getMessage(), e);
            // 可以根据错误信息区分 404 和 500
            if (e.getMessage() != null && e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.failed("取消预约失败: " + e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Result.failed("取消预约时发生错误: " + e.getMessage()));
            }
        } catch (Exception e) { // 捕获其他意外异常
            log.error("Unexpected error cancelling reservation {} for user {}: {}", id, currentUserId, e.getMessage(),
                    e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failed("取消预约时发生服务器内部错误"));
        }
    }
}