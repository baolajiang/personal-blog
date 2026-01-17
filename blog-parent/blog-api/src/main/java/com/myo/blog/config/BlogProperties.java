package com.myo.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 博客专用配置类
 * 对应 application.properties 中的 blog.xxx 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog")
public class BlogProperties {

    /**
     * 机器码 (1-99)
     * 对应配置: blog.machine.id
     */
    private Machine machine = new Machine();

    @Data
    public static class Machine {
        private Long id = 1L; // 默认值 1
    }
}