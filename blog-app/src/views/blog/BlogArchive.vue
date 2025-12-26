<template>
  <div data-title="归档">
    <div class="archives-content">
      <div class="archives-content-main">
        <article-scroll-page></article-scroll-page>
      </div>
    </div>
  </div>
</template>

<script>
// 👇 修改引用路径，直接指向具体的 ArticleScrollPage.vue 文件

import ArticleScrollPage from '@/views/common/ArticleScrollPage'

export default {
  name: "BlogArchive",
  components: {
    'article-scroll-page': ArticleScrollPage
  },
}
</script>

<style scoped>
.archives-content {
  position: relative;
}

.archives-content-main {
  /* 保持顶部间距，避免被导航栏遮挡 */
  padding-top: 80px;
  margin: 0 auto;
  max-width: 1200px;
}
</style>
