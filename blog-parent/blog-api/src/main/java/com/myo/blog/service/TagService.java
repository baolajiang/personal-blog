package com.myo.blog.service;

import com.myo.blog.entity.Result;
import com.myo.blog.entity.TagVo;

import java.util.List;

public interface TagService {

    List<TagVo> findTagsByArticleId(String articleId); // Long -> String

    Result hots(int limit);

    /**
     * 查询所有的文章标签
     * @return
     */
    Result findAll();

    Result findAllDetail();

    Result findDetailById(String id); // Long -> String
}