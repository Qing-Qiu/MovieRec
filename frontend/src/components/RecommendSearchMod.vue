<template>
  <a-row>
    <a-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8" :xxl="8"></a-col>
    <a-col :xs="12" :sm="12" :md="12" :lg="12" :xl="12" :xxl="12">
      <a-form
          :model="formState"
          name="basic"
          :label-col="{ span: 8 }"
          :wrapper-col="{ span: 16 }"
          autocomplete="off"
      >
        <a-form-item
            label=""
            name="search"
            :rules="[]"
        >
          <a-input-search
              v-model:value="formState.search"
              placeholder="请输入搜索内容"
              size="large"
              @search="submitForm"
          />
        </a-form-item>
      </a-form>
    </a-col>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
  </a-row>
  <template v-if="!search">
    <h2>猜你喜欢</h2>
    <template v-if="loading">
      <a-button disabled>换一批</a-button>
      <a-list :loading="loading">
      </a-list>
    </template>
    <template v-if="!loading">
      <a-button @click="refresh()">换一批</a-button>
      <a-row>
        <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
        <a-col :xs="16" :sm="16" :md="16" :lg="16" :xl="16" :xxl="16">
          <div class="image-grid">
            <a-card
                v-for="(item, itemIndex) in recommendedImages"
                :key="itemIndex"
                class="image-card"
                hoverable
                @click="watchMovieDetail(item.id)"
            >
              <div class="card-content" v-if="recommendedImages.length">
                <img :src="item.image" :alt="item.title" referrerpolicy="no-referrer"/>
                <a-card-meta :title="item.title" :description="item.description"/>
              </div>
            </a-card>
          </div>
        </a-col>
        <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
      </a-row>
    </template>
  </template>
  <template v-else-if="search">
    <a-button @click="backward()">
      <ArrowLeftOutlined/>
    </a-button>
    <a-row>
      <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
      <a-col :xs="16" :sm="16" :md="16" :lg="16" :xl="16" :xxl="16">
        <div v-if="person_list!==null">
          共查询到{{ count1 }}位影人。
          <div class="image-grid">
            <a-card
                v-for="(item, itemIndex) in person_list"
                :key="itemIndex"
                class="image-card"
                hoverable
                @click="watchPersonDetail(item.personID)"
            >
              <div class="card-content" v-if="this.person_list.length">
                <img :src="item.img" :alt="item.name" referrerpolicy="no-referrer"/>
                <a-card-meta :title="item.name" :description="item.role"/>
              </div>
            </a-card>
            <div
                v-for="(item,itemIndex) in new Array((4 - person_list.length % 4) % 4)"
                :key="itemIndex"
                class="image-card"
            >
            </div>
          </div>
          <div v-if="count1!==0">
            <a-pagination show-less-items v-model:current="current1" show-quick-jumper :total="this.count1"
                          :default-page-size="4" :show-size-changer="false" @change="onChange1"/>
          </div>
        </div>
      </a-col>
      <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
    </a-row>
    <a-row>
      <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
      <a-col :xs="16" :sm="16" :md="16" :lg="16" :xl="16" :xxl="16">
        <div v-if="movie_list!==null">
          为您搜索到{{ count2 }}部电影。
          <div class="image-grid">
            <a-card
                v-for="(item, itemIndex) in movie_list"
                :key="itemIndex"
                class="image-card"
                hoverable
                @click="watchMovieDetail(item.movieID)"
            >
              <div class="card-content" v-if="this.movie_list.length">
                <img :src="item.img" :alt="item.name" referrerpolicy="no-referrer"/>
                <a-card-meta :title="item.name" :description="item.role"/>
              </div>
            </a-card>
            <div
                v-for="(item,itemIndex) in new Array((4 - movie_list.length % 4) % 4)"
                :key="itemIndex"
                class="image-card"
            >
            </div>
          </div>
          <div v-if="count2!==0">
            <a-pagination show-less-items v-model:current="current2" show-quick-jumper :total="this.count2"
                          :default-page-size="8" :show-size-changer="false" @change="onChange2"/>
          </div>
        </div>
      </a-col>
      <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
    </a-row>
  </template>
