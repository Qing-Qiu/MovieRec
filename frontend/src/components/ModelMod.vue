<template>
  <div class="chat-layout">
    <!-- Sidebar (History) -->
    <div class="sidebar">
      <div class="sidebar-header">
        <a-button type="primary" block @click="startNewChat" class="new-chat-btn">
          <PlusOutlined /> 新对话
        </a-button>
      </div>
      <div class="history-list">
        <div 
          v-for="(session, index) in sessions" 
          :key="index"
          class="history-item"
          :class="{ active: currentSessionId === session.id }"
          @click="switchSession(session.id)"
        >
          <MessageOutlined class="history-icon" />
          <span class="history-title">{{ session.title }}</span>
        </div>
      </div>
    </div>

    <!-- Main Chat Area -->
    <div class="main-area">
      <!-- Header / Mode Switcher -->
      <div class="chat-header">
        <div class="header-content">
          <span class="model-name">
            <RobotOutlined class="model-icon" /> Qwen-Chat
          </span>
          <a-radio-group v-model:value="currentMode" button-style="solid" size="small">
            <a-radio-button :value="1">通用对话</a-radio-button>
            <a-radio-button :value="2">电影知识库</a-radio-button>
          </a-radio-group>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
           <div class="empty-icon-wrapper">
             <RocketOutlined />
           </div>
           <h3>开始一次新的探索</h3>
           <p>您可以问我任何问题，或者切换到“电影知识库”模式查询影片详情。</p>
        </div>

        <div v-for="(msg, index) in messages" :key="index" class="message-row" :class="msg.role">
          <div class="avatar">
            <UserOutlined v-if="msg.role === 'user'" />
            <RobotOutlined v-else />
          </div>
          <div class="bubble">
            <!-- Use v-html for markdown rendering -->
            <div class="bubble-content markdown-body" v-html="renderMessage(msg.content)"></div>
          </div>
        </div>

        <div v-if="loading" class="message-row assistant">
          <div class="avatar"><RobotOutlined /></div>
          <div class="bubble loading-bubble">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="input-area">
        <div class="input-wrapper">
          <a-textarea
            v-model:value="inputContent"
            placeholder="输入您的问题... (Shift + Enter 换行)"
            :auto-size="{ minRows: 1, maxRows: 5 }"
            @pressEnter="handleEnter"
            class="chat-input"
          />
          <a-button type="primary" shape="circle" @click="sendMessage" :loading="loading" class="send-btn">
            <template #icon><SendOutlined /></template>
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from "vue";
import axios from "axios";
import { 
  PlusOutlined, 
  MessageOutlined, 
  RobotOutlined, 
  UserOutlined, 
  SendOutlined,
  RocketOutlined
} from "@ant-design/icons-vue";
import { message } from "ant-design-vue";

// Markdown & Katex
import MarkdownIt from 'markdown-it';
import mk from 'markdown-it-katex';
import 'katex/dist/katex.min.css';

// Initialize Markdown parser
const md = new MarkdownIt({
  html: false, // disable HTML tags for security
  linkify: true,
  typographer: true
});
md.use(mk);

const renderMessage = (content) => {
  if (!content) return '';
  return md.render(content);
};

// --- State ---
const currentMode = ref(1); // 1: General, 2: Knowledge Base
const inputContent = ref("");
const loading = ref(false);
const messagesContainer = ref(null);

// Mock Sessions
const currentSessionId = ref(1);
const sessions = reactive([
  { id: 1, title: '新对话' }
]);

const messages = ref([
  // Example initial structure
  // { role: 'assistant', content: '您好！我是您的电影助手，有什么可以帮您？' }
]);

// --- Actions ---

const startNewChat = () => {
  messages.value = [];
  inputContent.value = "";
  // In a real app, create a new session ID
  message.success("已开启新对话");
};

