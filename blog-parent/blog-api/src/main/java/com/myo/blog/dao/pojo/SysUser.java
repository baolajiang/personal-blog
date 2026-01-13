package com.myo.blog.dao.pojo;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.baomidou.mybatisplus.annotation.TableField;
@Data
public class SysUser {

//    @TableId(type = IdType.ASSIGN_ID) // 默认id类型
    // 以后 用户多了之后，要进行分表操作，id就需要用分布式id了
//    @TableId(type = IdType.AUTO) 数据库自增
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String account;

    private Integer admin;// 是否是管理员 0-不是 1-是

    private String avatar;

    private Long createDate;

    private Integer deleted;

    private String email;

    private Long lastLogin;

    private String mobilePhoneNumber;

    private String nickname;

    private String password;

    private String salt;

    private String status;

    private String ipaddr;

    private String LastIpaddr;

    @TableField(exist = false) // 表示数据库表里没有这一列
    private Boolean online;


}
