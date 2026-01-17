<template>
  <div class="me-comment-item">
    <div class="me-comment-avatar">
      <img :src="comment.author.avatar || defaultAvatar" @error="imgError">
    </div>

    <div class="me-comment-content">

      <div class="me-comment-user">
        <span class="me-nickname">{{comment.author.nickname}}</span>
        <span class="me-level" v-if="rootCommentCounts">#{{rootCommentCounts - index}}</span>
      </div>

      <div class="me-comment-text">
        {{comment.content}}
      </div>

      <div class="me-comment-info">
        <span class="me-time">{{comment.createDate | format}}</span>
        <span class="me-reply-btn" @click="showComment(comment.id, comment.author)">回复</span>
      </div>

      <div class="me-reply-list" v-if="comment.childrens && comment.childrens.length > 0">

        <div class="me-reply-item" v-for="c in comment.childrens" :key="c.id">
          <div class="me-sub-avatar">
            <img :src="c.author.avatar || defaultAvatar" @error="imgError">
          </div>

          <div class="me-sub-content-box">
            <div class="me-sub-text-flow">
              <span class="me-sub-nickname">{{c.author.nickname}}</span>

              <span v-if="c.toUser && c.toUser.id !== comment.author.id" class="me-sub-to">
                   回复 <span class="me-at">@{{c.toUser.nickname}}</span>
                </span>

              <span class="me-sub-content-text">：{{c.content}}</span>
            </div>

            <div class="me-sub-info">
              <span class="me-time">{{c.createDate | format}}</span>
              <span class="me-reply-btn" @click="showComment(comment.id, c.author)">回复</span>
            </div>
          </div>
        </div>

      </div>

      <div class="me-reply-box" v-if="commentShow">
        <el-input
          v-model="reply.content"
          type="textarea"
          :placeholder="placeholder"
          class="me-reply-input"
          :rows="2"
          resize="none">
        </el-input>
        <div class="me-reply-footer">
          <el-button size="mini" @click="commentShow = false">取消</el-button>
          <el-button size="mini" type="primary" @click="publishComment()">发布</el-button>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import {publishComment} from '@/api/comment'
import default_avatar from '@/assets/img/default_avatar.png'

export default {
  name: "CommentItem",
  props: {
    articleId: '',
    comment: '',
    index: '',
    rootCommentCounts: ''
  },
  data() {
    return {
      defaultAvatar: default_avatar,
      placeholder: '发一条友善的评论...',
      commentShow: false,
      reply: {
        articleId: this.articleId,
        parent: '',
        toUserId: '',
        content: ''
      }
    }
  },
  methods: {
    imgError(e) {
      e.target.src = this.defaultAvatar;
    },
    showComment(rootId, toUser) {
      if (this.commentShow && this.reply.toUserId === toUser.id) {
        this.commentShow = false;
        return;
      }
      // 强制绑定到根评论ID
      this.reply.parent = rootId;
      this.reply.toUserId = toUser.id;
      this.reply.content = '';
      this.placeholder = `回复 @${toUser.nickname}：`;
      this.commentShow = true;
    },
    publishComment() {
      let that = this
      if (!that.reply.content.trim()) {
        that.$myMessage({type: 'warning', content: '内容不能为空'})
        return
      }
      publishComment(that.reply, this.$store.state.token).then(data => {
        if(data.success){
          that.$myMessage({type: 'success', content: '回复成功'})
          that.commentShow = false
          that.$emit('commentCountsIncrement') // 通知父组件刷新
        }else{
          that.$myMessage({type: 'error', content: data.msg})
        }
      }).catch(err => {
        that.$myMessage({type: 'error', content: '网络错误'})
      })
    }
  }
}
</script>

<style scoped>
/* --- 容器布局 --- */
.me-comment-item {
  display: flex;
  padding: 22px 0;
  border-bottom: 1px solid #e5e9ef;
}

/* 左侧头像 */
.me-comment-avatar {
  margin-right: 16px;
  flex-shrink: 0;
}
.me-comment-avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  cursor: pointer;
}

/* 右侧主体 */
.me-comment-content {
  flex: 1;
  min-width: 0;
}

/* 1. 昵称行 */
.me-comment-user {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  line-height: 1.2;
}
.me-nickname {
  font-size: 13px;
  font-weight: bold;
  color: #fb7299;
  cursor: pointer;
  margin-right: 10px;
  margin-top: 0; /* 确保无外边距 */
}
.me-level {
  font-size: 12px;
  color: #999;
}

/* 2. 评论内容 (已移除 white-space: pre-wrap) */
.me-comment-text {
  font-size: 14px;
  line-height: 24px;
  color: #222;
  margin: 0 0 8px 0 !important; /* 强制归零左边距 */
  padding: 0 !important;        /* 强制归零内边距 */
  word-break: break-all;
}

/* 3. 信息栏 */
.me-comment-info {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #99a2aa;
  margin-bottom: 14px;
}
.me-time {
  margin-right: 20px;
}
.me-reply-btn {
  cursor: pointer;
  transition: color 0.2s;
}
.me-reply-btn:hover {
  color: #00a1d6;
}

/* --- 子评论区域 --- */
.me-reply-list {
  background: #f9f9f9;
  padding: 10px 15px;
  border-radius: 4px;
  font-size: 13px;
}

.me-reply-item {
  display: flex;
  margin-bottom: 10px;
}
.me-reply-item:last-child { margin-bottom: 0; }

/* 子评论小头像 */
.me-sub-avatar { margin-right: 10px; flex-shrink: 0; }
.me-sub-avatar img { width: 24px; height: 24px; border-radius: 50%; }

.me-sub-content-box { flex: 1; }

/* 子评论内容流 */
.me-sub-text-flow {
  line-height: 20px;
  color: #222;
}
.me-sub-nickname {
  color: #fb7299;
  font-weight: bold;
  cursor: pointer;
  margin-right: 4px;
}
.me-sub-to {
  color: #222;
  margin-right: 4px;
}
.me-at { color: #00a1d6; }

.me-sub-content-text {
  color: #222;
}

/* 子评论底部信息 */
.me-sub-info {
  margin-top: 4px;
  font-size: 12px;
  color: #99a2aa;
}

/* --- 输入框 --- */
.me-reply-box {
  margin-top: 15px;
}
.me-reply-footer {
  margin-top: 10px;
  text-align: right;
}
</style>
