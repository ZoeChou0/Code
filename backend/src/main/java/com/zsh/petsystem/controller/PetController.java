package com.zsh.petsystem.controller;

import com.zsh.petsystem.annotation.CurrentUser;
import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.PetCreateDTO;
import com.zsh.petsystem.dto.PetUpdateDTO;
import com.zsh.petsystem.entity.Pets;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.PetService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/pets")
@CrossOrigin
public class PetController {

    @Autowired
    private PetService petService;

    // 如果 getCurrentUser 是唯一使用 userService 的地方，可以移除 userService
    // @Autowired
    // private UserService userService;

    // 添加宠物
    @PostMapping("/add")
    public ResponseEntity<?> addPet(@Valid @RequestBody PetCreateDTO dto, @CurrentUser Users currentUser) {
        if (currentUser == null) {
            // 返回符合 BackendResult 结构的错误响应
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("未授权或无效的 Token"));
        }
        try {
            Pets addedPet = petService.addPet(dto, currentUser);
            // 使用 Result.success() 包装成功的响应
            return ResponseEntity.ok(Result.success(addedPet, "宠物添加成功"));
        } catch (RuntimeException e) {
            // 如果 service 抛出异常，也返回 Result 结构
            return ResponseEntity.badRequest().body(Result.failed("添加宠物失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.failed("添加宠物时发生服务器错误: " + e.getMessage()));
        }
    }

    // 在 PetController.java 中 (可能的方法)
    @GetMapping("/my")
    public ResponseEntity<?> getMyPets(@CurrentUser Users currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("未授权或无效的 Token"));
        }

        List<Pets> petList = petService.getMyPets(currentUser);

        return ResponseEntity.ok(Result.success(petList));
    }

    @GetMapping("/{id}") // 映射 GET 请求到 /pets/{id} 路径
    public ResponseEntity<?> getPetById(@PathVariable Long id) { // @PathVariable 从路径中获取 id
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(Result.failed("无效的宠物ID"));
        }

        try {
            // PetServiceImpl 继承自 ServiceImpl，已经提供了 getById 方法
            Pets pet = petService.getById(id);

            if (pet != null) {
                // 找到了宠物，返回成功响应，并用 Result 包装
                // 注意：这里可能需要检查当前用户是否有权查看这只宠物（如果需要）
                // 例如: if (!Objects.equals(pet.getUserId(), currentUserId)) return ... FORBIDDEN
                // ...
                // 但 EditPet 页面通常是宠物主人访问，所以暂时不加这个检查

                return ResponseEntity.ok(Result.success(pet));
            } else {
                // 未找到宠物，返回 404 Not Found
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Result.failed("未找到指定ID的宠物"));
            }
        } catch (Exception e) {
            // 处理可能的其他异常
            System.err.println("根据ID获取宠物时出错 (ID: " + id + "): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.failed("获取宠物信息时发生服务器错误"));
        }
    }

    // // 修改宠物信息接口 (假设更新操作不需要直接使用当前用户上下文，或者 DTO 包含了必要的 ID)
    // @PutMapping("/update")
    // public ResponseEntity<?> updatePet(@Valid @RequestBody PetUpdateDTO dto,
    // @CurrentUser Long currentUserId) {
    // // 如果需要，添加授权检查：当前用户是否拥有这只宠物？
    // if (currentUserId == null) {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token
    // 或用户未登录");
    // }

    // Pets petToUpdate = petService.getById(dto.getId());

    // if (petToUpdate == null) {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到 ID 为 " +
    // dto.getId() + " 的宠物");
    // }
    // if (!Objects.equals(petToUpdate.getUserId(), currentUserId)) {

    // return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权修改不属于自己的宠物信息"); //
    // 403 Forbidden
    // }
    // try {
    // Pets updatedPet = petService.updatePet(dto, currentUserId);
    // return ResponseEntity.ok(updatedPet);
    // } catch (RuntimeException e) {
    // return ResponseEntity.badRequest().body("更新宠物信息失败: " + e.getMessage());
    // }
    // }

    @PutMapping("/update")
    public ResponseEntity<?> updatePet(@Valid @RequestBody PetUpdateDTO dto, @CurrentUser Long currentUserId) {
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("无效的 Token 或用户未登录"));
        }

        // 检查宠物是否存在以及用户是否有权限 (这部分逻辑保持)
        Pets petToUpdate = petService.getById(dto.getId());
        if (petToUpdate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.failed("未找到 ID 为 " + dto.getId() + " 的宠物"));
        }
        if (!Objects.equals(petToUpdate.getUserId(), currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.failed("无权修改不属于自己的宠物信息"));
        }

        try {
            // 调用 Service 层执行更新
            Pets updatedPet = petService.updatePet(dto, currentUserId);
            return ResponseEntity.ok(Result.success(updatedPet));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Result.failed("更新宠物信息失败: " + e.getMessage()));
        } catch (Exception e) { // 捕获更广泛的异常
            System.err.println("更新宠物时出错 (ID: " + dto.getId() + "): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.failed("更新宠物信息时发生服务器错误"));
        }
    }

    // 删除宠物 (假设删除操作不需要当前用户上下文，或者使用路径变量)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePet(@PathVariable Long id,
            @CurrentUser Long currentUserId) { // 1. 使用 @CurrentUser 获取 Long 类型的用户 ID
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("无效的 Token 或用户未登录"));
        }

        try {
            // 2. 调用 Service 的 deletePet(Long, Long) 方法
            boolean deleted = petService.deletePet(id, currentUserId);
            if (deleted) {
                return ResponseEntity.ok(Result.success(null, "删除成功")); // 使用 Result 包装
            } else {
                // 如果 service 返回 false，通常是因为未找到或无权限
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.failed("删除失败：宠物不存在或无权限"));
            }
        } catch (Exception e) {
            // 处理 Service 层可能抛出的其他异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.failed("删除宠物时发生错误: " + e.getMessage()));
        }
    }
}