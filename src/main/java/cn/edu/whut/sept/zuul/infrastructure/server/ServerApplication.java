package cn.edu.whut.sept.zuul.infrastructure.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * F6 联机 Spring Boot 服务端入口。
 *
 * <p>启动：{@code mvn spring-boot:run -Dspring-boot.run.mainClass=cn.edu.whut.sept.zuul.infrastructure.server.ServerApplication}
 * 或运行打包后的可执行 JAR。</p>
 */
@SpringBootApplication
@ComponentScan(basePackages = "cn.edu.whut.sept.zuul.infrastructure.server")
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
