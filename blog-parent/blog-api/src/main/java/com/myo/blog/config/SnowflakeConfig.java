package com.myo.blog.config;

import com.myo.blog.utils.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    /**
     * 配置雪花算法ID生成器
     * 机器ID：1，数据中心ID：1
     * 可以根据实际部署环境调整
     */
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(1, 1);
    }
}