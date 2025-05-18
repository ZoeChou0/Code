package com.zsh.petsystem.controller;

import com.zsh.petsystem.dto.NotifyRequestDTO;
import com.zsh.petsystem.dto.ReviewDTO;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.mapper.UserMapper;
import com.zsh.petsystem.service.ReviewService;
import com.zsh.petsystem.util.AuthUtil;
import com.zsh.petsystem.util.EmailApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/notify")
@CrossOrigin
public class AdminNotifyController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailApi emailApi;

    @PostMapping
    public ResponseEntity<?> sendNotification(@RequestParam NotifyRequestDTO dto, HttpServletRequest request) {
        // 权限校验
        if (!AuthUtil.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权限");
        }

        // 查询所有用户邮箱
        List<Users> users = userMapper.selectList(null);
        int successCount = 0;

        for (Users user : users) {
            try {
                emailApi.sendEmail(user.getEmail(), dto.getSubject(), dto.getContent());
                successCount++;
            } catch (Exception e) {
                System.out.println("发送失败：" + user.getEmail());
            }
        }

        return ResponseEntity.ok("发送成功 " + successCount + " 封邮件");
    }

}
