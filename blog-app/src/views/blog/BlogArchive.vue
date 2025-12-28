<template>
  <div data-title="归档">
    <div class="archives-content">
      <div class="archives-content-main">
        <article-list-page :theme="currentTheme"></article-list-page>
      </div>
    </div>
  </div>
</template>

<script>
import ArticleListPage from '@/views/common/ArticleListPage'
import { mapState } from 'vuex' // 👈 1. 引入 mapState (假设你用 Vuex 管理主题)

export default {
  name: "BlogArchive",
  components: {
    'article-list-page': ArticleListPage
  },
  computed: {
    // 👇 3. 获取全局主题状态
    // 假设你的 store 里有个变量叫 theme (值为 'dark' 或 'light')
    // 如果你的 store 结构不一样，请根据实际情况修改这里
    ...mapState({
      currentTheme: state => state.theme
    }),

    // 如果你没有 store，想写死测试一下，就用下面这个 computed：
    currentTheme() {
      return 'luna'; // 强制改为 'dark' 看看效果
    }
  }
}
</script>

<style scoped>
.archives-content {
  position: relative;
}

.archives-content-main {
  padding-top: 80px;
  margin: 0 auto;
  max-width: 1200px;
}
</style>
