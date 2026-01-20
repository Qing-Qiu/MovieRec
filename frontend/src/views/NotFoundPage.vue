<template>
  <div class="not-found-container">
    <h1>404 NOT FOUND</h1>
    <a-button @click="backward()">
      <ArrowLeftOutlined/>
    </a-button>
  </div>
</template>

<script>
import {ArrowLeftOutlined} from "@ant-design/icons-vue";
import router from "@/router/router";

export default {
  components: {
    ArrowLeftOutlined,
  },
  methods: {
    backward() {
      router.push('/auth/login');
    }
  }
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh; /* 使容器铺满整个视口高度 */
  text-align: center;
  font-family: 'Arial', sans-serif;
}

h1 {
  font-size: 2em;
  margin-bottom: 20px;
  color: #333; /* 设置文本颜色 */
}

/* 你可以在这里添加其他样式，如背景、链接样式等 */
</style>
