<template>
  <div class="recommend-container">
    <a-row justify="center" class="search-section">
      <a-col :xs="22" :sm="20" :md="16" :lg="12" :xl="10">
        <a-input-search
            v-model:value="formState.search"
            placeholder="搜索电影、影人、导演..."
            size="large"
            enter-button
            @search="submitForm"
            class="custom-search"
        />
      </a-col>
    </a-row>

    <div class="content-section">
      <!-- Default Recommendation View -->
      <template v-if="!search">
        <div class="section-header">
          <h2 class="section-title">猜你喜欢</h2>
          <a-button type="text" @click="refresh()" :loading="loading" class="refresh-btn">
            <template #icon><ReloadOutlined /></template>
            换一批
          </a-button>
        </div>
        
        <a-row :gutter="[24, 24]">
          <!-- Loading State -->
          <template v-if="loading">
            <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="i in 8" :key="i">
              <SkeletonCard />
            </a-col>
          </template>

          <!-- Data State -->
          <template v-else>
            <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(item, itemIndex) in recommendedImages" :key="itemIndex">
              <a-card
                  hoverable
                  class="movie-card"
                  @click="watchMovieDetail(item.id)"
              >
                <template #cover>
                  <div class="image-wrapper">
                    <span
                      v-if="item.source"
                      class="recommend-source-badge"
                      :class="sourceClass(item.source)"
                    >
                      {{ item.source }}
                    </span>
                    <imgWrapper 
                      :src="item.image"
                      :alt="item.title"
                    />
                  </div>
                </template>
                <a-card-meta :title="item.title">
                  <template #description>
                    <span class="movie-genre">{{ item.description }}</span>
                    <span v-if="item.tags && item.tags.length" class="recommend-tags">
                      <span v-for="tag in item.tags" :key="`${item.id}-${tag}`">{{ tag }}</span>
                    </span>
                    <span v-if="item.reason" class="recommend-reason">{{ item.reason }}</span>
                  </template>
                </a-card-meta>
              </a-card>
            </a-col>
          </template>
        </a-row>
      </template>

      <!-- Search Result View -->
      <template v-else>
        <div class="back-btn-wrapper">
          <a-button @click="backward()" type="link" class="back-btn">
            <ArrowLeftOutlined/> 返回推荐
          </a-button>
        </div>

        <!-- Person Results -->
        <div v-if="person_list !== null && person_list.length > 0" class="result-section">
          <h3 class="result-title">影人 ({{ count1 }})</h3>
          <a-row :gutter="[24, 24]">
            <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(item, itemIndex) in person_list" :key="itemIndex">
              <a-card hoverable class="movie-card" @click="watchPersonDetail(item.personID)">
                 <template #cover>
                  <div class="image-wrapper">
                     <imgWrapper :src="item.img" :alt="item.name" />
                  </div>
                </template>
                <a-card-meta :title="item.name" :description="item.role"/>
              </a-card>
            </a-col>
          </a-row>
          <div class="pagination-wrapper" v-if="count1 > 4">
             <a-pagination v-model:current="current1" show-less-items :total="count1" :default-page-size="4" :show-size-changer="false" :show-total="total => `共 ${total} 条`" @change="onChange1"/>
          </div>
        </div>

        <a-divider v-if="person_list && person_list.length && movie_list && movie_list.length" />

        <!-- Movie Results -->
        <div v-if="movie_list !== null && movie_list.length > 0" class="result-section">
          <h3 class="result-title">电影 ({{ count2 }})</h3>
          <a-row :gutter="[24, 24]">
            <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(item, itemIndex) in movie_list" :key="itemIndex">
              <a-card hoverable class="movie-card" @click="watchMovieDetail(item.movieID)">
                <template #cover>
                  <div class="image-wrapper">
                    <imgWrapper :src="item.img" :alt="item.name" />
                  </div>
                 </template>
                <a-card-meta :title="item.name" :description="item.role||item.year"/>
              </a-card>
            </a-col>
          </a-row>
           <div class="pagination-wrapper" v-if="count2 > 8">
            <a-pagination v-model:current="current2" show-less-items :total="count2" :default-page-size="8" :show-size-changer="false" :show-total="total => `共 ${total} 条`" @change="onChange2"/>
          </div>
        </div>
        
        <a-empty v-if="(!person_list || person_list.length === 0) && (!movie_list || movie_list.length === 0)" description="未找到相关结果" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ArrowLeftOutlined, ReloadOutlined } from "@ant-design/icons-vue";
import SkeletonCard from './SkeletonCard.vue';
import { defineComponent, h } from 'vue';

import defaultPoster from '@/assets/default_movie_poster.svg';

