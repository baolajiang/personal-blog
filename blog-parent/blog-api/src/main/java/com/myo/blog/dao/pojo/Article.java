package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Article {

    public static final int Article_TOP = 1;

    public static final int Article_Common = 0;
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String title;

    private String summary;

    private Integer commentCounts;

    private Integer viewCounts;
    /*
    * 封面
    * */
    private String cover;

    /**
     * 作者id
     */
    private String authorId;
    /**
     * 内容id
     */
    private String bodyId;
    /**
     *类别id
     */
    private String categoryId;

    /**
     * 置顶
     */
    private Integer weight;


    /**
     * 创建时间
     */
    private Long createDate;

    /**
     * 是否显示
     */
    private Integer viewKeys;
}
