package com.myo.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.dao.mapper.SysUserMapper;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.entity.params.PageParams;
import com.myo.blog.entity.params.UserParam;
import com.myo.blog.service.LoginService;
import com.myo.blog.service.SysUserService;
import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.LoginUserVo;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.UserVo;

import org.springframework.context.annotation.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.myo.blog.dao.mapper.UserTokenMapper; // 引入 Mapper
import com.myo.blog.dao.pojo.UserToken;         // 引入实体
import java.util.concurrent.TimeUnit;
/**
 * 用户服务实现类
 * 处理用户相关的业务逻辑，如查询、注册、登录、更新等
 */
@Service
@Transactional // 2. 类级别开启事务，确保数据一致性
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    @Lazy  // 2. 添加这个注解，打破循环依赖
    private LoginService loginService;
    @Autowired
    private UserTokenMapper userTokenMapper;
    @Override
    public UserVo findUserVoById(Long id) {

        SysUser sysUser = sysUserMapper.selectById(id);
        if(id==0){//这里表示评论区的回复那个评论的id 0表示在子评论的自己不回复自己评论
            return new UserVo();
        }

        if (sysUser == null){
            sysUser = new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar("/static/img/tx.gif");
            sysUser.setNickname("baola");
        }
        UserVo userVo  = new UserVo();
        BeanUtils.copyProperties(sysUser,userVo);
        userVo.setId(String.valueOf(sysUser.getId()));
        return userVo;
    }

    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser == null){
            sysUser = new SysUser();
            sysUser.setNickname("myo");
        }
        return sysUser;
    }

    @Override
    public SysUser findUser(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword,password);
        queryWrapper.select(SysUser::getAccount,
                SysUser::getStatus,
                SysUser::getId,
                SysUser::getAvatar,
                SysUser::getNickname,
                SysUser::getMobilePhoneNumber,
                SysUser::getEmail);
        queryWrapper.last("limit 1");

        return sysUserMapper.selectOne(queryWrapper);
    }
    @Override
    public SysUser findIpaddr(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword,password);
        queryWrapper.select(SysUser::getIpaddr);
        //queryWrapper.select(SysUser::getAccount,SysUser::getId,SysUser::getAvatar,SysUser::getNickname);

        queryWrapper.last("limit 1");

        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Result findUserByToken(String token) {
        /**
         * 1. token合法性校验
         *    是否为空，解析是否成功 redis是否存在
         * 2. 如果校验失败 返回错误
         * 3. 如果成功，返回对应的结果 LoginUserVo
         */
        SysUser sysUser = loginService.checkToken(token);
        if (sysUser == null){
            return Result.fail(ErrorCode.TOKEN_ERROR.getCode(),ErrorCode.TOKEN_ERROR.getMsg());
        }
        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setId(String.valueOf(sysUser.getId()));
        loginUserVo.setNickname(sysUser.getNickname());
        loginUserVo.setAvatar(sysUser.getAvatar());
        loginUserVo.setAccount(sysUser.getAccount());
        loginUserVo.setEmail(sysUser.getEmail());
        loginUserVo.setMobilePhoneNumber(sysUser.getMobilePhoneNumber());
        return Result.success(loginUserVo);
    }
    //查询redis中报存的用户信息
    public SysUser queryUserByToken(String token) {
        SysUser sysUser = loginService.checkToken(token);
        return sysUser;
    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.last("limit 1");
        return this.sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public UserVo findUserByAccount(UserParam user) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,user.getAccount())
                .eq(SysUser::getId,user.getId());
        queryWrapper.last("limit 1");
        SysUser sysUser=this.sysUserMapper.selectOne(queryWrapper);
        /*用户信息视图*/
        UserVo userVo  = new UserVo();
        userVo.setId(sysUser.getId().toString());
        userVo.setAccount(sysUser.getAccount());
        userVo.setNickname(sysUser.getNickname());
        userVo.setAvatar(sysUser.getAvatar());
        userVo.setEmail(sysUser.getEmail());
        userVo.setMobilePhoneNumber(sysUser.getMobilePhoneNumber());

        return userVo;
    }
    /*
      @Transactional：
     *
     * 加在了类名上方。

     * 作用：如果 sysUserMapper.update 成功了，但假设后面的代码抛出了异常，数据库的操作会自动回滚，保证数据不会脏读。

     * updateRedisCache 方法 (解决缓存不同步)：

     * 这是一个关键的辅助方法。

     * 逻辑：它利用了我们之前建立的 反向索引 (USER_TOKEN_userId)。

     * 它拿到 Token 后，重新把数据库里最新的 SysUser 对象序列化成 JSON，覆盖掉 Redis 里旧的 JSON。

     * 这样，用户一下次刷新页面，checkToken 从 Redis 拿到的就是新名字、新头像了！
      */

    /**
     * 修改用户信息 (资料更新)
     * 注意：不包含密码修改，密码修改建议单独走 changePassword 接口
     */
    @Override
    public int updateUser(UserParam userParam) {
        String id = userParam.getId();

        // 1. 参数校验 (防止 ID 为空)
        if (StringUtils.isBlank(id)) {
            // 可以在这里抛出自定义业务异常
            return 0;
        }

        // 2. 组装动态 SQL
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, id);

        boolean hasUpdate = false;
        // 定义一个标志位，判断是否需要踢下线
        boolean needKickout = false;
        if (StringUtils.isNotBlank(userParam.getStatus())) {
            updateWrapper.set(SysUser::getStatus, userParam.getStatus());
            hasUpdate = true;
            if ("99".equals(userParam.getStatus())) {
                needKickout = true;
            }
        }
        if (StringUtils.isNotBlank(userParam.getNickname())) {
            updateWrapper.set(SysUser::getNickname, userParam.getNickname());
            hasUpdate = true;
        }
        if (StringUtils.isNotBlank(userParam.getAvatar())) {
            updateWrapper.set(SysUser::getAvatar, userParam.getAvatar());
            hasUpdate = true;
        }
        if (StringUtils.isNotBlank(userParam.getEmail())) {
            // 优化：可以在这里先查一下邮箱是否已被其他人占用
            // checkEmailExist(userParam.getEmail());
            updateWrapper.set(SysUser::getEmail, userParam.getEmail());
            hasUpdate = true;
        }
        if (StringUtils.isNotBlank(userParam.getMobilePhoneNumber())) {
            updateWrapper.set(SysUser::getMobilePhoneNumber, userParam.getMobilePhoneNumber());
            hasUpdate = true;
        }
        // === 处理封禁逻辑 ===
        if (StringUtils.isNotBlank(userParam.getStatus())) {
            updateWrapper.set(SysUser::getStatus, userParam.getStatus());
            hasUpdate = true;

            // 如果状态是 99 (封禁)，标记为需要踢下线
            if ("99".equals(userParam.getStatus())) {
                needKickout = true;
            }
        }


        // 3. 执行数据库更新
        if (hasUpdate) {
            int rows = this.sysUserMapper.update(null, updateWrapper);

            if (rows > 0) {
                // 如果是封禁，直接踢下线（删缓存）
                if (needKickout) {
                    kickUserOffline(Long.parseLong(id));
                } else {
                    // 否则只是更新缓存信息
                    updateRedisCache(Long.parseLong(id));
                }
            }
            return rows;
        }

        return 0;
    }

    /**
     * 辅助方法：更新 Redis 中的用户信息
     */
    private void updateRedisCache(Long userId) {
        // A. 通过反向索引找到 Token
        String token = stringRedisTemplate.opsForValue().get("USER_TOKEN_" + userId);

        if (StringUtils.isNotBlank(token)) {
            // B. 从数据库查询最新的用户信息 (确保是全量最新数据)
            SysUser newestUser = sysUserMapper.selectById(userId);

            if (newestUser != null) {
                // C. 覆盖 Redis 中的旧数据 (保持过期时间一致，或重置)
                // 这里我们简单起见，重置为 3 天，或者读取旧 Key 的 TTL
                stringRedisTemplate.opsForValue().set("TOKEN_" + token, JSON.toJSONString(newestUser), 3, TimeUnit.DAYS);
            }
        }
    }


    @Override
    public int updateUserAvatar(UserParam userParam){


        return updateUser(userParam);
    }

    @Override
    public SysUser findUserByAccount(SysUser user) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,user.getAccount());
        queryWrapper.eq(SysUser::getId,user.getId());
        queryWrapper.last("limit 1");
        return this.sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(SysUser sysUser) {
        //保存用户这 id会自动生成
        //这个地方 默认生成的id是 分布式id 雪花算法
        //mybatis-plus
        this.sysUserMapper.insert(sysUser);
    }
    // 引入 StringUtils 工具类 (org.apache.commons.lang3.StringUtils)





    @Override
    public boolean updateById(SysUser sysUser) {
        // 根据用户ID更新用户信息
        // 使用MyBatis-Plus的updateById方法，根据实体类主键进行更新
        // 注意：sysUser对象必须包含有效的ID字段值
        return this.sysUserMapper.updateById(sysUser) > 0;
    }

    public List<SysUser> findUserByIds(Collection<Long> ids) {
        // 使用已經注入的 sysUserMapper，而不是 baseMapper
        return sysUserMapper.selectBatchIds(ids);
    }

    @Override
    public Result UserList(PageParams pageParams) {
        // 1. 分页查询 (按注册时间倒序)
        Page<SysUser> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.orderByDesc(SysUser::getCreateDate);

        Page<SysUser> sysUserPage = sysUserMapper.selectPage(page, queryWrapper);
        List<SysUser> records = sysUserPage.getRecords();

        if (records.isEmpty()) {
            return Result.success(sysUserPage);
        }

        // 2. 遍历处理 (填充在线状态 + 抹除密码)
        // 简单的循环查 Redis 在分页场景下性能是可以接受的
        for (SysUser u : records) {
            // A. 抹除敏感信息
            u.setPassword(null);
            u.setSalt(null);

            // B. 检查是否在线
            // 逻辑：只要 Redis 里有 USER_TOKEN_ID 这个Key，就说明 Token 没过期
            String key = "USER_TOKEN_" + u.getId();
            Boolean isOnline = stringRedisTemplate.hasKey(key);
            u.setOnline(isOnline);
        }

        return Result.success(sysUserPage);
    }



    // 更新用户状态（包含强制踢下线）
    @Override
    public Result updateUserStatus(UserParam userParam) {
        String id = userParam.getId();
        if (StringUtils.isBlank(id)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "用户ID不能为空");
        }

        // 校验状态是否为空
        if (StringUtils.isBlank(userParam.getStatus())) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "状态不能为空");
        }

        try {
            // 1. 先检查用户是否存在
            Long userId = Long.parseLong(id);
            SysUser existingUser = sysUserMapper.selectById(userId);
            if (existingUser == null) {
                return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "用户不存在，ID: " + id);
            }

            // 2. 更新数据库
            LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SysUser::getId, userId)
                    .set(SysUser::getStatus, userParam.getStatus());

            int rows = this.sysUserMapper.update(null, updateWrapper);

            if (rows > 0) {
                // 3. 同步 Redis 缓存
                // 如果是封禁操作，强制踢下线；否则只更新缓存
                if ("99".equals(userParam.getStatus())) {
                    kickUserOffline(userId);
                } else {
                    updateRedisCache(userId);
                }
                return Result.success("操作成功");
            } else {
                // 添加日志输出，帮助调试
                System.out.println("数据库更新失败，用户ID: " + userId + ", 状态: " + userParam.getStatus());
                return Result.fail(ErrorCode.OPERATION_FAILED.getCode(), "操作失败，可能用户不存在或数据未变化");
            }
        } catch (NumberFormatException e) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "用户ID格式错误: " + id);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常");
        }
    }

    // 强制踢下线逻辑（私有方法，供内部调用）
    private void kickUserOffline(Long userId) {
        // A. 尝试获取 Token
        String tokenKey = "USER_TOKEN_" + userId;
        String token = stringRedisTemplate.opsForValue().get(tokenKey);

        // B. 如果 Redis 没拿到，尝试从数据库 UserToken 表拿 (防止漏网之鱼)
        if (StringUtils.isBlank(token)) {
            LambdaQueryWrapper<UserToken> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserToken::getUserId, userId);
            UserToken userToken = userTokenMapper.selectOne(queryWrapper);
            if (userToken != null) {
                token = userToken.getToken();
            }
        }

        // C. 删除 Redis 里的 Token (杀掉会话)
        if (StringUtils.isNotBlank(token)) {
            stringRedisTemplate.delete("TOKEN_" + token);
        }
        stringRedisTemplate.delete(tokenKey);

        // D. 删除 MySQL 里的 Token (防止复活)
        LambdaQueryWrapper<UserToken> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserToken::getUserId, userId);
        userTokenMapper.delete(deleteWrapper);

        // E. 删除所有相关的缓存Key
        stringRedisTemplate.delete("USER_STATUS_" + userId);
        stringRedisTemplate.delete("USER_INFO_" + userId);
        stringRedisTemplate.delete("ONLINE_USER_" + userId);
        stringRedisTemplate.delete("USER_PERMISSIONS_" + userId);

        System.out.println("用户 " + userId + " 被强制踢下线，删除所有相关缓存");
    }





}
