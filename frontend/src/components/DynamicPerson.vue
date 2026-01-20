<template>
  <a-card class="image-card2">
    <a-row>
      <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
      <a-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8" :xxl="8">
        <img :src="this.person_content.img" :alt="this.person_content.name" referrerpolicy="no-referrer"
             style="display: block;width: auto"
        />
      </a-col>
      <a-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8" :xxl="8">
        <a-typography>
          <a-typography-title style="text-align: left">{{ this.person_content.name }}</a-typography-title>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>性别：</a-typography-text>
            {{ this.person_content.sex }}
          </a-typography-paragraph>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>生日：</a-typography-text>
            {{ this.person_content.birthday }}
          </a-typography-paragraph>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>出生地：</a-typography-text>
            {{ this.person_content.birthplace }}
          </a-typography-paragraph>
        </a-typography>
      </a-col>
      <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
    </a-row>
  </a-card>
  <a-row>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
    <a-col :xs="16" :sm="16" :md="16" :lg="16" :xl="16" :xxl="16">
      <a-card>
        <a-typography-paragraph style="text-align: left">
          <a-typography-text strong>简介：</a-typography-text>
          <a-typography-paragraph :content=" this.person_content.summary "
                                  :ellipsis="ellipsis ? { rows: 5, expandable: true, symbol: '展开全部' } : false"/>
        </a-typography-paragraph>
      </a-card>
    </a-col>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
  </a-row>
  <a-row>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
    <a-col :xs="16" :sm="16" :md="16" :lg="16" :xl="16" :xxl="16">
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
            <a-card-meta :title="item.name" :description="item.genre"/>
          </div>
        </a-card>
        <div
            v-for="(item,itemIndex) in new Array((4 - movie_list.length % 4) % 4)"
            :key="itemIndex"
            class="image-card"
        >
        </div>
      </div>
      <div>
        <a-pagination show-less-items v-model:current="current1" show-quick-jumper :total="this.count1"
                      :default-page-size="4" :show-size-changer="false" @change="onChange1"/>
      </div>
    </a-col>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
  </a-row>
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