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
        <a-col :xs="24" class="search-col">
          <a-input-search
            v-model:value="formState.search"
            placeholder="搜索歌曲、歌手..."
            enter-button="搜索"
            size="large"
            :loading="searching"
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
const searching = ref(false);
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

const changePage = async (delta) => {
  page_num.value += delta;
  if (page_num.value < 1) page_num.value = 1;
  await fetchSongs();
}

// Separate fetch logic for reuse
const fetchSongs = async () => {
  searching.value = true;
  try {
    const response = await axios.get(
        'http://localhost:5000/search?key=' + encodeURIComponent(formState.search) + '&pn=' + page_num.value
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
  } finally {
    searching.value = false;
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
    return;
  }

  try {
    const lrcRes = await axios.get('http://localhost:5000/lrc?rid=' + rid);
    
    // Double check race condition after await
    if (currentRid.value !== rid) return;

    if (lrcRes.data && lrcRes.data.length > 0) {
      // Success! Update only the lyrics in the store
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
  padding: 28px 0 40px;
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
  color: var(--movie-ink);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  color: var(--movie-accent);
}

.page-subtitle {
  color: var(--movie-muted);
  font-size: 16px;
}

.search-section {
  width: 100%;
  margin: 0 auto 40px;
}

.search-col {
  flex: 0 1 940px !important;
  max-width: min(940px, 100%) !important;
  margin: 0 auto;
}

:deep(.custom-search .ant-input-group) {
  display: flex;
  align-items: stretch;
}

:deep(.custom-search .ant-input-affix-wrapper) {
  height: 56px;
  display: flex;
  align-items: center;
  flex: 1;
  border-color: var(--movie-line);
  border-radius: var(--movie-radius) 0 0 var(--movie-radius) !important;
  padding: 0 18px !important;
  box-shadow: none;
}

:deep(.custom-search .ant-input-affix-wrapper:hover) {
  border-color: rgba(196, 59, 69, 0.45);
}

:deep(.custom-search .ant-input-affix-wrapper-focused) {
  border-color: var(--movie-accent);
  box-shadow: 0 0 0 3px rgba(196, 59, 69, 0.12);
}

:deep(.custom-search .ant-input-affix-wrapper .ant-input) {
  height: 54px;
  padding: 0 0 0 12px !important;
  background: transparent;
  border: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

:deep(.custom-search .ant-input-group-addon) {
  display: flex;
  background: transparent;
}

:deep(.custom-search .ant-input-search-button) {
  height: 56px;
  min-width: 128px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  border-radius: 0 var(--movie-radius) var(--movie-radius) 0 !important;
  padding: 0 36px;
  font-size: 16px;
  background: var(--movie-accent);
  border-color: var(--movie-accent);
  box-shadow: 0 12px 22px rgba(196, 59, 69, 0.18);
}

:deep(.custom-search .ant-input-search-button:hover) {
  background: var(--movie-accent-dark);
  border-color: var(--movie-accent-dark);
  box-shadow: 0 14px 26px rgba(196, 59, 69, 0.22);
}

.results-card {
  background: var(--movie-surface);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  box-shadow: var(--movie-shadow-sm);
  padding: 12px;
}

.song-item {
  padding: 16px 24px;
  transition: background 0.2s ease, transform 0.2s ease;
  border-radius: var(--movie-radius);
  margin-bottom: 8px;
  border-bottom: 1px solid var(--movie-line);
}

.song-item:last-child {
  border-bottom: none;
}

.song-item:hover {
  background-color: rgba(21, 127, 131, 0.08);
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
  color: var(--movie-muted);
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
  color: var(--movie-ink);
  margin-bottom: 4px;
}

.song-artist {
  font-size: 13px;
  color: var(--movie-muted);
}

.play-btn {
  background: var(--movie-teal);
  border-color: var(--movie-teal);
  border-radius: var(--movie-radius);
  box-shadow: 0 8px 16px rgba(21, 127, 131, 0.18);
}

.play-btn:hover {
  background: #0f6f73 !important;
  border-color: #0f6f73 !important;
  transform: translateY(-1px);
}

.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  padding-bottom: 24px;
}

.initial-state {
  text-align: center;
  padding: 80px 0;
  color: var(--movie-muted);
}

.bg-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: rgba(196, 59, 69, 0.18);
}

@media (max-width: 640px) {
  .music-container {
    padding-top: 20px;
  }

  .page-title {
    font-size: 26px;
  }

  .song-item {
    padding: 14px 12px;
  }
}
</style>
