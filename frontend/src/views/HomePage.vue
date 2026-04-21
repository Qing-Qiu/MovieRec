<template>
  <a-layout style="min-height: 100vh">
    <a-layout-header class="header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo-container" @click="handleLogoClick">
          <img src="@/assets/logo2.svg" class="logo-img" alt="movierec" />
          <span class="app-title">MovieRec</span>
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

          <a-menu-item key="3" @click="handleMenuClick('3')">
            <template #icon><RobotOutlined /></template>
            <span>电影助手</span>
          </a-menu-item>

          <a-menu-item key="4" @click="handleMenuClick('4')">
            <template #icon><CustomerServiceOutlined /></template>
            <span>音乐中心</span>
          </a-menu-item>

          <a-menu-item key="5" @click="handleMenuClick('5')">
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
                <a-menu-item key="logout" @click="handleMenuClick('6')">
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
  HomeOutlined,
  CustomerServiceOutlined,
  GithubOutlined,
  RobotOutlined,
  UserOutlined,
  LogoutOutlined
} from "@ant-design/icons-vue";
import router from "@/router/router";

export default {
  components: {
    PlayCircleOutlined,
    CustomerServiceOutlined,
    GithubOutlined,
    HomeOutlined,
    RobotOutlined,
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
        case '3':
          router.push({path: '/model'});
          break;
        case '4':
          router.push({path: '/music'});
          break;
        case '5':
          window.open('https://github.com/Qing-Qiu/MovieRec', '_blank');
          break;
        case '6':
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
.site-content {
  background: var(--movie-bg);
  margin-top: 64px;
  min-height: calc(100vh - 64px);
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  text-align: left;
}

.header {
  position: fixed;
  z-index: 100;
  width: 100%;
  padding: 0;
  background: var(--movie-header);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 22px rgba(10, 14, 20, 0.18);
  height: 64px;
}

.header-content {
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 0 24px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  margin-right: 12px;
  flex-shrink: 0;
}

.logo-img {
  height: 36px;
  width: 36px;
}

.app-title {
  color: #fff;
  font-size: 1.2rem;
  font-weight: 600;
  letter-spacing: 0;
}

.top-menu {
  flex: 1;
  min-width: 0;
  border-bottom: none;
  background: transparent;
  line-height: 64px;
}

:deep(.ant-menu-dark.ant-menu-horizontal) {
  border-bottom: 0;
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item), 
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu) {
  padding: 0 18px;
  font-size: 15px;
  transition: background 0.2s ease, color 0.2s ease;
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item:hover),
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu:hover) {
  background-color: rgba(255, 255, 255, 0.09);
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item-selected) {
  background-color: var(--movie-accent);
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu-selected) {
  background-color: rgba(196, 59, 69, 0.18);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
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
  padding: 4px 8px 4px 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--movie-radius);
  background: rgba(255, 255, 255, 0.06);
  transition: background 0.2s ease, border-color 0.2s ease;
}

.user-profile:hover {
  background: rgba(255, 255, 255, 0.13);
  border-color: rgba(255, 255, 255, 0.2);
}

.username {
  font-weight: 500;
  margin-right: 12px;
  color: #fff;
  font-size: 14px;
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.2);
  transition: border-color 0.2s ease;
}

.user-profile:hover .user-avatar {
  border-color: #fff;
}

.user-dropdown-menu {
  padding: 8px;
  min-width: 160px;
  border-radius: var(--movie-radius);
}

.user-info-header {
  padding: 8px 12px;
  border-radius: var(--movie-radius);
  background-color: var(--movie-surface-soft);
  margin-bottom: 4px;
  text-align: center;
}

.user-name-display {
  font-weight: 600;
  color: var(--movie-ink);
}

.user-role-display {
  font-size: 12px;
  color: var(--movie-muted);
}

@media (max-width: 992px) {
  .header-content {
    padding: 0 16px;
  }
  
  .app-title {
    display: none;
  }
  
  :deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item), 
  :deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu) {
    padding: 0 12px;
  }
}

@media (max-width: 760px) {
  .content-wrapper {
    padding: 16px;
  }

  .music-player-wrapper,
  .username {
    display: none;
  }

  .header-content {
    gap: 12px;
  }
}
</style>
