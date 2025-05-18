package com.zsh.petsystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private CurrentUserArgumentResolver currentUserArgumentResolver;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url-prefix}")
    private String accessUrlPrefix;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String urlPath = accessUrlPrefix.endsWith("/") ? accessUrlPrefix + "**" : accessUrlPrefix + "/**";
        // 确保 uploadDir 是一个绝对路径，并且以文件系统分隔符结尾 (根据操作系统)
        // 使用 "file:" 前缀表示本地文件系统路径
        String location = "file:" + (uploadDir.endsWith(File.separator) ? uploadDir : uploadDir + File.separator);

        System.out.println("配置静态资源映射：URL路径 " + urlPath + " -> 本地路径 " + location);

        registry.addResourceHandler(urlPath) // 前端访问的URL路径模式
                .addResourceLocations(location); // 映射到的服务器本地文件目录
    }
}