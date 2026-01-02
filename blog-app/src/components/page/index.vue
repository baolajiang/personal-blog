<template>
  <div class="manaria-duo-container" @mousemove="handleMouseMove">

    <div class="bg-layer-anne"></div> <div class="bg-layer-grea"></div> <div class="particles-anne" id="p-anne"></div> <div class="particles-grea" id="p-grea"></div> <div class="magic-circle-group" ref="magicGroup">
    <div class="circle-anne"></div>
    <div class="circle-grea"></div>
  </div>

    <div class="left-float-content" ref="leftContent">

      <div class="avatar-duo-wrapper">
        <div class="ring-anne"></div>
        <div class="ring-grea"></div>
        <div class="avatar-core">
          <img
            src="/static/img/露娜切露德7.png"
            class="avatar-img"
            alt="Luna"
            @error="handleImgError"
          >
        </div>
      </div>

      <div class="info-area">
        <div class="tag-line">
          <span class="gold-t">Manaria</span>
          <span class="cross">✕</span>
          <span class="dark-t">Friends</span>
        </div>
        <h1 class="main-title">LUNA<br><span class="highlight">LINKER</span></h1>
        <div class="dual-line"></div>
        <p class="desc">
          “ 所谓的魔法，就是连接两颗心的奇迹。 ”
        </p>
      </div>

      <div class="nav-list">
        <div
          class="nav-item"
          v-for="(item, index) in menuItems"
          :key="index"
          @click="navigate(item.path)"
          @mouseenter="hoverItem($event)"
          @mouseleave="leaveItem($event)"
        >
          <span class="icon">{{ item.icon }}</span>
          <span class="text">{{ item.cn }}</span>
          <div class="hover-fill"></div>
        </div>
      </div>

    </div>

    <div class="corner-mark">
      <div class="mark-symbol">⚜️</div>
      <div class="mark-text">Twin Souls Resonance</div>
    </div>

  </div>
</template>

<script>
import { gsap } from 'gsap';

