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
      <div class="search-section">
        <div class="search-shell">
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
        </div>
      </div>

      <!-- Results Section -->
      <a-card :bordered="false" class="results-card" v-if="hasSearched">
        <template v-if="song_list.length > 0">
          <div class="results-header">
            <div>
              <h2 class="results-title">搜索结果</h2>
              <p class="results-subtitle">
                “{{ formState.search.trim() }}”
                <span v-if="search_total">共 {{ search_total }} 首</span>
              </p>
            </div>
            <span class="page-badge">第 {{ page_num }} 页</span>
          </div>

          <a-list item-layout="horizontal" :data-source="song_list" :split="false" class="song-list">
            <template #renderItem="{ item, index }">
              <a-list-item class="song-item" :class="{ active: isCurrentSong(item.rid) }">
                <div class="song-main">
                  <div class="song-index">{{ (page_num - 1) * 30 + index + 1 }}</div>
                  <div class="song-cover" :class="{ 'empty-cover': !item.pic }">
                    <img v-if="item.pic" :src="item.pic" :alt="item.name" loading="lazy" decoding="async" @error="handleCoverError" />
                    <span class="cover-note">♪</span>
                  </div>
                  <div class="song-details">
                    <div class="song-name">{{ item.name }}</div>
                    <div class="song-meta">
                      <span><UserOutlined /> {{ item.artist || '未知歌手' }}</span>
                      <span v-if="item.album" class="album-name">{{ item.album }}</span>
                    </div>
                  </div>
                </div>
                <template #actions>
                  <a-button 
                    type="primary" 
                    size="middle"
                    class="play-btn"
                    :class="{ playing: isCurrentPlaying(item.rid) }"
                    @click="playSong(item.rid)"
                  >
                    <template #icon>
                      <PauseCircleOutlined v-if="isCurrentPlaying(item.rid)" />
                      <PlayCircleOutlined v-else />
                    </template>
                    {{ playButtonText(item.rid) }}
                  </a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
          
          <div class="music-pager">
            <a-button class="pager-btn" @click="changePage(-1)" :disabled="page_num <= 1">上一页</a-button>
            <span class="pager-current">第 {{ page_num }} 页</span>
            <a-button class="pager-btn" @click="changePage(1)" :disabled="song_list.length < 30">下一页</a-button>
          </div>
        </template>
        
        <div v-else class="empty-music-state">
          <a-empty description="暂无搜索结果" />
        </div>
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
  PauseCircleOutlined,
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

