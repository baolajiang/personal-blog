<template>
  <div class="moon-contract" v-title data-title="首页 | 月之别邸">
    <div class="bg-mask"></div>
    <canvas id="particle-canvas" class="particle-bg"></canvas>

    <div class="contract-paper animated-fade-up">

      <div class="border-line top"></div>
      <div class="border-line bottom"></div>
      <div class="border-line left"></div>
      <div class="border-line right"></div>

      <div class="side-profile">
        <div class="avatar-frame-square">
          <img src="../../../static/img/tx.gif" class="avatar-img" @error="handleImgError">
          <div class="frame-corner c-tl"></div>
          <div class="frame-corner c-br"></div>
        </div>

        <div class="profile-meta">
          <h1 class="lord-title">MOON'S VILLA</h1>
          <p class="lord-subtitle">The Sanctuary of Code & Dreams</p>
          <div class="gold-divider-short"></div>
          <p class="motto">“ {{ currentPhrase }} ”</p>
        </div>

        <div class="social-links">
          <a @click="openLink('https://space.bilibili.com/36932814')">Bilibili</a>
          <span>/</span>
          <a @click="openLink('https://music.163.com/#/user/home?id=342473756')">NetEase</a>
          <span>/</span>
          <span class="wechat-hover">
            WeChat
            <div class="qr-box"><img src="../../../static/user/mywx.png"></div>
          </span>
        </div>
      </div>

      <div class="side-index">
        <div class="index-header">
          <span>CHAPTERS</span>
          <div class="long-line"></div>
        </div>

        <div class="chapter-list">

          <div class="chapter-item" @click="$router.push('/write')">
            <span class="chap-num">I</span>
            <div class="chap-info">
              <span class="chap-en">THE SCRIPTURES</span>
              <span class="chap-cn">阅读记录</span>
            </div>
            <div class="chap-line"></div>
          </div>

          <div class="chapter-item" @click="$router.push('/archives')">
            <span class="chap-num">II</span>
            <div class="chap-info">
              <span class="chap-en">CHRONOLOGY</span>
              <span class="chap-cn">时光回廊</span>
            </div>
            <div class="chap-line"></div>
          </div>

          <div class="chapter-item" @click="$router.push('/messageBoard')">
            <span class="chap-num">III</span>
            <div class="chap-info">
              <span class="chap-en">WHISPERS</span>
              <span class="chap-cn">绘马留言</span>
            </div>
            <div class="chap-line"></div>
          </div>

          <div class="chapter-item" @click="$router.push('/Resume')">
            <span class="chap-num">IV</span>
            <div class="chap-info">
              <span class="chap-en">THE LORD</span>
              <span class="chap-cn">别邸主人</span>
            </div>
            <div class="chap-line"></div>
          </div>

        </div>

        <div class="index-footer">
          <span class="date">EST. 2026</span>
          <span class="signature">Moon's Villa System</span>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import CardMe from "@/components/card/CardMe";
