<template>
  <div class="page-container">
    <a-card class="person-hero-card" :bordered="false">
      <div class="hero-content">
        <div class="poster-wrapper">
          <img :src="'http://localhost:8080/image?url=' + this.person_content.img" :alt="this.person_content.name" referrerpolicy="no-referrer" class="person-poster" @error="handleImageError"/>
        </div>
        <div class="info-wrapper">
          <div class="detail-kicker">影人档案</div>
          <a-typography-title :level="1" class="person-title">{{ this.person_content.name }}</a-typography-title>

          <div class="identity-panel">
            <div class="identity-item">
              <span class="identity-label">性别</span>
              <strong>{{ this.person_content.sex || '-' }}</strong>
            </div>
            <div class="identity-divider"></div>
            <div class="identity-item">
              <span class="identity-label">生日</span>
              <strong>{{ this.person_content.birthday || '-' }}</strong>
            </div>
            <div class="identity-divider"></div>
            <div class="identity-item">
              <span class="identity-label">出生地</span>
              <strong>{{ this.person_content.birthplace || '-' }}</strong>
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <a-card class="section-card" title="影人简介" :bordered="false">
      <a-typography-paragraph :content=" this.person_content.summary "
                              :ellipsis="ellipsis ? { rows: 5, expandable: true, symbol: '展开全部' } : false"
                              class="summary-text"/>
    </a-card>

    <a-card class="section-card" :bordered="false">
      <template #title>
        <div class="section-title-line">
          <span>代表作品</span>
          <span class="section-count">{{ count1 }} 部</span>
        </div>
      </template>

      <div class="movie-grid" v-if="movie_list.length">
        <a-card
            v-for="(item, itemIndex) in movie_list"
            :key="itemIndex"
            class="movie-card"
            hoverable
            @click="watchMovieDetail(item.movieID)"
        >
          <div class="movie-image-wrapper">
             <img :src="'http://localhost:8080/image?url=' + item.img" :alt="item.name" referrerpolicy="no-referrer" @error="handleImageError"/>
          </div>
          <a-card-meta :title="item.name">
             <template #description>
               <span class="movie-genre">{{ formatGenre(item.genre) }}</span>
             </template>
          </a-card-meta>
        </a-card>
        <div v-for="i in (4 - (movie_list.length % 4)) % 4" :key="'placeholder-'+i" class="movie-card-placeholder"></div>
      </div>
      <a-empty v-else description="暂无代表作品" class="empty-state"/>
      <div class="pagination-wrapper" v-if="count1 > 4">
        <a-pagination show-less-items v-model:current="current1" show-quick-jumper :total="this.count1"
                      :default-page-size="4" :show-size-changer="false" :show-total="total => `共 ${total} 条`" @change="onChange1"/>
      </div>
    </a-card>
  </div>
</template>

