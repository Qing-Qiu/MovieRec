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
                  <div class="song-index">{{ (page_num - 1) * 10 + index + 1 }}</div>
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
            <a-pagination 
              v-model:current="page_num" 
              :total="500" 
              :show-size-changer="false" 
              @change="handlePageChange"
              show-quick-jumper
            />
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

// Separate fetch logic for reuse
const fetchSongs = async () => {
  try {
    const response = await axios.get(
        'http://localhost:5000/search?key=' + formState.search + '&pn=' + page_num.value
    );
    song_list.value = response.data;
  } catch (error) {
    console.error("Search failed:", error);
    message.error("搜索失败，请稍后重试");
    song_list.value = [];
  }
}

const playSong = async (rid) => {
  try {
    // Parallel requests for URL and LRC
    const [urlRes, lrcRes] = await Promise.all([
      axios.get('http://localhost:5000/mp3?rid=' + rid),
      axios.get('http://localhost:5000/lrc?rid=' + rid)
    ]);

    counter.change(urlRes.data, lrcRes.data);
    message.success('开始播放');
  } catch (error) {
    console.error("Play failed:", error);
    message.error("无法播放该歌曲");
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
  padding-bottom: 16px;
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