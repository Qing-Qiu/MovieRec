<template>
  <div class="page-container">
    <!-- Hero Section: Movie Details -->
    <a-card class="movie-hero-card" :bordered="false">
      <div class="hero-content">
        <div class="poster-wrapper">
          <img :src="'http://localhost:8080/image?url=' + this.movie_content.img" :alt="this.movie_content.name" referrerpolicy="no-referrer" class="movie-poster"/>
        </div>
        <div class="info-wrapper">
          <a-typography-title :level="1" class="movie-title">{{ this.movie_content.name }}</a-typography-title>
          
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">导演：</span>
              <span class="info-value">{{ this.movie_content.director }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">主演：</span>
              <span class="info-value">{{ this.movie_content.actor }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">类型：</span>
              <a-tag color="blue" v-if="this.movie_content.tag">{{ this.movie_content.tag }}</a-tag>
            </div>
             <div class="info-item">
              <span class="info-label">年份：</span>
              <span class="info-value">{{ this.movie_content.year }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">地区：</span>
              <span class="info-value">{{ this.movie_content.region }}</span>
            </div>
             <div class="info-item highlight">
              <span class="info-label">评分：</span>
              <span class="rating-value">{{ this.movie_content.rate }}</span>
              <span class="rate-count">({{ this.movie_content.popular }}人评价)</span>
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <!-- Summary Section -->
    <a-card class="section-card" title="剧情简介" :bordered="false">
      <a-typography-paragraph :content="this.movie_content.summary"
                              :ellipsis="ellipsis ? { rows: 5, expandable: true, symbol: '展开全部' } : false"
                              class="summary-text"/>
    </a-card>

    <!-- Cast Section -->
    <a-card class="section-card" title="演职员表" :bordered="false">
      <div class="cast-grid">
        <a-card
            v-for="(item, itemIndex) in person_list"
            :key="itemIndex"
            class="cast-card"
            hoverable
            @click="watchPersonDetail(item.personID)"
        >
          <div class="cast-image-wrapper">
             <img :src="'http://localhost:8080/image?url=' + item.img" :alt="item.name" referrerpolicy="no-referrer"/>
          </div>
          <a-card-meta :title="item.name">
             <template #description>
               <span class="cast-role">{{ item.role }}</span>
             </template>
          </a-card-meta>
        </a-card>
        <!-- Placeholders to keep grid alignment if needed, though flex/grid gaps handle this better usually -->
        <div v-for="i in (4 - (person_list.length % 4)) % 4" :key="'placeholder-'+i" class="cast-card-placeholder"></div>
      </div>
       <div class="pagination-wrapper">
        <a-pagination show-less-items v-model:current="current1" show-quick-jumper :total="this.count1"
                      :default-page-size="4" :show-size-changer="false" :show-total="total => `共 ${total} 条`" @change="onChange1"/>
      </div>
    </a-card>

    <!-- Comments Section -->
    <a-card class="section-card" title="评论" :bordered="false">
      <div class="comment-input-section">
        <a-comment>
          <template #avatar>
            <a-avatar :src="UserImage" size="large"/>
          </template>
          <template #content>
            <a-form-item>
              <a-textarea v-model:value="new_comment" :rows="4" placeholder="写下你的评论..." />
            </a-form-item>
            <a-form-item>
              <a-button v-if="disable()" type="primary" html-type="submit" :loading="submitting" @click="submitComment">
                添加评论
              </a-button>
               <a-button v-else disabled>登录以发表评论</a-button>
            </a-form-item>
          </template>
        </a-comment>
      </div>

      <a-list
          class="comment-list"
          item-layout="horizontal"
          :data-source="comment_list"
          :loading="loading"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-comment :author="item.nickname" :avatar="item.avatar || UserImage">
              <template #content>
                 <div class="comment-text">{{ item.comment }}</div>
              </template>
            </a-comment>
          </a-list-item>
        </template>
      </a-list>
      
      <div class="pagination-wrapper">
        <a-pagination show-less-items v-model:current="current2" show-quick-jumper :total="this.count2"
                      :default-page-size="8" :show-size-changer="false" :show-total="total => `共 ${total} 条`" @change="onChange2"/>
      </div>
    </a-card>
  </div>
</template>

<script>
import axios from "axios";
import router from "@/router/router";

export default {
  data() {
    return {
      movie_content: [],
      movie_list: [],
      person_list: [],
      comment_list: [],
      current1: 1,
      count1: 0,
      limit1: 4,
      offset1: 0,
      current2: 1,
      count2: 0,
      limit2: 8,
      offset2: 0,
      username: '游客',
      loading: false,
      ellipsis: true,
      new_comment: '',
      submitting: false,
    }
  },
  beforeMount() {
    this.fetchData();
    this.fetchData1();
    this.fetchData2();
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
    async fetchData() {
      try {
        const response = await axios.post('http://localhost:8080/movie/detail',
            {movie: this.movie_id}).then(
            response => {
              this.movie_content = response.data;
            },
            error => {
            }
        )
      } catch (error) {
      }
    },
    async fetchData1() {
      try {
        const response = await axios.post('http://localhost:8080/person/count',
            {id: this.movie_id}).then(
            response => {
              this.count1 = response.data;
            },
            error => {
            }
        )
      } catch (error) {
      }
      try {
        const response = await axios.post('http://localhost:8080/person/relevant',
            {id: this.movie_id, limit: this.limit1, offset: this.offset1}).then(
            response => {
              this.person_list = response.data;
            },
            error => {
            }
        )
      } catch (error) {
      }
    },
    async fetchData2() {
      this.loading = true;
      try {
        const response = await axios.post('http://localhost:8080/comment/count',
            {id: this.movie_id}).then(
            response => {
              this.count2 = response.data;
            },
            error => {
            }
        )
      } catch (error) {
      }
      try {
        const response = await axios.post('http://localhost:8080/comment/comment',
            {id: this.movie_id, limit: this.limit2, offset: this.offset2}).then(
            response => {
              this.comment_list = response.data;
            },
            error => {
            }
        )
      } catch (error) {
      }
      this.loading = false;
    },

    async submitComment() {
      this.submitting = true;
      try {
        const response = await axios.post('http://localhost:8080/comment/append',
            {
              userID: sessionStorage.getItem('username'),
              nickname: sessionStorage.getItem('nickname'),
              comment: this.new_comment,
              movieID: this.movie_id,
            }).then(
            response => {

            },
            error => {
            }
        )
      } catch (error) {
      }
      this.new_comment = "";
      this.submitting = false;
      await this.fetchData2();
    },

    disable() {
      return !(sessionStorage.getItem('nickname') === null
          || sessionStorage.getItem('username') === null);
    }
  },
  props: {
    movie_id: {
      type: String,
      required: true
    }
  }
}
</script>
<script setup>
import HomePage from "@/views/HomePage";
import UserImage from '@/assets/meow.jpg';
</script>

<style scoped>
.page-container {
  max-width: 1200px; /* Standardize width */
  margin: 0 auto;
  padding: 24px;
  background-color: #f5f7fa; /* Light grey background for the whole page area */
  text-align: left; /* Fix global center alignment */
}

/* Common Card Styles */
.movie-hero-card, .section-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* Soft shadow */
  background: white;
  overflow: hidden;
}

/* Hero Section */
.hero-content {
  display: flex;
  flex-direction: row;
  gap: 32px;
  padding: 24px;
}

.poster-wrapper {
  flex-shrink: 0;
  width: 240px;
}

.movie-poster {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  object-fit: cover;
}

.info-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.movie-title {
  margin-bottom: 24px;
  font-weight: 700;
  color: #1a1a1a;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: baseline;
  font-size: 15px;
  line-height: 1.6;
}

.info-label {
  font-weight: 600;
  color: #666;
  width: 80px; /* Fixed label width for alignment */
  flex-shrink: 0;
}

.info-value {
  color: #333;
}

.highlight {
  margin-top: 12px;
  align-items: center;
}

.rating-value {
  font-size: 24px;
  font-weight: bold;
  color: #faad14; /* Amber color for rating */
  margin-right: 8px;
}

.rate-count {
  font-size: 13px;
  color: #999;
}

/* Summary Section */
.summary-text {
  font-size: 15px;
  line-height: 1.8;
  color: #444;
}

/* Cast Section */
.cast-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between; /* Distribute space */
  gap: 24px;
}

.cast-card {
  width: calc(25% - 18px); /* 4 items per row accounting for gap */
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.cast-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.cast-image-wrapper {
  height: 280px; /* Fixed height for consistency */
  overflow: hidden;
  border-radius: 8px 8px 0 0;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cast-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cast-role {
  font-size: 12px;
  color: #888;
}

/* Placeholders for grid alignment */
.cast-card-placeholder {
  width: calc(25% - 18px);
}

/* Comments Section */
.comment-input-section {
  margin-bottom: 32px;
}

.comment-list .ant-list-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-text {
  font-size: 14px;
  color: #333;
}

/* Pagination */
.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  padding-bottom: 12px;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
    align-items: center;
  }
  
  .poster-wrapper {
    width: 180px;
    margin-bottom: 16px;
  }
  
  .info-wrapper {
    width: 100%;
    text-align: center;
  }
  
  .info-item {
    justify-content: center;
  }
  
  .info-label {
    width: auto;
    margin-right: 8px;
  }
  
  .cast-card {
    width: calc(50% - 12px); /* 2 items per row on mobile */
  }
  
  .cast-card-placeholder {
     width: calc(50% - 12px);
  }
}
</style>