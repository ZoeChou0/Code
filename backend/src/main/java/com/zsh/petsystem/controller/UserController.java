// package com.zsh.petsystem.controller;

// import com.zsh.petsystem.common.Result;
// import com.zsh.petsystem.dto.LoginRequestDTO;
// import com.zsh.petsystem.dto.UserUpdateProfileDTO;
// import com.zsh.petsystem.model.Users;
// import com.zsh.petsystem.service.UserService;
// import com.zsh.petsystem.util.JwtUtil;

// import lombok.extern.slf4j.Slf4j;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus; // 导入 HttpStatus
// import org.springframework.http.ResponseEntity;
// // 导入 PasswordEncoder (你需要添加 Spring Security 依赖并配置 Bean)
// // import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;

// import java.util.Collections;
// import java.util.List;
// import java.util.Map; // 导入 Map

// @RestController
// @RequestMapping("/users")
// @CrossOrigin
// @Slf4j
// public class UserController {

//     @Autowired
//     private UserService userService;

//     // 注入 PasswordEncoder Bean (需要在配置类中定义 @Bean public PasswordEncoder
//     // passwordEncoder())
//     // @Autowired
//     // private PasswordEncoder passwordEncoder; // 取消注释并确保已配置

//     /**
//      * 添加用户 (可能需要管理员权限)
//      * 
//      * @param user 用户信息
//      * @return ResponseEntity
//      */
//     @PostMapping("/add")
//     // TODO: 添加权限控制, 例如 @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<?> addUser(@RequestBody Users user) {
//         // TODO: 在保存前进行密码哈希处理
//         // String hashedPassword = passwordEncoder.encode(user.getPassword());
//         // user.setPassword(hashedPassword);

//         try {
//             userService.saveUser(user); // 假设 saveUser 内部处理了哈希 (更好的方式)
//             // 或者直接在这里处理哈希后调用 save
//             return ResponseEntity.status(HttpStatus.CREATED).body("新用户添加成功"); // 201 Created
//         } catch (Exception e) {
//             // 处理可能的异常，例如数据库错误
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("添加用户失败: " + e.getMessage());
//         }
//     }

//     /**
//      * 用户注册
//      * 
//      * @param user 注册用户信息 (包含 email, phone, password 等)
//      * @return ResponseEntity
//      */
//     @PostMapping("/register")
//     public ResponseEntity<?> register(@RequestBody Users user) {
//         // ... (检查邮箱、手机号是否存在的代码) ...

//         // **设置默认角色** (取消注释或添加这行)
//         if (user.getRole() == null || user.getRole().isEmpty()) { // 只有当角色未指定时才设置默认值
//             user.setRole("user"); // 设置默认角色为 "user"
//         }

//         // ... (密码加密的代码) ...

//         try {
//             userService.saveUser(user); // 保存带有角色的用户
//             return ResponseEntity.status(HttpStatus.CREATED).body(Result.success("注册成功")); // 使用 Result 包装
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Result.failed("注册失败: " + e.getMessage()));
//         }
//     }

//     /**
//      * 获取所有用户 (需要管理员权限)
//      * 
//      * @return 用户列表
//      */
//     @GetMapping("/getAll")
//     // TODO: 添加权限控制, 例如 @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<?> getAllUsers() {
//         try {
//             List<Users> users = userService.getAllUsers();
//             return ResponseEntity.ok(users);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取用户列表失败: " + e.getMessage());
//         }
//     }

//     /**
//      * 用户登录
//      * 
//      * @param user 包含登录凭证 (email, password) 的对象
//      * @return ResponseEntity 包含 JWT Token 或错误信息
//      */
//     @PostMapping("/login")
//     public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {

//         Users dbUser = userService.findUserByIdentifier(loginRequest.getIdentifier());

//         if (dbUser == null) {
//             // 统一错误提示
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户名、邮箱或手机号或密码错误"));
//         }

//         // --- 安全警告：移除哈希验证，使用明文比较 ---
//         // **极其不安全！仅用于临时测试！**
//         // 原来的哈希比较:
//         // boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(),
//         // dbUser.getPassword());
//         // if (!passwordMatches) { ... }

//         // **修改为明文比较:**
//         if (!loginRequest.getPassword().equals(dbUser.getPassword())) {
//             log.warn("正在使用不安全的明文密码比较进行登录验证！"); // 添加日志警告
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户名、邮箱或手机号或密码错误"));
//         }
//         // --- 明文比较结束 ---

//         // 密码验证通过，生成 JWT
//         String tokenValue = JwtUtil.generateToken(dbUser.getId(), dbUser.getEmail(), dbUser.getRole());
//         Map<String, String> tokenData = Collections.singletonMap("token", tokenValue);

//         return ResponseEntity.ok(Result.success(tokenData));
//     }

