<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider
        v-model:collapsed="collapsed"
        :trigger="null"
        collapsible
        class="custom-sider"
        width="260"
    >
      <div class="logo-container">
        <img
            :src="(collapsed ? require('../assets/logo2.svg') : require('../assets/logo.svg'))"
            class="logo-img"
            alt="movie safari"
            @click="handleLogoClick"
        />
      </div>
      
      <a-menu
          v-model:selectedKeys="$route.meta.selects"
          v-model:openKeys="openKeys"
          theme="dark"
          mode="inline"
          class="custom-menu"
      >
        <a-menu-item key="1" @click="handleMenuClick('1')">
          <template #icon><HomeOutlined /></template>
          <span>首页</span>
        </a-menu-item>
        <a-menu-item key="2" @click="handleMenuClick('2')">
          <template #icon><PlayCircleOutlined /></template>
          <span>电影中心</span>
        </a-menu-item>
        
        <a-sub-menu key="3" title="功能中心">
          <template #icon><AppstoreOutlined /></template>
          <a-menu-item key="3-1" @click="handleMenuClick('3-1')">
            <template #icon><SettingOutlined /></template>
            <span>大模型展示</span>
          </a-menu-item>
          <a-menu-item key="3-2" @click="handleMenuClick('3-2')">
            <template #icon><BarChartOutlined /></template>
            <span>数据可视化</span>
          </a-menu-item>
          <a-menu-item key="3-3" @click="handleMenuClick('3-3')">
            <template #icon><CustomerServiceOutlined /></template>
            <span>音乐播放</span>
          </a-menu-item>
        </a-sub-menu>
        
        <div class="menu-divider"></div>
        
        <a-menu-item key="4" @click="handleMenuClick('4')">
          <template #icon><GithubOutlined /></template>
          <span>关于项目</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    
    <a-layout class="site-layout">
      <a-layout-header class="site-header">
        <div class="header-left">
          <menu-unfold-outlined
              v-if="collapsed"
              class="trigger"
              @click="() => (collapsed = !collapsed)"
          />
          <menu-fold-outlined v-else class="trigger" @click="() => (collapsed = !collapsed)"/>
        </div>

        <div class="header-right">
           <div class="music-player-wrapper">
             <MusicPlayer/>
           </div>
          
          <a-dropdown>
            <div class="user-profile">
              <span class="username">{{ nickname }}</span>
              <a-avatar :src="UserImage" :size="40" class="user-avatar"/>
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile">
                  <UserOutlined /> 个人中心
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleMenuClick('5')">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      
      <a-layout-content class="site-content">
        <div class="content-wrapper">
          <slot/>
        </div>
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
  GithubOutlined,
  AppstoreOutlined,
  UserOutlined,
  LogoutOutlined
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
    HomeOutlined,
    AppstoreOutlined,
    UserOutlined,
    LogoutOutlined
  },
  data() {
    return {
      username: '',
      nickname: '',
      collapsed: false,
      openKeys: ['3'],
    }
  },
  methods: {
    handleLogoClick() {
        router.push({path: '/home'});
    },
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
    if (!this.nickname) {
      this.nickname = "游客";
      this.username = "Guest";
    }
  }
}
</script>

<script setup>
import UserImage from '@/assets/meow.jpg';
import MusicPlayer from "@/components/MusicPlayer";
</script>

<style scoped>
/* Sider Customization */
.custom-sider {
  box-shadow: 2px 0 8px rgba(0,0,0,0.15);
  z-index: 10;
  background: #001529;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background: #002140;
  cursor: pointer;
  overflow: hidden;
}

.logo-img {
  height: 40px;
  transition: all 0.3s;
}

.custom-menu {
  border-right: none;
  font-size: 15px;
  padding-top: 10px;
}

.menu-divider {
  height: 1px;
  background-color: rgba(255,255,255,0.1);
  margin: 10px 20px;
}

/* Header Customization */
.site-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
  height: 64px;
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
}

.trigger {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
  padding: 0 12px;
  border-radius: 4px; 
}

.trigger:hover {
  color: #1890ff;
  background: rgba(0,0,0,0.025);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.music-player-wrapper {
  margin-right: 15px;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.3s;
}

.user-profile:hover {
  background: rgba(0,0,0,0.025);
}

.username {
  font-weight: 500;
  margin-right: 10px;
  color: #333;
}

.user-avatar {
  border: 1px solid #f0f0f0;
}

/* Content Area */
.site-content {
  margin: 0;
  background: #f5f7fa; /* Light grey background for better contrast */
  overflow-y: auto;
}

.content-wrapper {
  min-height: 100%;
}
</style>
