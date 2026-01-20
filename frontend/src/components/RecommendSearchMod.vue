<template>
  <div class="recommend-container">
    <a-row justify="center" class="search-section">
      <a-col :xs="22" :sm="20" :md="16" :lg="12" :xl="10">
        <a-input-search
            v-model:value="formState.search"
            placeholder="Search for movies, actors, directors..."
            size="large"
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
                    <imgWrapper 
                      :src="'http://localhost:8080/image?url=' + item.image" 
                      :alt="item.title" 
                    />
                  </div>
                </template>
                <a-card-meta :title="item.title">
                  <template #description>
                    <span class="movie-genre">{{ item.description }}</span>
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
                     <imgWrapper :src="'http://localhost:8080/image?url=' + item.img" :alt="item.name" />
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
                    <imgWrapper :src="'http://localhost:8080/image?url=' + item.img" :alt="item.name" />
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

// Helper component to handle image errors simply
const imgWrapper = defineComponent({
  props: ['src', 'alt'],
  setup(props) {
    return () => h('img', {
      src: props.src,
      alt: props.alt,
      referrerpolicy: "no-referrer",
      style: { width: '100%', height: '100%', objectFit: 'cover' },
      onError: (e) => { e.target.src = 'https://via.placeholder.com/300x450?text=No+Image'; }
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
                if (desc.endsWith(',')) desc = desc.slice(0, -1);
                
                this.recommendedImages.push({
                  title: response.data[i].name,
                  id: response.data[i].movieID,
                  image: response.data[i].img,
                  description: desc
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

    backward() {
      this.search = false;
      this.current1 = 1;
      this.offset1 = 0;
      this.current2 = 1;
      this.offset2 = 0;
      this.formState.search = ''; 
    },

    async submitForm() {
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
       try {
        const countRes = await axios.post('http://localhost:8080/person/count3', {keyword: this.formState.search});
        this.count1 = countRes.data;
        
        const searchRes = await axios.post('http://localhost:8080/person/search', 
            {keyword: this.formState.search, limit: this.limit1, offset: this.offset1});
        this.person_list = searchRes.data;
      } catch (e) { console.error(e); }
    },

    async fetchData2() {
       try {
        const countRes = await axios.post('http://localhost:8080/movie/count2', {keyword: this.formState.search});
        this.count2 = countRes.data;
        
        const searchRes = await axios.post('http://localhost:8080/movie/search', 
            {keyword: this.formState.search, limit: this.limit2, offset: this.offset2});
        this.movie_list = searchRes.data;
      } catch (e) { console.error(e); }
    },
  },
}
</script>

<style scoped>
.recommend-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.search-section {
  margin-bottom: 40px;
  margin-top: 20px;
}

/* Deep selector to override Ant Design input styles */
:deep(.custom-search .ant-input-group .ant-input) {
  border-radius: 24px 0 0 24px;
  padding-left: 20px;
}
:deep(.custom-search .ant-input-group .ant-btn) {
  border-radius: 0 24px 24px 0;
  padding: 0 20px;
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
  color: #333;
  margin: 0;
  position: relative;
  padding-left: 16px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background-color: #1890ff;
  border-radius: 2px;
}

.refresh-btn {
  color: #666;
  font-size: 14px;
}
.refresh-btn:hover {
  color: #1890ff;
}

.movie-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  height: 100%;
}

.movie-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.12);
}

.image-wrapper {
  height: 360px; /* Fixed height for consistency */
  overflow: hidden;
  position: relative;
}

/* Make sure inner image fills the wrapper */
.image-wrapper :deep(img) {
  transition: transform 0.5s ease;
}

.movie-card:hover .image-wrapper :deep(img) {
  transform: scale(1.05);
}

.movie-genre {
  color: #888;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Pagination Styling */
.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  padding-bottom: 24px;
}

.back-btn-wrapper {
    margin-bottom: 20px;
}

.result-section {
    margin-bottom: 40px;
}

.result-title {
    font-size: 20px;
    margin-bottom: 20px;
    color: #444;
}
</style>