const switchSession = (id) => {
  currentSessionId.value = id;
  // TODO: Load history for this session
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const handleEnter = (e) => {
  if (e.shiftKey) return;
  e.preventDefault();
  sendMessage();
};

const sendMessage = async () => {
  const content = inputContent.value.trim();
  if (!content) return;

  // 1. Add User Message
  messages.value.push({ role: 'user', content });
  inputContent.value = "";
  scrollToBottom();
  loading.value = true;

  try {
    // 2. Prepare Payload
    const payload = {
      mode: currentMode.value,
      messages: messages.value.map(m => ({ role: m.role, content: m.content })) 
    };
    
    const response = await axios.post('http://localhost:8080/api/chat', payload);
    
    if (response.data && response.data.data) {
      messages.value.push({ role: 'assistant', content: response.data.data });
    } else {
      messages.value.push({ role: 'assistant', content: "抱歉，由于服务器繁忙，我没有收到回复。" });
    }
  } catch (error) {
    console.error(error);
    messages.value.push({ role: 'assistant', content: "网络请求出错，请检查连接。" });
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 85vh; /* Reduced height to avoid page scroll */
  background-color: #f5f7fa;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 260px;
  background-color: white;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
}

.new-chat-btn {
  border-radius: 8px;
  height: 40px;
  font-size: 15px;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 4px;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
  color: #333;
}

.history-item:hover {
  background-color: #f5f5f5;
}

.history-item.active {
  background-color: #e6f7ff;
  color: #1890ff;
}

.history-icon {
  margin-right: 12px;
  font-size: 16px;
}

.history-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}

/* Main Area */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.chat-header {
  height: 60px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  z-index: 10;
}

.header-content {
  display: flex;
  gap: 20px;
  align-items: center;
}

.model-name {
  font-weight: 600;
  color: #444;
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-icon {
  color: #722ed1;
}

/* Messages */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message-row {
  display: flex;
  gap: 16px;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  padding: 0 20px;
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
  color: #666;
  margin-top: 4px; /* Align with top of bubble */
}

.message-row.assistant .avatar {
  background: #f9f0ff;
  color: #722ed1;
}

.message-row.user .avatar {
  background: #e6f7ff;
  color: #1890ff;
}

.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  max-width: 80%;
  word-wrap: break-word;
  text-align: left; /* Explicitly set left alignment */
}

.message-row.assistant .bubble {
  background: white;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.message-row.user .bubble {
  background: #1890ff;
  color: white;
}

/* Make sure markdown content inside also follows rules */
.bubble-content {
  overflow-wrap: break-word;
}

/* Markdown styling overrides for bubbles */
:deep(.markdown-body) {
  font-family: -apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif;
}
:deep(.markdown-body p) {
  margin-bottom: 0.5em;
}
:deep(.markdown-body p:last-child) {
  margin-bottom: 0;
}
:deep(.markdown-body pre) {
  background-color: #f6f8fa;
  padding: 10px;
  border-radius: 6px;
  overflow-x: auto;
}
/* Ensure white text for code blocks in user bubble if needed, 
   but usually user sends plain text. If user sends code, it might look odd on blue.
   Let's keep user bubble simple or adapt only assistant. */
.message-row.user :deep(.markdown-body pre) {
  background-color: rgba(255,255,255,0.2);
  color: white;
}

/* Empty State */
.empty-state {
  margin: auto;
  text-align: center;
  color: #999;
}

.empty-icon-wrapper {
  font-size: 48px;
  margin-bottom: 20px;
  color: #e0e0e0;
}

.empty-state h3 {
  color: #333;
  margin-bottom: 10px;
}

/* Input Area */
.input-area {
  background: white;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input {
  border-radius: 12px;
  padding: 10px 15px;
  resize: none;
  border-color: #d9d9d9;
}

.chat-input:hover, .chat-input:focus {
  border-color: #1890ff;
  box-shadow: none; 
}

.send-btn {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
}

.footer-text {
  text-align: center;
  font-size: 12px;
  color: #ccc;
  margin-top: 10px;
}

/* Loading Dots */
.loading-bubble {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 16px 20px !important;
}

.dot {
  width: 6px;
  height: 6px;
  background: #bbb;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .sidebar {
    display: none; /* Hide sidebar on small screens for now */
  }
}
</style>
