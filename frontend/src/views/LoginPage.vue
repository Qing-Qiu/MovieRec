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
          <a-button type="primary" html-type="submit" class="submit-button" size="large" block>
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

let nickname = ref('');

const submitForm = async () => {
  try {
    const response = await axios.post('http://localhost:8080/auth/login',
        {
          username: formState.username,
          password: formState.password
        });
    if (response.data[0] === "success") {
      nickname = response.data[1];
      sessionStorage.setItem('username', formState.username);
      sessionStorage.setItem('nickname', nickname);
      message.success('登录成功！');
      await router.push('/home');
    } else {
      message.error('用户名或密码错误');
    }
  } catch (error) {
    console.error(error);
    message.error('系统错误，请稍后再试');
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
  background-color: #f0f2f5;
  background-image: 
    radial-gradient(at 0% 0%, hsla(253,16%,7%,1) 0, transparent 50%), 
    radial-gradient(at 50% 0%, hsla(225,39%,30%,1) 0, transparent 50%), 
    radial-gradient(at 100% 0%, hsla(339,49%,30%,1) 0, transparent 50%);
  background-image: 
    radial-gradient(at 40% 20%, hsla(28,100%,74%,0.1) 0px, transparent 50%),
    radial-gradient(at 80% 0%, hsla(189,100%,56%,0.1) 0px, transparent 50%),
    radial-gradient(at 0% 50%, hsla(355,100%,93%,0.1) 0px, transparent 50%),
    radial-gradient(at 80% 50%, hsla(340,100%,76%,0.1) 0px, transparent 50%),
    radial-gradient(at 0% 100%, hsla(22,100%,77%,0.1) 0px, transparent 50%),
    radial-gradient(at 80% 100%, hsla(242,100%,70%,0.1) 0px, transparent 50%),
    radial-gradient(at 0% 0%, hsla(343,100%,76%,0.1) 0px, transparent 50%);
  background: #ffffff; /* Fallback */
  position: relative;
  overflow: hidden;
}

/* Animated Background Shapes */
.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  z-index: 0;
  animation: float 20s infinite;
}

.login-container::before {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  top: -100px;
  left: -100px;
}

.login-container::after {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  bottom: -50px;
  right: -50px;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -50px) rotate(10deg); }
  66% { transform: translate(-20px, 20px) rotate(-5deg); }
}

.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.1);
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
  transform: translateY(-5px);
  box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.15);
}

.logo-area {
  text-align: center;
  margin-bottom: 30px;
}

.app-title {
  background: linear-gradient(45deg, #1890ff, #722ed1);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-size: 2.5rem;
  font-weight: 800;
  margin: 0;
  letter-spacing: -0.5px;
}

.app-subtitle {
  color: #666;
  font-size: 1rem;
  margin-top: 5px;
  font-weight: 500;
}

.login-form {
  width: 100%;
}

.form-title {
  color: #333;
  font-size: 1.5rem;
  margin-bottom: 25px;
  text-align: center;
  font-weight: 700;
}

:deep(.ant-input-affix-wrapper), :deep(.ant-input) {
  background: #f7f9fa !important;
  border: 1px solid transparent !important;
  color: #333 !important;
  border-radius: 12px;
  padding: 8px 11px;
  transition: all 0.3s;
  box-shadow: none !important;
}

:deep(.ant-input-affix-wrapper:hover), :deep(.ant-input:hover) {
  background: #fff !important;
  border-color: rgba(24, 144, 255, 0.5) !important;
  box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.1) !important;
}

:deep(.ant-input-affix-wrapper:focus), :deep(.ant-input-affix-wrapper-focused), :deep(.ant-input:focus) {
  background: #fff !important;
  border-color: #1890ff !important;
  box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.1) !important;
}

:deep(.ant-input::placeholder) {
  color: #999;
}

:deep(.ant-input-prefix) {
  color: #1890ff;
}

:deep(.ant-form-item-explain-error) {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

.submit-button {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  border: none;
  height: 48px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 10px;
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.3);
  transition: all 0.3s ease;
}

.submit-button:hover {
  background: linear-gradient(135deg, #40a9ff 0%, #5cdbd3 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.4);
}

.form-footer {
  margin-top: 24px;
  text-align: center;
  color: #666;
  font-size: 14px;
}

.footer-text {
  margin-right: 5px;
}

.footer-link {
  color: #1890ff;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.3s;
}

.footer-link:hover {
  color: #096dd9;
  text-decoration: underline;
}

.divider {
  margin: 0 10px;
  color: #ddd;
}
</style>
