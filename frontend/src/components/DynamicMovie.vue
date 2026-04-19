<template>
  <div class="page-container">
    <a-card class="movie-hero-card" :bordered="false">
      <div class="hero-content">
        <div class="poster-wrapper">
          <img :src="'http://localhost:8080/image?url=' + this.movie_content.img" :alt="this.movie_content.name" referrerpolicy="no-referrer" class="movie-poster" @error="handleImageError"/>
        </div>
        <div class="info-wrapper">
          <div class="detail-kicker">电影详情</div>
          <a-typography-title :level="1" class="movie-title">{{ this.movie_content.name }}</a-typography-title>

          <div class="hero-tags">
            <a-tag class="movie-tag" v-if="this.movie_content.tag">{{ formatGenre(this.movie_content.tag) }}</a-tag>
            <span class="soft-pill" v-if="this.movie_content.year">{{ this.movie_content.year }}</span>
            <span class="soft-pill" v-if="this.movie_content.region">{{ this.movie_content.region }}</span>
          </div>

          <div class="rating-panel">
            <div>
              <div class="rating-label">MovieRec 评分</div>
              <div class="rating-line">
                <span class="rating-value">{{ this.movie_content.rate || '-' }}</span>
                <span class="rating-unit">/ 5</span>
              </div>
            </div>
            <div class="rating-divider"></div>
            <div>
              <div class="rating-label">热度</div>
              <div class="rating-line">
                <span class="popular-value">{{ this.movie_content.popular || 0 }}</span>
                <span class="rating-unit">人评价</span>
              </div>
            </div>
          </div>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">导演：</span>
              <span class="info-value">{{ this.movie_content.director }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">主演：</span>
              <span class="info-value">{{ formatList(this.movie_content.actor) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">类型：</span>
              <span class="info-value">{{ formatGenre(this.movie_content.genre || this.movie_content.tag) }}</span>
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <a-card class="section-card" title="剧情简介" :bordered="false">
      <a-typography-paragraph :content="this.movie_content.summary"
                              :ellipsis="ellipsis ? { rows: 5, expandable: true, symbol: '展开全部' } : false"
                              class="summary-text"/>
    </a-card>

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
             <img :src="'http://localhost:8080/image?url=' + item.img" :alt="item.name" referrerpolicy="no-referrer" @error="handleImageError"/>
          </div>
          <a-card-meta :title="item.name">
             <template #description>
               <span class="cast-role">{{ item.role }}</span>
             </template>
          </a-card-meta>
        </a-card>
        <div v-for="i in (4 - (person_list.length % 4)) % 4" :key="'placeholder-'+i" class="cast-card-placeholder"></div>
      </div>
       <div class="pagination-wrapper" v-if="count1 > 4">
        <a-pagination show-less-items v-model:current="current1" show-quick-jumper :total="this.count1"
                      :default-page-size="4" :show-size-changer="false" :show-total="total => `共 ${total} 条`" @change="onChange1"/>
      </div>
    </a-card>

    <a-card class="section-card comments-card" :bordered="false">
      <template #title>
        <div class="section-title-line">
          <span>评论</span>
          <span class="section-count">{{ count2 }} 条</span>
        </div>
      </template>

      <div class="comment-input-section" :class="{ disabled: !disable() }">
        <a-avatar :src="UserImage" size="large" class="compose-avatar"/>
        <div class="compose-panel">
          <div class="compose-head">
            <span class="compose-title">{{ disable() ? '写下你的观影感受' : '登录后参与讨论' }}</span>
            <span class="compose-hint">{{ new_comment.length }}/280</span>
          </div>
          <a-textarea
              v-model:value="new_comment"
              :rows="4"
              :maxlength="280"
              placeholder="聊聊剧情、表演、镜头，或者这部电影给你的感觉..."
              class="comment-textarea"
              :disabled="!disable()"
          />
          <div class="compose-actions">
            <span class="comment-tip">保持友善，具体的观点会让推荐更懂你。</span>
            <a-button v-if="disable()" type="primary" html-type="submit" class="comment-submit" :loading="submitting" @click="submitComment">
              发布评论
            </a-button>
            <a-button v-else disabled class="comment-login">登录以发表评论</a-button>
          </div>
        </div>
      </div>

      <a-list
          class="comment-list"
          item-layout="horizontal"
          :data-source="comment_list"
          :loading="loading"
          :locale="{ emptyText: '暂无评论，来写第一条吧' }"
      >
        <template #renderItem="{ item }">
          <a-list-item class="comment-list-item">
            <div class="comment-card">
              <a-avatar :src="item.avatar || UserImage" size="large" class="comment-avatar"/>
              <div class="comment-body">
                <div class="comment-meta">
                  <span class="comment-author">{{ item.nickname || '匿名用户' }}</span>
                  <span class="comment-badge">观影评论</span>
                </div>
                <div class="comment-text">{{ item.comment }}</div>
              </div>
            </div>
          </a-list-item>
        </template>
      </a-list>
      
      <div class="pagination-wrapper" v-if="count2 > 8">
        <a-pagination show-less-items v-model:current="current2" show-quick-jumper :total="this.count2"
                      :default-page-size="8" :show-size-changer="false" :show-total="total => `共 ${total} 条`" @change="onChange2"/>
      </div>
    </a-card>
  </div>
</template>

<script>
import axios from "axios";
import router from "@/router/router";
import defaultPoster from '@/assets/default_movie_poster.svg';
import { message } from "ant-design-vue";

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
    handleImageError(e) {
      const target = e.target;
      if (target.src !== defaultPoster) {
        target.src = defaultPoster;
      }
    },
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
    formatGenre(value) {
      return (value || '').replace(/[\s,，、]+$/g, '');
    },
    formatList(value) {
      return (value || '').replace(/[\s,，、]+$/g, '');
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
      const comment = this.new_comment.trim();
      if (!comment) {
        message.warning('请先写下评论');
        return;
      }
      this.submitting = true;
      try {
        await axios.post('http://localhost:8080/comment/append', {
          userID: sessionStorage.getItem('username'),
          nickname: sessionStorage.getItem('nickname'),
          comment,
          movieID: this.movie_id,
        });
        this.new_comment = "";
        this.current2 = 1;
        this.offset2 = 0;
        message.success('评论已发布');
        await this.fetchData2();
      } catch (error) {
        message.error('评论发布失败，请稍后再试');
      } finally {
        this.submitting = false;
      }
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
import UserImage from '@/assets/meow.jpg';
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 8px 0 32px;
  background: transparent;
  text-align: left;
}

.movie-hero-card, .section-card {
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
      linear-gradient(135deg, rgba(21, 25, 31, 0.05), rgba(196, 59, 69, 0.04)),
      #fff;
}

.poster-wrapper {
  flex-shrink: 0;
  width: min(260px, 32vw);
  aspect-ratio: 2 / 3;
  overflow: hidden;
  border-radius: var(--movie-radius);
  background: #e8edf2;
  box-shadow: 0 18px 36px rgba(18, 24, 33, 0.16);
}

.movie-poster {
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
  color: var(--movie-accent);
  background: rgba(196, 59, 69, 0.08);
  border: 1px solid rgba(196, 59, 69, 0.16);
  border-radius: var(--movie-radius);
  font-size: 12px;
  font-weight: 650;
}

.movie-title {
  margin-bottom: 14px;
  font-weight: 700;
  color: var(--movie-ink);
  letter-spacing: 0;
  line-height: 1.16;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 22px;
}

.soft-pill {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 2px 10px;
  color: var(--movie-muted);
  background: rgba(101, 112, 124, 0.08);
  border: 1px solid rgba(101, 112, 124, 0.15);
  border-radius: var(--movie-radius);
  font-size: 12px;
  font-weight: 600;
}

.rating-panel {
  width: min(520px, 100%);
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  box-shadow: 0 10px 24px rgba(18, 24, 33, 0.06);
}

.rating-label {
  color: var(--movie-muted);
  font-size: 12px;
  font-weight: 650;
  margin-bottom: 6px;
}

.rating-line {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.rating-divider {
  width: 1px;
  height: 44px;
  background: var(--movie-line);
}

.rating-unit {
  color: var(--movie-muted);
  font-size: 13px;
}

.popular-value {
  color: var(--movie-ink);
  font-size: 24px;
  font-weight: 750;
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
  color: var(--movie-muted);
  width: 80px;
  flex-shrink: 0;
}

.info-value {
  color: var(--movie-ink);
}

.movie-tag {
  color: var(--movie-accent);
  background: rgba(196, 59, 69, 0.08);
  border-color: rgba(196, 59, 69, 0.2);
  border-radius: var(--movie-radius);
  margin-inline-end: 0;
}

.rating-value {
  font-size: 36px;
  font-weight: 800;
  color: var(--movie-gold);
}

.summary-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--movie-ink);
  margin-bottom: 0;
}

.cast-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.cast-card {
  border-radius: var(--movie-radius);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
  border: 1px solid var(--movie-line);
  overflow: hidden;
}

.cast-card:hover {
  transform: translateY(-4px);
  border-color: rgba(196, 59, 69, 0.32);
  box-shadow: var(--movie-shadow-md);
}

:deep(.cast-card .ant-card-body) {
  min-height: 82px;
  padding: 14px 16px 16px;
}

:deep(.cast-card .ant-card-meta-title) {
  color: var(--movie-ink);
  font-weight: 650;
  margin-bottom: 6px !important;
  white-space: normal;
  line-height: 1.35;
}

.cast-image-wrapper {
  aspect-ratio: 2 / 3;
  overflow: hidden;
  background: #e8edf2;
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
  color: var(--movie-muted);
  line-height: 1.45;
}

.cast-card-placeholder {
  display: none;
}

.comments-card :deep(.ant-card-body) {
  padding-top: 22px;
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

.comment-input-section {
  display: flex;
  gap: 16px;
  margin-bottom: 26px;
  padding: 18px;
  background: var(--movie-surface-soft);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
}

.comment-input-section.disabled {
  opacity: 0.82;
}

.compose-avatar {
  flex: 0 0 auto;
  box-shadow: 0 6px 14px rgba(18, 24, 33, 0.08);
}

.compose-panel {
  flex: 1;
  min-width: 0;
}

.compose-head,
.compose-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.compose-head {
  margin-bottom: 10px;
}

.compose-title {
  color: var(--movie-ink);
  font-size: 14px;
  font-weight: 650;
}

.compose-hint,
.comment-tip {
  color: var(--movie-muted);
  font-size: 12px;
}

.comment-textarea {
  border-radius: var(--movie-radius);
  resize: vertical;
}

.comment-textarea:focus,
:deep(.comment-textarea:focus) {
  border-color: var(--movie-accent);
  box-shadow: 0 0 0 3px rgba(196, 59, 69, 0.1);
}

.compose-actions {
  margin-top: 12px;
}

.comment-submit {
  background: var(--movie-accent);
  border-color: var(--movie-accent);
  min-width: 96px;
}

.comment-submit:hover {
  background: var(--movie-accent-dark) !important;
  border-color: var(--movie-accent-dark) !important;
}

.comment-login {
  min-width: 128px;
}

.comment-list :deep(.ant-list-empty-text) {
  padding: 36px 0;
  color: var(--movie-muted);
}

.comment-list-item {
  padding: 0 !important;
  border-bottom: 0 !important;
}

.comment-card {
  width: 100%;
  display: flex;
  gap: 14px;
  padding: 18px 0;
  border-top: 1px solid var(--movie-line);
}

.comment-avatar {
  flex: 0 0 auto;
}

.comment-body {
  min-width: 0;
  flex: 1;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-author {
  color: var(--movie-ink);
  font-size: 14px;
  font-weight: 650;
}

.comment-badge {
  color: var(--movie-teal);
  background: rgba(21, 127, 131, 0.08);
  border: 1px solid rgba(21, 127, 131, 0.16);
  border-radius: var(--movie-radius);
  padding: 2px 8px;
  font-size: 12px;
}

.comment-text {
  font-size: 14px;
  color: var(--movie-ink);
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  padding-bottom: 12px;
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
    width: 180px;
    margin-bottom: 16px;
  }
  
  .info-wrapper {
    width: 100%;
    text-align: left;
  }

  .detail-kicker,
  .hero-tags,
  .rating-panel {
    margin-left: auto;
    margin-right: auto;
  }

  .movie-title {
    text-align: center;
  }
  
  .info-label {
    width: 72px;
  }
  
  .cast-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .comment-input-section,
  .comment-card {
    gap: 12px;
  }

  .compose-actions {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .cast-grid {
    gap: 16px;
  }

  .rating-panel {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .rating-divider {
    width: 100%;
    height: 1px;
  }

  .comment-input-section {
    padding: 14px;
  }
}
</style>
