package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ArticleTag {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String articleId;

    private String tagId;
}
