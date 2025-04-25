package com.zsh.petsystem.controller;

import com.zsh.petsystem.annotation.CurrentUser;
import com.zsh.petsystem.dto.PetUpdateDTO;
import com.zsh.petsystem.model.Pets;
import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.service.PetService;

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

    //添加宠物
    @PostMapping("/add")
    // 使用 @CurrentUser 直接注入 Users 对象
    public ResponseEntity<?> addPet(@RequestBody Pets pet, @CurrentUser Users currentUser) {
        if (currentUser == null) {
             // 处理 Token 无效或用户未找到的情况
             return ResponseEntity.status(401).body("未授权或无效的 Token");
        }
    
        return ResponseEntity.ok(petService.addPet(pet, currentUser));
    }

    //查询用户的所有宠物
    @GetMapping("/my")
    // 直接注入 Users 对象
    public ResponseEntity<?> getMyPets(@CurrentUser Users currentUser) {
         if (currentUser == null) {
             return ResponseEntity.status(401).body("未授权或无效的 Token");
        }

        return ResponseEntity.ok(petService.getMyPets(currentUser));
    }

    // @GetMapping("/my")
    // public ResponseEntity<?> getMyPetsById(@CurrentUser Long currentUserId) {
    //     if (currentUserId == null) {
    //         return ResponseEntity.status(401).body("未授权或无效的 Token");
    //     }
    //     // 如果 petService.getMyPets 需要 Users 对象，先获取它
    //     // Users currentUser = userService.getById(currentUserId);
    //     // return ResponseEntity.ok(petService.getMyPets(currentUser));
    //
    //     // 或者修改 petService.getMyPets 来接受 userId
    //      return ResponseEntity.ok(petService.getMyPetsByUserId(currentUserId)); // 假设有这个方法
    // }


    // 修改宠物信息接口 (假设更新操作不需要直接使用当前用户上下文，或者 DTO 包含了必要的 ID)
    @PutMapping("/update")
    public ResponseEntity<?> updatePet(@RequestBody PetUpdateDTO dto, @CurrentUser Long currentUserId) {
        // 如果需要，添加授权检查：当前用户是否拥有这只宠物？
        if(currentUserId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录"); 
        }

        Pets petToUpdate = petService.getById(dto.getId());

        if(petToUpdate == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到 ID 为 " + dto.getId() + " 的宠物");
        }
        if (!Objects.equals(petToUpdate.getUserId(), currentUserId)) {
        
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权修改不属于自己的宠物信息"); // 403 Forbidden
        }
        try {
            Pets updatedPet = petService.updatePet(dto); 
            return ResponseEntity.ok(updatedPet);
        } catch (RuntimeException e) {
             return ResponseEntity.badRequest().body("更新宠物信息失败: " + e.getMessage());
        }
    }

    // 删除宠物 (假设删除操作不需要当前用户上下文，或者使用路径变量)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePet(@PathVariable Long id, @CurrentUser Long currentUserId) {
         // 1. 检查当前用户是否存在
         if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效的 Token 或用户未登录");
         }

         // 2. 获取要删除的宠物信息
         Pets petToDelete = petService.getById(id);

         // 3. 检查宠物是否存在
         if (petToDelete == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到 ID 为 " + id + " 的宠物");
         }

         // 4. 授权检查
         if (!Objects.equals(petToDelete.getUserId(), currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权删除不属于自己的宠物信息");
         }

         // 5. 授权通过，执行删除
        petService.deletePet(id);
        return ResponseEntity.ok("删除成功");
    }
}