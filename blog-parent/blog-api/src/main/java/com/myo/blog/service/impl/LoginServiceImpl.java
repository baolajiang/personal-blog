package com.myo.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.service.LoginService;
import com.myo.blog.service.MailService;
import com.myo.blog.service.SysUserService;
import com.myo.blog.utils.HttpContextUtils;
import com.myo.blog.utils.IpUtils;
import com.myo.blog.utils.JWTUtils;
import com.myo.blog.entity.ErrorCode;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.LoginParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    // 注入邮件发送器
    @Autowired
    private JavaMailSender mailSender;
    // 注入邮件服务类
    @Autowired
    private MailService mailService;

    // 从配置文件读取发件人，防止硬编码
    @Value("${spring.mail.username}")
    private String fromEmail;
    // 密码盐值，用于密码加密
    private static final String slat = "myo!@#";
    /**
     * 登录功能
     * @param loginParam
     * @return
     */
    @Override
    public Result login(LoginParam loginParam) {
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();

        log.debug("开始验证登录信息 - 账号: {}", account);

        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)){
            log.warn("登录参数为空 - 账号: {}, 密码长度: {}", account, password != null ? password.length() : 0);
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }

        password = DigestUtils.md5Hex(password + slat);
        log.debug("密码加密完成 - 账号: {}", account);

        SysUser sysUser = sysUserService.findUser(account,password);
        if (sysUser == null){
            log.warn("用户不存在或密码错误 - 账号: {}", account);
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(),ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }

        log.info("用户验证成功 - 用户ID: {}, 账号: {}, 昵称: {}", sysUser.getId(), account, sysUser.getNickname());

        String token = JWTUtils.createToken(sysUser.getId());
        //更新最后登录IP,并设置登录时间
        updateLoginInfo(sysUser.getId());
        saveTokenToRedis(token, sysUser);

        log.debug("登录信息更新完成 - 用户ID: {}, Token生成成功", sysUser.getId());
        return Result.success(token);
    }

    /**
     * 更新用户登录信息
     * @param userId 用户ID
     */
    public void updateLoginInfo(Long userId){
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip= IpUtils.getIpAddr(request);
        log.debug("更新用户登录信息 - 用户ID: {}, 登录IP: {}", userId, ip);
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastIpaddr(ip);
        user.setLastLogin(System.currentTimeMillis());
        this.sysUserService.updateById(user);
        log.debug("用户登录信息更新完成 - 用户ID: {}", userId);
    }

    @Override
    public SysUser checkToken(String token) {
        if (StringUtils.isBlank(token)){
            return null;
        }
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token);
        if (stringObjectMap == null){
            return null;
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if (StringUtils.isBlank(userJson)){
            return null;
        }
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);
        redisTemplate.expire("TOKEN_" + token, 3, TimeUnit.DAYS);
        redisTemplate.expire("USER_TOKEN_" + sysUser.getId(), 3, TimeUnit.DAYS);
        return sysUser;
    }

    @Override
    public Result logout(String token) {
        SysUser sysUser = checkToken(token);
        redisTemplate.delete("TOKEN_" + token);
        if (sysUser != null) {
            redisTemplate.delete("USER_TOKEN_" + sysUser.getId());
        }
        return Result.success(null);
    }

    // ================== 【核心修改区域：发送验证码】 ==================
    /**
     * 发送验证码接口（优化版）
     * 1. 生成验证码
     * 2. 存入 Redis
     * 3. 唤起异步线程发邮件
     * 4. 立即返回成功
     */
    public Result sendEmailCode(String email) {
        log.debug("开始发送邮箱验证码 - 邮箱: {}", email);

        // 1. 基础校验：邮箱不能为空
        if (StringUtils.isBlank(email)) {
            log.warn("邮箱验证码发送失败 - 邮箱为空");
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "邮箱不能为空");
        }

        // 2. 生成 6 位随机数字验证码 (100000 ~ 999999)
        String code = String.valueOf(new Random().nextInt(899999) + 100000);
        log.debug("验证码生成完成 - 邮箱: {}, 验证码: {}", email, code);

        // 3. 将验证码存入 Redis
        // Key 格式：REGISTER_CODE_xxxx@qq.com
        // 有效期：5 分钟
        redisTemplate.opsForValue().set("REGISTER_CODE_" + email, code, 5, TimeUnit.MINUTES);
        log.debug("验证码存入Redis完成 - 邮箱: {}, 有效期: 5分钟", email);

        // 4. 【核心】调用异步服务发送邮件
        // 这行代码会立刻返回，不会等待邮件真的发出去
        mailService.sendMailAsync(
                email,
                "【月之别邸】注册验证码",
                "欢迎来到月之别邸，您的注册验证码是：" + code + "。有效期 5 分钟，请勿泄露。"
        );

        log.info("邮箱验证码发送成功 - 邮箱: {}", email);

        // 5. 立即告诉前端：处理成功
        return Result.success("验证码已发送");
    }


    // ================== 【核心修改区域：注册逻辑】 ==================
    @Override
    public Result register(LoginParam loginParam) {
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        String email = loginParam.getEmail();
        String code = loginParam.getCode(); // 前端传来的验证码

        log.info("注册请求开始 - 账号: {}, 邮箱: {}, 昵称: {}", account, email, nickname);

        // 1. 基础校验
        if (StringUtils.isBlank(account)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(nickname)
                || StringUtils.isBlank(email)
                || StringUtils.isBlank(code) // 必填验证码
        ){
            log.warn("注册参数校验失败 - 账号: {}, 邮箱: {}, 昵称: {}", account, email, nickname);
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }

        // 2. 【核心】校验验证码
        String redisCode = redisTemplate.opsForValue().get("REGISTER_CODE_" + email);
        // 如果 Redis 里没有，说明过期了或者根本没发
        if (StringUtils.isBlank(redisCode)) {
            log.warn("验证码校验失败 - 邮箱: {}, 原因: 验证码已过期或未获取", email);
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "验证码已过期或未获取");
        }
        // 比对验证码
        if (!redisCode.equals(code)) {
            log.warn("验证码校验失败 - 邮箱: {}, 输入验证码: {}, 正确验证码: {}", email, code, redisCode);
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), "验证码错误");
        }
        log.debug("验证码校验成功 - 邮箱: {}", email);

        // 3. 校验账号是否已存在
        SysUser sysUser = sysUserService.findUserByAccount(account);
        if (sysUser != null){
            log.warn("账号已存在 - 账号: {}", account);
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(),"账户已经被注册了");
        }
        log.debug("账号唯一性校验通过 - 账号: {}", account);

        // 4. 执行注册入库
        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/tx.gif");
        sysUser.setAdmin(0);
        sysUser.setDeleted(0);
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail(email);

        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip= IpUtils.getIpAddr(request);
        sysUser.setIpaddr(ip);
        sysUser.setLastIpaddr("");

        log.debug("准备保存用户信息 - 账号: {}, 邮箱: {}, IP: {}", account, email, ip);
        this.sysUserService.save(sysUser);
        log.info("用户注册成功 - 用户ID: {}, 账号: {}, 邮箱: {}", sysUser.getId(), account, email);

        // 5. 注册成功后，删除验证码（防止二次使用）
        redisTemplate.delete("REGISTER_CODE_" + email);
        log.debug("验证码删除完成 - 邮箱: {}", email);

        // 6. 自动登录
        String token = JWTUtils.createToken(sysUser.getId());
        saveTokenToRedis(token, sysUser);
        log.info("用户自动登录完成 - 用户ID: {}, Token生成成功", sysUser.getId());

        return Result.success(token);
    }

    @Override
    public Result kick(Long userId) {
        String token = redisTemplate.opsForValue().get("USER_TOKEN_" + userId);
        if (StringUtils.isBlank(token)) {
            return Result.fail(ErrorCode.NO_LOGIN.getCode(), "该用户未登录或已下线");
        }
        redisTemplate.delete("TOKEN_" + token);
        redisTemplate.delete("USER_TOKEN_" + userId);
        return Result.success("用户已强制下线");
    }

    private void saveTokenToRedis(String token, SysUser sysUser) {
        redisTemplate.opsForValue().set("TOKEN_" + token, JSON.toJSONString(sysUser), 3, TimeUnit.DAYS);
        redisTemplate.opsForValue().set("USER_TOKEN_" + sysUser.getId(), token, 3, TimeUnit.DAYS);
    }
}