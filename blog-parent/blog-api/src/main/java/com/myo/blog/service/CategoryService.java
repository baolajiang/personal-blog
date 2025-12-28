package com.myo.blog.service;

import com.myo.blog.entity.CategoryVo;
import com.myo.blog.entity.Result;
import com.myo.blog.dao.pojo.Category;
import java.util.Collection;
import java.util.List;

public interface CategoryService {

    CategoryVo findCategoryById(Long categoryId);

    Result findAll();

    Result findAllDetail();

    Result categoryDetailById(Long id);

    /**
     * 批量查詢分類
     */
    List<Category> findCategoryByIds(Collection<Long> ids);
}