// Helper component to handle image errors simply
const imgWrapper = defineComponent({
  props: ['src', 'alt'],
  setup(props) {
    const proxySrc = (src) => src
      ? `http://localhost:8080/image?url=${encodeURIComponent(src)}`
      : defaultPoster;

    return () => h('img', {
      src: proxySrc(props.src),
      alt: props.alt,
      'data-direct-src': props.src || '',
      'data-fallback-step': 'proxy',
      referrerpolicy: "no-referrer",
      loading: "lazy",
      decoding: "async",
      style: { width: '100%', height: '100%', objectFit: 'cover' },
      onError: (e) => {
        const target = e.target;
        const directSrc = target.dataset.directSrc;
        if (target.dataset.fallbackStep === 'proxy' && directSrc) {
          target.dataset.fallbackStep = 'direct';
          target.src = directSrc;
          return;
        }
        if (target.src !== defaultPoster) {
          target.dataset.fallbackStep = 'default';
          target.src = defaultPoster;
        }
      }
    })
  }
})
</script>

<script>
import axios from "axios";
import router from "@/router/router";

export default {
  components: {
    SkeletonCard
  },
  data() {
    return {
      formState: {
        search: '',
      },
      search: false,
      movie_list: [],
      recommendedImages: [],
      person_list: [],
      current1: 1,
      limit1: 4,
      offset1: 0,
      count1: 0,
      current2: 1,
      limit2: 8,
      offset2: 0,
      count2: 0,
      nickname: '',
      loading: false,
      searchTimer: null,
      personSearchController: null,
      movieSearchController: null,
    };
  },
  beforeMount() {
    this.nickname = sessionStorage.getItem('nickname');
    this.fetchData();
  },
  methods: {
    onChange1() {
      this.offset1 = (this.current1 - 1) * 4;
      this.fetchData1();
    },

    onChange2() {
      this.offset2 = (this.current2 - 1) * 8;
      this.fetchData2();
    },

    async watchPersonDetail(id) {
      await router.push('/person/' + id);
    },

    async watchMovieDetail(id) {
      await router.push('/movie/' + id);
    },

    async fetchData() {
      try {
        this.loading = true;
        // Simulate a small delay to show skeleton screen effect if network is too fast
        // await new Promise(r => setTimeout(r, 800)); 
        
        const response = await axios.post('http://localhost:8080/movie/recommend',
            {nickname: this.nickname});
            
        this.recommendedImages = []; // Clear existing
        if (response.data) {
             for (let i = 0; i < Math.min(8, response.data.length); i++) {
                let desc = response.data[i].genre || '';
                desc = desc.replace(/[\s,，、]+$/g, '');
                
                this.recommendedImages.push({
                  title: response.data[i].name,
                  id: response.data[i].movieID,
                  image: response.data[i].img,
                  description: desc,
                  reason: response.data[i].recommendReason || '',
                  source: response.data[i].recommendSource || '',
                  tags: Array.isArray(response.data[i].recommendTags) ? response.data[i].recommendTags : []
                });
            }
        }
      } catch (error) {
        console.error("Failed to fetch recommendations", error);
      } finally {
        this.loading = false;
      }
    },

    refresh() {
      this.recommendedImages = [];
      this.fetchData();
    },

    sourceClass(source) {
      if (source === '相似口味') return 'source-cf';
      if (source === '口味相近' || source === '常看地区') return 'source-profile';
      if (source === '高分佳片') return 'source-rating';
      return 'source-explore';
    },

    backward() {
      this.abortSearchRequests();
      this.search = false;
      this.current1 = 1;
      this.offset1 = 0;
      this.current2 = 1;
      this.offset2 = 0;
      this.formState.search = ''; 
    },

    submitForm() {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer);
      }
      this.searchTimer = setTimeout(() => this.runSearch(), 180);
    },

    async runSearch() {
      if(!this.formState.search.trim()) return;
      
      this.search = true;
      this.current1 = 1;
      this.current2 = 1;
      this.offset1 = 0;
      this.offset2 = 0;
      
      this.fetchData1();
      this.fetchData2();
    },

    async fetchData1() {
       if (this.personSearchController) {
        this.personSearchController.abort();
      }
      const controller = new AbortController();
      this.personSearchController = controller;
      const keyword = this.formState.search.trim();
       try {
        const countRes = await axios.post('http://localhost:8080/person/count3', {keyword}, {signal: controller.signal});
        this.count1 = countRes.data;
        
        const searchRes = await axios.post('http://localhost:8080/person/search', 
            {keyword, limit: this.limit1, offset: this.offset1}, {signal: controller.signal});
        this.person_list = searchRes.data;
      } catch (e) {
        if (!this.isCanceled(e)) {
          console.error(e);
        }
      } finally {
        if (this.personSearchController === controller) {
          this.personSearchController = null;
        }
      }
    },

    async fetchData2() {
       if (this.movieSearchController) {
        this.movieSearchController.abort();
      }
      const controller = new AbortController();
      this.movieSearchController = controller;
      const keyword = this.formState.search.trim();
       try {
        const countRes = await axios.post('http://localhost:8080/movie/count2', {keyword}, {signal: controller.signal});
        this.count2 = countRes.data;
        
        const searchRes = await axios.post('http://localhost:8080/movie/search', 
            {keyword, limit: this.limit2, offset: this.offset2}, {signal: controller.signal});
        this.movie_list = searchRes.data;
      } catch (e) {
        if (!this.isCanceled(e)) {
          console.error(e);
        }
      } finally {
        if (this.movieSearchController === controller) {
          this.movieSearchController = null;
        }
      }
    },

    abortSearchRequests() {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer);
        this.searchTimer = null;
      }
      if (this.personSearchController) {
        this.personSearchController.abort();
        this.personSearchController = null;
      }
      if (this.movieSearchController) {
        this.movieSearchController.abort();
        this.movieSearchController = null;
      }
    },

    isCanceled(error) {
      return axios.isCancel(error) || error.code === 'ERR_CANCELED';
    },
  },
  beforeUnmount() {
    this.abortSearchRequests();
  },
}
</script>

