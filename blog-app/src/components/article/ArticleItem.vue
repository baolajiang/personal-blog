<template>
  <div class="luna-wrap" ref="cardWrap">
    <div
      class="luna-card"
      :class="`theme-${currentTheme}`"
      ref="card"
      @click="view(id)"
      @mouseenter="setHoverLevel(1)"
      @mouseleave="setHoverLevel(0)"
    >
      <div class="bg-crest" ref="crest">
        <svg viewBox="0 0 100 100" class="crest-svg">
          <path d="M50 0 C20 0 0 20 0 50 C0 80 20 100 50 100 C40 80 40 20 50 0 Z" fill="currentColor" />
        </svg>
      </div>

      <div class="particle-container" ref="particleContainer">
        <span class="particle p1">❀</span>
        <span class="particle p2">❀</span>
      </div>

      <div
        class="frame-box"
        @mouseenter.stop="setHoverLevel(2)"
        @mouseleave.stop="setHoverLevel(1)"
      >
        <div class="img-mask">
          <img :src="Imgview(index)" class="luna-img" ref="img" alt="cover">
        </div>
        <div class="sheen-effect" ref="sheen"></div>
        <div class="border-overlay" ref="borderOverlay"></div>
      </div>

      <div class="text-panel">
        <div class="fancy-line-top" ref="lineTop"></div>

        <div class="meta-row">
          <span class="chapter-num">CHAPTER.{{ index + 1 }}</span>
          <span class="date-txt">{{ createDate | format }}</span>
        </div>

        <h3 class="luna-title" ref="title">{{ title }}</h3>

        <p class="luna-summary">{{ summary }}</p>

        <div class="luna-footer">
          <div class="tag-ribbon">
            <span>{{ tags && tags.length > 0 ? tags[0].tagName : 'Story' }}</span>
          </div>
          <div class="read-more-btn" ref="btn">
            Read More <span class="flower-icon">✿</span>
          </div>
        </div>

        <div class="fancy-line-bottom" ref="lineBottom"></div>
      </div>

    </div>
  </div>
</template>

<script>
import { formatTime } from "@/utils/time";
import { gsap } from "gsap";

