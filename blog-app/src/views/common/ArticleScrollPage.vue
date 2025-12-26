<template>
  <div class="article-list-container">
    <article-item
      v-for="(a, index) in articles"
      :key="a.id"
      v-bind="a"
      :index="index">
    </article-item>

    <div v-if="noData" class="no-data">
      <el-empty description="这里空空如也..."></el-empty>
    </div>

    <div class="pagination-box" v-if="!loading && !noData">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page.sync="innerPage.pageNumber"
        :page-size="innerPage.pageSize"
        :total="total"
        @current-change="handlePageChange">
      </el-pagination>
    </div>

    <div v-if="loading" class="loading-box">
      <i class="el-icon-loading"></i> 加载中...
    </div>
  </div>
</template>

<script>
import ArticleItem from '@/components/article/ArticleItem'
import {getArticles} from '@/api/article'

export default {
  name: "ArticleScrollPage",
  components: {
    'article-item': ArticleItem
  },
  props: {
    offset: {
      type: Number,
      default: 100
    },
    page: {
      type: Object,
      default() {
        return {}
      }
    },
    query: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  data() {
    return {
      loading: false,
      noData: false,
      innerPage: {
        pageSize: 10,
        pageNumber: 1,
        name: 'a.createDate',
        sort: 'desc'
      },
      total: 0, // 用于存放文章总数
      articles: []
    }
  },
  watch: {
    'query': {
      handler() {
        this.resetAndLoad()
      },
      deep: true
    },
    'page': {
      handler() {
        this.innerPage = { ...this.innerPage, ...this.page }
        this.resetAndLoad()
      },
      deep: true
    }
  },
  created() {
    this.getArticles()
  },
  methods: {
    // 重置页码并重新加载
    resetAndLoad() {
      this.noData = false
      this.articles = []
      this.innerPage.pageNumber = 1
      this.getArticles()
    },

    // 处理页码改变事件
    handlePageChange(val) {
      this.innerPage.pageNumber = val
      this.getArticles()
      // 回到顶部，提升体验
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },

    view(id) {
      this.$router.push({ path: `/view/${id}` })
    },

    getArticles() {
      let that = this
      that.loading = true

      getArticles(that.query, that.innerPage, this.$store.state.token).then(data => {

        let responseData = data.data;
        let newArticles = []

        // 处理数据结构 (兼容 List 和 Map)
        if (responseData && responseData.articles) {
          newArticles = responseData.articles
          that.total = responseData.total
        } else {
          newArticles = responseData
          that.total = 0
        }

        if (newArticles && newArticles.length > 0) {
          // 因为后端已经处理成了乱码，前端只需要直接显示即可
          that.articles = newArticles
          that.noData = false

        } else {
          that.articles = []
          that.noData = true
        }

      }).catch(error => {
        if (error !== 'error') {
          that.$message({ type: 'error', message: '文章加载失败!', showClose: true })
        }
      }).finally(() => {
        that.loading = false
      })
    }
  }
}
</script>

<style scoped>
.el-card {
  border-radius: 0;
}

.el-card:not(:first-child) {
  margin-top: 10px;
}

.pagination-box {
  margin: 20px 0;
  text-align: center;
  background-color: #fff; /* 给个背景色或者透明看你喜好 */
  padding: 10px;
}

.loading-box {
  text-align: center;
  padding: 20px;
  color: #666;
}

.no-data {
  margin-top: 20px;
  background: #fff;
  padding: 20px;
  text-align: center;
}
</style>
