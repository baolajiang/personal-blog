package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ArticleBody {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;


    private String content;
    private String contentHtml;
    private String articleId;// 关联文章ID
}
