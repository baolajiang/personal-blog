<template>
  <div class="chat-wrapper">
    <div v-show="!isVisible" class="float-btn" @click="toggleChat">
      <span class="icon">🤖</span>
      <span class="text">AI 助手</span>
    </div>

    <transition name="slide-up">
      <div v-show="isVisible" class="chat-box">
        <div class="chat-header">
          <div class="header-left">
            <span class="avatar">🐱</span>
            <span class="title">AI 奈奈酱</span>
          </div>
          <span class="close-btn" @click="toggleChat">×</span>
        </div>

        <div class="chat-content" ref="msgBoxRef">
          <div v-for="(item, index) in msgList" :key="index" :class="['message-row', item.role]">
            <div class="msg-avatar">{{ item.role === 'ai' ? '🐱' : '🧑' }}</div>
            <div class="msg-bubble">
              <div v-if="item.loading" class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
              <div v-else>{{ item.content }}</div>
            </div>
          </div>
        </div>

        <div class="chat-footer">
          <input
            v-model="inputMsg"
            @keyup.enter="handleSend"
            type="text"
            placeholder="问我关于 Java 的问题..."
            :disabled="isSending"
          />
          <button @click="handleSend" :disabled="isSending || !inputMsg.trim()">
            {{ isSending ? '...' : '发送' }}
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
// 引入 API (确保这个路径下的文件里没有用 requset 这个恶意包)
import { sendChatMsg } from '@/api/chat';

export default {
  name: 'ChatBot',
  // Vue 2: 所有的变量都要定义在 data 函数里
  data() {
    return {
      isVisible: false, // 控制显示隐藏
      inputMsg: '',     // 输入框内容
      isSending: false, // 发送状态
      // 初始对话列表
      msgList: [
        { role: 'ai', content: '主人好喵~ 我是你的博客助理，有什么技术问题可以问我哦！' }
      ]
    };
  },
  // Vue 2: 所有的方法都要定义在 methods 里
  methods: {
    // 切换显示状态
    toggleChat() {
      this.isVisible = !this.isVisible;
      if (this.isVisible) {
        this.scrollToBottom();
      }
    },

    // 滚动到底部
    scrollToBottom() {
      // Vue 2 使用 this.$nextTick
      this.$nextTick(() => {
        // Vue 2 使用 this.$refs 获取 DOM 元素
        const box = this.$refs.msgBoxRef;
        if (box) {
          box.scrollTop = box.scrollHeight;
        }
      });
    },

    // 发送消息核心逻辑
    async handleSend() {
      const text = this.inputMsg.trim();
      if (!text || this.isSending) return;

      // 1. 推入用户消息
      this.msgList.push({ role: 'user', content: text });
      this.inputMsg = '';
      this.isSending = true;
      this.scrollToBottom();

      // 2. 推入一个“正在输入”的占位符
      this.msgList.push({ role: 'ai', loading: true });
      this.scrollToBottom();

      try {
        // 3. 调用后端 API
        const res = await sendChatMsg(text);

        // 4. 处理返回数据
        // 注意：如果你后端返回的是对象结构，请根据实际情况修改这里
        // 例如：const reply = res.data.answer || res.data;
        const reply = typeof res === 'string' ? res : (res.data || '喵? 后端没返回数据');

        // 移除 loading (最后一个元素)
        this.msgList.pop();
        // 添加真实回复
        this.msgList.push({ role: 'ai', content: reply });

      } catch (error) {
        this.msgList.pop(); // 移除 loading
        this.msgList.push({ role: 'ai', content: '呜呜... 连接服务器失败了 (´；ω；`)' });
        console.error('Chat Error:', error);
      } finally {
        this.isSending = false;
        this.scrollToBottom();
      }
    }
  }
};
</script>

<style scoped>
/* 加上 scoped 属性，防止样式污染全局
  如果你希望样式全局生效，可以去掉 scoped
*/

/* 悬浮按钮样式 */
.float-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: #409EFF;
  color: white;
  padding: 12px 20px;
  border-radius: 50px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  z-index: 999;
}
.float-btn:hover { transform: scale(1.05); background: #66b1ff; }

/* 聊天框主体 */
.chat-box {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 350px;
  height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1000;
  border: 1px solid #e0e0e0;
}

/* 头部 */
.chat-header {
  background: #409EFF;
  color: white;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left { display: flex; align-items: center; gap: 8px; font-weight: bold; }
.close-btn { cursor: pointer; font-size: 20px; opacity: 0.8; }
.close-btn:hover { opacity: 1; }

/* 内容区 */
.chat-content {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background: #f9f9f9;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 消息行 */
.message-row { display: flex; gap: 10px; align-items: flex-start; }
.message-row.user { flex-direction: row-reverse; }

.msg-avatar {
  width: 32px; height: 32px;
  background: #e0e0e0;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
}
.message-row.ai .msg-avatar { background: #fff3e0; }
.message-row.user .msg-avatar { background: #d9ecff; }

/* 气泡 */
.msg-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
  position: relative;
}
.message-row.ai .msg-bubble { background: white; border: 1px solid #eee; color: #333; border-top-left-radius: 0; }
.message-row.user .msg-bubble { background: #409EFF; color: white; border-top-right-radius: 0; }

/* 底部输入框 */
.chat-footer {
  padding: 12px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
  background: white;
}
.chat-footer input {
  flex: 1;
  border: 1px solid #ddd;
  padding: 8px 12px;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
}
.chat-footer input:focus { border-color: #409EFF; }
.chat-footer button {
  background: #409EFF;
  color: white;
  border: none;
  padding: 0 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  transition: 0.2s;
}
.chat-footer button:disabled { background: #a0cfff; cursor: not-allowed; }

/* 打字动画 */
.typing-indicator span {
  display: inline-block;
  width: 6px; height: 6px;
  background: #909399;
  border-radius: 50%;
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out both;
}
.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 进场动画 */
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s ease; }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(20px); opacity: 0; }
</style>
