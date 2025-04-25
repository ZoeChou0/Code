package com.zsh.petsystem.controller;

import com.zsh.petsystem.model.Users;
import com.zsh.petsystem.service.UserService;
import com.zsh.petsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // 导入 HttpStatus
import org.springframework.http.ResponseEntity;
// 导入 PasswordEncoder (你需要添加 Spring Security 依赖并配置 Bean)
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map; // 导入 Map

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    // 注入 PasswordEncoder Bean (需要在配置类中定义 @Bean public PasswordEncoder
    // passwordEncoder())
    // @Autowired
    // private PasswordEncoder passwordEncoder; // 取消注释并确保已配置

    /**
     * 添加用户 (可能需要管理员权限)
     * 
     * @param user 用户信息
     * @return ResponseEntity
     */
    @PostMapping("/add")
    // TODO: 添加权限控制, 例如 @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addUser(@RequestBody Users user) {
        // TODO: 在保存前进行密码哈希处理
        // String hashedPassword = passwordEncoder.encode(user.getPassword());
        // user.setPassword(hashedPassword);
        try {
            userService.saveUser(user); // 假设 saveUser 内部处理了哈希 (更好的方式)
            // 或者直接在这里处理哈希后调用 save
            return ResponseEntity.status(HttpStatus.CREATED).body("新用户添加成功"); // 201 Created
        } catch (Exception e) {
            // 处理可能的异常，例如数据库错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("添加用户失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     * 
     * @param user 注册用户信息 (包含 email, phone, password 等)
     * @return ResponseEntity
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Users user) {
        // 检查邮箱或手机号是否已存在
        if (userService.existByEmail(user.getEmail())) {
            // 使用 409 Conflict 表示资源冲突
            return ResponseEntity.status(HttpStatus.CONFLICT).body("邮箱已注册");
        }
        if (userService.existByPhone(user.getPhone())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("手机号已注册");
        }

        // **在这里对密码进行哈希处理**
        // if (passwordEncoder == null) { // 简单的检查，确保 PasswordEncoder 已注入
        // return
        // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器密码编码器未配置");
        // }
        // String hashedPassword = passwordEncoder.encode(user.getPassword());
        // user.setPassword(hashedPassword);

        // 设置默认角色等（如果需要）
        // user.setRole("user"); // 例如

        try {
            // 调用 Service 保存用户 (Service 的 saveUser 方法内部**不**应该再次哈希)
            // 或者修改 Service 的 saveUser 让其接收原始密码并内部处理哈希
            userService.saveUser(user); // 确保这里的 user 对象的密码是哈希后的
            return ResponseEntity.status(HttpStatus.CREATED).body("注册成功"); // 201 Created
        } catch (Exception e) {
            // 处理保存时可能发生的异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("注册失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有用户 (需要管理员权限)
     * 
     * @return 用户列表
     */
    @GetMapping("/getAll")
    // TODO: 添加权限控制, 例如 @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<Users> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     * 
     * @param user 包含登录凭证 (email, password) 的对象
     * @return ResponseEntity 包含 JWT Token 或错误信息
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user) {
        Users dbUser = userService.findByEmail(user.getEmail());

        // 检查用户是否存在
        if (dbUser == null) {
            // 不要明确提示是用户名错了还是密码错了，统一返回 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("邮箱或密码错误"); // 401 Unauthorized
        }

        // **在这里比较哈希密码**
        // if (passwordEncoder == null) {
        // return
        // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器密码编码器未配置");
        // }
        // boolean passwordMatches = passwordEncoder.matches(user.getPassword(),
        // dbUser.getPassword());

        // **移除原来的明文比较逻辑**
        // if (!dbUser.getPassword().equals(user.getPassword())) { // <- 这是不安全的，移除掉

        // 使用哈希比较结果
        // if (!passwordMatches) {
        // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("邮箱或密码错误"); // 401
        // Unauthorized
        // }

        // --- 如果暂时无法配置 PasswordEncoder，保留原来的逻辑，但强烈建议修改 ---
        // 临时的明文比较 (极不推荐，尽快替换为哈希比较)
        if (!dbUser.getPassword().equals(user.getPassword())) {
            System.err.println("警告: 正在使用明文密码比较进行登录，请尽快替换为哈希比较！");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("邮箱或密码错误"); // 401 Unauthorized
        }
        // --- 临时逻辑结束 ---

        // 密码验证通过，生成 JWT
        // 注意：JwtUtil.generateToken 的参数，原始代码只传了 email，
        // 但之前的设计是传入 userId, username(email), role
        String token = JwtUtil.generateToken(dbUser.getId(), dbUser.getEmail(), dbUser.getRole()); // 确保
                                                                                                   // JwtUtil.generateToken
                                                                                                   // 支持这些参数

        // 返回 Token，通常放在 JSON 对象中
        Map<String, String> tokenMap = Collections.singletonMap("token", token);
        return ResponseEntity.ok(tokenMap);
    }
}