<style scoped>
.recommend-container {
  padding: 8px 0 32px;
  max-width: 1400px;
  margin: 0 auto;
  text-align: left;
}

.search-section {
  margin: 8px 0 36px;
}

:deep(.custom-search .ant-input-group .ant-input) {
  height: 46px;
  border-color: var(--movie-line);
  border-radius: var(--movie-radius) 0 0 var(--movie-radius) !important;
  padding-left: 20px;
  color: var(--movie-ink);
}

:deep(.custom-search .ant-input-group .ant-btn) {
  height: 46px;
  min-width: 96px;
  border-color: var(--movie-accent);
  border-radius: 0 var(--movie-radius) var(--movie-radius) 0 !important;
  background: var(--movie-accent);
  padding: 0 22px;
}

:deep(.custom-search .ant-input-group .ant-btn:hover) {
  border-color: var(--movie-accent-dark);
  background: var(--movie-accent-dark);
}

:deep(.custom-search .ant-input:focus),
:deep(.custom-search .ant-input-focused) {
  border-color: var(--movie-accent);
  box-shadow: 0 0 0 3px rgba(196, 59, 69, 0.12);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--movie-ink);
  margin: 0;
  position: relative;
  padding-left: 16px;
  letter-spacing: 0;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background-color: var(--movie-accent);
  border-radius: 2px;
}

.refresh-btn {
  color: var(--movie-muted);
  font-size: 14px;
}

.refresh-btn:hover {
  color: var(--movie-accent);
  background: rgba(196, 59, 69, 0.08);
}

.movie-card {
  border-radius: var(--movie-radius);
  overflow: hidden;
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
  border: 1px solid var(--movie-line);
  background: var(--movie-surface);
  box-shadow: var(--movie-shadow-sm);
  height: 100%;
}

.movie-card:hover {
  transform: translateY(-4px);
  border-color: rgba(196, 59, 69, 0.32);
  box-shadow: var(--movie-shadow-md);
}

:deep(.movie-card .ant-card-body) {
  min-height: 92px;
  padding: 14px 16px 16px;
}

:deep(.movie-card .ant-card-meta-title) {
  color: var(--movie-ink);
  font-weight: 650;
  letter-spacing: 0;
  margin-bottom: 6px !important;
}

:deep(.movie-card .ant-card-meta-description) {
  min-height: 54px;
}

.image-wrapper {
  aspect-ratio: 2 / 3;
  background: #e8edf2;
  overflow: hidden;
  position: relative;
}

.image-wrapper :deep(img) {
  display: block;
  transition: transform 0.42s ease;
}

.movie-card:hover .image-wrapper :deep(img) {
  transform: scale(1.04);
}

.recommend-source-badge {
  position: absolute;
  z-index: 2;
  top: 10px;
  right: 10px;
  max-width: calc(100% - 20px);
  padding: 4px 9px;
  border-radius: 999px;
  color: #fff;
  background: rgba(17, 24, 32, 0.72);
  box-shadow: 0 8px 18px rgba(17, 24, 32, 0.18);
  backdrop-filter: blur(6px);
  font-size: 12px;
  font-weight: 750;
  line-height: 1.35;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.recommend-source-badge.source-cf {
  background: rgba(15, 127, 131, 0.88);
}

.recommend-source-badge.source-profile {
  background: rgba(80, 101, 163, 0.88);
}

.recommend-source-badge.source-rating {
  background: rgba(196, 59, 69, 0.9);
}

.recommend-source-badge.source-explore {
  background: rgba(95, 105, 116, 0.86);
}

.movie-genre {
  color: var(--movie-muted);
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recommend-reason {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 38px;
  margin-top: 6px;
  color: #50606d;
  font-size: 12px;
  line-height: 1.55;
  overflow: hidden;
}

.recommend-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  min-height: 22px;
  margin-top: 8px;
}

.recommend-tags span {
  max-width: 88px;
  padding: 2px 7px 3px;
  border-radius: 999px;
  color: #60707d;
  background: #f0f4f6;
  border: 1px solid #dde7ec;
  font-size: 11px;
  font-weight: 700;
  line-height: 1.55;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  padding-bottom: 24px;
}

.back-btn-wrapper {
  margin-bottom: 20px;
}

.back-btn {
  color: var(--movie-teal);
  padding-left: 0;
}

.back-btn:hover {
  color: var(--movie-accent);
}

.result-section {
  margin-bottom: 40px;
}

.result-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: var(--movie-ink);
  letter-spacing: 0;
}

@media (max-width: 576px) {
  .recommend-container {
    padding-bottom: 20px;
  }

  .section-header {
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
