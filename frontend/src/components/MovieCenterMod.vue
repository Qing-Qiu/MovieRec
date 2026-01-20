<template>
  <div class="movie-center-container">
    <div class="filter-section">
      <div class="filter-group">
        <span class="filter-label">类型：</span>
        <a-radio-group v-model:value="value1" button-style="solid" @change="onChange1()">
          <a-radio-button value="a">全部</a-radio-button>
          <a-radio-button value="b">动作</a-radio-button>
          <a-radio-button value="c">动画</a-radio-button>
          <a-radio-button value="d">喜剧</a-radio-button>
          <a-radio-button value="e">犯罪</a-radio-button>
          <a-radio-button value="f">科幻</a-radio-button>
          <a-radio-button value="g">历史</a-radio-button>
          <a-radio-button value="h">音乐</a-radio-button>
          <a-radio-button value="i">爱情</a-radio-button>
          <a-radio-button value="j">悬疑</a-radio-button>
          <a-radio-button value="k">惊悚</a-radio-button>
          <a-radio-button value="l">其它</a-radio-button>
        </a-radio-group>
      </div>
      
      <div class="filter-group">
        <span class="filter-label">年份：</span>
        <a-radio-group v-model:value="value2" button-style="solid" @change="onChange1()">
          <a-radio-button value="a">全部</a-radio-button>
          <a-radio-button value="2015">2015</a-radio-button>
          <a-radio-button value="2014">2014</a-radio-button>
          <a-radio-button value="2013">2013</a-radio-button>
          <a-radio-button value="2012">2012</a-radio-button>
          <a-radio-button value="2011">2011</a-radio-button>
          <a-radio-button value="2010">2010</a-radio-button>
          <a-radio-button value="2009">2009</a-radio-button>
          <a-radio-button value="2008">2008</a-radio-button>
          <a-radio-button value="2007">2007</a-radio-button>
          <a-radio-button value="2006">2006</a-radio-button>
          <a-radio-button value="l">更早</a-radio-button>
        </a-radio-group>
      </div>

      <div class="filter-group">
        <span class="filter-label">地区：</span>
        <a-radio-group v-model:value="value3" button-style="solid" @change="onChange1()">
          <a-radio-button value="a">全部</a-radio-button>
          <a-radio-button value="b">内地</a-radio-button>
          <a-radio-button value="c">香港</a-radio-button>
          <a-radio-button value="d">台湾</a-radio-button>
          <a-radio-button value="e">美国</a-radio-button>
          <a-radio-button value="f">韩国</a-radio-button>
          <a-radio-button value="g">日本</a-radio-button>
          <a-radio-button value="h">俄罗斯</a-radio-button>
          <a-radio-button value="i">印度</a-radio-button>
          <a-radio-button value="j">泰国</a-radio-button>
          <a-radio-button value="k">英国</a-radio-button>
          <a-radio-button value="l">其它</a-radio-button>
        </a-radio-group>
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

    onChange1() {
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
}

.filter-group {
  display: flex;
  align-items: center;
}

.filter-label {
  font-weight: 500;
  margin-right: 12px;
  min-width: 50px;
  color: #555;
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
