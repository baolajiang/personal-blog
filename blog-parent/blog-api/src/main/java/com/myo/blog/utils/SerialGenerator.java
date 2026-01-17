package com.myo.blog.utils;

import com.myo.blog.config.BlogProperties; // 导入配置类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
/**
 * 全局唯一ID生成器
 * 1. 时间部分（14位）：精确到毫秒级别的时间戳。
 * 2. 机器码部分（2位）：01-99，用于区分不同的部署实例。
 * 3. 随机数部分（2位）：10-99，增加随机性，防止碰撞。
 * 4. 序列号部分（4位）：1000-9999，用于在同一毫秒内生成多个ID。
 */
@Component
public class SerialGenerator {

    private static Long MACHINE_ID;

    // 注入强类型配置类，而不是用 @Value
    @Autowired
    private BlogProperties blogProperties;

    private static final AtomicLong atomicSeq = new AtomicLong(1);

    @PostConstruct
    public void init() {
        // 从对象中获取属性
        MACHINE_ID = blogProperties.getMachine().getId();

        if (MACHINE_ID == null || MACHINE_ID < 1 || MACHINE_ID > 99) {
            MACHINE_ID = 1L;
        }
        System.out.println("当前机器码已初始化为: " + MACHINE_ID);
    }

    // 生成全局唯一的字符串
    public static String generate(String bizPrefix) {
        // A. 时间部分
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());

        // B. 机器码部分
        String machineStr = String.format("%02d", MACHINE_ID);

        // C. 随机数部分
        int randomNum = ThreadLocalRandom.current().nextInt(10, 99);

        // D. 序列号部分
        long seq = atomicSeq.addAndGet(ThreadLocalRandom.current().nextInt(1, 3));
        if (seq > 9999) {
            atomicSeq.set(1);
            seq = 1;
        }
        String seqStr = String.format("%04d", seq);

        return bizPrefix + dateStr + machineStr + randomNum + seqStr;
    }
}