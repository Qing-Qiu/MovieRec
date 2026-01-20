<template>
  <div class="movie-center-container">
    <div class="filter-section">
      <!-- 类型 -->
      <div class="filter-group">
        <span class="filter-label">类型：</span>
        <div class="tags-wrapper">
          <span 
            v-for="item in typeOptions" 
            :key="item.value"
            class="tag-item" 
            :class="{ active: value1 === item.value }"
            @click="selectType(item.value)"
          >
            {{ item.label }}
          </span>
        </div>
      </div>
      
      <!-- 年份 -->
      <div class="filter-group">
        <span class="filter-label">年份：</span>
        <div class="tags-wrapper">
          <span 
            v-for="item in yearOptions" 
            :key="item.value"
            class="tag-item" 
            :class="{ active: value2 === item.value }"
            @click="selectYear(item.value)"
          >
            {{ item.label }}
          </span>
        </div>
      </div>

      <!-- 地区 -->
      <div class="filter-group">
        <span class="filter-label">地区：</span>
        <div class="tags-wrapper">
          <span 
            v-for="item in regionOptions" 
            :key="item.value"
            class="tag-item" 
            :class="{ active: value3 === item.value }"
            @click="selectRegion(item.value)"
          >
            {{ item.label }}
          </span>
        </div>
      </div>
    </div>

    <a-divider style="margin: 24px 0" />

    <div class="movie-list-section">
      <a-row :gutter="[24, 24]">
        <!-- Loading State -->
        <template v-if="loading">
          <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="i in 8" :key="i">
            <SkeletonCard />
          </a-col>
        </template>

        <!-- Data State -->
        <template v-else>
          <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(item, itemIndex) in movie_list" :key="itemIndex">
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
      
      <div v-if="!loading && movie_list.length === 0" class="empty-state">
        <a-empty description="暂无相关电影" />
      </div>

      <div class="pagination-wrapper" v-if="count > 0 && !loading">
         <a-pagination 
            v-model:current="current1" 
            show-quick-jumper 
            :total="count"
            :default-page-size="8" 
            :show-size-changer="false" 
            :show-total="total => `共 ${total} 条`"
            @change="onChange"
         />
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineComponent, h } from 'vue';
import SkeletonCard from './SkeletonCard.vue';

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
      value1: 'a',
      value2: 'a',
      value3: 'a',
      tag1: '',
      tag2: '',
      tag3: '',
      current1: 1,
      movie_id: 0,
      movie_content: [],
      movie_list: [],
      limit: 0,
      offset: 0,
      count: 0,
      loading: false,
      
      // Options data
      typeOptions: [
        { label: '全部', value: 'a' },
        { label: '动作', value: 'b' },
        { label: '动画', value: 'c' },
        { label: '喜剧', value: 'd' },
        { label: '犯罪', value: 'e' },
        { label: '科幻', value: 'f' },
        { label: '历史', value: 'g' },
        { label: '音乐', value: 'h' },
        { label: '爱情', value: 'i' },
        { label: '悬疑', value: 'j' },
        { label: '惊悚', value: 'k' },
        { label: '其它', value: 'l' }
      ],
      yearOptions: [
        { label: '全部', value: 'a' },
        { label: '2015', value: '2015' },
        { label: '2014', value: '2014' },
        { label: '2013', value: '2013' },
        { label: '2012', value: '2012' },
        { label: '2011', value: '2011' },
        { label: '2010', value: '2010' },
        { label: '2009', value: '2009' },
        { label: '2008', value: '2008' },
        { label: '2007', value: '2007' },
        { label: '2006', value: '2006' },
        { label: '更早', value: 'l' }
      ],
      regionOptions: [
        { label: '全部', value: 'a' },
        { label: '内地', value: 'b' },
        { label: '香港', value: 'c' },
        { label: '台湾', value: 'd' },
        { label: '美国', value: 'e' },
        { label: '韩国', value: 'f' },
        { label: '日本', value: 'g' },
        { label: '俄罗斯', value: 'h' },
        { label: '印度', value: 'i' },
        { label: '泰国', value: 'j' },
        { label: '英国', value: 'k' },
        { label: '其它', value: 'l' }
      ]
    }
  },
  beforeMount() {
    this.fetchData();
  },
  methods: {
    async fetchData() {
      try {
        this.loading = true;
        // Simulate loading for better UX
        // await new Promise(r => setTimeout(r, 600));

        this.tag1 = '';
        switch (this.value1) {
          case "a": this.tag1 = '全部'; break;
          case "b": this.tag1 = '动作'; break;
          case "c": this.tag1 = '动画'; break;
          case "d": this.tag1 = '喜剧'; break;
          case "e": this.tag1 = '犯罪'; break;
          case "f": this.tag1 = '科幻'; break;
          case "g": this.tag1 = '历史'; break;
          case "h": this.tag1 = '音乐'; break;
          case "i": this.tag1 = '爱情'; break;
          case "j": this.tag1 = '悬疑'; break;
          case "k": this.tag1 = '惊悚'; break;
          case "l": this.tag1 = '其它'; break;
        }
        
        this.tag2 = '';
        switch (this.value2) {
          case "a": this.tag2 = '全部'; break;
          case "l": this.tag2 = '其它'; break;
          default: this.tag2 = this.value2; break;
        }

        this.tag3 = '';
        switch (this.value3) {
          case "a": this.tag3 = '全部'; break;
          case "b": this.tag3 = '大陆'; break;
          case "c": this.tag3 = '香港'; break;
          case "d": this.tag3 = '台湾'; break;
          case "e": this.tag3 = '美国'; break;
          case "f": this.tag3 = '韩国'; break;
          case "g": this.tag3 = '日本'; break;
          case "h": this.tag3 = '俄罗斯'; break;
          case "i": this.tag3 = '印度'; break;
          case "j": this.tag3 = '泰国'; break;
          case "k": this.tag3 = '英国'; break;
          case "l": this.tag3 = '其它'; break;
        }

        // Parallel requests
        const [countRes, listRes] = await Promise.all([
             axios.post("http://localhost:8080/movie/count", {tag1: this.tag1, tag2: this.tag2, tag3: this.tag3}),
             axios.post("http://localhost:8080/movie/classify", {
                 tag1: this.tag1, 
                 tag2: this.tag2, 
                 tag3: this.tag3, 
                 limit: 8, 
                 offset: (this.current1 - 1) * 8
             })
        ]);

        if (countRes.data) {
             this.count = countRes.data;
        }

        this.movie_list = [];
        if (listRes.data) {
            for (let i = 0; i < Math.min(8, listRes.data.length); i++) {
                let desc = listRes.data[i].genre || '';
                if (desc.endsWith(',')) desc = desc.slice(0, -1);

                this.movie_list.push({
                  title: listRes.data[i].name,
                  id: listRes.data[i].movieID,
                  image: listRes.data[i].img,
                  description: desc
                });
            }
        }
      } catch (error) {
        console.error("Failed to fetch movie data", error);
      } finally {
        this.loading = false;
      }
    },
    
    // New handling methods
    selectType(val) {
      if (this.value1 === val) return;
      this.value1 = val;
      this.current1 = 1;
      this.fetchData();
    },
    selectYear(val) {
      if (this.value2 === val) return;
      this.value2 = val;
      this.current1 = 1;
      this.fetchData();
    },
    selectRegion(val) {
      if (this.value3 === val) return;
      this.value3 = val;
      this.current1 = 1;
      this.fetchData();
    },

    onChange() {
      // this.limit = 8;
      // this.offset = (this.current1 - 1) * 8;
      this.fetchData();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    async watchMovieDetail(id) {
        await router.push("/movie/" + id);
    }
  }
}
</script>

<style scoped>
.movie-center-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: #fff;
  min-height: 80vh;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #fdfdfd;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.filter-group {
  display: flex;
  align-items: flex-start;
}

.filter-label {
  font-weight: 600;
  margin-right: 16px;
  min-width: 60px;
  color: #666;
  margin-top: 6px; /* Align with tags */
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 5px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #555;
  transition: all 0.2s ease;
  user-select: none;
  background: transparent;
}

.tag-item:hover {
  text-decoration: none;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.08);
}

.tag-item.active {
  background: #1890ff;
  color: #fff;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.25);
}

.movie-list-section {
  min-height: 400px;
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
  height: 360px;
  overflow: hidden;
  position: relative;
}

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

.pagination-wrapper {
  margin-top: 32px;
  text-align: center;
  padding-bottom: 24px;
}

.empty-state {
 padding: 60px 0;
 text-align: center;
}
</style>
