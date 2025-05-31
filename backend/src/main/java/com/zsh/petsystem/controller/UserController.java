package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.LoginRequestDTO;
import com.zsh.petsystem.dto.UserUpdateProfileDTO;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.UserService;
import com.zsh.petsystem.util.JwtUtil;
import com.zsh.petsystem.service.CaptchaService;
import com.zsh.petsystem.dto.RegisterDataDTO;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 导入 (如果注册/添加需要)
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // 导入 Collectors

@RestController
@RequestMapping("/users")
@CrossOrigin
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 添加用户 (权限待定) - 返回 void
     * 假设 userService.saveUser 内部处理密码哈希
     * 
     * @param user 用户信息
     */
    @PostMapping("/add")
    // TODO: 添加权限控制, 例如 @PreAuthorize("hasRole('ADMIN')")
    public void addUser(@RequestBody Users user) {

        // 如果 saveUser 内部有唯一约束（如 email），重复添加会抛出异常，由 GlobalExceptionHandler 处理
        userService.saveUser(user);
        log.info("新用户 {} 添加成功 (ID: {})", user.getEmail(), user.getId());
        // 成功时方法正常结束，会被 GlobalResponseAdvice 包装成 Result.success(null)
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDataDTO registerDto) {

        boolean isCodeValid = captchaService.verifyCaptcha(registerDto.getEmail(), registerDto.getVerificationCode());
        if (!isCodeValid) {
            return Result.failed("验证码错误或已过期");
        }

        // 2. 检查邮箱、手机号是否已存在
        if (userService.existByEmail(registerDto.getEmail())) {
            return Result.failed("邮箱已被注册"); // <<--- 返回Result对象
        }
        if (registerDto.getPhone() != null && userService.existByPhone(registerDto.getPhone())) {
            return Result.failed("手机号已被注册"); // <<--- 返回Result对象
        }

        // 3. 创建 Users 实体并填充数据
        Users newUser = new Users();
        newUser.setName(registerDto.getName());
        newUser.setEmail(registerDto.getEmail());
        newUser.setPhone(registerDto.getPhone());
        // 对密码进行哈希处理
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword())); // <<--- 哈希密码
        newUser.setRole(
                registerDto.getRole() == null || registerDto.getRole().isEmpty() ? "user" : registerDto.getRole());
        newUser.setStatus("active"); // 新用户默认为激活状态

        // 4. 保存用户
        try {
            userService.saveUser(newUser);
            // 注册成功后，可以选择性地移除Redis中的验证码，防止重复使用
            // stringRedisTemplate.delete(redisKey);
            captchaService.clearCaptcha(registerDto.getEmail()); // 假设CaptchaService有此方法

            log.info("用户 {} 注册成功", newUser.getEmail());
            return Result.success(null, "注册成功"); // <<--- 返回Result对象
        } catch (Exception e) {
            log.error("用户 {} 注册失败: {}", registerDto.getEmail(), e.getMessage(), e);
            return Result.failed("注册过程中发生错误，请稍后重试。"); // <<--- 返回Result对象
        }
    }

    /**
     * 获取所有用户 (权限待定) - 直接返回列表
     * 
     * @return 用户列表 (密码已清除)
     */
    @GetMapping("/getAll")
    // TODO: 添加权限控制, 例如 @PreAuthorize("hasRole('ADMIN')")
    public List<Users> getAllUsers() {
        // 注意：这个接口和 AdminUserController 里的 GET /admin/users 功能类似
        // 需要明确这个接口的目标用户和权限，或者移除一个。
        // 这里假设它不需要管理员权限，但仍然需要清除密码。
        log.warn("公共接口 /users/getAll 被调用，请确认权限是否合适");
        List<Users> users = userService.getAllUsers();
        // 清除密码
        users.forEach(u -> u.setPassword(null));
        return users; // 由 Advice 包装
    }

    /**
     * 用户登录 (支持用户名、邮箱、手机号) - 返回包含 Token 的 Map
     * 
     * @param loginRequest 包含登录凭证 (identifier, password) 的对象
     * @return 包含 token 的 Map
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequestDTO loginRequest) {

        Users dbUser = userService.findUserByIdentifier(loginRequest.getIdentifier());

        // 检查用户是否存在或状态是否正常
        if (dbUser == null) {
            throw new RuntimeException("用户名、邮箱或手机号或密码错误"); // 统一错误提示
        }
        if ("banned".equalsIgnoreCase(dbUser.getStatus())) {
            throw new RuntimeException("用户账号已被禁用"); // 明确提示被禁用
        }
        // --- (可选) 使用密码哈希比较 ---
        // if (passwordEncoder == null) {
        // log.error("PasswordEncoder 未配置，无法验证密码！");
        // throw new IllegalStateException("服务器认证配置错误");
        // }
        // boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(),
        // dbUser.getPassword());
        // if (!passwordMatches) {
        // throw new RuntimeException("用户名、邮箱或手机号或密码错误");
        // }
        // ---

        // --- (当前) 使用明文比较 (极其不安全) ---
        if (!loginRequest.getPassword().equals(dbUser.getPassword())) {
            log.warn("正在使用不安全的明文密码比较进行登录验证！");
            throw new RuntimeException("用户名、邮箱或手机号或密码错误");
        }
        // ---

        // 密码验证通过，生成 JWT
        String tokenValue = JwtUtil.generateToken(dbUser.getId(), dbUser.getEmail(), dbUser.getRole());
        Map<String, String> tokenData = Collections.singletonMap("token", tokenValue);

        log.info("用户 {} (角色: {}) 登录成功", dbUser.getEmail(), dbUser.getRole());
        // 直接返回 Map，由 Advice 包装成 Result.success(tokenData)
        return tokenData;
    }

    /**
     * 获取当前登录用户信息 - 返回 Users 对象
     * 
     * @param authentication Spring Security 提供的认证信息
     * @return 当前用户信息 (密码已清除)
     */
    @GetMapping("/info")
    public Users getUserInfo(Authentication authentication) {
        // 日志记录已在之前添加
        log.info("--- Entering /users/info endpoint ---");
        // ... (打印 authentication 信息的日志) ...

        if (authentication == null || !authentication.isAuthenticated()) {
            // 理论上 Filter 应该阻止未认证访问，这里作为保险
            log.warn("/users/info 访问时认证信息无效或未认证。");
            // 抛出异常，让全局处理器处理
            throw new RuntimeException("用户未认证");
        }

        String userIdentifier = authentication.getName(); // Principal 通常是登录时用的标识符
        log.info("Fetching info for identifier: {}", userIdentifier);

        // 注意：如果 principal 不是 email，需要用 findUserByIdentifier
        // Users user = userService.findUserByIdentifier(userIdentifier);
        // 假设 principal 就是 email (需要与 Filter 中设置 principal 一致)
        Users user = userService.findByEmail(userIdentifier);

        if (user == null) {
            log.error("数据库中未找到标识符为 {} 的用户", userIdentifier);
            throw new RuntimeException("用户未找到");
        }
        // 检查用户状态是否被禁用
        if ("banned".equalsIgnoreCase(user.getStatus())) {
            log.warn("尝试获取已被禁用用户 {} 的信息", userIdentifier);
            // 清除安全上下文，防止后续使用可能过期的认证信息
            SecurityContextHolder.clearContext();
            throw new RuntimeException("用户账号已被禁用");
        }

        user.setPassword(null); // 清除密码
        log.info("成功返回用户 {} 的信息", userIdentifier);
        // 直接返回 Users 对象，由 Advice 包装
        return user;
    }

    /**
     * 更新用户个人资料 - 返回更新后的 Users 对象
     * 
     * @param dto            更新的数据
     * @param authentication 当前认证信息
     * @return 更新后的用户信息 (密码已清除)
     */
    @PutMapping("/profile")
    public Users updateUserProfile(@RequestBody UserUpdateProfileDTO dto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未认证");
        }
        String userIdentifier = authentication.getName();
        // Users currentUser = userService.findUserByIdentifier(userIdentifier);
        Users currentUser = userService.findByEmail(userIdentifier); // 假设 principal 是 email

        if (currentUser == null) {
            throw new RuntimeException("当前用户未找到");
        }
        if ("banned".equalsIgnoreCase(currentUser.getStatus())) {
            throw new RuntimeException("用户账号已被禁用，无法更新资料");
        }

        // 调用 Service 更新，如果 Service 内部有错误（如手机号重复）会抛出异常，由全局处理器处理
        Users updatedUser = userService.updateUserProfile(currentUser.getId(), dto);
        updatedUser.setPassword(null); // 清除密码
        log.info("用户 {} 的个人资料已更新", userIdentifier);
        // 直接返回更新后的对象，由 Advice 包装
        return updatedUser;
    }

    /**
     * 用户登出 - 返回 void
     * 
     * @param authentication 当前认证信息
     */
    @PostMapping("/logout")
    public void logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            log.info("用户 '{}' 正在登出。", username);
            SecurityContextHolder.clearContext(); // 清除安全上下文
        } else {
            log.warn("未认证的用户尝试调用登出接口。");
        }
        // 成功则方法正常结束，Advice 会包装成 Result.success(null)
    }

    @PostMapping("/send-email-code")
    public Result<?> sendEmailVerificationCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            return Result.failed("邮箱不能为空");
        }
        try {
            boolean success = captchaService.sendCaptcha(email);
            if (success) {
                return Result.success(null, "验证码已发送至您的邮箱，有效期5分钟，请注意查收。");
            } else {
                // 此路径可能不会被命中，因为 CaptchaServiceImpl 中的失败通常会抛出异常
                return Result.failed("验证码发送失败，请稍后重试。");
            }
        } catch (RuntimeException e) {
            // 捕获 CaptchaService 中抛出的业务异常 (例如频率限制)
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            log.error("发送邮件验证码至 {} 时发生错误: {}", email, e.getMessage(), e);
            return Result.failed("发送验证码时发生未知系统错误。");
        }
    }
}