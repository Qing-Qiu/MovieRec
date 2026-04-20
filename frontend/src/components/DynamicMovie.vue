<template>
  <div class="page-container">
    <a-card class="movie-hero-card" :bordered="false">
      <div class="hero-content">
        <div class="poster-wrapper">
          <img :src="imageProxyUrl(this.movie_content.img)" :data-direct-src="this.movie_content.img || ''" :alt="this.movie_content.name" referrerpolicy="no-referrer" class="movie-poster" @error="handleImageError"/>
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

          <div class="hero-summary">
            <div class="summary-heading">剧情简介</div>
            <div class="summary-panel">
              <div class="summary-rule"></div>
              <a-typography-paragraph :content="this.movie_content.summary || '暂无剧情简介。'"
                                      :ellipsis="ellipsis ? { rows: 4, expandable: true, symbol: '展开全部' } : false"
                                      class="summary-text"/>
            </div>
          </div>
        </div>
      </div>
    </a-card>

    <a-card class="section-card paged-section" :bordered="false">
      <template #title>
        <div class="paged-title-line">
          <span>演职员表</span>
          <span class="page-center-status" v-if="castTotalPages > 1">
            <button
                type="button"
                class="page-nav-button"
                :disabled="current1 <= 1"
                aria-label="上一页"
                @click="goCastPage(current1 - 1)"
            >
              ‹
            </button>
            <span class="page-index">
              <strong>{{ current1 }}</strong>
              <span>/ {{ castTotalPages }}</span>
            </span>
            <button
                type="button"
                class="page-nav-button"
                :disabled="current1 >= castTotalPages"
                aria-label="下一页"
                @click="goCastPage(current1 + 1)"
            >
              ›
            </button>
          </span>
          <span class="section-count">共 {{ count1 }} 位</span>
        </div>
      </template>

      <div class="paged-stage">
        <div class="cast-grid" v-if="person_list.length">
          <button
              v-for="(item, itemIndex) in person_list"
              :key="itemIndex"
              type="button"
              class="cast-tile"
              @click="watchPersonDetail(item.personID)"
          >
            <span class="cast-photo">
               <img :src="imageProxyUrl(item.img)" :data-direct-src="item.img || ''" :alt="item.name" referrerpolicy="no-referrer" @error="handleImageError"/>
            </span>
            <span class="cast-meta">
              <span class="cast-role">{{ item.role || '演职员' }}</span>
              <span class="cast-name">{{ item.name }}</span>
            </span>
          </button>
        </div>
        <a-empty v-else description="暂无演职员资料" class="empty-state"/>

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

      <div class="comment-load-area" ref="commentLoadTrigger" v-if="count2 > 0 || loading">
        <a-spin v-if="loading || loadingMore" size="small"/>
        <span v-else-if="hasMoreComments" class="comment-load-hint">下滑加载更多评论</span>
        <span v-else class="comment-load-hint">已显示全部评论</span>
      </div>
    </a-card>
  </div>
</template>

