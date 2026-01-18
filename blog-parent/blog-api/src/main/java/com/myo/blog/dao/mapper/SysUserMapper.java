package com.myo.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myo.blog.dao.pojo.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface SysUserMapper extends BaseMapper<SysUser> {
     /**
     * 根据用户ID查询权限标识列表
     * @param userId 用户ID
     * @return 权限标识列表
     */
     //这个方法，对应 XML 里的 <select id="findPermissionsByUserId">
    List<String> findPermissionsByUserId(String userId);

    /**
     * 插入用户角色关联关系
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void insertUserRole(@Param("userId") String userId, @Param("roleId") Long roleId);

}