//     @GetMapping("/info")
//     public ResponseEntity<?> getUserInfo(Authentication authentication) {
//         log.info("--- Entering /users/info endpoint ---");
//         if (authentication != null) {
//             log.info("Received Authentication object: Principal={}, IsAuthenticated={}, Authorities={}",
//                     authentication.getPrincipal(),
//                     authentication.isAuthenticated(),
//                     authentication.getAuthorities());
//         } else {
//             log.warn("Received null Authentication object in /users/info");
//         }
//         if (authentication == null || !authentication.isAuthenticated()) {
//             log.warn("/users/info accessed without proper authentication.");
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户未认证"));
//         }
//         String userEmail = authentication.getName();
//         log.info("Fetching info for user: {}", userEmail);
//         Users user = userService.findByEmail(userEmail);
//         if (user == null) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.failed("用户未找到"));
//         }
//         // 重要：返回前清除密码等敏感信息
//         user.setPassword(null);
//         log.info("Returning user info successfully for: {}", userEmail);
//         return ResponseEntity.ok(Result.success(user)); // 使用 Result 包装
//     }

//     @PutMapping("/profile")
//     public ResponseEntity<?> updateUserProfile(@RequestBody UserUpdateProfileDTO dto, Authentication authentication) {
//         if (authentication == null || !authentication.isAuthenticated()) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.failed("用户未认证"));
//         }
//         String userEmail = authentication.getName();
//         Users currentUser = userService.findByEmail(userEmail);
//         if (currentUser == null) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.failed("用户未找到"));
//         }

//         try {
//             Users updatedUser = userService.updateUserProfile(currentUser.getId(), dto);
//             return ResponseEntity.ok(Result.success(updatedUser));
//         } catch (RuntimeException e) {
//             // 更精细的错误处理，例如手机号重复等
//             return ResponseEntity.badRequest().body(Result.failed("更新失败: " + e.getMessage()));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failed("更新时发生服务器错误"));
//         }
//     }

//     @PostMapping("/logout") // 使用 POST 请求更符合语义
//     public ResponseEntity<?> logout(Authentication authentication) {
//         if (authentication != null && authentication.isAuthenticated()) {
//             String username = authentication.getName(); // 获取当前用户名 (邮箱)
//             log.info("User '{}' logging out.", username);

//             SecurityContextHolder.clearContext();

//             return ResponseEntity.ok(Result.success(null, "登出成功"));
//         } else {
//             // 如果用户未认证就调用 logout，可以返回错误或直接成功
//             log.warn("Unauthenticated user attempted to call logout.");
//             // 返回成功，避免泄露用户是否登录的信息
//             return ResponseEntity.ok(Result.success(null, "已登出"));
//         }
//     }
// }

package com.zsh.petsystem.controller;

import com.zsh.petsystem.dto.LoginRequestDTO;
import com.zsh.petsystem.dto.UserUpdateProfileDTO;
import com.zsh.petsystem.entity.Users;
import com.zsh.petsystem.service.UserService;
import com.zsh.petsystem.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus; // 不再需要
// import org.springframework.http.ResponseEntity; // 不再需要
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

    // 假设 PasswordEncoder Bean 已配置并在需要时注入
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    /**
     * 添加用户 (权限待定) - 返回 void
     * 假设 userService.saveUser 内部处理密码哈希
     * 
     * @param user 用户信息
     */
    @PostMapping("/add")
    // TODO: 添加权限控制, 例如 @PreAuthorize("hasRole('ADMIN')")
    public void addUser(@RequestBody Users user) {
        // TODO: 确认 userService.saveUser 是否处理密码哈希，否则需要在这里处理
        // if (passwordEncoder != null && user.getPassword() != null) {
        // user.setPassword(passwordEncoder.encode(user.getPassword()));
        // } else if (passwordEncoder == null) {
        // log.warn("PasswordEncoder 未注入，密码未哈希！");
        // // 或者抛出配置异常 throw new IllegalStateException("PasswordEncoder not
        // configured");
        // }

        // 如果 saveUser 内部有唯一约束（如 email），重复添加会抛出异常，由 GlobalExceptionHandler 处理
        userService.saveUser(user);
        log.info("新用户 {} 添加成功 (ID: {})", user.getEmail(), user.getId());
        // 成功时方法正常结束，会被 GlobalResponseAdvice 包装成 Result.success(null)
    }

    /**
     * 用户注册 - 返回 void
     * 
     * @param user 注册用户信息
     */
    @PostMapping("/register")
    public void register(@RequestBody Users user) {
        // 检查邮箱、手机号是否已存在 (如果需要)
        if (userService.existByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        if (user.getPhone() != null && userService.existByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 设置默认角色
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("user");
        }
        // 设置默认状态
        user.setStatus("active"); // 确保新用户状态是 active

        // TODO: 确认密码哈希是在 saveUser 内部还是需要在这里处理
        // if (passwordEncoder != null && user.getPassword() != null) {
        // user.setPassword(passwordEncoder.encode(user.getPassword()));
        // }

        userService.saveUser(user);
        log.info("用户 {} 注册成功", user.getEmail());
        // 成功时方法正常结束
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
}