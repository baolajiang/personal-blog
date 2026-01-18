package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/8 15:35
 */
@Data
public class Message {
    // 1. ID 改为 String，并指定自定义 UUID 策略
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String content;

    // 2. 关联字段全部改成 String
    private String parentId; // 对应 parent_id

    private String authorId; // 对应 author_id (之前报错的就是这个)

    private String toUid;    // 对应 to_uid

    private Long createDate;

    private Integer level;

    private Integer state;
}