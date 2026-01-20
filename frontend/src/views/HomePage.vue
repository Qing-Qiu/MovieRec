<template>
  <a-layout style="height: auto">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible style="height: auto; position: fixed">
      <img :src="(collapsed ? require('../assets/logo2.svg') : require('../assets/logo.svg'))"
           style="cursor: pointer; stroke-opacity: 0" alt="movie safari"/>
      <a-menu v-model:selectedKeys="$route.meta.selects"
              :openKeys="openKeys" theme="dark" mode="inline"
              style="height: 100vh;">
        <a-menu-item key="1" @click="handleMenuClick('1')">
          <home-outlined/>
          <span>首页</span>
        </a-menu-item>
        <a-menu-item key="2" @click="handleMenuClick('2')">
          <PlayCircleOutlined/>
          <span>电影中心</span>
        </a-menu-item>
        <a-sub-menu key="3" title="其他功能">
          <a-menu-item key="3-1" @click="handleMenuClick('3-1')">
            <SettingOutlined/>
            <span>大模型嵌套</span>
          </a-menu-item>
          <a-menu-item key="3-2" @click="handleMenuClick('3-2')">
            <BarChartOutlined/>
            <span>可视化展示</span>
          </a-menu-item>
          <a-menu-item key="3-3" @click="handleMenuClick('3-3')">
            <CustomerServiceOutlined/>
            <span>音乐播放</span>
          </a-menu-item>
        </a-sub-menu>
        <a-menu-item key="4" @click="handleMenuClick('4')">
          <GithubOutlined/>
          <span>关于作者</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout :style="{ marginLeft:(collapsed?'80px':'200px')}">
      <a-layout-header style="background: #fff; padding: 0;display: flex;align-items: center">
        <menu-unfold-outlined
            v-if="collapsed"
            class="trigger"
            @click="() => (collapsed = !collapsed)"
        />
        <menu-fold-outlined v-else class="trigger" @click="() => (collapsed = !collapsed)"/>
        <MusicPlayer/>
        <a-typography-text
            style="margin-left: auto;
                  margin-right: 10px;
                  margin-bottom: -15px;">{{ nickname }}
        </a-typography-text>
        <a-dropdown class="avatar-container">
          <a-avatar :src="UserImage" :size="50" style="cursor: pointer;"/>
          <template #overlay>
            <a-menu>
              <a-menu-item :key="5" @click="handleMenuClick('5')">
                登出
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>
      <a-layout-content
          :style="{ margin:'0', padding: '0', background: '#fff', minHeight: '100vh' }"
      >
        <slot/>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
<script>
import {
  PlayCircleOutlined,
  SettingOutlined,
  BarChartOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  HomeOutlined,
  CustomerServiceOutlined,
  GithubOutlined
} from "@ant-design/icons-vue";
import router from "@/router/router";

export default {
  components: {
    PlayCircleOutlined,
    SettingOutlined,
    BarChartOutlined,
    CustomerServiceOutlined,
    GithubOutlined,
    MenuUnfoldOutlined,
    MenuFoldOutlined,
    HomeOutlined
  },
  data() {
    return {
      username: '',
      nickname: '',
      collapsed: false,
      openKeys: ['3'],
      // selectedKeys: ['1'],
    }
  },
  methods: {
    handleMenuClick(key) {
      switch (key) {
        case '1':
          router.push({path: '/home'});
          break;
        case '2':
          router.push({path: '/center'});
          break;
        case '3-1':
          router.push({path: '/model'});
          break;
        case '3-2':
          router.push({path: '/chart'});
          break;
        case '3-3':
          router.push({path: '/music'});
          break;
        case '4':
          window.open('https://github.com/Qing-Qiu/MovieSafari', '_blank');
          break;
        case '5':
          sessionStorage.clear();
          this.username = '';
          this.nickname = '';
          router.push('/auth/login');
          break;
      }
    }
  },
  mounted() {
    this.username = sessionStorage.getItem('username');
    this.nickname = sessionStorage.getItem('nickname');
    if (this.nickname === null || this.username === null) {
      this.nickname = "游客";
      this.username = "游客";
    }
    //获取当前用户的id和昵称
    console.log(this.username);
    console.log(this.nickname);
  }
}
</script>

<script setup>
import UserImage from '@/assets/meow.jpg';
import MusicPlayer from "@/components/MusicPlayer";
</script>

<style scoped>
.trigger {
  font-size: 18px;
  line-height: 64px;
  padding: 0 24px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger:hover {
  color: #1890ff;
}

.logo {
  /*height: 32px;*/
  /*background: rgba(255, 255, 255, 0.3);*/
  /*margin: 16px;*/

  font-family: cursive; /* 使用一个手写艺术字体 */
  font-size: 20px;
  color: #1890ff; /* 设置字体颜色 */
  text-align: center; /* 文本居中 */
  padding: 16px; /* 内边距 */
}

.avatar-container {
  margin-bottom: -15px;
  margin-right: 20px;
  /*margin-left: auto;*/
}
</style>