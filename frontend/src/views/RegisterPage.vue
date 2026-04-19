<template>
  <div class="login-container">
    <div class="glass-card" v-if="stat==='login'" :key="1">
      <div class="logo-area">
        <h1 class="app-title">MovieRec</h1>
        <p class="app-subtitle">加入我们</p>
      </div>

      <a-form
        ref="formRef"
        :model="formState"
        name="basic"
        layout="vertical"
        :rules="rules"
        autocomplete="off"
        @finish="onFinish"
        class="login-form"
        @validate="handleValidate"
      >
        <h2 class="form-title">创建账号</h2>

        <a-form-item
          name="nickname"
          class="input-item"
        >
          <a-input
            v-model:value="formState.nickname"
            placeholder="昵称"
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
          class="input-item"
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

        <a-form-item
          name="password2"
          class="input-item"
        >
          <a-input-password
            v-model:value="formState.password2"
            placeholder="确认密码"
            size="large"
            class="custom-input"
          >
             <template #prefix>
              <lock-outlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-button" size="large" block :loading="submitting">
            注册
          </a-button>
        </a-form-item>
        
        <div class="form-footer">
          <span class="footer-text">已有账号？</span>
          <a @click="login()" class="footer-link">登录</a>
        </div>
      </a-form>
    </div>

    <div class="glass-card success-card" v-else-if="stat==='success'" :key="2">
      <div class="logo-area">
        <h1 class="app-title">欢迎！</h1>
      </div>
      <a-result
        status="success"
        title="注册成功！"
        :sub-title="generator(username)"
        class="custom-result"
      >
        <template #extra>
          <a-button type="primary" @click="login()" class="submit-button" size="large">前往登录</a-button>
        </template>
      </a-result>
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
const formRef = ref();
let stat = ref('login');
let username = ref('');
const submitting = ref(false);

const formState = reactive({
  nickname: '',
  password: '',
  password2: '',
});

const checkPwd = async (_rule, value) => {
  if (value === '')
    return Promise.reject('请再次输入密码');
  else if (value !== formState.password)
    return Promise.reject('两次密码不一致');
  return Promise.resolve();
}

const rules = {
  nickname: [
    {
      required: true,
      message: '请输入昵称',
      trigger: 'change'
    }
  ],
  password: [
    {
      required: true,
      message: '密码必须由6-20位字母或数字组成',
      pattern: '^[A-Za-z0-9]{6,20}$',
      trigger: 'change'
    }
  ],
  password2: [
    {
      required: true,
      trigger: 'change',
      validator: checkPwd,
    }
  ]
}

const handleValidate = () => {}

const login = () => {
  router.push('/auth/login');
}

const onFinish = async values => {
  if (submitting.value) return;
  submitting.value = true;
  try {
      const response = await axios.post('http://localhost:8080/auth/register',
          {
            nickname: formState.nickname,
            password: formState.password
          });
      if (response.data[0] === "success") {
        username.value = response.data[1];
        // Copy logic
        var textArea = document.createElement("textarea");
        textArea.value = response.data[1];
        textArea.style.position = "fixed";
        textArea.style.top = 0;
        textArea.style.left = 0;
        textArea.style.width = "2em";
        textArea.style.height = "2em";
        textArea.style.padding = 0;
        textArea.style.border = "none";
        textArea.style.outline = "none";
        textArea.style.boxShadow = "none";
        textArea.style.background = "transparent";
        document.body.appendChild(textArea);
        textArea.select();
        try {
          var successful = document.execCommand('copy');
          var msg = successful ? '成功复制到剪贴板' : '无法复制到剪贴板';
          if (successful) {
            message.success('账号已复制到剪贴板');
          } else {
            message.warning('无法复制到剪贴板，请手动记录');
          }
          console.log(msg);
        } catch (err) {
          console.error('无法执行复制命令', err);
        }
        document.body.removeChild(textArea);
        stat.value = 'success';
      } else {
        message.error("注册过程中发生错误");
        setTimeout(() => window.location.reload(), 1500);
      }
  } catch (error) {
      console.log(error);
      message.error("网络错误");
  } finally {
      submitting.value = false;
  }
};

const generator = (username) => {
  return "请记住您的账号：" + username + "\n账号已经复制到您的剪贴板.";
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
  max-width: 450px;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
}

.logo-area {
  text-align: center;
  margin-bottom: 20px;
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

/* Success State Styles */
.success-card {
  text-align: center;
}

:deep(.ant-result-title) {
  color: var(--movie-ink) !important;
  font-weight: 600;
}

:deep(.ant-result-subtitle) {
  color: var(--movie-muted) !important;
}

:deep(.ant-result-icon > .anticon) {
  color: var(--movie-teal) !important;
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
