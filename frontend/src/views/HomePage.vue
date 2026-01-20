<template>
  <a-layout style="min-height: 100vh">
    <a-layout-header class="header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo-container" @click="handleLogoClick">
          <img src="@/assets/logo2.svg" class="logo-img" alt="movie safari" />
          <span class="app-title">Movie Safari</span>
        </div>

        <!-- Top Navigation Menu -->
        <a-menu
          v-model:selectedKeys="$route.meta.selects"
          theme="dark"
          mode="horizontal"
          class="top-menu"
        >
          <a-menu-item key="1" @click="handleMenuClick('1')">
            <template #icon><HomeOutlined /></template>
            <span>首页</span>
          </a-menu-item>
          <a-menu-item key="2" @click="handleMenuClick('2')">
            <template #icon><PlayCircleOutlined /></template>
            <span>电影中心</span>
          </a-menu-item>
          
          <a-sub-menu key="3">
            <template #icon><AppstoreOutlined /></template>
            <template #title>功能中心</template>
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

          <a-menu-item key="4" @click="handleMenuClick('4')">
            <template #icon><GithubOutlined /></template>
            <span>关于项目</span>
          </a-menu-item>
        </a-menu>

        <!-- Right Side: Music & Profile -->
        <div class="header-right">
           <div class="music-player-wrapper">
             <MusicPlayer/>
           </div>
          
          <a-dropdown placement="bottomRight" :trigger="['click']">
            <div class="user-profile">
              <span class="username">{{ nickname }}</span>
              <a-avatar :src="UserImage" :size="36" class="user-avatar"/>
            </div>
            <template #overlay>
              <a-menu class="user-dropdown-menu">
                <div class="user-info-header">
                   <div class="user-name-display">{{ nickname }}</div>
                   <div class="user-role-display">{{ username }}</div>
                </div>
                <a-menu-divider />
                <a-menu-item key="profile">
                  <UserOutlined /> 个人中心
                </a-menu-item>
                <a-menu-item key="logout" @click="handleMenuClick('5')">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </div>
    </a-layout-header>
    
    <a-layout-content class="site-content">
      <div class="content-wrapper">
        <slot/>
      </div>
    </a-layout-content>
  </a-layout>
</template>

<script>
import {
  PlayCircleOutlined,
  SettingOutlined,
  BarChartOutlined,
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
    HomeOutlined,
    AppstoreOutlined,
    UserOutlined,
    LogoutOutlined
  },
  data() {
    return {
      username: '',
      nickname: '',
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
/* Layout Background */
.site-content {
  background: #f5f7fa;
  margin-top: 64px; /* Height of header */
  min-height: calc(100vh - 64px);
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

/* Header Styles */
.header {
  position: fixed;
  z-index: 100;
  width: 100%;
  padding: 0;
  background: #001529; /* Dark theme for header to match movie vibe */
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  height: 64px;
}

.header-content {
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

/* Logo */
.logo-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 40px;
  flex-shrink: 0;
}

.logo-img {
  height: 36px;
  margin-right: 12px;
}

.app-title {
  color: #fff;
  font-size: 1.2rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  font-family: 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* Top Menu Override */
.top-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
  line-height: 64px;
}

:deep(.ant-menu-dark.ant-menu-horizontal) {
  border-bottom: 0;
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item), 
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu) {
  padding: 0 20px;
  font-size: 15px;
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item:hover),
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item-selected) {
  background-color: #1890ff;
}

/* Header Right */
.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-shrink: 0;
}

.music-player-wrapper {
  display: flex;
  align-items: center;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 20px;
  transition: background 0.3s;
  background: rgba(255, 255, 255, 0.05);
}

.user-profile:hover {
  background: rgba(255, 255, 255, 0.15);
}

.username {
  font-weight: 500;
  margin-right: 12px;
  color: #fff;
  font-size: 14px;
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.2);
  transition: border-color 0.3s;
}

.user-profile:hover .user-avatar {
  border-color: #fff;
}

/* User Dropdown */
.user-dropdown-menu {
  padding: 8px;
  min-width: 160px;
}

.user-info-header {
  padding: 8px 12px;
  border-radius: 4px;
  background-color: #f9f9f9;
  margin-bottom: 4px;
  text-align: center;
}

.user-name-display {
  font-weight: 600;
  color: #333;
}

.user-role-display {
  font-size: 12px;
  color: #888;
}

@media (max-width: 992px) {
  .header-content {
    padding: 0 16px;
  }
  
  .app-title {
    display: none; /* Hide text on smaller screens if needed */
  }
  
  :deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item), 
  :deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu) {
    padding: 0 12px;
  }
}
</style>