export default {
  name: 'ArticleItem',
  props: {
    // 接收主题参数：'asahi', 'luna', 'minato', 'ursule', 'mizuho'
    theme: { type: String, default: 'asahi' },
    id: String, weight: Number, title: String, commentCounts: Number, viewCounts: Number,
    summary: String, author: [String, Object], tags: Array, createDate: String, cover: String,
    index: { type: Number, default: 0 }
  },
  data() {
    return {
      hoverLevel: 0
    }
  },
  computed: {
    // 容错处理，防止传入奇怪的字符串
    currentTheme() {
      const validThemes = ['asahi', 'luna', 'minato', 'ursule', 'mizuho'];
      return validThemes.includes(this.theme) ? this.theme : 'asahi';
    }
  },
  watch: {
    hoverLevel(newVal) {
      this.animateState(newVal);
    },
    // 切换主题时，重置状态
    theme() {
      this.setHoverLevel(0);
    }
  },
  mounted() {
    gsap.from(this.$refs.cardWrap, {
      opacity: 0,
      y: 20,
      duration: 1,
      ease: "power2.out",
      delay: this.index * 0.15
    });
  },
  methods: {
    view(id) { this.$router.push({ path: `/view/${id}` }) },
    Imgview(index) { return "https://www.loliapi.com/acg/?uuid=" + (index + 900) },
    setHoverLevel(level) { this.hoverLevel = level; },

    // 动画调度器：全部使用 CSS 变量 (var)
    animateState(level) {
      const commonOpts = { overwrite: true, duration: 0.5, ease: "power2.out" };

      // GSAP 读取 CSS 变量时，需要让它去 tween 具体的 CSS 属性
      // 这里我们主要控制 Opacity 和 Transform，颜色由 CSS 类名决定，
      // 但对于需要 GSAP 插值的颜色 (如 border, text)，我们使用 "var(--accent-color)" 这种写法。

      if (level === 0) {
        // === State 0: 离开 ===
        gsap.to(this.$refs.img, { scale: 1, ...commonOpts });
        // 恢复基础阴影
        gsap.to(this.$refs.card, { boxShadow: "var(--shadow-base)", ...commonOpts });

        // 边框淡化
        gsap.to(this.$refs.borderOverlay, { borderColor: "var(--border-color-dim)", boxShadow: "inset 0 0 10px var(--border-color-dim)", ...commonOpts });

        // 装饰线半透明
        gsap.to([this.$refs.lineTop, this.$refs.lineBottom], { opacity: 0.5, ...commonOpts });
        gsap.set([this.$refs.lineTop, this.$refs.lineBottom], { background: (i) => i===0 ? "linear-gradient(90deg, var(--accent-color), transparent)" : "linear-gradient(90deg, transparent, var(--accent-color))" });

        // 背景纹章淡化
        gsap.to(this.$refs.crest, { rotation: 0, opacity: 0.2, duration: 1, overwrite: true });
        gsap.to(this.$refs.particleContainer, { y: 0, opacity: 0.4, duration: 1, overwrite: true });

        // 清除内联颜色，回归 CSS 默认色
        gsap.to(this.$refs.title, { color: "", x: 0, duration: 0.3, overwrite: true });
        gsap.to(this.$refs.btn, { color: "", x: 0, duration: 0.3, overwrite: true });

      } else if (level === 1) {
        // === State 1: 氛围模式 ===
        gsap.to(this.$refs.img, { scale: 1.05, duration: 1.2, ease: "power2.out", overwrite: true });
        gsap.to(this.$refs.card, { boxShadow: "var(--shadow-hover)", ...commonOpts });

        // 边框高亮
        gsap.to(this.$refs.borderOverlay, { borderColor: "var(--accent-color)", boxShadow: "inset 0 0 15px var(--accent-color-alpha)", ...commonOpts });
        gsap.to([this.$refs.lineTop, this.$refs.lineBottom], { opacity: 1, background: "var(--accent-color)", ...commonOpts });

        // 纹章旋转
        gsap.to(this.$refs.crest, { rotation: 10, opacity: 0.6, duration: 1, overwrite: true });
        gsap.to(this.$refs.particleContainer, { y: 5, opacity: 0.8, duration: 1, overwrite: true });

        // 标题变色
        gsap.to(this.$refs.title, { color: "var(--accent-color)", x: 2, duration: 0.3, overwrite: true });
        gsap.to(this.$refs.btn, { color: "var(--accent-color)", x: -2, duration: 0.3, overwrite: true });

      } else if (level === 2) {
        // === State 2: 高亮模式 ===
        gsap.to(this.$refs.img, { scale: 1.08, duration: 1.2, ease: "power2.out", overwrite: true });
        gsap.fromTo(this.$refs.sheen, { x: "-100%", opacity: 0 }, { x: "100%", opacity: 0.3, duration: 0.6, ease: "power2.inOut", overwrite: true });

        // 维持 State 1 的外观
        gsap.to(this.$refs.card, { boxShadow: "var(--shadow-hover)", ...commonOpts });
        gsap.to(this.$refs.borderOverlay, { borderColor: "var(--accent-color)", boxShadow: "inset 0 0 15px var(--accent-color-alpha)", ...commonOpts });
        gsap.to([this.$refs.lineTop, this.$refs.lineBottom], { opacity: 1, background: "var(--accent-color)", ...commonOpts });
      }
    }
  },
  filters: {
    format: formatTime
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;700&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap');

/* =========================================
   1. CSS 变量定义 (5个主题)
   ========================================= */

/* --- 朝日 (Asahi): 默认, 樱花粉, 纯白女仆 --- */
.theme-asahi {
  --bg-color: #fffaf5;            /* 象牙白 */
  --text-main: #4a4a4a;           /* 深灰 */
  --text-sub: #666;               /* 中灰 */
  --accent-color: #d4af37;        /* 经典金 (朝日是女仆，金色代表对主人的忠诚) */
  --accent-color-alpha: rgba(212, 175, 55, 0.3);
  --secondary-color: #ffb7c5;     /* 樱花粉 */
  --border-color: #e8e0d5;
  --border-color-dim: rgba(212, 175, 55, 0.5);
  --shadow-base: 0 5px 15px rgba(0,0,0,0.03);
  --shadow-hover: 0 10px 40px rgba(0,0,0,0.08);
  --crest-color: #d4af37;
  --particle-color: #ffb7c5;
}

/* --- 露娜 (Luna): 夜晚, 银月, 深蓝 --- */
.theme-luna {
  --bg-color: #222233;            /* 夜空蓝 */
  --text-main: #e0d8c8;           /* 月光米 */
  --text-sub: #aaaabb;            /* 银灰 */
  --accent-color: #ffd700;        /* 亮金 */
  --accent-color-alpha: rgba(255, 215, 0, 0.3);
  --secondary-color: #c0c0c0;     /* 银色 */
  --border-color: #444455;
  --border-color-dim: rgba(255, 215, 0, 0.3);
  --shadow-base: 0 5px 20px rgba(0,0,0,0.5);
  --shadow-hover: 0 15px 40px rgba(255, 215, 0, 0.15); /* 金色光晕 */
  --crest-color: #ffd700;
  --particle-color: #e0e0e0;      /* 银白花瓣 */
}

/* --- 湊 (Minato): 活力, 橙色, 温暖 --- */
.theme-minato {
  --bg-color: #fffbe6;            /* 淡黄底 */
  --text-main: #5d4037;           /* 暖棕色 */
  --text-sub: #8d6e63;            /* 浅棕 */
  --accent-color: #ff9800;        /* 活力橙 */
  --accent-color-alpha: rgba(255, 152, 0, 0.3);
  --secondary-color: #8bc34a;     /* 嫩绿 */
  --border-color: #ffe0b2;
  --border-color-dim: rgba(255, 152, 0, 0.4);
  --shadow-base: 0 5px 15px rgba(255, 152, 0, 0.05);
  --shadow-hover: 0 10px 40px rgba(255, 152, 0, 0.15);
  --crest-color: #ffb74d;
  --particle-color: #ffcc80;      /* 橙色光点 */
}

/* --- 尤尔希儿 (Ursule): 蔷薇, 红色, 奢华 --- */
.theme-ursule {
  --bg-color: #fff0f5;            /* 浅粉红底 (Lavender Blush) */
  --text-main: #4a0e1c;           /* 深红褐色 */
  --text-sub: #883344;            /* 玫瑰灰 */
  --accent-color: #e91e63;        /* 蔷薇红 */
  --accent-color-alpha: rgba(233, 30, 99, 0.3);
  --secondary-color: #ffd700;     /* 金色 */
  --border-color: #f8bbd0;
  --border-color-dim: rgba(233, 30, 99, 0.4);
  --shadow-base: 0 5px 15px rgba(233, 30, 99, 0.05);
  --shadow-hover: 0 10px 40px rgba(233, 30, 99, 0.15);
  --crest-color: #e91e63;
  --particle-color: #ff80ab;      /* 玫瑰花瓣色 */
}

/* --- 瑞穗 (Mizuho): 鸢尾花, 蓝色, 大和抚子 --- */
.theme-mizuho {
  --bg-color: #f0f8ff;            /* 爱丽丝蓝 */
  --text-main: #1a237e;           /* 深靛蓝 */
  --text-sub: #3949ab;            /* 浅靛蓝 */
  --accent-color: #304ffe;        /* 宝蓝 */
  --accent-color-alpha: rgba(48, 79, 254, 0.3);
  --secondary-color: #b71c1c;     /* 弓道红 (点缀) */
  --border-color: #c5cae9;
  --border-color-dim: rgba(48, 79, 254, 0.4);
  --shadow-base: 0 5px 15px rgba(48, 79, 254, 0.05);
  --shadow-hover: 0 10px 40px rgba(48, 79, 254, 0.15);
  --crest-color: #304ffe;
  --particle-color: #8c9eff;      /* 淡紫色花瓣 */
}

/* =========================================
   2. 结构样式 (引用变量)
   ========================================= */

.luna-wrap {
  padding: 15px 0;
  width: 100%;
  display: flex;
  justify-content: center;
}

.luna-card {
  position: relative;
  display: flex;
  height: 240px;
  width: 100%;
  max-width: 1000px;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  transition: background-color 0.4s, border-color 0.4s; /* 主题切换时的平滑过渡 */
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  will-change: box-shadow;

  /* 应用变量 */
  background: var(--bg-color);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-base);
  color: var(--text-main);
}

/* 装饰层 */
.bg-crest {
  position: absolute;
  top: -20px; right: -20px;
  width: 120px; height: 120px;
  opacity: 0.2;
  z-index: 0;
  pointer-events: none;
  color: var(--crest-color); /* 纹章颜色 */
}
.crest-svg {
  width: 100%; height: 100%;
  filter: drop-shadow(0 0 5px currentColor);
}

.particle-container {
  position: absolute;
  bottom: 10px; right: 10px;
  pointer-events: none;
  opacity: 0.4;
  z-index: 0;
  color: var(--particle-color); /* 粒子颜色 */
}
.particle { font-size: 20px; position: absolute; }
.p1 { bottom: 0; right: 0; transform: rotate(15deg); }
.p2 { bottom: 20px; right: 30px; transform: rotate(-10deg) scale(0.8); }

/* 图片区域 */
.frame-box {
  flex: 0 0 320px;
  position: relative;
  margin: 0;
  background: #fff;
  z-index: 2;
  border-radius: 4px 0 0 4px;
  overflow: hidden;
  border-right: 1px solid var(--accent-color); /* 分隔线 */
}

.border-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  border: 4px solid #fff; /* 内白边 */
  outline: 1px solid var(--border-color-dim);
  box-shadow: inset 0 0 10px var(--border-color-dim);
  z-index: 3;
  pointer-events: none;
  border-radius: 4px 0 0 4px;
  outline-offset: -5px;
}

