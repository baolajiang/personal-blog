package com.myo.blog.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("myo_ip_blacklist")
/**
 * IP 黑名单
 */
public class IpBlacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ip;
    private Long createDate;
    private String reason;
}