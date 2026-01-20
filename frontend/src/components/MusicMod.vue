<template>
  <div>
    <a-row>
      <a-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8" :xxl="8"></a-col>
      <a-col :xs="12" :sm="12" :md="12" :lg="12" :xl="12" :xxl="12">
        <a-form
            :model="formState"
            name="basic"
            :label-col="{ span: 8 }"
            :wrapper-col="{ span: 16 }"
            autocomplete="off"
            @submit="submitForm"
        >
          <a-form-item
              label=""
              name="search"
              :rules="[]"
          >
            <a-input
                v-model:value="formState.search"
                placeholder="请输入搜索内容"
                size="large"
            />
          </a-form-item>
        </a-form>
      </a-col>
      <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
    </a-row>
    <br/><br/>
    <a-row>
      <a-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8" :xxl="8"></a-col>
      <a-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8" :xxl="8">
        <a-card style="height: 100vh; overflow: auto">
          <div v-if="song_list.length > 0">
            <a-list item-layout="horizontal" :data-source="song_list">
              <template #renderItem="{ item }">
                <a-list-item>
                  <template #actions>
                    <a-button @click="playSong(item.rid)">
                      播放
                    </a-button>
                  </template>
                  <a-list-item-meta style="text-align: left">
                    <template #title>
                      {{ item.name }}
                    </template>
                    <template #description>
                      {{ item.artist }}
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
            <a-button disabled v-if="page_num === 1">上一页</a-button>
            <a-button @click="prevSong()" v-else>上一页</a-button>
            &nbsp;&nbsp;<a-tooltip>{{ page_num }}</a-tooltip>&nbsp;&nbsp;
            <a-button @click="nextSong()">下一页</a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8" :xxl="8"></a-col>
    </a-row>
  </div>
</template>

<script setup>
import axios from "axios";
import {useCounterStore} from "@/store/store";
import {reactive, ref} from "vue";

const formState = reactive({
  search: '',
});
let song_list = ref([]);
let page_num = ref(1);
let music_url = ref('');
let music_lrc = ref('');
const counter = useCounterStore();

const submitForm = async () => {
  page_num.value = 1;
  try {
    const response = await axios.get(
        'http://localhost:5000/search?key=' + formState.search + '&pn=' + page_num.value,
        {}).then(
        response => {
          song_list.value = response.data;
          console.log(song_list);
        },
        error => {
        }
    )
  } catch (error) {

  }
}

const playSong = async (rid) => {
  try {
    console.log(rid);
    const response = await axios.get(
        'http://localhost:5000/mp3?rid=' + rid,
        {}).then(
        response => {
          music_url = response.data;
        },
        error => {
        }
    )
    const response1 = await axios.get(
        'http://localhost:5000/lrc?rid=' + rid,
        {}).then(
        response => {
          music_lrc = response.data;
        },
        error => {
        }
    )
    counter.change(music_url, music_lrc);
  } catch (error) {
    console.log(error);
  }
}

const prevSong = async () => {
  page_num.value -= 1;
  try {
    const response = await axios.get(
        'http://localhost:5000/search?key=' + formState.search + '&pn=' + page_num.value,
        {}).then(
        response => {
          song_list.value = response.data;
          console.log(song_list);
        },
        error => {
        }
    )
  } catch (error) {

  }
}

const nextSong = async () => {
  page_num.value += 1;
  try {
    const response = await axios.get(
        'http://localhost:5000/search?key=' + formState.search + '&pn=' + page_num.value,
        {}).then(
        response => {
          song_list.value = response.data;
          console.log(song_list);
        },
        error => {
        }
    );
    if (song_list.value.length === 0) {
      page_num.value -= 1;
      const response = await axios.get(
          'http://localhost:5000/search?key=' + formState.search + '&pn=' + page_num.value,
          {}).then(
          response => {
            song_list.value = response.data;
            console.log(song_list);
          },
          error => {
          }
      );
    }
  } catch (error) {

  }
}
</script>

<style scoped>

</style>