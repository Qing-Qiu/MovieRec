<template>
  <div class="music-container">
    <div class="music-content">
      <!-- Header Section -->
      <div class="header-section">
        <h1 class="page-title">
          <SoundOutlined class="title-icon" /> 音乐中心
        </h1>
        <p class="page-subtitle">探索千万首好歌，随时随地聆听</p>
      </div>

      <!-- Search Section -->
      <a-row justify="center" class="search-section">
        <a-col :xs="24" :sm="20" :md="16" :lg="12">
          <a-input-search
            v-model:value="formState.search"
            placeholder="搜索歌曲、歌手..."
            enter-button="搜索"
            size="large"
            @search="submitForm"
            class="custom-search"
          >
            <template #prefix>
              <SearchOutlined class="search-icon" />
            </template>
          </a-input-search>
        </a-col>
      </a-row>

      <!-- Results Section -->
      <a-card :bordered="false" class="results-card" v-if="hasSearched">
        <template v-if="song_list.length > 0">
          <a-list item-layout="horizontal" :data-source="song_list" :split="false">
            <template #renderItem="{ item, index }">
              <a-list-item class="song-item">
                <div class="song-info">
                  <div class="song-index">{{ (page_num - 1) * 30 + index + 1 }}</div>
                  <div class="song-details">
                    <div class="song-name">{{ item.name }}</div>
                    <div class="song-artist"><UserOutlined /> {{ item.artist }}</div>
                  </div>
                </div>
                <template #actions>
                  <a-button 
                    type="primary" 
                    shape="round" 
                    size="middle"
                    class="play-btn"
                    @click="playSong(item.rid)"
                  >
                    <template #icon><PlayCircleOutlined /></template>
                    播放
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
          
          <div class="pagination-wrapper">
             <a-space>
                <a-button @click="changePage(-1)" :disabled="page_num <= 1">上一页</a-button>
                <span style="margin: 0 10px">第 {{ page_num }} 页</span>
                <a-button @click="changePage(1)" :disabled="song_list.length < 30">下一页</a-button>
             </a-space>
          </div>
        </template>
        
        <a-empty v-else description="暂无搜索结果" />
      </a-card>
      
      <!-- Initial State -->
      <div v-else class="initial-state">
        <CustomerServiceOutlined class="bg-icon" />
        <p>输入关键词开始搜索音乐</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from "axios";
import { useCounterStore } from "@/store/store";
import { reactive, ref } from "vue";
import { 
  SearchOutlined, 
  PlayCircleOutlined, 
  SoundOutlined, 
  UserOutlined,
  CustomerServiceOutlined
} from "@ant-design/icons-vue";
import { message } from "ant-design-vue";

const formState = reactive({
  search: '',
});
let song_list = ref([]);
let page_num = ref(1);
let search_total = ref(0);
let hasSearched = ref(false); // Track if a search has been performed
const counter = useCounterStore();

const submitForm = async () => {
  if (!formState.search.trim()) {
    message.warning('请输入搜索内容');
    return;
  }
  
  page_num.value = 1;
  hasSearched.value = true;
  await fetchSongs();
}

const handlePageChange = async (page) => {
  page_num.value = page;
  await fetchSongs();
}

const changePage = async (delta) => {
  page_num.value += delta;
  if (page_num.value < 1) page_num.value = 1;
  await fetchSongs();
}

// Separate fetch logic for reuse
const fetchSongs = async () => {
  try {
    const response = await axios.get(
        'http://localhost:5000/search?key=' + formState.search + '&pn=' + page_num.value
    );
    
    if (response.data && response.data.list) {
        song_list.value = response.data.list;
        search_total.value = parseInt(response.data.total) || 0;
    } else {
        // Fallback for backward compatibility
        song_list.value = response.data;
        search_total.value = 600; 
    }
  } catch (error) {
    console.error("Search failed:", error);
    message.error("搜索失败，请稍后重试");
    song_list.value = [];
    search_total.value = 0;
  }
}

// Track current playing song to prevent race conditions
const currentRid = ref(null);

const playSong = async (rid) => {
  // Update current ID immediately
  currentRid.value = rid;
  
  try {
    // 1. Fetch Music URL first (Priority)
    const urlRes = await axios.get('http://localhost:5000/mp3?rid=' + rid);
    
    // Check if user switched songs while waiting
    if (currentRid.value !== rid) return;

    // Start playing immediately with empty lyrics
    // This ensures music starts ASAP without waiting for lyrics
    counter.change(urlRes.data, []); 
    message.success('开始播放');

    // 2. Fetch Lyrics in background with retry logic
    fetchLyricsWithRetry(rid);
    
  } catch (error) {
    if (currentRid.value === rid) {
      console.error("Play failed:", error);
      message.error("无法播放该歌曲");
    }
  }
}

