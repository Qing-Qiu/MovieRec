<template>
  <div class="page-container">
    <!-- Hero Section: Person Details -->
    <a-card class="person-hero-card" :bordered="false">
      <div class="hero-content">
        <div class="poster-wrapper">
          <img :src="'http://localhost:8080/image?url=' + this.person_content.img" :alt="this.person_content.name" referrerpolicy="no-referrer" class="person-poster"/>
        </div>
        <div class="info-wrapper">
          <a-typography-title :level="1" class="person-title">{{ this.person_content.name }}</a-typography-title>
          
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">性别：</span>
              <span class="info-value">{{ this.person_content.sex }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">生日：</span>
              <span class="info-value">{{ this.person_content.birthday }}</span>
            </div>
             <div class="info-item">
              <span class="info-label">出生地：</span>
              <span class="info-value">{{ this.person_content.birthplace }}</span>
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <!-- Summary Section -->
    <a-card class="section-card" title="影人简介" :bordered="false">
      <a-typography-paragraph :content=" this.person_content.summary "
                              :ellipsis="ellipsis ? { rows: 5, expandable: true, symbol: '展开全部' } : false"
                              class="summary-text"/>
    </a-card>

     <!-- Related Movies Section -->
    <a-card class="section-card" title="代表作品" :bordered="false">
      <div class="movie-grid">
        <a-card
            v-for="(item, itemIndex) in movie_list"
            :key="itemIndex"
            class="movie-card"
            hoverable
            @click="watchMovieDetail(item.movieID)"
        >
          <div class="movie-image-wrapper">
             <img :src="'http://localhost:8080/image?url=' + item.img" :alt="item.name" referrerpolicy="no-referrer"/>
          </div>
          <a-card-meta :title="item.name">
             <template #description>
               <span class="movie-genre">{{ item.genre }}</span>
             </template>
          </a-card-meta>
        </a-card>
        <!-- Placeholders to keep grid alignment -->
        <div v-for="i in (4 - (movie_list.length % 4)) % 4" :key="'placeholder-'+i" class="movie-card-placeholder"></div>
      </div>
      <div class="pagination-wrapper">
        <a-pagination show-less-items v-model:current="current1" show-quick-jumper :total="this.count1"
                      :default-page-size="4" :show-size-changer="false" :show-total="total => `共 ${total} 条`" @change="onChange1"/>
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
      person_content: [],
      movie_list: [],
      person_list: [],
      comment_list: [],
      current1: 1,
      count1: 0,
      limit1: 4,
      offset1: 0,
      username: '游客',
    }
  },
  beforeMount() {
    this.fetchData();
  },
  methods: {
    onChange1() {
      this.offset1 = (this.current1 - 1) * 4;
      this.fetchData();
    },

    async watchMovieDetail(id) {
      await router.push('/movie/' + id);
    },

    async fetchData() {
      try {
        const response = await axios.post('http://localhost:8080/person/detail',
            {id: this.person_id}).then(
            response => {
              this.person_content = response.data;
              console.log(response.data);
            },
            error => {
            }
        )
      } catch (error) {
      }
      try {
        const response = await axios.post('http://localhost:8080/person/count2',
            {id: this.person_id}).then(
            response => {
              this.count1 = response.data;
              console.log(this.count1);
            },
            error => {
            }
        )
      } catch (error) {
      }
      try {
        const response = await axios.post('http://localhost:8080/person/relevant2',
            {id: this.person_id, limit: this.limit1, offset: this.offset1}).then(
            response => {
              this.movie_list = response.data;
              for (let i = 0; i < 4; i++) {
                if (this.movie_list[i].genre !== null) {
                  if (this.movie_list[i].genre.endsWith(',')) {
                    this.movie_list[i].genre =
                        this.movie_list[i].genre.slice(0, -1);
                  }
                }
              }
              console.log(this.person_list);
            },
            error => {
            }
        )
      } catch (error) {
      }
    }
  },
  props: {
    person_id: {
      type: String,
      required: true
    }
  },
  created() {
    console.log(this.movie_id);
  }
}
</script>
<script setup>
import HomePage from "@/views/HomePage";
import {ref} from "vue";

const ellipsis = ref(true);
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background-color: #f5f7fa;
  text-align: left; /* Fix global center alignment */
}

/* Common Card Styles */
.person-hero-card, .section-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
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
  width: 200px; /* Slightly smaller than movie poster */
}

.person-poster {
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

.person-title {
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
  width: 80px;
  flex-shrink: 0;
}

.info-value {
  color: #333;
}

/* Summary Section */
.summary-text {
  font-size: 15px;
  line-height: 1.8;
  color: #444;
}

/* Movie List Section */
.movie-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 24px;
}

.movie-card {
  width: calc(25% - 18px);
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.movie-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.movie-image-wrapper {
  height: 280px;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.movie-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.movie-genre {
  font-size: 12px;
  color: #888;
}

.movie-card-placeholder {
  width: calc(25% - 18px);
}

/* Pagination */
.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  padding-bottom: 12px;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
    align-items: center;
  }
  
  .poster-wrapper {
    width: 160px;
    margin-bottom: 16px;
  }
  
  .info-wrapper {
    width: 100%;
    text-align: center;
  }
  
  .info-item {
    justify-content: center;
  }

    .movie-card {
    width: calc(50% - 12px);
  }
  
  .movie-card-placeholder {
     width: calc(50% - 12px);
  }
}
</style>