<template>
  <div class="me-card">
    <div class="me-banner">
      <img :src="bannerImg" class="banner-img">
    </div>

    <div class="me-content">
      <div class="me-avatar-box">
        <img :src="user.avatar || defaultAvatar" class="me-avatar" @error="handleImgError">
      </div>

      <h1 class="me-name">{{ user.nickname || "Master" }}</h1>
      <p class="me-motto">{{ user.motto || "Code is poetry." }}</p>

      <div class="me-status">
        <div class="status-item">
          <div class="status-label">
            <span class="key">Lv.7</span>
            <span class="val">EXP</span>
          </div>
          <div class="progress-track">
            <div class="progress-bar hp" style="width: 80%"></div>
          </div>
        </div>
        <div class="status-item">
          <div class="status-label">
            <span class="key">MP</span>
            <span class="val">100%</span>
          </div>
          <div class="progress-track">
            <div class="progress-bar mp" style="width: 65%"></div>
          </div>
        </div>
      </div>

      <div class="me-social">
        <el-tooltip content="Github" placement="top">
          <a class="social-btn" @click="openLink('https://github.com')">
            <i class="el-icon-position"></i>
          </a>
        </el-tooltip>

        <el-tooltip content="Bilibili" placement="top">
          <a class="social-btn" @click="openLink('https://space.bilibili.com')">
            <img :src="bilibiliIcon" class="icon-img">
          </a>
        </el-tooltip>

        <el-tooltip content="WeChat" placement="top">
          <div class="social-btn wx-trigger">
            <img :src="wechatIcon" class="icon-img">
            <div class="wx-pop">
              <img :src="qrCode" style="width: 100%">
            </div>
          </div>
        </el-tooltip>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: 'CardMe',
  data() {
    return {
      // ★★★ 核心修复：统一使用 require('@/...') 引入图片 ★★★
      // 请根据实际文件名大小写检查，如果报错 Module not found，请检查文件名

      // 1. 默认头像 (在 src/assets/img/)
      defaultAvatar: require('@/assets/img/default_avatar.png'),

      // 2. 背景图 (在 src/static/img/)
      // 注意：webpack 中引入 static 目录下的文件可能需要使用相对路径或者拷贝插件
      // 如果 @/static 报错，请尝试使用绝对路径 '/static/img/...' (如果 vue-cli 配置了 static 目录拷贝)
      // 这里先尝试 require 方式：
      bannerImg: require('../../../static/img/anime-sunset-art-wallpaper-2560x1080_14.jpg'),

      // 3. 社交图标 (在 src/static/user/)
      bilibiliIcon: require('../../../static/user/bilibili.png'),
      wechatIcon: require('../../../static/user/wechat.png'),
      qrCode: require('../../../static/user/mywx.png')
    }
  },
  computed: {
    user() {
      if (this.$store.state.account && this.$store.state.account.length > 0) {
        return {
          nickname: this.$store.state.name,
          avatar: this.$store.state.avatar,
          motto: "愿荣光尽归于你"
        }
      }
      return { nickname: "Moon's Master", motto: "Coding the Future" };
    }
  },
  methods: {
    openLink(url) { window.open(url, '_blank') },
    handleImgError(e) { e.target.src = this.defaultAvatar }
  }
}
</script>

<style scoped>
/* 保持原有样式不变 */
.me-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  overflow: hidden;
  position: relative;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", sans-serif;
  transition: transform 0.3s;
}
.me-card:hover { transform: translateY(-5px); box-shadow: 0 8px 25px rgba(0,0,0,0.1); }

.me-banner { height: 120px; overflow: hidden; }
.banner-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.me-card:hover .banner-img { transform: scale(1.05); }

.me-content { padding: 0 20px 20px; text-align: center; position: relative; }

.me-avatar-box {
  width: 80px; height: 80px;
  margin: -40px auto 10px;
  border-radius: 50%;
  padding: 3px;
  background: #fff;
  position: relative;
  z-index: 2;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.me-avatar { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }

.me-name { font-size: 18px; font-weight: 700; color: #333; margin: 0 0 5px; }
.me-motto { font-size: 12px; color: #999; margin-bottom: 20px; }

.me-status { margin-bottom: 20px; padding: 0 10px; }
.status-item { margin-bottom: 8px; }
.status-label { display: flex; justify-content: space-between; font-size: 12px; color: #666; margin-bottom: 4px; font-weight: 600; }
.progress-track { width: 100%; height: 6px; background: #eee; border-radius: 3px; overflow: hidden; }
.progress-bar { height: 100%; border-radius: 3px; }
.hp { background: #ff7675; }
.mp { background: #74b9ff; }

.me-social { display: flex; justify-content: center; gap: 15px; }
.social-btn {
  width: 36px; height: 36px;
  background: #f5f5f5; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.3s;
  color: #666; font-size: 18px;
  position: relative;
}
.social-btn:hover { background: #d4af37; color: #fff; }
.icon-img { width: 20px; height: 20px; }

.wx-pop {
  position: absolute; bottom: 45px; left: 50%; transform: translateX(-50%);
  width: 100px; padding: 5px; background: #fff; border-radius: 4px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  opacity: 0; pointer-events: none; transition: opacity 0.3s;
  z-index: 10;
}
.wx-trigger:hover .wx-pop { opacity: 1; }
</style>
