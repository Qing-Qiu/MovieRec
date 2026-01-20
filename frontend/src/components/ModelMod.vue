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
              placeholder="请输入问题"
              size="large"
              @search="submitForm"
          />
          <a-radio-group v-model:value="value">
            <a-radio :value="1">常规</a-radio>
            <a-radio :value="2">影片</a-radio>
            <a-radio :value="3">影人</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-col>
    <a-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4" :xxl="4"></a-col>
  </a-row>
  <a-row>
    <a-col :xs="6" :sm="6" :md="6" :lg="6" :xl="6" :xxl="6"></a-col>
    <a-col :xs="12" :sm="12" :md="12" :lg="12" :xl="12" :xxl="12">
      <a-card style="margin-top:100px; height: 100vh;">
        <a-list :loading="loading"
                style="margin-left: 100px;
                margin-right: 100px;
                text-align: left;">
          {{ this.result }}
        </a-list>
      </a-card>
    </a-col>
    <a-col :xs="6" :sm="6" :md="6" :lg="6" :xl="6" :xxl="6"></a-col>
  </a-row>
</template>
<script setup>
import HomePage from "@/views/HomePage";
</script>
<script>
import axios from "axios";

export default {
  data() {
    return {
      formState: {
        search: '',
      },
      value: 1,
      result: null,
      loading: false,
      msg: '',
    }
  },
  methods: {
    async submitForm() {
      try {
        this.loading = true;
        console.log(this.formState.search);
        if (/^\s*$/.test(this.formState.search)) {
          this.result = "请输入有效字符。"
          this.loading = false;
          return;
        }
        this.msg = this.formState.search;
        if (this.value === 2) {
          this.msg = "影片《" + this.msg + "》";
        } else if (this.value === 3) {
          this.msg = "影人 " + this.msg;
        }
        console.log(this.msg);
        const response = await axios.post('http://localhost:8080/api/chat',
            {msg: this.msg}).then(
            response => {
              this.result = response.data.data;
              console.log(this.result);
              this.loading = false;
            }, error => {

            }
        )

      } catch (error) {
      }
    },
  }
}
</script>

<style scoped>
</style>