// Helper to fetch lyrics with retries
// Strategy: Fast retries initially (for 3s), then slower poling
const fetchLyricsWithRetry = async (rid, attempts = 0) => {
  if (currentRid.value !== rid) return;
  
  // Max attempts configuration
  const MAX_ATTEMPTS = 10;
  if (attempts >= MAX_ATTEMPTS) {
    console.log(`Stopped retrying lyrics for ${rid} after ${MAX_ATTEMPTS} attempts`);
    return;
  }

  try {
    const lrcRes = await axios.get('http://localhost:5000/lrc?rid=' + rid);
    
    // Double check race condition after await
    if (currentRid.value !== rid) return;

    if (lrcRes.data && lrcRes.data.length > 0) {
      // Success! Update only the lyrics in the store
      console.log(`Lyrics found for ${rid} on attempt ${attempts + 1}`);
      counter.music_lrc = lrcRes.data;
    } else {
      // Decide base delay based on attempt count
      // First 3 retries: fast (500ms)
      // Afterwards: slow (2000ms)
      const baseDelay = attempts < 3 ? 500 : 2000;
      
      // Add random jitter (perturbation) to avoid synchronized requests
      // Jitter range: ±20% of the base delay
      const jitter = (Math.random() * 0.4 - 0.2) * baseDelay;
      const finalDelay = Math.max(100, Math.floor(baseDelay + jitter));
      
      console.log(`Retry lyrics for ${rid} (Attempt ${attempts + 1}/${MAX_ATTEMPTS}), next in ${finalDelay}ms (jittered)`);
      setTimeout(() => fetchLyricsWithRetry(rid, attempts + 1), finalDelay);
    }
  } catch (error) {
    console.warn("Lyrics fetch error:", error);
    if (currentRid.value === rid) {
       const baseDelay = attempts < 3 ? 500 : 2000;
       const jitter = (Math.random() * 0.4 - 0.2) * baseDelay;
       const finalDelay = Math.max(100, Math.floor(baseDelay + jitter));
       setTimeout(() => fetchLyricsWithRetry(rid, attempts + 1), finalDelay);
    }
  }
}
</script>

<style scoped>
.music-container {
  min-height: calc(100vh - 64px);
  padding: 40px 24px;
}

.music-content {
  max-width: 1000px;
  margin: 0 auto;
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f1f1f;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  color: #1890ff;
}

.page-subtitle {
  color: #8c8c8c;
  font-size: 16px;
}

.search-section {
  margin-bottom: 40px;
}

/* Custom Search Input Styles */
:deep(.custom-search .ant-input) {
  border-radius: 24px 0 0 24px !important;
  padding-left: 20px;
}

:deep(.custom-search .ant-btn) {
  border-radius: 0 24px 24px 0 !important;
  padding: 0 40px;
  font-size: 16px;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  border: none;
  box-shadow: 0 4px 10px rgba(24, 144, 255, 0.3);
}

:deep(.custom-search .ant-btn:hover) {
  background: linear-gradient(135deg, #40a9ff 0%, #5cdbd3 100%);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}

.results-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
  padding: 12px;
}

.song-item {
  padding: 16px 24px;
  transition: all 0.3s ease;
  border-radius: 12px;
  margin-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.song-item:last-child {
  border-bottom: none;
}

.song-item:hover {
  background-color: #f6ffed;
  transform: translateX(5px);
}

.song-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.song-index {
  font-size: 18px;
  font-weight: 600;
  color: #bfbfbf;
  width: 30px;
  text-align: center;
}

.song-details {
  display: flex;
  flex-direction: column;
  text-align: left;
  align-items: flex-start;
}

.song-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.song-artist {
  font-size: 13px;
  color: #888;
}

.play-btn {
  background: #52c41a;
  border-color: #52c41a;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

.play-btn:hover {
  background: #73d13d;
  border-color: #73d13d;
  transform: scale(1.05);
}

.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  padding-bottom: 24px;
}

.initial-state {
  text-align: center;
  padding: 80px 0;
  color: #ccc;
}

.bg-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #e6f7ff;
}
</style>