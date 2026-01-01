<template>
  <div data-title="文章页面">
    <div class="articles-content">
      <div class="articles-content-main">
        <article-scroll-page></article-scroll-page>
      </div>
    </div>
  </div>
</template>

<script>

import ArticleListPage from '@/views/common/ArticleListPage'

export default {
  name: "BlogArchive",
  components: {
    'article-scroll-page': ArticleListPage
  },
}
</script>

<style scoped>
.articles-content {
  position: relative;
}

.articles-content-main {
  /* 保持顶部间距，避免被导航栏遮挡 */
  padding-top: 80px;
  margin: 0 auto;
  max-width: 1200px;
}
</style>
