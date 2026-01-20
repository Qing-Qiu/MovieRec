<template>
  <div class="login-container">
    <a-form
        :model="formState"
        name="basic"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 16 }"
        autocomplete="off"
        class="login-form"
        @submit="submitForm"
    >
      <h1 class="login-title">用户登录</h1>
      <a-form-item
          label="用户名"
          name="username"
          :rules="[{ required: true, message: '请输入用户名'}]"
          class="input-item"
          style="text-align: left;"
      >
        <a-input
            v-model:value="formState.username"
            placeholder="用户名"
        />
      </a-form-item>

      <a-form-item
          label="密码"
          name="password"
          :rules="[{ required: true, message: '请输入密码'}]"
          class="input-item"
          style="text-align: left;"
      >
        <a-input-password
            v-model:value="formState.password"
            placeholder="密码"
        />
      </a-form-item>

      <div class="center-content">
        <!--        <a-form-item name="remember">-->
        <!--          <a-checkbox v-model:checked="formState.remember" class="remember-checkbox">记住密码</a-checkbox>-->
        <!--        </a-form-item>-->

        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-button">
            登录
          </a-button>
        </a-form-item>
      </div>
      <a-form-item style="margin-left: 110px;">
        <a-typography style="display: inline;">还没有账号，赶紧去</a-typography>
        <a-typography-link style="display: inline;" @click="register()">
          注册
        </a-typography-link>
        <a-typography style="display: inline;">或</a-typography>&nbsp;
        <a-typography-link style="display: inline;" @click="tourist()">游客登陆</a-typography-link>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup>
import {reactive, ref} from 'vue';
import {useRouter} from 'vue-router';
import axios from "axios";

const router = useRouter();

const formState = reactive({
  username: '',
  password: '',
  // remember: true,
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
      await router.push('/home');
    } else {

    }
  } catch (error) {
    console.error(error);
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
