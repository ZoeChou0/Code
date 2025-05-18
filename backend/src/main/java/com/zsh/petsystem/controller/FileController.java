package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result; // 导入你的 Result 类
import org.springframework.beans.factory.annotation.Value; // 用于从配置文件读取路径
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct; // 用于初始化目录
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api") // 基础路径为 /api
@CrossOrigin // 允许跨域请求 (如果你的全局配置已允许，这里可能非必需)
public class FileController {

  // --- 配置项 ---
  // 文件存储的基础路径 (推荐在 application.yml 中配置)
  @Value("${file.upload-dir:/path/to/your/upload/directory}") // 在 application.yml 中添加 file.upload-dir
  private String uploadDir;

  // 访问上传文件的基础 URL (需要与你的静态资源配置匹配)
  @Value("${file.access-url-prefix:/uploads/}") // 在 application.yml 中添加 file.access-url-prefix
  private String accessUrlPrefix;

  // 确保上传目录在应用启动时存在
  @PostConstruct
  public void init() {
    try {
      Path path = Paths.get(uploadDir);
      if (!Files.exists(path)) {
        Files.createDirectories(path);
        System.out.println("创建上传目录成功: " + uploadDir);
      }
    } catch (IOException e) {
      System.err.println("无法创建上传目录: " + uploadDir + "，请检查权限或路径配置！");
      // 抛出运行时异常可能导致应用启动失败，根据需要处理
      // throw new RuntimeException("无法创建上传目录", e);
    }
  }

  /**
   * 处理文件上传请求 (对应前端 action="/api/upload")
   * 
   * @param file 前端上传的文件 (参数名 "file" 需要与 el-upload 的 name 属性匹配，默认为 "file")
   * @return 包含文件访问 URL 的 Result 对象
   */
  @PostMapping("/upload")
  public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {

    // 1. 检查文件是否为空
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body(Result.failed("上传文件不能为空"));
    }

    // 2. 检查文件类型 (基础示例，可以根据需要扩展更严格的检查)
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return ResponseEntity.badRequest().body(Result.failed("只允许上传图片文件"));
    }

    // 3. 生成唯一文件名，保留原始扩展名
    String originalFilename = file.getOriginalFilename();
    String fileExtension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    // 使用 UUID 防止文件名冲突
    String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

    // 4. 构建目标文件路径
    // 使用 Paths.get().resolve() 来安全地拼接路径
    Path destinationFilePath = Paths.get(this.uploadDir).resolve(uniqueFileName).normalize();

    // 确保目标路径在预期的上传目录下 (简单安全检查)
    if (!destinationFilePath.getParent().equals(Paths.get(this.uploadDir).normalize())) {
      System.err.println("警告：尝试保存文件到非法路径: " + destinationFilePath);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failed("文件保存路径错误"));
    }

    try {
      // 5. 保存文件到服务器本地
      // 方法一：使用 transferTo (更常用)
      file.transferTo(destinationFilePath.toFile());

      // 方法二：使用 Files.copy (需要处理 InputStream)
      // try (InputStream inputStream = file.getInputStream()) {
      // Files.copy(inputStream, destinationFilePath,
      // StandardCopyOption.REPLACE_EXISTING);
      // }

      // 6. 构建可访问的 URL
      // 确保 accessUrlPrefix 以 / 结尾，uniqueFileName 不以 / 开头
      String fileAccessUrl = (accessUrlPrefix.endsWith("/") ? accessUrlPrefix : accessUrlPrefix + "/") + uniqueFileName;

      // 7. 返回成功响应，包含 URL (格式要与前端 handleUploadSuccess 匹配)
      // 使用 Map 包装 URL，符合 { "url": "..." } 格式
      Map<String, String> responseData = Map.of("url", fileAccessUrl);
      return ResponseEntity.ok(Result.success(responseData, "文件上传成功"));

    } catch (IOException e) {
      // 8. 处理文件保存过程中的 IO 异常
      System.err.println("文件上传失败: " + e.getMessage());
      e.printStackTrace(); // 打印详细错误，方便调试
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Result.failed("文件上传失败，服务器内部错误"));
    } catch (Exception e) {
      // 处理其他可能的异常
      System.err.println("文件上传时发生未知错误: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Result.failed("文件上传时发生未知错误"));
    }
  }
}