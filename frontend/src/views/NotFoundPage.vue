<template>
  <div class="not-found-container">
    <div class="not-found-panel">
      <div class="status-code">404</div>
      <h1>页面走丢了</h1>
      <p>当前地址没有对应的 MovieRec 页面。</p>
      <a-button type="primary" class="back-button" @click="backward()">
        <ArrowLeftOutlined/>
        返回登录
      </a-button>
    </div>
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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  background: var(--movie-header);
  text-align: center;
}

.not-found-panel {
  width: min(420px, 100%);
  padding: 40px;
  background: var(--movie-surface);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.28);
}

.status-code {
  color: var(--movie-accent);
  font-size: 64px;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 16px;
}

h1 {
  color: var(--movie-ink);
  font-size: 28px;
  margin: 0 0 10px;
}

p {
  color: var(--movie-muted);
  margin: 0 0 28px;
}

.back-button {
  height: 42px;
  background: var(--movie-accent);
  border-color: var(--movie-accent);
  border-radius: var(--movie-radius);
  padding: 0 24px;
}

.back-button:hover {
  background: var(--movie-accent-dark) !important;
  border-color: var(--movie-accent-dark) !important;
}
</style>
