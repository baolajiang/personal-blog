<template>
  <div class="header-monitor">
    <div class="header-wrapper" ref="headerWrapper">
      <div class="header-content">

        <div class="flex-header">
          <div class="logo-box">
            <router-link to="/" class="header-logo">
              <span class="logo-text" ref="logoText">{{ $myName || 'Blog' }}</span>
            </router-link>
          </div>

          <div class="nav-box hidden-xs-only">
            <ul class="nav-list">
              <li v-for="(item, index) in navItems"
                  :key="item.path"
                  :class="{ active: activeIndex === item.path }"
                  @mouseenter="onNavEnter($event)"
                  @mouseleave="onNavLeave($event)">

                <router-link :to="item.path">
                  <i :class="[item.icon, 'nav-icon']"></i>
                  <span class="nav-text">{{ item.name }}</span>
                </router-link>
              </li>

              <li v-if="user.login" class="write-btn"
                  @mouseenter="onNavEnter($event)"
                  @mouseleave="onNavLeave($event)">
                <router-link to="/write">
                  <i class="el-icon-edit-outline nav-icon"></i>
                  <span class="nav-text">寫文章</span>
                </router-link>
              </li>
            </ul>
          </div>

          <div class="user-box">
            <template v-if="!user.login">
              <span class="auth-btn" @click="login">登入</span>
              <span class="auth-btn" @click="register">註冊</span>
            </template>
            <template v-else>
              <el-dropdown trigger="click">
                <div class="user-info">
                  <el-avatar :size="36" :src="user.avatar || require('@/assets/img/default_avatar.png')"></el-avatar>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click.native="logout">退出登錄</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import { gsap } from 'gsap'

export default {
  name: 'BaseHeader',
  props: {
    activeIndex: { type: String, default: '/' },
    simple: { type: Boolean, default: false }
  },
  data() {
    return {
      isScrolled: false,
      ticking: false,
      navItems: [
        { name: '首頁', path: '/', icon: 'el-icon-s-home' },
        { name: '文章', path: '/archives', icon: 'el-icon-document' },
        { name: '分類', path: '/category/all', icon: 'el-icon-menu' },
        { name: '標籤', path: '/tag/all', icon: 'el-icon-price-tag' },
        { name: '導航', path: '/nav', icon: 'el-icon-compass' },
        { name: '留言板', path: '/messageBoard', icon: 'el-icon-chat-dot-round' }
      ]
    }
  },
  computed: {
    user() {
      let login = this.$store.state.account ? this.$store.state.account.length != 0 : false
      let avatar = this.$store.state.avatar
      let nickname = this.$store.state.name
      return { login, avatar, nickname }
    }
  },
  watch: {
    isScrolled(newVal) {
      newVal ? this.animateToCapsule() : this.animateToFull()
    }
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll)
    this.animateToFull(true)
  },
  destroyed() {
    window.removeEventListener('scroll', this.handleScroll)
  },
  methods: {
    // --- 修復後的滑鼠懸停特效 ---
    onNavEnter(e) {
      const target = e.currentTarget
      const icon = target.querySelector('.nav-icon')
      const text = target.querySelector('.nav-text')

      // [關鍵修復] 1. 強制停止該元素上正在進行的所有動畫，防止堆疊
      gsap.killTweensOf(icon)
      gsap.killTweensOf(text)

      // Icon 特效
      gsap.to(icon, {
        scale: 1.2,
        rotation: 15,
        color: '#409EFF',
        duration: 0.4,
        ease: 'back.out(2.5)',
        overwrite: true // [關鍵修復] 2. 確保覆蓋之前的狀態
      })

      // 文字特效
      gsap.to(text, {
        y: -3,
        color: '#409EFF',
        textShadow: '0 0 8px rgba(64, 158, 255, 0.8)',
        fontWeight: 'bold',
        duration: 0.3,
        ease: 'power2.out',
        overwrite: true
      })
    },

    onNavLeave(e) {
      const target = e.currentTarget
      const icon = target.querySelector('.nav-icon')
      const text = target.querySelector('.nav-text')

      // [關鍵修復] 同樣要殺死動畫，防止快速移出時卡住
      gsap.killTweensOf(icon)
      gsap.killTweensOf(text)

      // 恢復原狀
      gsap.to(icon, {
        scale: 1,
        rotation: 0,
        color: '#555',
        duration: 0.3,
        ease: 'power2.out',
        overwrite: true
      })

      gsap.to(text, {
        y: 0,
        color: '#555',
        textShadow: 'none',
        fontWeight: 'normal',
        duration: 0.3,
        ease: 'power2.out',
        overwrite: true
      })
    },

    // --- 滾動監聽 (不變) ---
    handleScroll() {
      if (!this.ticking) {
        window.requestAnimationFrame(() => {
          const scrollTop = window.pageYOffset || document.documentElement.scrollTop
          const shouldScroll = scrollTop > 60
          if (this.isScrolled !== shouldScroll) {
            this.isScrolled = shouldScroll
          }
          this.ticking = false
        })
        this.ticking = true
      }
    },
    animateToCapsule() {
      gsap.to(this.$refs.headerWrapper, {
        top: 15,
        width: '95%',
        maxWidth: '1200px',
        borderRadius: 50,
        backgroundColor: 'rgba(255, 255, 255, 0.85)',
        backdropFilter: 'blur(15px)',
        border: '1px solid rgba(255, 255, 255, 0.5)',
        boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)',
        duration: 0.6,
        ease: 'power3.out'
      })
      gsap.to(this.$refs.logoText, {
        scale: 0.9,
        transformOrigin: 'left center',
        color: '#409EFF',
        duration: 0.6,
        ease: 'power3.out'
      })
    },
    animateToFull(immediate = false) {
      const duration = immediate ? 0 : 0.6
      gsap.to(this.$refs.headerWrapper, {
        top: 0,
        width: '100%',
        maxWidth: '100%',
        borderRadius: 0,
        backgroundColor: 'rgba(255, 255, 255, 0.15)',
        backdropFilter: 'blur(10px)',
        border: '1px solid rgba(255, 255, 255, 0.1)',
        boxShadow: 'none',
        duration: duration,
        ease: 'power3.out'
      })
      gsap.to(this.$refs.logoText, {
        scale: 1,
        transformOrigin: 'left center',
        color: '#409EFF',
        duration: duration,
        ease: 'power3.out'
      })
    },
    logout() {
      let that = this
      this.$store.dispatch('logout').then(() => {
        this.$router.push({path: '/'})
      }).catch((error) => {
        if (error !== 'error') {
          that.$message({message: error, type: 'error', showClose: true});
        }
      })
    },
    login() { this.$router.push({path: '/login'}) },
    register() { this.$router.push({path: '/register'}) },
    nav() { this.$router.push({ path: `/nav` }) },
  }
}
</script>

