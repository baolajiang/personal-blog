package com.myo.blog.dao.pojo;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;

@Data
@TableName("myo_sys_user_token")
public class UserToken {
    @TableId
    // 这里不需要自增，因为是直接用 userId 作为主键 (1对1关系)
    private Long userId;
    private String token;
    private Long expireTime;
}