package com.myo.blog.dao.pojo;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;

@Data
@TableName("myo_sys_user_token")
public class UserToken {
    @TableId(type = IdType.ASSIGN_UUID)
    private String userId;
    private String token;
    private Long expireTime;
}