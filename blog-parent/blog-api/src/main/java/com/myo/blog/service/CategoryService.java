package com.myo.blog.service;

import com.myo.blog.entity.CategoryVo;
import com.myo.blog.entity.Result;
import com.myo.blog.dao.pojo.Category;
import java.util.Collection;
import java.util.List;

public interface CategoryService {

    CategoryVo findCategoryById(String categoryId); // Long -> String

    Result findAll();

    Result findAllDetail();

    Result categoryDetailById(String id); // Long -> String

    /**
     * 批量查询分类
     */
    List<Category> findCategoryByIds(Collection<String> ids); // Long -> String
}