package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myo.blog.dao.pojo.IpBlacklist;
import org.apache.ibatis.annotations.Mapper;
/**
 * IP 黑名单映射器
 */
@Mapper
public interface IpBlacklistMapper extends BaseMapper<IpBlacklist> {

}