package com.myo.blog.task;

import com.myo.blog.config.IpBlacklistRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 【黑名单哨兵任务】
 * 作用：利用“哨兵标记”机制，监控 Redis 中的黑名单数据是否丢失。
 * 场景：防止 Redis 服务重启后内存数据被清空，导致黑名单失效。
 */
@Slf4j
@Component
@EnableScheduling
public class BlacklistSyncTask {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 注入启动加载器，用于执行“从数据库恢复数据”的操作
    @Autowired
    private IpBlacklistRunner ipBlacklistRunner;

    /**
     * 定时巡检任务
     * 执行频率：每 30 秒执行一次 (0/30 * * * * ?)
     * 性能说明：
     * 1. 绝大多数情况（Redis正常）只查 1 个 Key，耗时 < 1ms，对数据库 0 压力。
     * 2. 只有当 Redis 数据丢失时，才会触发查询数据库。
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void checkRedisStatus() {
        try {
            // ==========================================
            // 第一步：检查“哨兵旗帜”是否还在
            // ==========================================
            // BLACKLIST_MARKER_KEY 是我们在项目启动(IpBlacklistRunner)时写入的一个永久 Key (例如 "BAN:IS_LOADED")
            Boolean hasData = redisTemplate.hasKey(IpBlacklistRunner.BLACKLIST_MARKER_KEY);// 定期检查（哨兵巡逻）

            // ==========================================
            // 第二步：正常情况（Redis 活着）
            // ==========================================
            // 如果 hasData 为 true，说明 Redis 没重启过，黑名单数据肯定是全的。
            if (hasData) {  // 2. 健康检查（哨兵观察）
                // 直接结束任务，不去打扰 MySQL 数据库，保证高性能。
                return; // 正常情况
            }

            // ==========================================
            // 第三步：异常情况（Redis 重启/清空）
            // ==========================================
            // 走到这里说明标记没了！意味着 Redis 内存被清空了，黑名单失效了！
            log.warn(">>> 🚨 警报：哨兵监测到 Redis 黑名单标记丢失（可能发生了重启），正在执行紧急恢复...");// 3. 故障检测与恢复（哨兵报警并修复）

            // 立即调用 Runner 的方法，从 MySQL 把所有黑名单重新加载到 Redis，并重新插上标记
            ipBlacklistRunner.loadBlacklist();

        } catch (Exception e) {
            // ==========================================
            // 第四步：灾难情况（Redis 挂了）
            // ==========================================
            // 如果连 redisTemplate.hasKey 都报错，说明 Redis 服务彻底挂了（连不上）。
            // 此时捕获异常，防止报错日志刷屏，等待运维修复 Redis。
            // (注：此时拦截器 IpBlackListInterceptor 会自动降级查数据库，不用担心安全问题)
            log.error(">>> ❌ 黑名单哨兵检查失败 (Redis连接异常): {}", e.getMessage());
        }
    }
}