const handleCoverError = (event) => {
  const cover = event.target.parentElement;
  event.target.remove();
  if (cover) {
    cover.classList.add('empty-cover');
  }
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

const isCurrentSong = (rid) => counter.music_rid === rid;

const isCurrentPlaying = (rid) => isCurrentSong(rid) && counter.music_playing;

const playButtonText = (rid) => {
  if (isCurrentPlaying(rid)) return '播放中';
  return isCurrentSong(rid) ? '继续' : '播放';
};

const playSong = async (rid) => {
  if (isCurrentSong(rid) && counter.music_url) {
    counter.togglePlayback();
    return;
  }

  // Update current ID immediately
  currentRid.value = rid;
  
  try {
    // 1. Fetch Music URL first (Priority)
    const urlRes = await axios.get('http://localhost:5000/mp3?rid=' + rid);
    
    // Check if user switched songs while waiting
    if (currentRid.value !== rid) return;

    // Start playing immediately with empty lyrics
    // This ensures music starts ASAP without waiting for lyrics
    counter.change(urlRes.data, [], rid);
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
      counter.setLyrics(lrcRes.data);
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
  padding: 24px 0 40px;
}

.music-content {
  max-width: 1040px;
  margin: 0 auto;
}

.header-section {
  text-align: center;
  margin-bottom: 34px;
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
  display: flex;
  justify-content: center;
  margin: 0 auto;
  margin-bottom: 32px;
}

.search-shell {
  width: calc(100% - 56px);
  margin: 0 auto;
}

:deep(.custom-search) {
  display: block;
  width: 100%;
}

:deep(.custom-search .ant-input-wrapper) {
  display: flex;
  width: 100%;
}

:deep(.custom-search .ant-input-group) {
  display: flex;
  align-items: stretch;
  width: 100%;
}

:deep(.custom-search .ant-input-affix-wrapper) {
  height: 56px;
  display: flex;
  align-items: center;
  flex: 1 1 auto;
  width: auto !important;
  min-width: 0;
  border-color: var(--movie-line);
  border-radius: var(--movie-radius) 0 0 var(--movie-radius) !important;
  padding: 0 18px !important;
  box-shadow: none;
  background: #fff;
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
  flex: 0 0 auto;
  width: auto !important;
  background: transparent;
}

:deep(.custom-search .ant-input-search-button) {
  height: 56px;
  min-width: 104px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  border-radius: 0 var(--movie-radius) var(--movie-radius) 0 !important;
  padding: 0 24px;
  font-size: 16px;
  font-weight: 650;
  color: var(--movie-accent);
  background: rgba(196, 59, 69, 0.06);
  border-color: var(--movie-line);
  border-left-color: rgba(196, 59, 69, 0.18);
  box-shadow: none;
}

:deep(.custom-search .ant-input-search-button:hover) {
  color: var(--movie-accent-dark);
  background: rgba(196, 59, 69, 0.1);
  border-color: rgba(196, 59, 69, 0.28);
  box-shadow: none;
  transform: none;
}

:deep(.custom-search .ant-input-search-button:focus) {
  color: var(--movie-accent-dark);
  background: rgba(196, 59, 69, 0.1);
  border-color: rgba(196, 59, 69, 0.34);
  box-shadow: inset 0 0 0 1px rgba(196, 59, 69, 0.05);
}

.results-card {
  background: var(--movie-surface);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  box-shadow: var(--movie-shadow-sm);
  overflow: hidden;
}

.results-card :deep(.ant-card-body) {
  padding: 24px 28px 22px;
}

.results-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.results-title {
  margin: 0 0 5px;
  color: var(--movie-ink);
  font-size: 21px;
  font-weight: 750;
  letter-spacing: 0;
}

.results-subtitle {
  margin: 0;
  color: var(--movie-muted);
  font-size: 13px;
}

.results-subtitle span {
  margin-left: 8px;
}

.page-badge {
  flex: 0 0 auto;
  padding: 4px 10px;
  color: var(--movie-muted);
  background: var(--movie-surface-soft);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  font-size: 12px;
  font-weight: 650;
}

.song-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.song-item {
  padding: 12px 14px !important;
  background: linear-gradient(180deg, #fff, var(--movie-surface-soft));
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  transition: background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.song-item.active {
  border-color: rgba(21, 127, 131, 0.32);
  background: rgba(21, 127, 131, 0.06);
}

.song-item:hover {
  background: #fff;
  border-color: rgba(196, 59, 69, 0.22);
  box-shadow: 0 8px 18px rgba(18, 24, 33, 0.07);
  transform: translateY(-1px);
}

.song-main {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.song-index {
  flex: 0 0 34px;
  width: 34px;
  color: var(--movie-muted);
  font-size: 14px;
  font-weight: 700;
  text-align: center;
}

.song-cover {
  position: relative;
  flex: 0 0 52px;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: rgba(196, 59, 69, 0.62);
  background: linear-gradient(135deg, rgba(196, 59, 69, 0.1), rgba(21, 127, 131, 0.1));
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
}

.song-cover img {
  position: absolute;
  inset: 0;
  z-index: 1;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-note {
  font-size: 20px;
  font-weight: 750;
}

.song-details {
  min-width: 0;
  display: flex;
  flex-direction: column;
  text-align: left;
  align-items: flex-start;
  gap: 6px;
}

.song-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--movie-ink);
  line-height: 1.35;
  word-break: break-word;
}

.song-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--movie-muted);
}

.song-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.album-name {
  max-width: 360px;
  padding-left: 10px;
  border-left: 1px solid var(--movie-line);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.play-btn {
  background: var(--movie-teal);
  border-color: var(--movie-teal);
  border-radius: var(--movie-radius);
  box-shadow: 0 6px 14px rgba(21, 127, 131, 0.14);
}

.play-btn:hover {
  background: #0f6f73 !important;
  border-color: #0f6f73 !important;
  transform: translateY(-1px);
}

.play-btn.playing {
  background: #0f6f73;
  border-color: #0f6f73;
}

.play-btn.playing:hover {
  background: #0b5c60 !important;
  border-color: #0b5c60 !important;
}

.music-pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 22px;
}

.pager-btn {
  min-width: 82px;
}

.pager-current {
  color: var(--movie-muted);
  font-size: 13px;
}

.empty-music-state {
  padding: 46px 0 36px;
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

  .results-card :deep(.ant-card-body) {
    padding: 18px 14px;
  }

  .results-header {
    flex-direction: column;
    gap: 10px;
  }

  .song-index {
    display: none;
  }

  .song-cover {
    flex-basis: 46px;
    width: 46px;
    height: 46px;
  }

  :deep(.custom-search .ant-input-search-button) {
    min-width: 88px;
    padding: 0 18px;
  }

  .search-shell {
    width: 100%;
  }

  .album-name {
    max-width: 180px;
  }
}
</style>
