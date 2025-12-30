package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ArticleBody {
    // 同样修改为雪花算法
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    private String content;
    private String contentHtml;
    private Long articleId;
}