export default {
  name: "IndexContract",
  data() {
    return {
      currentPhrase: '',
      phrases: [
        '即使是虚假的星空，也想为你闪烁',
        '愿荣光尽归于你',
        '月色真美，风也温柔',
        'Loading world data...'
      ],
      phraseIndex: 0,
      typingTimer: null
    };
  },
  mounted() {
    this.initParticles();
    this.startTyping();
  },
  beforeDestroy() {
    if (this.typingTimer) clearTimeout(this.typingTimer);
  },
  methods: {
    openLink(url) { window.open(url, '_blank'); },
    handleImgError(e) {
      e.target.src = require('@/assets/img/default_avatar.png');
    },
    startTyping() {
      const text = this.phrases[this.phraseIndex];
      let i = 0;
      this.currentPhrase = '';
      const type = () => {
        if (i < text.length) {
          this.currentPhrase += text.charAt(i);
          i++;
          this.typingTimer = setTimeout(type, 150);
        } else {
          this.typingTimer = setTimeout(() => {
            this.phraseIndex = (this.phraseIndex + 1) % this.phrases.length;
            this.startTyping();
          }, 3000);
        }
      };
      type();
    },
    // 金色星尘粒子
    initParticles() {
      const canvas = document.getElementById('particle-canvas');
      if (!canvas) return;
      const ctx = canvas.getContext('2d');
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;

      const particles = [];
      for(let i=0; i<40; i++) {
        particles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          size: Math.random() * 2,
          speed: Math.random() * 0.5 + 0.1,
          opacity: Math.random()
        });
      }

      const animate = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        particles.forEach(p => {
          p.y -= p.speed;
          if(p.y < 0) p.y = canvas.height;
          ctx.beginPath();
          ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
          ctx.fillStyle = `rgba(212, 175, 55, ${p.opacity})`;
          ctx.fill();
        });
        requestAnimationFrame(animate);
      }
      animate();
      window.onresize = () => { canvas.width = window.innerWidth; canvas.height = window.innerHeight; };
    }
  },
  components: {
    CardMe,
  }
};
</script>

<style scoped>
/* 核心字体：衬线体 */
@import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700&family=Noto+Serif+SC:wght@300;500;700&display=swap');

.moon-contract {
  position: relative; width: 100%; min-height: 100vh;
  /* 你的背景图 */
  background-image: url('../../../static/img/anime-sunset-art-wallpaper-2560x1080_14.jpg');
  background-size: cover; background-position: center; background-attachment: fixed;
  font-family: 'Cinzel', 'Noto Serif SC', serif;
  color: #333;
  overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}

/* 遮罩：使用高浓度的米白，营造纸张感 */
.bg-mask {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255, 250, 245, 0.90);
  backdrop-filter: blur(8px);
  z-index: 0;
}

.particle-bg { position: fixed; top: 0; left: 0; width: 100%; height: 100%; z-index: 1; pointer-events: none; }

/* === 主契约书 (Container) === */
.contract-paper {
  position: relative; z-index: 10;
  width: 900px; height: 550px;
  background: rgba(255, 255, 255, 0.5); /* 半透明 */
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.1);
  display: flex;
  padding: 50px;
  box-sizing: border-box;
}

