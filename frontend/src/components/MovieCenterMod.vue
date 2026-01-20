<template>
  <div>
    <a-radio-group v-model:value="value1" button-style="solid" @change="onChange1()">
      <a-radio-button value="a">类型</a-radio-button>
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
  <div :style="{ marginTop: '16px' }">
    <a-radio-group v-model:value="value2" button-style="solid" @change="onChange1()">
      <a-radio-button value="a">年份</a-radio-button>
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
      <a-radio-button value="l">更久之前</a-radio-button>
    </a-radio-group>
  </div>
  <div :style="{ marginTop: '16px'}">
    <a-radio-group v-model:value="value3" button-style="solid" @change="onChange1()">
      <a-radio-button value="a">国家与地区</a-radio-button>
      <a-radio-button value="b">内地</a-radio-button>
      <a-radio-button value="c">中国香港</a-radio-button>
      <a-radio-button value="d">中国台湾</a-radio-button>
      <a-radio-button value="e">美国</a-radio-button>
      <a-radio-button value="f">韩国</a-radio-button>
      <a-radio-button value="g">日本</a-radio-button>
      <a-radio-button value="h">俄罗斯</a-radio-button>
      <a-radio-button value="i">印度</a-radio-button>
      <a-radio-button value="j">泰国</a-radio-button>
      <a-radio-button value="k">英国</a-radio-button>
      <a-radio-button value="l">其他国家</a-radio-button>
    </a-radio-group>
  </div>
  <a-row>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
    <a-col :xs="16" :sm="16" :md="16" :lg="16" :xl="16" :xxl="16">
      <div class="image-grid">
        <a-card
            v-for="(item, itemIndex) in movie_list"
            :key="itemIndex"
            class="image-card"
            hoverable
            @click="watchMovieDetail(item.id)"
        >
          <div class="card-content" v-if="this.movie_list.length">
            <img :src="item.image" :alt="item.title" referrerpolicy="no-referrer"/>
            <a-card-meta :title="item.title" :description="item.description"/>
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
        <a-pagination show-less-items v-model:current="current1" show-quick-jumper :total="this.count"
                      :default-page-size="8" :show-size-changer="false" @change="onChange"/>
      </div>
    </a-col>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
  </a-row>
  <br/><br/>
</template>

<script setup>
import HomePage from "@/views/HomePage";
</script>

<script>
import axios from "axios";
import router from "@/router/router";

export default {
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
    }
  },
  beforeMount() {
    this.fetchData();
  },
  methods: {
    async fetchData() {
      try {
        this.tag1 = '';
        switch (this.value1) {
          case "a":
            this.tag1 = '全部';
            break;
          case "b":
            this.tag1 = '动作';
            break;
          case "c":
            this.tag1 = '动画';
            break;
          case "d":
            this.tag1 = '喜剧';
            break;
          case "e":
            this.tag1 = '犯罪';
            break;
          case "f":
            this.tag1 = '科幻';
            break;
          case "g":
            this.tag1 = '历史';
            break;
          case "h":
            this.tag1 = '音乐';
            break;
          case "i":
            this.tag1 = '爱情';
            break;
          case "j":
            this.tag1 = '悬疑';
            break;
          case "k":
            this.tag1 = '惊悚';
            break;
          case "l":
            this.tag1 = '其它';
            break;
        }
        this.tag2 = '';
        switch (this.value2) {
          case "a":
            this.tag2 = '全部';
            break;
          case "l":
            this.tag2 = '其它';
            break;
          default:
            this.tag2 = this.value2;
            break;
        }
        this.tag3 = '';
        switch (this.value3) {
          case "a":
            this.tag3 = '全部';
            break;
          case "b":
            this.tag3 = '大陆';
            break;
          case "c":
            this.tag3 = '香港';
            break;
          case "d":
            this.tag3 = '台湾';
            break;
          case "e":
            this.tag3 = '美国';
            break;
          case "f":
            this.tag3 = '韩国';
            break;
          case "g":
            this.tag3 = '日本';
            break;
          case "h":
            this.tag3 = '俄罗斯';
            break;
          case "i":
            this.tag3 = '印度';
            break;
          case "j":
            this.tag3 = '泰国';
            break;
          case "k":
            this.tag3 = '英国';
            break;
          case "l":
            this.tag3 = '其它';
            break;
        }
        const response1 = await axios.post("http://localhost:8080/movie/count",
            {tag1: this.tag1, tag2: this.tag2, tag3: this.tag3}).then(
            response => {
              this.count = response.data;
              console.log(this.count);
            }, error => {

            }
        )
        this.limit = 8;
        this.offset = (this.current1 - 1) * 8;
        this.movie_list = [];
        const response = await axios.post("http://localhost:8080/movie/classify",
            {tag1: this.tag1, tag2: this.tag2, tag3: this.tag3, limit: this.limit, offset: this.offset}).then(
            response => {
              for (let i = 0; i < 8; i++) {
                this.movie_list.push({
                  title: response.data[i].name,
                  id: response.data[i].movieID,
                  image: response.data[i].img,
                  description: response.data[i].genre
                })
              }
              console.log(response.data);
              for (let i = 0; i < 8; i++) {
                if (this.movie_list[i].description !== null) {
                  if (this.movie_list[i].description.endsWith(',')) {
                    this.movie_list[i].description =
                        this.movie_list[i].description.slice(0, -1);
                  }
                }
              }
            }, error => {
            }
        )
      } catch (error) {

      }
    },

    onChange1() {
      this.current1 = 1;
      this.fetchData();
    },

    onChange() {
      this.limit = 8;
      this.offset = (this.current1 - 1) * 8;
      this.fetchData();
    },

    backward() {
      this.detail = false;
    },

    async watchMovieDetail(id) {
      await router.push("/movie/" + id);
    }
  }
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

.image-card2 {
  /*width: 50%; !* Adjust this to control the card width *!*/
  /*!*margin: 100px 10px 100px 10px;*!*/
  /*margin: auto;*/
}

.card-content {
  display: flex;
  flex-direction: column;
}
</style>