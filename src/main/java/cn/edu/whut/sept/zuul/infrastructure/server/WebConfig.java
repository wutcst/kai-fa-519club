package cn.edu.whut.sept.zuul.infrastructure.server;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Vue 前端 Web 配置：CORS 与 GUI 静态资源。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Path AVATAR_DIR =
        Paths.get("data", "uploads", "avatars").toAbsolutePath().normalize();

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    /**
     * 暴露 classpath:/assets/gui 与本地头像目录。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/assets/");

        String avatarLocation = AVATAR_DIR.toUri().toASCIIString();
        if (!avatarLocation.endsWith("/")) {
            avatarLocation = avatarLocation + "/";
        }
        registry.addResourceHandler("/uploads/avatars/**")
            .addResourceLocations(avatarLocation);
    }
}