<script>
import axios from "axios";
import router from "@/router/router";
import defaultPoster from '@/assets/default_movie_poster.svg';

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
    handleImageError(e) {
      const target = e.target;
      if (target.src !== defaultPoster) {
        target.src = defaultPoster;
      }
    },
    onChange1() {
      this.offset1 = (this.current1 - 1) * 4;
      this.fetchData();
    },

    async watchMovieDetail(id) {
      await router.push('/movie/' + id);
    },
    formatGenre(value) {
      return (value || '').replace(/[\s,，、]+$/g, '');
    },

    async fetchData() {
      try {
        const response = await axios.post('http://localhost:8080/person/detail',
            {id: this.person_id}).then(
            response => {
              this.person_content = response.data;
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
              this.movie_list = response.data || [];
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
}
</script>
<script setup>
import {ref} from "vue";

const ellipsis = ref(true);
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 8px 0 32px;
  background: transparent;
  text-align: left;
}

.person-hero-card, .section-card {
  margin-bottom: 24px;
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  box-shadow: var(--movie-shadow-sm);
  background: var(--movie-surface);
  overflow: hidden;
}

:deep(.section-card .ant-card-head) {
  border-bottom-color: var(--movie-line);
  min-height: 58px;
}

:deep(.section-card .ant-card-head-title) {
  color: var(--movie-ink);
  font-weight: 650;
  letter-spacing: 0;
}

.hero-content {
  display: flex;
  flex-direction: row;
  gap: 36px;
  padding: 32px;
  background:
      linear-gradient(135deg, rgba(21, 25, 31, 0.05), rgba(21, 127, 131, 0.06)),
      #fff;
}

.poster-wrapper {
  flex-shrink: 0;
  width: min(230px, 30vw);
  aspect-ratio: 2 / 3;
  overflow: hidden;
  border-radius: var(--movie-radius);
  background: #e8edf2;
  box-shadow: 0 18px 36px rgba(18, 24, 33, 0.16);
}

.person-poster {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.info-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.detail-kicker {
  width: fit-content;
  margin-bottom: 12px;
  padding: 4px 10px;
  color: var(--movie-teal);
  background: rgba(21, 127, 131, 0.08);
  border: 1px solid rgba(21, 127, 131, 0.16);
  border-radius: var(--movie-radius);
  font-size: 12px;
  font-weight: 650;
}

.person-title {
  margin-bottom: 14px;
  font-weight: 700;
  color: var(--movie-ink);
  letter-spacing: 0;
  line-height: 1.18;
}

.identity-panel {
  width: min(620px, 100%);
  display: flex;
  align-items: stretch;
  gap: 22px;
  margin-bottom: 24px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  box-shadow: 0 10px 24px rgba(18, 24, 33, 0.06);
}

.identity-item {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.identity-label {
  color: var(--movie-muted);
  font-size: 12px;
  font-weight: 650;
}

.identity-item strong {
  color: var(--movie-ink);
  font-size: 16px;
  line-height: 1.45;
  font-weight: 700;
}

.identity-divider {
  width: 1px;
  background: var(--movie-line);
}

.summary-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--movie-ink);
  margin-bottom: 0;
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.movie-card {
  border-radius: var(--movie-radius);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
  border: 1px solid var(--movie-line);
  overflow: hidden;
}

.movie-card:hover {
  transform: translateY(-4px);
  border-color: rgba(196, 59, 69, 0.32);
  box-shadow: var(--movie-shadow-md);
}

:deep(.movie-card .ant-card-body) {
  min-height: 82px;
  padding: 14px 16px 16px;
}

:deep(.movie-card .ant-card-meta-title) {
  color: var(--movie-ink);
  font-weight: 650;
  margin-bottom: 6px !important;
  white-space: normal;
  line-height: 1.35;
}

.movie-image-wrapper {
  aspect-ratio: 2 / 3;
  overflow: hidden;
  background: #e8edf2;
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
  color: var(--movie-muted);
  line-height: 1.45;
}

.movie-card-placeholder {
  display: none;
}

.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  padding-bottom: 12px;
}

.section-title-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-count {
  padding: 3px 9px;
  color: var(--movie-muted);
  background: var(--movie-surface-soft);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  font-size: 12px;
  font-weight: 600;
}

.empty-state {
  padding: 40px 0 28px;
}

@media (max-width: 768px) {
  .page-container {
    padding-bottom: 20px;
  }

  .hero-content {
    flex-direction: column;
    align-items: center;
    padding: 20px;
  }
  
  .poster-wrapper {
    width: 160px;
    margin-bottom: 16px;
  }
  
  .info-wrapper {
    width: 100%;
    text-align: left;
  }

  .detail-kicker,
  .identity-panel {
    margin-left: auto;
    margin-right: auto;
  }

  .person-title {
    text-align: center;
  }

  .movie-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 480px) {
  .movie-grid {
    gap: 16px;
  }

  .identity-panel {
    flex-direction: column;
    gap: 14px;
  }

  .identity-divider {
    width: 100%;
    height: 1px;
  }
}
</style>
