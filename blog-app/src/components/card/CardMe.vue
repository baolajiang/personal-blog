<template>
  <div class="comic-me-card">

    <div class="comic-header">
      <div class="header-pattern"></div>
    </div>

    <div class="comic-avatar-box">
      <div class="avatar-border">
        <div class="headPhoto"></div>
      </div>
    </div>

    <div class="comic-info">
      <h1 class="comic-name">
        <span class="prefix">{{ $myName }}</span>
        <span class="suffix">の里世界</span>
      </h1>

      <div class="typing-box">
        <span class="typing-text">PRESS START BUTTON...</span>
      </div>
    </div>

    <div class="comic-talk-bubble" v-if="hitokoto">
      <p class="talk-content">“{{ hitokoto }}”</p>
      <p class="talk-from" v-if="from">——《{{ from }}》</p>
    </div>

    <div class="comic-tools">
      <a class="comic-btn qq" @click="showQQ" title="QQ交谈">
        <img src="../../../static/img/qq.png" alt="QQ">
      </a>
      <a class="comic-btn github" href="https://github.com" target="_blank" title="Github">
        <span>GIT</span>
      </a>
      <a class="comic-btn bilibili" href="#" title="Bilibili">
        <span>BILI</span>
      </a>
    </div>

    <div class="corner-mark">R</div>
  </div>
</template>

<script>

export default {
  name: 'cardMe',
  data() {
    return {
      qq: {title: 'QQ', message: 'qq:2693398551'},
      hitokoto: '',
      from: '',
      from_who: ''
    }
  },
  mounted() {
    this.showDazi()
  },
  methods: {
    showQQ() {
      location.href = "tencent://message/?uin=2693398551&Site=&Menu=yes";
    },
    showDazi() {
      let that = this;
      this.$axios.get('https://v1.hitokoto.cn/')
        .then(function (data) {
          that.hitokoto = data.data.hitokoto;
          that.from = data.data.from;
          that.from_who = data.data.from_who;
        })
        .catch(function (error) {
          console.log(error);
        });
    }
  }
}
</script>

<style scoped>
/* --- 整体卡片容器 --- */
.comic-me-card {
  background: #fff;
  border: 2px solid #333;
  border-radius: 8px;
  /* 漫画硬阴影 */
  box-shadow: 8px 8px 0px rgba(0,0,0,0.2);
  padding-bottom: 25px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  margin-bottom: 20px; /* 侧边栏卡片间距 */
}

.comic-me-card:hover {
  transform: translate(-2px, -2px);
  box-shadow: 10px 10px 0px #FF7F50; /* 悬停变橙色阴影 */
}

/* --- 1. 顶部背景 --- */
.comic-header {
  height: 100px;
  background-color: #333;
  position: relative;
  overflow: hidden;
}
/* 波点纹理 */
.header-pattern {
  width: 100%;
  height: 100%;
  background-image: radial-gradient(#555 15%, transparent 16%),
  radial-gradient(#555 15%, transparent 16%);
  background-size: 10px 10px;
  opacity: 0.3;
}

/* --- 2. 头像区域 --- */
.comic-avatar-box {
  display: flex;
  justify-content: center;
  margin-top: -50px; /* 向上插入背景 */
  position: relative;
  z-index: 2;
}

.avatar-border {
  padding: 4px;
  background: #fff;
  border: 2px solid #333;
  border-radius: 50%;
}

.headPhoto {
  width: 90px;
  height: 90px;
  /* 你的图片路径 */
  background: url(../../../static/img/f.jpg) no-repeat;
  background-size: cover;
  border-radius: 50%;
  border: 2px solid #333;
  transition: transform 0.5s cubic-bezier(0.68, -0.55, 0.27, 1.55);
}

.headPhoto:hover {
  transform: rotate(360deg) scale(1.1);
}

/* --- 3. 名字 & 信息 --- */
.comic-info {
  text-align: center;
  padding-top: 15px;
}

.comic-name {
  margin: 0;
  font-size: 22px;
  font-weight: 900;
  color: #333;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.suffix {
  color: #FF7F50; /* 橙色点缀 */
}

/* 打字机特效容器 */
.typing-box {
  margin: 10px auto;
  background: #f4f4f4;
  border: 1px dashed #999;
  padding: 5px 10px;
  display: inline-block;
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  color: #666;
}

.typing-text {
  border-right: 2px solid #333;
  white-space: nowrap;
  overflow: hidden;
  animation: typing 4s steps(30, end) infinite, blink 0.75s step-end infinite;
}

@keyframes typing {
  0% { width: 0; }
  50% { width: 100%; }
  90% { width: 100%; }
  100% { width: 0; }
}
@keyframes blink {
  50% { border-color: transparent; }
}

/* --- 4. 对话气泡 (一言) --- */
.comic-talk-bubble {
  position: relative;
  background: #333;
  color: #fff;
  margin: 15px 20px;
  padding: 15px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.6;
}
/* 气泡的小三角 */
.comic-talk-bubble::after {
  content: '';
  position: absolute;
  top: -10px;
  left: 50%;
  margin-left: -10px;
  border-width: 0 10px 10px;
  border-style: solid;
  border-color: #333 transparent;
  display: block;
}

.talk-from {
  text-align: right;
  font-size: 12px;
  color: #aaa;
  margin-top: 5px;
  font-style: italic;
}

/* --- 5. 社交按钮组 --- */
.comic-tools {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding: 0 20px;
}

.comic-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #333;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  color: #333;
  font-weight: 800;
  font-size: 10px;
  text-decoration: none;

  /* 默认硬阴影 */
  box-shadow: 3px 3px 0px rgba(0,0,0,0.1);
}

.comic-btn img {
  width: 24px;
}

.comic-btn:hover {
  transform: translate(-2px, -2px);
  box-shadow: 4px 4px 0px #333;
}
.comic-btn:active {
  transform: translate(0, 0);
  box-shadow: 0px 0px 0px #333;
}

.comic-btn.qq:hover { background: #E0F2F1; }
.comic-btn.github:hover { background: #ECEFF1; }
.comic-btn.bilibili:hover { background: #FCE4EC; color: #FB7299; }

/* 装饰角标 */
.corner-mark {
  position: absolute;
  bottom: 5px;
  right: 8px;
  font-family: Impact, sans-serif;
  font-size: 40px;
  color: rgba(0,0,0,0.05);
  pointer-events: none;
  transform: rotate(-15deg);
}
</style>