</template>

<script setup>
import HomePage from "@/views/HomePage";
import {ArrowLeftOutlined} from "@ant-design/icons-vue";
</script>

<script>
import axios from "axios";
import router from "@/router/router";

export default {
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
        const response = await axios.post('http://localhost:8080/movie/recommend',
            {nickname: this.nickname}).then(
            response => {
              console.log(response.data);
              for (let i = 0; i < 8; i++) {
                this.recommendedImages.push({
                  title: response.data[i].name,
                  id: response.data[i].movieID,
                  image: response.data[i].img,
                  description: response.data[i].genre
                });
              }
              for (let i = 0; i < 8; i++) {
                if (this.recommendedImages[i].description !== null) {
                  if (this.recommendedImages[i].description.endsWith(',')) {
                    this.recommendedImages[i].description =
                        this.recommendedImages[i].description.slice(0, -1);
                  }
                }
              }
              console.log(this.recommendedImages);
            },
            error => {
            }
        )
        this.loading = false;
      } catch (error) {
      }
    },

    refresh() {
      this.recommendedImages.splice(0, 8);
      this.fetchData();
    },

    backward() {
      this.search = false;
      this.current1 = 1;
      this.offset1 = 0;
      this.current2 = 1;
      this.offset2 = 0;
    },

    async submitForm() {
      this.search = true;
      this.current1 = 1;
      this.current2 = 1;
      this.offset1 = 0;
      this.offset2 = 0;
      try {
        const response = await axios.post('http://localhost:8080/person/count3',
            {keyword: this.formState.search}).then(
            response => {
              this.count1 = response.data;
            }, error => {
            }
        )
      } catch (error) {
      }
      try {
        const response = await axios.post('http://localhost:8080/person/search',
            {keyword: this.formState.search, limit: this.limit1, offset: this.offset1}).then(
            response => {
              this.person_list = response.data;
            }, error => {
            }
        )
      } catch (error) {
      }
      try {
        const response = await axios.post('http://localhost:8080/movie/count2',
            {keyword: this.formState.search}).then(
            response => {
              this.count2 = response.data;
            }, error => {
            }
        )
      } catch (error) {
      }
      try {
        const response = await axios.post('http://localhost:8080/movie/search',
            {keyword: this.formState.search, limit: this.limit2, offset: this.offset2}).then(
            response => {
              this.movie_list = response.data;
            }, error => {
            }
        )
      } catch (error) {
      }
    },

    async fetchData1() {
      try {
        const response = await axios.post('http://localhost:8080/person/count3',
            {keyword: this.formState.search}).then(
            response => {
              this.count1 = response.data;
            }, error => {
            }
        )
      } catch (error) {
      }
      try {
        const response = await axios.post('http://localhost:8080/person/search',
            {keyword: this.formState.search, limit: this.limit1, offset: this.offset1}).then(
            response => {
              this.person_list = response.data;
            }, error => {
            }
        )
      } catch (error) {
      }
    },

    async fetchData2() {
      try {
        const response = await axios.post('http://localhost:8080/movie/count2',
            {keyword: this.formState.search}).then(
            response => {
              this.count2 = response.data;
            }, error => {
            }
        )
      } catch (error) {
      }
      try {
        const response = await axios.post('http://localhost:8080/movie/search',
            {keyword: this.formState.search, limit: this.limit2, offset: this.offset2}).then(
            response => {
              this.movie_list = response.data;
            }, error => {
            }
        )
      } catch (error) {
      }
    },
  },
}
</script>
<style scoped>
.image-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.image-card {
  width: 20%; /* Adjust this to control the card width */
  margin: 20px 20px 20px 25px;
}

.card-content {
  display: flex;
  flex-direction: column;
}

img {
  max-width: 100%;
}
</style>
