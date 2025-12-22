<template>
  <div class="comic-card-wrap">
    <div
      class="comic-card"
      :class="{ 'is-reverse': textalign(index) === 'right' }"
      @click="view(id)"
    >

      <div class="comic-img-box">
        <img :src="Imgview(index)" class="comic-cover" alt="cover">
        <div class="img-mask">
          <span class="mask-text">READ</span>
        </div>
      </div>

      <div class="comic-content">
        <div class="bg-number">{{ index + 1 < 10 ? '0' + (index + 1) : index + 1 }}</div>

        <div class="meta-header">
           <span class="date-tag">
             <i class="el-icon-date"></i> {{ createDate }}
           </span>
        </div>

        <h3 class="comic-title">
          <span class="hashtag">#</span> {{ title }}
        </h3>

        <div class="comic-summary">
          {{ summary }}
        </div>

        <div class="comic-footer">
          <div class="tags-group">
            <el-tag
              v-for="tag in tags"
              :key="tag.id"
              size="mini"
              type="info"
              effect="plain"
              class="comic-tag">
              {{ tag.tagName }}
            </el-tag>
          </div>

          <div class="stats-group">
            <span><i class="el-icon-view"></i> {{ viewCounts }}</span>
            <span><i class="el-icon-chat-square"></i> {{ commentCounts }}</span>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { formatTime } from "@/utils/time";

export default {
  name: 'ArticleItem',
  props: {
    id: String,
    weight: Number,
    title: String,
    commentCounts: Number,
    viewCounts: Number,
    summary: String,
    author: String,
    tags: Array, // 接收标签数组
    createDate: String,
    cover: String,
    index: Number,
  },
  methods: {
    view(id) {
      this.$router.push({ path: `/view/${id}` })
    },
    Imgview(index) {
      // 随机二次元图，加 random 参数防止图片重复
      return "https://www.loliapi.com/acg/?uuid=" + index
    },
    textalign(index) {
      return index % 2 != 0 ? "right" : "left";
    },
    formatTime
  }
}
</script>

<style scoped>
/* 容器边距 */
.comic-card-wrap {
  padding: 0 10px;
  margin-bottom: 30px;
}

/* === 核心卡片设计 === */
.comic-card {
  position: relative;
  display: flex;
  height: 220px;
  background: #fff;
  border: 2px solid #333; /* 粗黑边框 */
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
  box-shadow: 6px 6px 0px rgba(50, 50, 50, 0.2); /* 实色阴影 */
}

/* 悬停动效 */
.comic-card:hover {
  transform: translate(-4px, -4px);
  box-shadow: 10px 10px 0px #FF7F50; /* 悬停变橙色 */
  border-color: #333;
}

/* === 1. 图片区 === */
.comic-img-box {
  width: 45%;
  position: relative;
  border-right: 2px solid #333; /* 电脑端：右侧分割线 */
  overflow: hidden;
  flex-shrink: 0;
}

.comic-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.comic-card:hover .comic-cover { transform: scale(1.1); }

.img-mask {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.mask-text {
  color: #fff; border: 2px solid #fff; padding: 5px 15px;
  font-weight: 900; letter-spacing: 2px; transform: rotate(-5deg);
}
.comic-card:hover .img-mask { opacity: 1; }

/* === 2. 内容区 === */
.comic-content {
  flex: 1;
  padding: 15px 20px;
  display: flex;
  flex-direction: column;
  position: relative;
  background-image: radial-gradient(#ddd 1px, transparent 1px);
  background-size: 8px 8px;
  overflow: hidden;
}

.bg-number {
  position: absolute; right: 10px; top: -10px;
  font-size: 5rem; font-weight: 900; color: rgba(0,0,0,0.04);
  font-family: Impact, sans-serif; pointer-events: none;
}

.meta-header { margin-bottom: 5px; z-index: 1; }
.date-tag {
  background: #333; color: #fff; padding: 2px 6px;
  font-size: 12px; border-radius: 4px; font-weight: bold;
}

.comic-title {
  margin: 5px 0;
  font-size: 1.3rem;
  color: #333;
  font-weight: 800;
  z-index: 1;
  transition: color 0.3s;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.hashtag { color: #FF7F50; margin-right: 5px; }
.comic-card:hover .comic-title { color: #FF7F50; }

.comic-summary {
  font-size: 13px; color: #666; line-height: 1.6;
  height: 42px; overflow: hidden;
  display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2;
  margin-bottom: auto; z-index: 1;
}

/* === 底部栏 (Tag + 统计) === */
.comic-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 1;
  border-top: 2px dashed #ccc; /* 虚线分割 */
  padding-top: 8px;
  margin-top: 8px;
}

/* ⭐ 标签样式自定义 ⭐ */
.tags-group {
  display: flex;
  flex-wrap: wrap; /* 标签多了自动换行 */
  gap: 5px; /* 标签间距 */
  max-width: 70%; /* 防止挤压统计数据 */
}

/* 强制覆盖 el-tag 样式使其符合漫画风 */
.comic-tag {
  border-radius: 0 !important; /* 方形标签 */
  border: 1px solid #333 !important;
  color: #333 !important;
  background: #fff !important;
  font-weight: bold;
}

.stats-group {
  font-size: 12px; color: #888; font-weight: 600;
  flex-shrink: 0;
}
.stats-group span { margin-left: 10px; }

/* === 电脑端：左右反转 (Zigzag) === */
.is-reverse { flex-direction: row-reverse; }
.is-reverse .comic-img-box {
  border-right: none;
  border-left: 2px solid #333; /* 分割线变到左边 */
}
.is-reverse .bg-number { right: auto; left: 10px; }


/* =========================================
   📱 移动端响应式核心 (Max-width: 768px)
   ========================================= */
@media screen and (max-width: 768px) {
  .comic-card-wrap {
    padding: 0;
  }

  /* 1. 强制变成上下结构 */
  .comic-card,
  .comic-card.is-reverse {
    flex-direction: column !important;
    height: auto; /* 高度自适应 */
  }

  /* 2. 图片在上面 */
  .comic-img-box {
    width: 100%;
    height: 160px; /* 图片高度 */
    border-right: none;
    border-left: none;
    /* ⭐ 核心：这就是你要的分割线 ⭐ */
    border-bottom: 2px solid #333 !important;
  }

  /* 3. 清楚反转带来的副作用 */
  .is-reverse .comic-img-box {
    border-left: none;
  }

  /* 4. 内容区调整 */
  .comic-content {
    padding: 12px 15px;
  }

  .comic-title { font-size: 1.1rem; }
  .bg-number { font-size: 4rem; top: 0; }

  /* 5. 手机上标签只显示一行，多了隐藏 */
  .tags-group {
    max-width: 60%;
    height: 24px;
    overflow: hidden;
  }
}
</style>