/* 装饰细线 */
.border-line { position: absolute; background: #d4af37; opacity: 0.5; }
.top { top: 20px; left: 20px; right: 20px; height: 1px; }
.bottom { bottom: 20px; left: 20px; right: 20px; height: 1px; }
.left { top: 20px; bottom: 20px; left: 20px; width: 1px; }
.right { top: 20px; bottom: 20px; right: 20px; width: 1px; }

/* === 左侧：Profile === */
.side-profile {
  width: 350px;
  border-right: 1px solid rgba(212, 175, 55, 0.3);
  padding-right: 40px;
  display: flex; flex-direction: column; justify-content: center; align-items: center;
  text-align: center;
}

.avatar-frame-square {
  width: 140px; height: 140px; margin-bottom: 30px;
  position: relative;
  padding: 5px;
  border: 1px solid rgba(212, 175, 55, 0.3); /* 淡金细框 */
}
.avatar-img {
  width: 100%; height: 100%; object-fit: cover;
  filter: grayscale(30%); transition: filter 0.5s;
}
.avatar-frame-square:hover .avatar-img { filter: grayscale(0%); }

/* 装饰角标 */
.frame-corner {
  position: absolute; width: 10px; height: 10px;
  border: 2px solid #d4af37; transition: all 0.3s;
}
.c-tl { top: -1px; left: -1px; border-right: none; border-bottom: none; }
.c-br { bottom: -1px; right: -1px; border-left: none; border-top: none; }
.avatar-frame-square:hover .c-tl { top: -5px; left: -5px; }
.avatar-frame-square:hover .c-br { bottom: -5px; right: -5px; }

.lord-title {
  font-size: 28px; margin: 0; color: #2c3e50; letter-spacing: 2px;
  font-family: 'Cinzel', serif; font-weight: 700;
}
.lord-subtitle {
  font-size: 12px; color: #888; margin-top: 5px; letter-spacing: 1px; font-style: italic;
}
.gold-divider-short {
  width: 30px; height: 2px; background: #d4af37; margin: 20px auto;
}
.motto {
  font-family: 'Noto Serif SC', serif; font-size: 14px; color: #555;
  min-height: 24px;
}

.social-links {
  margin-top: auto;
  font-size: 12px; color: #aaa;
  display: flex; gap: 10px;
}
.social-links a, .social-links span.wechat-hover {
  cursor: pointer; transition: color 0.3s; position: relative;
  font-family: 'Cinzel', serif;
}
.social-links a:hover, .social-links span.wechat-hover:hover { color: #d4af37; border-bottom: 1px solid #d4af37; }

.qr-box {
  position: absolute; bottom: 25px; left: 50%; transform: translateX(-50%);
  width: 100px; padding: 5px; background: #fff; border: 1px solid #d4af37;
  opacity: 0; pointer-events: none; transition: opacity 0.3s;
}
.qr-box img { width: 100%; display: block; }
.wechat-hover:hover .qr-box { opacity: 1; }


/* === 右侧：Index === */
.side-index {
  flex: 1; padding-left: 50px;
  display: flex; flex-direction: column; justify-content: center;
}

.index-header {
  display: flex; align-items: center; margin-bottom: 40px;
}
.index-header span { font-size: 14px; color: #d4af37; letter-spacing: 2px; font-weight: 700; margin-right: 15px; }
.long-line { flex: 1; height: 1px; background: rgba(212, 175, 55, 0.3); }

.chapter-list { display: flex; flex-direction: column; gap: 10px; }

.chapter-item {
  display: flex; align-items: center; height: 60px;
  cursor: pointer; position: relative;
  transition: all 0.4s;
}

.chap-num {
  font-family: 'Cinzel', serif; font-size: 20px; color: #ddd; font-weight: 700; width: 40px;
  transition: color 0.4s;
}
.chap-info { display: flex; flex-direction: column; z-index: 2; }
.chap-en { font-family: 'Cinzel', serif; font-size: 16px; color: #444; letter-spacing: 1px; transition: transform 0.4s; }
.chap-cn { font-family: 'Noto Serif SC', serif; font-size: 12px; color: #999; margin-top: 2px; opacity: 0; transform: translateX(-10px); transition: all 0.4s; }

/* 底部线条动画 */
.chap-line {
  position: absolute; bottom: 0; left: 0; width: 0%; height: 1px;
  background: #d4af37; transition: width 0.4s ease;
}

/* 悬停效果 */
.chapter-item:hover .chap-num { color: #d4af37; }
.chapter-item:hover .chap-en { transform: translateY(-2px); color: #000; }
.chapter-item:hover .chap-cn { opacity: 1; transform: translateX(0); color: #d4af37; }
.chapter-item:hover .chap-line { width: 100%; }

.index-footer {
  margin-top: auto; display: flex; justify-content: space-between;
  font-size: 10px; color: #bbb; letter-spacing: 1px; font-family: 'Cinzel', serif;
}

/* 动画 */
.animated-fade-up { animation: fadeUp 1s cubic-bezier(0.23, 1, 0.32, 1); }
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 移动端适配 */
@media screen and (max-width: 900px) {
  .contract-paper {
    width: 90%; height: auto; flex-direction: column; padding: 30px;
  }
  .side-profile {
    width: 100%; border-right: none; border-bottom: 1px solid rgba(212, 175, 55, 0.3);
    padding-right: 0; padding-bottom: 30px; margin-bottom: 30px;
  }
  .side-index { padding-left: 0; }
  .chapter-item { justify-content: center; }
  .chap-num { display: none; } /* 手机上隐藏序号 */
  .chap-info { align-items: center; }
  .chap-cn { opacity: 1; transform: none; margin-top: 5px; } /* 手机上常驻中文 */
}
</style>
