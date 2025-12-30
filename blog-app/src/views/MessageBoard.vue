<template>

  <div class="me-message" v-title :data-title="title">
    <el-container class="">
      <el-main class="me-main">
        <el-alert
          class="message-alert"
          title="不能留言哦。。。。。。。。。。。"
          type="warning"
          center
          show-icon>
        </el-alert>

      </el-main>
    </el-container>
  </div>
</template>

<script>
  export default {
    name: 'MessageBoard',
    data() {
      return {
      }
    },
    computed: {
      title (){
        return '留言板 - Blog'
      }
    }
  }
</script>

<style scoped>
  .me-message {
    height: 100vh;
    width: 100%;
  }

  .el-container {
    width: 700px;
  }
  .message-alert{
    margin-top: 20px;
    align-content: center;
  }

  .me-main {
    overflow: hidden;
  }

  .me-log-box {
    margin-left: 30%;
    margin-top: 20px;

  }

</style>
