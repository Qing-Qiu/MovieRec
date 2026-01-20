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
          <a-button type="primary" html-type="submit" class="submit-button" size="large" block>
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

const handleValidate = (...args) => {
  console.log(args);
}

const login = () => {
  router.push('/auth/login');
}

const onFinish = async values => {
  console.log('Success:', values);
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
  background: #ffffff;
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

/* Success State Styles */
.success-card {
  text-align: center;
}

:deep(.ant-result-title) {
  color: #333 !important;
  font-weight: 600;
}

:deep(.ant-result-subtitle) {
  color: #666 !important;
}

:deep(.ant-result-icon > .anticon) {
  color: #52c41a !important; /* Green for success */
}
</style>
