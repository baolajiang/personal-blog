package com.myo.blog.utils;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/9/29 10:33
 */
public class ArticleUtils {

    // 定义一个更“酷”的乱码字符池 (包含块状字符、符号等)
    private static final String MASK_POOL = "▓▒░!@#$%^&*()_+{}|:<>?[]-=";
    // 如果喜欢纯黑客帝国风格，可以用这个: "01"
    // 如果喜欢纯块状风格，可以用这个: "█▓▒░"

    private static final Random RANDOM = new Random();

    /**
     * 生成加密/乱码字符串
     * @param length 参考的原文字符长度
     * @return 乱码字符串
     */
    public static String keys(int length) {
        if (length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // 改进点1：长度模糊化
        // 不直接使用 length，而是生成一个 length * 0.8 到 length * 1.5 之间的随机长度
        // 这样可以防止通过乱码长度猜测原文长度
        int fuzzyLength = (int) (length * (0.8 + RANDOM.nextDouble() * 0.7));

        // 设定一个最小长度，保证至少显示一点东西 (例如标题太短时)
        if (fuzzyLength < 5) {
            fuzzyLength = 5 + RANDOM.nextInt(5);
        }

        for (int i = 0; i < fuzzyLength; i++) {
            // 改进点2：随机取字符
            int index = RANDOM.nextInt(MASK_POOL.length());
            sb.append(MASK_POOL.charAt(index));

            // 改进点3：偶尔插入空格，让它看起来更像一段文字/代码，而不是一长串毫无意义的符号
            // (大约每10个字符插入一个空格)
            if (i > 0 && i % (8 + RANDOM.nextInt(5)) == 0) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}