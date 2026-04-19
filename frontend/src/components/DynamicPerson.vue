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

    <a-card class="section-card summary-card" title="影人简介" :bordered="false">
      <div class="summary-panel">
        <div class="summary-rule"></div>
        <a-typography-paragraph :content="this.person_content.summary || '暂无影人简介。'"
                                :ellipsis="ellipsis ? { rows: 5, expandable: true, symbol: '展开全部' } : false"
                                class="summary-text"/>
      </div>
    </a-card>

    <a-card class="section-card" :bordered="false">
      <template #title>
        <div class="paged-title-line">
          <span>代表作品</span>
          <span class="page-center-status" v-if="worksTotalPages > 1">
            <strong>{{ current1 }}</strong>
            <span>/ {{ worksTotalPages }}</span>
          </span>
          <span class="section-count">{{ count1 }} 部</span>
        </div>
      </template>

      <div class="paged-stage">
        <button
            v-if="worksTotalPages > 1"
            type="button"
            class="side-page-button side-page-left"
            :disabled="current1 <= 1"
            aria-label="上一页"
            @click="goWorkPage(current1 - 1)"
        >
          ‹
        </button>

        <div class="work-grid" v-if="movie_list.length">
          <button
              v-for="(item, itemIndex) in movie_list"
              :key="itemIndex"
              type="button"
              class="work-tile"
              @click="watchMovieDetail(item.movieID)"
          >
            <span class="work-poster">
               <img :src="'http://localhost:8080/image?url=' + item.img" :alt="item.name" referrerpolicy="no-referrer" @error="handleImageError"/>
            </span>
            <span class="work-meta">
              <span class="work-genre">{{ formatGenre(item.genre) || '暂无类型' }}</span>
              <span class="work-title">{{ item.name }}</span>
            </span>
          </button>
        </div>
        <a-empty v-else description="暂无代表作品" class="empty-state"/>

        <button
            v-if="worksTotalPages > 1"
            type="button"
            class="side-page-button side-page-right"
            :disabled="current1 >= worksTotalPages"
            aria-label="下一页"
            @click="goWorkPage(current1 + 1)"
        >
          ›
        </button>
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
  computed: {
    worksTotalPages() {
      return Math.max(1, Math.ceil((Number(this.count1) || 0) / this.limit1));
    },
  },
  methods: {
    handleImageError(e) {
      const target = e.target;
      if (target.src !== defaultPoster) {
        target.src = defaultPoster;
      }
    },
    onChange1(page = this.current1) {
      this.goWorkPage(page);
    },
    goWorkPage(page) {
      const nextPage = Math.min(Math.max(page, 1), this.worksTotalPages);
      if (nextPage === this.current1) {
        return;
      }
      this.current1 = nextPage;
      this.offset1 = (this.current1 - 1) * this.limit1;
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

.summary-card :deep(.ant-card-body) {
  padding-top: 22px;
}

.summary-panel {
  display: grid;
  grid-template-columns: 4px minmax(0, 1fr);
  gap: 18px;
  padding: 20px 22px;
  background:
      linear-gradient(135deg, rgba(21, 127, 131, 0.07), rgba(181, 138, 47, 0.06)),
      var(--movie-surface-soft);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
}

.summary-rule {
  width: 4px;
  min-height: 100%;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--movie-teal), var(--movie-gold));
}

.summary-text {
  font-size: 15px;
  line-height: 1.9;
  color: var(--movie-ink);
  margin-bottom: 0;
  letter-spacing: 0;
}

:deep(.summary-text .ant-typography-expand) {
  color: var(--movie-teal);
  font-weight: 650;
}

.work-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.work-tile {
  width: 100%;
  min-height: 142px;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  text-align: left;
  color: inherit;
  background: linear-gradient(180deg, #fff, var(--movie-surface-soft));
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  cursor: pointer;
  box-shadow: 0 6px 14px rgba(18, 24, 33, 0.05);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background 0.22s ease;
}

.work-tile:hover,
.work-tile:focus-visible {
  transform: translateY(-2px);
  border-color: rgba(196, 59, 69, 0.32);
  background: #fff;
  box-shadow: var(--movie-shadow-sm);
  outline: none;
}

.work-poster {
  width: 80px;
  height: 116px;
  flex: 0 0 80px;
  overflow: hidden;
  border-radius: var(--movie-radius);
  background: #e8edf2;
  box-shadow: 0 8px 18px rgba(18, 24, 33, 0.12);
}

.work-poster img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.work-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.work-title {
  color: var(--movie-ink);
  font-size: 16px;
  font-weight: 750;
  line-height: 1.35;
  word-break: break-word;
}

.work-genre {
  width: fit-content;
  max-width: 100%;
  padding: 3px 9px;
  color: var(--movie-accent);
  background: rgba(196, 59, 69, 0.08);
  border: 1px solid rgba(196, 59, 69, 0.16);
  border-radius: var(--movie-radius);
  font-size: 12px;
  font-weight: 650;
  line-height: 1.45;
  white-space: normal;
  word-break: break-word;
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

:deep(.section-card .ant-card-head-title) {
  width: 100%;
}

.paged-title-line {
  position: relative;
  width: 100%;
  min-height: 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-center-status {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: inline-flex;
  align-items: baseline;
  gap: 5px;
  color: var(--movie-muted);
  font-size: 13px;
  font-weight: 600;
}

.page-center-status strong {
  color: var(--movie-accent);
  font-size: 16px;
  font-weight: 800;
}

.paged-stage {
  position: relative;
  min-height: 142px;
  padding: 0 52px;
}

.side-page-button {
  position: absolute;
  top: 0;
  bottom: 0;
  z-index: 2;
  width: 30px;
  height: auto;
  min-height: 142px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: rgba(101, 112, 124, 0.72);
  background: rgba(248, 250, 252, 0.72);
  border: 1px solid rgba(223, 229, 235, 0.82);
  border-radius: var(--movie-radius);
  cursor: pointer;
  font-size: 22px;
  line-height: 1;
  box-shadow: none;
  transition: color 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.side-page-left {
  left: 0;
}

.side-page-right {
  right: 0;
}

.side-page-button:hover,
.side-page-button:focus-visible {
  color: var(--movie-accent);
  border-color: rgba(196, 59, 69, 0.34);
  background: #fff;
  box-shadow: 0 6px 14px rgba(18, 24, 33, 0.07);
  outline: none;
}

.side-page-button:disabled {
  color: rgba(184, 192, 200, 0.62);
  background: rgba(248, 250, 252, 0.5);
  border-color: rgba(223, 229, 235, 0.62);
  box-shadow: none;
  cursor: not-allowed;
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

  .paged-title-line {
    flex-wrap: wrap;
  }

  .page-center-status {
    position: static;
    width: 100%;
    justify-content: center;
    order: 3;
    transform: none;
  }

  .paged-stage {
    padding: 0 42px;
  }

  .side-page-button {
    width: 28px;
  }

  .work-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .work-grid {
    gap: 16px;
  }

  .paged-stage {
    padding: 0 38px;
  }

  .summary-panel {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 16px;
  }

  .summary-rule {
    width: 42px;
    min-height: 4px;
  }

  .work-tile {
    min-height: 122px;
    padding: 10px;
  }

  .work-poster {
    width: 68px;
    height: 98px;
    flex-basis: 68px;
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
