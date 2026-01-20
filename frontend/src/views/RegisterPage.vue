<template>
  <div class="login-container" v-if="stat==='login'" :key="1">
    <a-form
        ref="formRef"
        :model="formState"
        name="basic"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 16 }"
        :rules="rules"
        autocomplete="off"
        @finish="onFinish"
        class="login-form"
        @validate="handleValidate"
    >
      <h1 class="login-title">用户注册</h1>
      <a-form-item
          label="昵称"
          name="nickname"
          class="input-item"
          style="text-align: left;"
      >
        <a-input
            v-model:value="formState.nickname"
            placeholder="昵称"
        />
      </a-form-item>
      <a-form-item
          label="密码"
          name="password"
          class="input-item"
          style="text-align: left;"
      >
        <a-input-password
            v-model:value="formState.password"
            placeholder="密码"
        />
      </a-form-item>

      <a-form-item
          label="确认密码"
          name="password2"
          class="input-item"
          style="text-align: left;"
      >
        <a-input-password
            v-model:value="formState.password2"
            placeholder="确认密码"
        />
      </a-form-item>

      <div class="center-content">
        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-button">
            注册
          </a-button>
        </a-form-item>
      </div>
      <a-form-item style="margin-left: 110px;">
        <a-typography style="display: inline;">已经有账号了，赶紧去</a-typography>
        <a-typography-link style="display:inline;" @click="login()">
          登录
        </a-typography-link>
      </a-form-item>
    </a-form>
  </div>
  <div class="login-container" v-else-if="stat==='success'" :key="2">
    <a-result
        status="success"
        title="注册成功!"
        :sub-title="generator(username)"
    >
      <template #extra>
        <a-button @click="login()">返回至登录页面</a-button>
      </template>
    </a-result>
  </div>
</template>
<script setup>
import {reactive, ref} from 'vue';
import {useRouter} from 'vue-router';
import axios from "axios";

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
      message: '请输入6-20位字母和数字组成的密码',
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
  const response = await axios.post('http://localhost:8080/auth/register',
      {
        nickname: formState.nickname,
        password: formState.password
      });
  if (response.data[0] === "success") {
    username.value = response.data[1];
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
      console.log(msg);
    } catch (err) {
      console.error('无法执行复制命令', err);
    }
    document.body.removeChild(textArea);
    stat.value = 'success';
  } else {
    alert("错误");
    window.location.reload();
  }

};

const generator = (username) => {
  return "请记住您的账号：" + username + "\n账号已经复制到您的剪贴板.";
}

</script>
<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f0f0;
}

.login-form {
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
  padding: 15px;
  background-color: #ffffff;
  width: 500px;
}

.center-content {
  display: flex;
  flex-direction: column; /* 显示为两列 */
  align-items: center;
}

.input-item {
  margin-left: -100px; /* 调整左边距 */
}

.remember-checkbox {
  margin-top: -10px;
  display: flex;
  align-items: center;
  white-space: nowrap;
}

.submit-button {
  margin-top: -10px;
  background-color: #0070f3;
  border: none;
  color: #ffffff;
  border-radius: 5px;
  padding: 0 20px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-button:hover {
  background-color: #0052cc;
}
</style>