.sheen-effect {
  position: absolute;
  top: 0; left: 0; width: 50%; height: 100%;
  background: linear-gradient(to right, transparent, rgba(255,255,255,0.6), transparent);
  transform: skewX(-20deg);
  z-index: 2;
  opacity: 0;
  pointer-events: none;
}

.img-mask { width: 100%; height: 100%; background: #fff; }
.luna-img { width: 100%; height: 100%; object-fit: cover; will-change: transform; }

/* 内容区域 */
.text-panel {
  flex: 1;
  padding: 25px 35px 25px 25px;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.fancy-line-top {
  width: 100%; height: 1px;
  background: linear-gradient(90deg, var(--accent-color), transparent);
  margin-bottom: 15px;
  opacity: 0.5;
}
.fancy-line-bottom {
  width: 100%; height: 1px;
  background: linear-gradient(90deg, transparent, var(--accent-color));
  margin-top: 15px;
  opacity: 0.5;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-sub);
  font-style: italic;
  margin-bottom: 8px;
}
.chapter-num { color: var(--accent-color); letter-spacing: 1px; }

.luna-title {
  font-size: 1.6rem;
  color: var(--text-main);
  font-weight: 700;
  margin-bottom: 12px;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.luna-summary {
  font-size: 14px;
  color: var(--text-sub);
  line-height: 1.8;
  margin-bottom: auto;
  display: -webkit-box;
  /*! autoprefixer: off */
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
}

.luna-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}

.tag-ribbon span {
  background: rgba(255,255,255,0.1); /* 稍微通透一点 */
  color: var(--accent-color);
  padding: 2px 10px;
  font-size: 12px;
  border: 1px solid var(--border-color);
  border-radius: 2px;
}

.read-more-btn {
  font-family: 'Playfair Display', serif;
  font-size: 14px;
  color: var(--text-sub);
  font-style: italic;
  display: flex;
  align-items: center;
  gap: 5px;
}
.flower-icon { font-size: 16px; }

@media (max-width: 768px) {
  .luna-card { flex-direction: column; height: auto; }
  .frame-box { flex: 0 0 200px; border-radius: 4px 4px 0 0; border-right: none; border-bottom: 1px solid var(--accent-color); }
  .border-overlay { border-radius: 4px 4px 0 0; }
  .text-panel { padding: 20px; }
}
</style>