<style scoped>
/* 樣式保持不變，為了完整性我列在下面 */
.header-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  margin: 0 auto;
  height: 60px;
  width: 100%;
  z-index: 1024;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  will-change: width, top, border-radius, background, transform;
}
.header-content { height: 100%; padding: 0 25px; width: 100%; box-sizing: border-box; }
.flex-header { display: flex; justify-content: space-between; align-items: center; height: 100%; width: 100%; }
.logo-box { flex-shrink: 0; }
.header-logo { text-decoration: none; display: flex; align-items: center; }
.logo-text { font-size: 24px; font-weight: 700; color: #409EFF; font-family: 'Righteous', sans-serif; letter-spacing: 1px; display: inline-block; text-shadow: 0 2px 5px rgba(64, 158, 255, 0.2); }
.nav-box { flex-grow: 1; display: flex; justify-content: center; }
.nav-list { display: flex; list-style: none; padding: 0; margin: 0; }
.nav-list li { margin: 0 15px; position: relative; cursor: pointer; }
.nav-list li a { display: flex; align-items: center; color: #555; font-size: 15px; font-weight: 600; text-decoration: none; padding: 8px 5px; }
.nav-icon { margin-right: 4px; font-size: 16px; color: #888; display: inline-block; transform-origin: center center; }
.nav-text { display: inline-block; position: relative; }
.nav-list li::after { display: none; }
.write-btn a { display: flex; align-items: center; color: #555; font-size: 15px; font-weight: 600; text-decoration: none; padding: 8px 5px; }
.user-box { flex-shrink: 0; display: flex; justify-content: flex-end; align-items: center; }
.auth-btn { cursor: pointer; margin-left: 15px; color: #666; font-weight: 600; transition: color 0.3s; }
.auth-btn:hover { color: #409EFF; }
</style>
