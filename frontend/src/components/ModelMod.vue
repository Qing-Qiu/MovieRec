<template>
  <div class="model-container">
    <div class="model-content">
      <!-- Header Section -->
      <div class="header-section">
        <h1 class="page-title">
          <RobotOutlined class="title-icon" /> 大模型助手
        </h1>
        <p class="page-subtitle">智能问答，探索电影与影人的奥秘</p>
      </div>

      <!-- Search Section -->
      <a-row justify="center" class="search-section">
        <a-col :xs="24" :sm="20" :md="18" :lg="14">
          <div class="search-wrapper">
             <a-input-search
                v-model:value="formState.search"
                placeholder="请输入您的问题..."
                enter-button="提问"
                size="large"
                @search="submitForm"
                class="custom-search"
                :loading="loading"
              >
                <template #prefix>
                   <MessageOutlined class="search-icon" />
                </template>
              </a-input-search>
              
              <div class="radio-group-wrapper">
                 <a-radio-group v-model:value="value" button-style="solid">
                  <a-radio-button :value="1">通用对话</a-radio-button>
                  <a-radio-button :value="2">影片查询</a-radio-button>
                  <a-radio-button :value="3">影人查询</a-radio-button>
                </a-radio-group>
              </div>
          </div>
        </a-col>
      </a-row>

      <!-- Result Section -->
       <a-row justify="center">
          <a-col :xs="24" :sm="22" :md="20" :lg="16">
            <a-card class="result-card" :bordered="false" v-if="result || loading">
               <template v-if="loading">
                  <a-skeleton active />
               </template>
               <template v-else-if="result">
                  <div class="result-header">
                     <span class="user-query">Q: {{ lastMsg }}</span>
                  </div>
                  <a-divider style="margin: 12px 0"/>
                  <div class="result-content">
                    <div class="ai-avatar">
                      <RobotOutlined />
                    </div>
                    <div class="ai-text">
                       {{ result }}
                    </div>
                  </div>
               </template>
            </a-card>
            
            <div v-else class="initial-state">
              <BulbOutlined class="bg-icon" />
              <p>选择模式，输入问题，开始智能对话</p>
            </div>
          </a-col>
       </a-row>

    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import axios from "axios";
import { 
  RobotOutlined, 
  MessageOutlined, 
  BulbOutlined 
} from "@ant-design/icons-vue";
import { message } from "ant-design-vue";

const formState = reactive({
  search: '',
});
const value = ref(1);
const result = ref(null);
const loading = ref(false);
const lastMsg = ref('');

const submitForm = async () => {
  if (!formState.search || /^\s*$/.test(formState.search)) {
    message.warning("请输入有效内容");
    return;
  }

  loading.value = true;
  result.value = null; // Clear previous result to show skeleton
  
  let msgToSend = formState.search;
  
  // Format message based on type
  if (value.value === 2) {
    msgToSend = "影片《" + msgToSend + "》";
  } else if (value.value === 3) {
    msgToSend = "影人 " + msgToSend;
  }
  
  lastMsg.value = msgToSend; // Save for display

  try {
    const response = await axios.post('http://localhost:8080/api/chat', { msg: msgToSend });
    if (response.data && response.data.data) {
       result.value = response.data.data;
    } else {
       result.value = "未获取到回答，请稍后再试。";
    }
  } catch (error) {
    console.error(error);
    result.value = "请求出错，请检查网络或服务器状态。";
    message.error("请求失败");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.model-container {
  min-height: calc(100vh - 64px);
  padding: 40px 24px;
  background-color: #f5f7fa; /* Slightly different bg for distinction */
}

.model-content {
  max-width: 1200px;
  margin: 0 auto;
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f1f1f;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  color: #722ed1; /* Purple for AI/Mystery vibe */
}

.page-subtitle {
  color: #8c8c8c;
  font-size: 16px;
}

.search-section {
  margin-bottom: 40px;
}

.search-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
}

.radio-group-wrapper {
  margin-top: 10px;
}

/* Custom Search Input Styles */
:deep(.custom-search .ant-input) {
  border-radius: 24px 0 0 24px !important;
  padding-left: 20px;
}

:deep(.custom-search .ant-btn) {
  border-radius: 0 24px 24px 0 !important;
  padding: 0 40px;
  font-size: 16px;
  background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%); /* Purple gradient */
  border: none;
  box-shadow: 0 4px 10px rgba(114, 46, 209, 0.3);
}

:deep(.custom-search .ant-btn:hover) {
  background: linear-gradient(135deg, #9254de 0%, #d3adf7 100%);
  box-shadow: 0 6px 16px rgba(114, 46, 209, 0.4);
}

.result-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
  padding: 24px;
  min-height: 200px;
}

.result-header {
  font-weight: 600;
  color: #555;
  font-size: 16px;
}

.result-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.ai-avatar {
  font-size: 32px;
  color: #722ed1;
  background: #f9f0ff;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-text {
  flex: 1;
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap; /* Preserve formatting */
}

.initial-state {
  text-align: center;
  padding: 80px 0;
  color: #ccc;
}

.bg-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #efdbff; /* Light purple */
}
</style>