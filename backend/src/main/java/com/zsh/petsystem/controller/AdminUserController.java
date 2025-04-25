package com.zsh.petsystem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.service.UserService;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

  @Autowired
  private UserService userService;

  @GetMapping("/providers/pending-review")
  public ResponseEntity<?> getPendingProviders(){

    try{
      List<Users> pendingProviders = userService.findPendingQualificationProviders();
      return ResponseEntity.ok(pendingProviders);
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("获取待审核服务商列表失败: " + e.getMessage());
    }
  }

  // @PutMapping("/providers/{id}/approve")
  // @PreAuthorize("hasRole('ADMIN')")
  // public ResponseEntity<?> approveProvider(@PathVariable Long id) { ... }

  // @PutMapping("/providers/{id}/reject")
  // @PreAuthorize("hasRole('ADMIN')")
  // public ResponseEntity<?> rejectProvider(@PathVariable Long id) { ... }


}