<script>
import axios from "axios";
import router from "@/router/router";
import defaultPoster from '@/assets/default_movie_poster.svg';
import { message } from "ant-design-vue";
import { markRaw } from "vue";

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
      loadingMore: false,
      commentObserver: null,
      commentObservedTarget: null,
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
  mounted() {
    this.initCommentObserver();
  },
  beforeUnmount() {
    if (this.commentObserver) {
      this.commentObserver.disconnect();
      this.commentObserver = null;
    }
    this.commentObservedTarget = null;
    window.removeEventListener('scroll', this.handleCommentScroll);
  },
  computed: {
    castTotalPages() {
      return Math.max(1, Math.ceil((Number(this.count1) || 0) / this.limit1));
    },
    hasMoreComments() {
      return this.comment_list.length < (Number(this.count2) || 0);
    },
  },
  methods: {
    imageProxyUrl(src) {
      return src ? `http://localhost:8080/image?url=${encodeURIComponent(src)}` : defaultPoster;
    },
    handleImageError(e) {
      const target = e.target;
      const directSrc = target.dataset.directSrc;
      if (target.dataset.fallbackStep !== 'direct' && directSrc) {
        target.dataset.fallbackStep = 'direct';
        target.src = directSrc;
        return;
      }
      if (target.src !== defaultPoster) {
        target.dataset.fallbackStep = 'default';
        target.src = defaultPoster;
      }
    },
    onChange1(page = this.current1) {
      this.goCastPage(page);
    },
    goCastPage(page) {
      const nextPage = Math.min(Math.max(page, 1), this.castTotalPages);
      if (nextPage === this.current1) {
        return;
      }
      this.current1 = nextPage;
      this.offset1 = (this.current1 - 1) * this.limit1;
      this.fetchData1();
    },
    onChange2() {
      this.loadMoreComments();
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
    initCommentObserver() {
      this.$nextTick(() => {
        const target = this.$refs.commentLoadTrigger;
        if (!target) {
          return;
        }
        if (this.commentObserver && this.commentObservedTarget === target) {
          return;
        }
        if (this.commentObserver) {
          this.commentObserver.disconnect();
          this.commentObserver = null;
        }
        window.removeEventListener('scroll', this.handleCommentScroll);
        this.commentObservedTarget = markRaw(target);
        if ('IntersectionObserver' in window) {
          this.commentObserver = markRaw(new IntersectionObserver((entries) => {
            if (entries[0] && entries[0].isIntersecting) {
              this.loadMoreComments();
            }
          }, {
            root: null,
            rootMargin: '160px',
            threshold: 0,
          }));
          this.commentObserver.observe(target);
          return;
        }
        window.addEventListener('scroll', this.handleCommentScroll, {passive: true});
        this.handleCommentScroll();
      });
    },
    handleCommentScroll() {
      const target = this.$refs.commentLoadTrigger;
      if (!target) {
        return;
      }
      const rect = target.getBoundingClientRect();
      if (rect.top <= window.innerHeight + 160) {
        this.loadMoreComments();
      }
    },
    async loadMoreComments() {
      await this.fetchData2({append: true});
    },
    async fetchData2({append = false} = {}) {
      if (append) {
        if (this.loading || this.loadingMore || !this.hasMoreComments) {
          return;
        }
        this.loadingMore = true;
      } else {
        this.loading = true;
        this.current2 = 1;
        this.offset2 = 0;
      }
      try {
        const countResponse = await axios.post('http://localhost:8080/comment/count',
            {id: this.movie_id});
        this.count2 = Number(countResponse.data) || 0;

        const nextOffset = append ? this.comment_list.length : 0;
        const listResponse = await axios.post('http://localhost:8080/comment/comment',
            {id: this.movie_id, limit: this.limit2, offset: nextOffset});
        const nextComments = Array.isArray(listResponse.data) ? listResponse.data : [];

        this.comment_list = append ? [...this.comment_list, ...nextComments] : nextComments;
        this.offset2 = this.comment_list.length;
        this.current2 = Math.max(1, Math.ceil(this.offset2 / this.limit2));
      } catch (error) {
      } finally {
        this.loading = false;
        this.loadingMore = false;
        if (this.count2 > 0) {
          this.initCommentObserver();
        }
      }
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

.hero-summary {
  margin-top: 20px;
}

.summary-heading {
  margin-bottom: 10px;
  color: var(--movie-ink);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0;
}

.summary-panel {
  display: grid;
  grid-template-columns: 4px minmax(0, 1fr);
  gap: 16px;
  padding: 16px 18px;
  background:
      linear-gradient(135deg, rgba(196, 59, 69, 0.06), rgba(21, 127, 131, 0.05)),
      rgba(255, 255, 255, 0.62);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
}

.summary-rule {
  width: 4px;
  min-height: 100%;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--movie-accent), var(--movie-gold));
}

.summary-text {
  font-size: 15px;
  line-height: 1.9;
  color: var(--movie-ink);
  margin-bottom: 0;
  letter-spacing: 0;
}

:deep(.summary-text .ant-typography-expand) {
  color: var(--movie-accent);
  font-weight: 650;
}

.cast-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.cast-tile {
  width: 100%;
  min-height: 124px;
  display: flex;
  align-items: center;
  gap: 14px;
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

.cast-tile:hover,
.cast-tile:focus-visible {
  transform: translateY(-2px);
  border-color: rgba(196, 59, 69, 0.32);
  background: #fff;
  box-shadow: var(--movie-shadow-sm);
  outline: none;
}

.cast-photo {
  width: 74px;
  height: 100px;
  flex: 0 0 74px;
  overflow: hidden;
  border-radius: var(--movie-radius);
  background: #e8edf2;
  box-shadow: 0 8px 18px rgba(18, 24, 33, 0.12);
}

.cast-photo img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.cast-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cast-name {
  color: var(--movie-ink);
  font-size: 15px;
  font-weight: 750;
  line-height: 1.35;
  word-break: break-word;
}

.cast-role {
  width: fit-content;
  max-width: 100%;
  padding: 3px 9px;
  color: var(--movie-teal);
  background: rgba(21, 127, 131, 0.08);
  border: 1px solid rgba(21, 127, 131, 0.16);
  border-radius: var(--movie-radius);
  font-size: 12px;
  font-weight: 650;
  line-height: 1.45;
  white-space: normal;
  word-break: break-word;
}

.empty-state {
  padding: 36px 0 24px;
}

:deep(.paged-section .ant-card-head-title) {
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
  align-items: center;
  gap: 6px;
  padding: 0;
  color: var(--movie-muted);
  background: transparent;
  border: 0;
  font-size: 13px;
  font-weight: 600;
}

.page-index {
  min-width: 54px;
  display: inline-flex;
  align-items: baseline;
  justify-content: center;
  gap: 5px;
}

.page-center-status strong {
  color: var(--movie-accent);
  font-size: 16px;
  font-weight: 800;
}

.page-nav-button {
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: rgba(101, 112, 124, 0.74);
  background: transparent;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  transition: color 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.page-nav-button:hover,
.page-nav-button:focus-visible {
  color: var(--movie-accent);
  background: rgba(196, 59, 69, 0.07);
  border-color: transparent;
  outline: none;
}

.page-nav-button:disabled {
  color: rgba(184, 192, 200, 0.68);
  background: transparent;
  border-color: transparent;
  cursor: not-allowed;
}

.paged-stage {
  position: relative;
  min-height: 124px;
  padding: 0;
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

.comment-load-area {
  min-height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8px;
  color: var(--movie-muted);
  font-size: 13px;
}

.comment-load-hint {
  padding: 5px 12px;
  color: var(--movie-muted);
  background: var(--movie-surface-soft);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
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
    padding: 0;
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

  .paged-stage {
    padding: 0;
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

  .cast-tile {
    min-height: 112px;
    padding: 10px;
  }

  .cast-photo {
    width: 64px;
    height: 88px;
    flex-basis: 64px;
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
