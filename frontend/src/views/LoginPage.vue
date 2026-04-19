<template>
  <div class="login-container">
    <div class="glass-card">
      <div class="logo-area">
        <h1 class="app-title">MovieRec</h1>
        <p class="app-subtitle">探索电影世界</p>
      </div>
      
      <a-form
        :model="formState"
        name="basic"
        layout="vertical"
        autocomplete="off"
        class="login-form"
        @submit="submitForm"
      >
        <h2 class="form-title">欢迎回来</h2>
        
        <a-form-item
          name="username"
          :rules="[{ required: true, message: '请输入用户名'}]"
        >
          <a-input
            v-model:value="formState.username"
            placeholder="用户名"
            size="large"
            class="custom-input"
          >
            <template #prefix>
              <user-outlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="password"
          :rules="[{ required: true, message: '请输入密码'}]"
        >
          <a-input-password
            v-model:value="formState.password"
            placeholder="密码"
            size="large"
            class="custom-input"
          >
             <template #prefix>
              <lock-outlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-button" size="large" block :loading="loading">
            登录
          </a-button>
        </a-form-item>
        
        <div class="form-footer">
          <span class="footer-text">还没有账号？</span>
          <a @click="register()" class="footer-link">注册</a>
          <span class="divider">|</span>
          <a @click="tourist()" class="footer-link">游客登录</a>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from "axios";
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

const router = useRouter();

const formState = reactive({
  username: '',
  password: '',
});

const nickname = ref('');
const loading = ref(false);

const submitForm = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const response = await axios.post('http://localhost:8080/auth/login',
        {
          username: formState.username,
          password: formState.password
        });
    if (response.data[0] === "success") {
      nickname.value = response.data[1];
      sessionStorage.setItem('username', formState.username);
      sessionStorage.setItem('nickname', nickname.value);
      message.success('登录成功！');
      await router.push('/home');
    } else {
      message.error('用户名或密码错误');
    }
  } catch (error) {
    console.error(error);
    message.error('系统错误，请稍后再试');
  } finally {
    loading.value = false;
  }
}

const register = () => {
  router.push('/auth/register');
}

const tourist = () => {
  router.push('/home');
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32px 16px;
  background: var(--movie-header);
  position: relative;
  overflow: hidden;
}

/* Animated Background Shapes */
.login-container::before,
.login-container::after {
  content: none;
}

.glass-card {
  background: var(--movie-surface);
  border-radius: var(--movie-radius);
  border: 1px solid var(--movie-line);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.28);
  padding: 40px;
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  z-index: 1;
}

.glass-card:hover {
  transform: none;
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.28);
}

.logo-area {
  text-align: center;
  margin-bottom: 30px;
}

.app-title {
  background: none;
  -webkit-text-fill-color: initial;
  color: var(--movie-ink);
  font-size: 2.5rem;
  font-weight: 800;
  margin: 0;
  letter-spacing: 0;
}

.app-subtitle {
  color: var(--movie-muted);
  font-size: 1rem;
  margin-top: 5px;
  font-weight: 500;
}

.login-form {
  width: 100%;
}

.form-title {
  color: var(--movie-ink);
  font-size: 1.5rem;
  margin-bottom: 25px;
  text-align: center;
  font-weight: 700;
}

:deep(.ant-input-affix-wrapper) {
  background: var(--movie-surface-soft) !important;
  border: 1px solid var(--movie-line) !important;
  color: var(--movie-ink) !important;
  border-radius: var(--movie-radius) !important;
  padding: 8px 11px;
  transition: all 0.3s;
  box-shadow: none !important;
}

:deep(.ant-input-affix-wrapper:hover) {
  background: #fff !important;
  border-color: rgba(196, 59, 69, 0.45) !important;
  box-shadow: none !important;
}

:deep(.ant-input-affix-wrapper:focus), :deep(.ant-input-affix-wrapper-focused) {
  background: #fff !important;
  border-color: var(--movie-accent) !important;
  box-shadow: 0 0 0 3px rgba(196, 59, 69, 0.12) !important;
}

:deep(.ant-input-affix-wrapper .ant-input) {
  background: transparent !important;
  border: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  padding-left: 8px;
}

:deep(.ant-input::placeholder) {
  color: #999;
}

:deep(.ant-input-prefix) {
  color: var(--movie-accent);
}

:deep(.ant-form-item-explain-error) {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

.submit-button {
  background: var(--movie-accent);
  border-color: var(--movie-accent);
  height: 48px;
  border-radius: var(--movie-radius);
  font-size: 16px;
  font-weight: 600;
  margin-top: 10px;
  box-shadow: 0 12px 22px rgba(196, 59, 69, 0.2);
  transition: all 0.3s ease;
}

.submit-button:hover {
  background: var(--movie-accent-dark) !important;
  border-color: var(--movie-accent-dark) !important;
  transform: translateY(-1px);
  box-shadow: 0 14px 26px rgba(196, 59, 69, 0.24);
}

.form-footer {
  margin-top: 24px;
  text-align: center;
  color: var(--movie-muted);
  font-size: 14px;
}

.footer-text {
  margin-right: 5px;
}

.footer-link {
  color: var(--movie-accent);
  font-weight: 600;
  cursor: pointer;
  transition: color 0.3s;
}

.footer-link:hover {
  color: var(--movie-accent-dark);
  text-decoration: underline;
}

.divider {
  margin: 0 10px;
  color: var(--movie-line);
}

@media (max-width: 480px) {
  .glass-card {
    padding: 32px 24px;
  }

  .app-title {
    font-size: 2.1rem;
  }
}
</style>
