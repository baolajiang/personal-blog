package com.myo.blog.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myo.blog.dao.mapper.IpBlacklistMapper;
import com.myo.blog.dao.pojo.IpBlacklist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目启动时执行：将数据库中的黑名单同步到 Redis
 */
@Slf4j
@Component
public class IpBlacklistRunner implements CommandLineRunner {

    @Autowired
    private IpBlacklistMapper ipBlacklistMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info(">>> 开始加载 IP 黑名单到 Redis...");

        // 1. 查询所有黑名单
        List<IpBlacklist> blacklists = ipBlacklistMapper.selectList(new LambdaQueryWrapper<>());

        // 2. 写入 Redis
        int count = 0;
        for (IpBlacklist record : blacklists) {
            String key = "BAN:IP:" + record.getIp();
            // 如果Redis里没有，就放进去
            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, "Restored from DB: " + record.getReason());
                count++;
            }
        }

        log.info(">>> IP 黑名单加载完毕，共同步 {} 条数据", count);
    }
}