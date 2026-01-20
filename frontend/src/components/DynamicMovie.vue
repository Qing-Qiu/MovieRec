<template>
  <a-card>
    <a-row>
      <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
      <a-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8" :xxl="8">
        <img :src="this.movie_content.img" :alt="this.movie_content.name" referrerpolicy="no-referrer"
             style="display: block;width: auto"
        />
      </a-col>
      <a-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8" :xxl="8">
        <a-typography>
          <a-typography-title style="text-align: left">{{ this.movie_content.name }}</a-typography-title>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>导演：</a-typography-text>
            {{ this.movie_content.director }}
          </a-typography-paragraph>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>主演：</a-typography-text>
            {{ this.movie_content.actor }}
          </a-typography-paragraph>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>类型：</a-typography-text>
            {{ this.movie_content.tag }}
          </a-typography-paragraph>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>评分：</a-typography-text>
            {{ this.movie_content.rate }}
          </a-typography-paragraph>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>评价人数：</a-typography-text>
            {{ this.movie_content.popular }}
          </a-typography-paragraph>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>年份：</a-typography-text>
            {{ this.movie_content.year }}
          </a-typography-paragraph>
          <a-typography-paragraph style="text-align: left">
            <a-typography-text strong>制片国家/地区：</a-typography-text>
            {{ this.movie_content.region }}
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
          <a-typography-text strong>剧情简介：</a-typography-text>
          <a-typography-paragraph :content="this.movie_content.summary"
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
      <div>
        <a-pagination show-less-items v-model:current="current1" show-quick-jumper :total="this.count1"
                      :default-page-size="4" :show-size-changer="false" @change="onChange1"/>
      </div>
    </a-col>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
  </a-row>
  <a-row>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
    <a-col :xs="16" :sm="16" :md="16" :lg="16" :xl="16" :xxl="16">
      <a-comment>
        <template #avatar>
          <a-avatar :src="UserImage"/>
        </template>
        <template #content>
          <a-form-item>
            <a-textarea v-model:value="new_comment" :rows="4"/>
          </a-form-item>
          <a-form-item v-if="disable()">
            <a-button html-type="submit" :loading="submitting"
                      @click="submitComment">
              添加评论
            </a-button>
          </a-form-item>
          <a-form-item v-if="!disable()">
            <a-button disabled>登录以发表评论</a-button>
          </a-form-item>
        </template>
      </a-comment>
      <a-list
          class="comment-list"
          item-layout="horizontal"
          :data-source="comment_list"
          :loading="loading"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <a-comment :author="item.nickname" :avatar="item.avatar">
              <template #content>
                <a-typography-paragraph style="text-align: left">
                  {{ item.comment }}
                </a-typography-paragraph>
              </template>
            </a-comment>
          </a-list-item>
        </template>
      </a-list>
    </a-col>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
  </a-row>
  <div>
    <a-pagination show-less-items v-model:current="current2" show-quick-jumper :total="this.count2"
                  :default-page-size="8" :show-size-changer="false" @change="onChange2"/>
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