export default {
  name: "IndexManariaDuo",
  data() {
    return {
      menuItems: [
        { cn: '学院记录', path: '/write', icon: '📜' },
        { cn: '时光轨迹', path: '/archives', icon: '⏳' },
        { cn: '龙之传音', path: '/messageBoard', icon: '🔥' },
        { cn: '契约书', path: '/Resume', icon: '💠' }
      ]
    };
  },
  mounted() {
    this.createParticles();
    this.entranceAnim();
    this.loopAnim();
  },
  methods: {
    navigate(path) {
      // 离场：双色光辉爆发后消失
      const tl = gsap.timeline({ onComplete: () => this.$router.push(path) });
      tl.to('.left-float-content', { x: -50, opacity: 0, duration: 0.5, ease: "power2.in" })
        .to('.manaria-duo-container', { opacity: 0, duration: 0.5 }, "-=0.3");
    },
    handleImgError(e) {
      e.target.style.display = 'none';
      e.target.parentNode.style.backgroundColor = '#2c2c2c';
    },

    // === 粒子生成 ===
    createParticles() {
      // 安的金色粒子
      const anneContainer = document.getElementById('p-anne');
      for(let i=0; i<20; i++) this.addParticle(anneContainer, 'particle-gold');

      // 古雷亚的暗色粒子
      const greaContainer = document.getElementById('p-grea');
      for(let i=0; i<15; i++) this.addParticle(greaContainer, 'particle-dark');
    },
    addParticle(container, className) {
      const p = document.createElement('div');
      p.className = className;
      p.style.left = Math.random() * 100 + '%';
      p.style.top = Math.random() * 100 + '%';
      p.style.animationDuration = (Math.random() * 5 + 5) + 's';
      p.style.animationDelay = (Math.random() * 5) + 's';
      container.appendChild(p);
    },

    // === 交互动画 ===
    hoverItem(e) {
      // 悬停时，金色填充滑入
      gsap.to(e.currentTarget.querySelector('.hover-fill'), { width: '100%', duration: 0.3, ease: 'power2.out' });
      gsap.to(e.currentTarget.querySelector('.text'), { color: '#000', x: 5, duration: 0.3 });
      gsap.to(e.currentTarget.querySelector('.icon'), { color: '#000', duration: 0.3 });
    },
    leaveItem(e) {
      gsap.to(e.currentTarget.querySelector('.hover-fill'), { width: '0%', duration: 0.3, ease: 'power2.in' });
      gsap.to(e.currentTarget.querySelector('.text'), { color: '#fff', x: 0, duration: 0.3 });
      gsap.to(e.currentTarget.querySelector('.icon'), { color: '#d4af37', duration: 0.3 });
    },
    handleMouseMove(e) {
      const x = (e.clientX / window.innerWidth - 0.5);
      const y = (e.clientY / window.innerHeight - 0.5);

      // 双重光环反向移动，制造极强的空间感
      gsap.to('.ring-anne', { x: x * 20, y: y * 20, duration: 1.5 });
      gsap.to('.ring-grea', { x: -x * 20, y: -y * 20, duration: 1.5 });

      // 魔法阵视差
      gsap.to(this.$refs.magicGroup, { x: x * 40, y: y * 40, rotationY: x * 10, duration: 2 });
    },

    // === 进场动画 ===
    entranceAnim() {
      const tl = gsap.timeline();
      tl.from('.left-float-content', { x: -80, opacity: 0, duration: 1.2, ease: "power3.out" })
        .from('.avatar-core', { scale: 0, duration: 0.8, ease: "back.out(1.5)" }, "-=0.8")
        .from(['.ring-anne', '.ring-grea'], { scale: 1.5, opacity: 0, rotation: 180, duration: 1 }, "-=0.8")
        .from('.nav-item', { x: -20, opacity: 0, stagger: 0.1, duration: 0.6 }, "-=0.6");
    },

    // === 循环动画 ===
    loopAnim() {
      // 安的光环：顺时针，呼吸
      gsap.to('.ring-anne', { rotation: 360, duration: 40, repeat: -1, ease: 'none' });
      gsap.to('.ring-anne', { boxShadow: "0 0 20px rgba(255, 215, 0, 0.4)", duration: 2, yoyo: true, repeat: -1 });

      // 古雷亚的光环：逆时针，更慢
      gsap.to('.ring-grea', { rotation: -360, duration: 50, repeat: -1, ease: 'none' });

      // 魔法阵转动
      gsap.to('.circle-anne', { rotation: 360, duration: 80, repeat: -1, ease: 'none' });
      gsap.to('.circle-grea', { rotation: -360, duration: 60, repeat: -1, ease: 'none' });
    }
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700&family=Noto+Serif+SC:wght@400;700&display=swap');

/* 全局容器 */
.manaria-duo-container {
  position: relative; width: 100%; height: 100vh;
  overflow: hidden;
  /* 核心背景：左边是安的暖光，右边是古雷亚的夜色 */
  background: linear-gradient(110deg, #1a1a2e 30%, #2e2030 100%);
  font-family: 'Cinzel', 'Noto Serif SC', serif;
  color: #fff;
}

/* 1. 背景层 (双色交融) */
.bg-layer-anne {
  position: absolute; top: -50%; left: -20%; width: 100%; height: 150%;
  background: radial-gradient(circle, rgba(255, 225, 150, 0.15), transparent 70%);
  z-index: 0; pointer-events: none;
}
.bg-layer-grea {
  position: absolute; bottom: -20%; right: -20%; width: 100%; height: 100%;
  background: radial-gradient(circle, rgba(100, 50, 255, 0.1), transparent 60%);
  z-index: 0; pointer-events: none;
}

/* 粒子 */
.particles-anne, .particles-grea { position: absolute; width: 100%; height: 100%; pointer-events: none; z-index: 1; }
</style>
<style>
/* 全局粒子样式 */
.particle-gold {
  position: absolute; width: 4px; height: 4px; background: #d4af37; border-radius: 50%;
  box-shadow: 0 0 5px #d4af37; animation: floatUp 8s infinite linear;
}
.particle-dark {
  position: absolute; width: 6px; height: 6px; background: transparent;
  border: 1px solid #a08cff; transform: rotate(45deg); /* 菱形代表龙鳞 */
  animation: floatUp 12s infinite linear; opacity: 0.6;
}
@keyframes floatUp { from { transform: translateY(0) rotate(0deg); opacity: 0; } 50% { opacity: 1; } to { transform: translateY(-100vh) rotate(360deg); opacity: 0; } }
</style>

<style scoped>
/* 2. 魔法阵 (背景装饰) */
.magic-circle-group {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  width: 600px; height: 600px; pointer-events: none; z-index: 0;
  opacity: 0.15;
}
.circle-anne {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  border: 2px solid #d4af37; border-radius: 50%;
  border-left: 2px dashed transparent; border-right: 2px dashed transparent;
}
.circle-grea {
  position: absolute; top: 15%; left: 15%; width: 70%; height: 70%;
  border: 2px solid #a08cff; border-radius: 50%;
  border-top: 2px dashed transparent; border-bottom: 2px dashed transparent;
}

/* 3. 左侧内容 (IamYukino 布局) */
.left-float-content {
  position: absolute;
  /* 关键布局：左侧 15%，垂直居中 */
  left: 15%; top: 50%; transform: translateY(-50%);
  z-index: 10;
  display: flex; flex-direction: column; align-items: flex-start;
  gap: 30px;
}

/* 头像组 (安的金环 + 古雷亚的龙环) */
.avatar-duo-wrapper {
  position: relative; width: 140px; height: 140px;
  display: flex; justify-content: center; align-items: center;
}
.ring-anne {
  position: absolute; width: 100%; height: 100%; border-radius: 50%;
  border: 1px solid #d4af37; /* 金色 */
}
.ring-grea {
  position: absolute; width: 120%; height: 120%; border-radius: 50%;
  border: 1px dashed #a08cff; /* 龙紫色 */
  opacity: 0.6;
}
.avatar-core {
  width: 110px; height: 110px; border-radius: 50%;
  overflow: hidden; z-index: 2;
  border: 3px solid #fff;
  box-shadow: 0 0 20px rgba(0,0,0,0.5);
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }

/* 文字区 */
.info-area { text-align: left; }
.tag-line {
  font-size: 12px; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 8px;
  display: flex; align-items: center; gap: 8px;
}
.gold-t { color: #d4af37; }
.dark-t { color: #a08cff; }
.cross { font-size: 10px; color: #555; }

.main-title {
  font-size: 3.5rem; font-weight: 700; margin: 0; line-height: 0.9;
  letter-spacing: -1px; color: #fff;
}
.highlight { color: #d4af37; font-weight: 300; }

.dual-line {
  width: 80px; height: 4px;
  background: linear-gradient(to right, #d4af37, #a08cff); /* 双色渐变线 */
  margin: 20px 0; border-radius: 2px;
}
.desc {
  font-family: 'Noto Serif SC', serif; font-size: 1rem; color: #ccc;
  line-height: 1.6; font-style: italic; opacity: 0.9;
}

/* 导航菜单 (胶囊) */
.nav-list {
  display: flex; flex-direction: column; gap: 12px; margin-top: 10px;
}
.nav-item {
  position: relative;
  display: flex; align-items: center; gap: 15px;
  padding: 10px 25px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50px;
  cursor: pointer; width: 180px;
  overflow: hidden; /* 必须，为了填充动画 */
}
.icon { font-size: 1.1rem; z-index: 2; color: #d4af37; transition: color 0.3s; }
.text { font-size: 0.95rem; font-weight: 600; z-index: 2; transition: color 0.3s; }
.hover-fill {
  position: absolute; top:0; left:0; width: 0%; height: 100%;
  background: #d4af37; /* 安的金色 */
  z-index: 1;
}

/* 右下角 */
.corner-mark {
  position: absolute; bottom: 30px; right: 40px; text-align: right; opacity: 0.5;
}
.mark-symbol { font-size: 2rem; color: #d4af37; margin-bottom: 5px; }
.mark-text { font-size: 12px; letter-spacing: 2px; text-transform: uppercase; }

/* 移动端适配 */
@media screen and (max-width: 900px) {
  .left-float-content { left: 10%; top: 45%; }
  .main-title { font-size: 2.8rem; }
  .magic-circle-group { width: 300px; height: 300px; }
}
</style>
