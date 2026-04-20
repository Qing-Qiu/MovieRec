<template>
  <div class="ai-shell">
    <div class="ai-page">
    <aside class="ai-side">
      <div class="side-brand"><RobotOutlined /> MovieRec AI</div>
      <button type="button" class="new-chat" @click="startNewChat">
        <PlusOutlined /> 新对话
      </button>
      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: currentSessionId === session.id }"
        >
          <button type="button" class="session-open" @click="switchSession(session.id)">
            <MessageOutlined />
            <span>{{ session.title }}</span>
          </button>
          <button type="button" class="session-delete" @click.stop="deleteSession(session.id)" title="删除对话">
            <DeleteOutlined />
          </button>
        </div>
      </div>
    </aside>

    <section class="ai-main">
      <header class="ai-header">
        <div>
          <span class="eyebrow">智能电影助手</span>
          <h1>{{ activeMode.title }}</h1>
        </div>
        <div class="mode-tabs">
          <button
            v-for="item in modes"
            :key="item.id"
            type="button"
            class="mode-tab"
            :class="{ active: currentMode === item.id, disabled: isModeLocked && currentMode !== item.id }"
            :disabled="isModeLocked && currentMode !== item.id"
            :title="isModeLocked && currentMode !== item.id ? '当前对话已锁定模式，请新建对话切换' : item.name"
            @click="changeMode(item.id)"
          >
            <component :is="item.icon" />
            <span>{{ item.name }}</span>
          </button>
        </div>
        <div v-if="isModeLocked" class="mode-lock-note">
          本对话已锁定为 {{ activeMode.name }}，新建对话后可切换模式。
        </div>
      </header>

      <main ref="messagesContainer" class="chat-scroll">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon"><component :is="activeMode.icon" /></div>
          <h2>{{ activeMode.emptyTitle }}</h2>
          <p>{{ activeMode.emptyText }}</p>
          <div class="prompt-grid">
            <button
              v-for="prompt in activeMode.prompts"
              :key="prompt.text"
              type="button"
              class="prompt-card"
              @click="usePrompt(prompt.text)"
            >
              <span>{{ prompt.kicker }}</span>
              {{ prompt.text }}
            </button>
          </div>
        </div>

        <article v-for="(item, index) in messages" :key="index" class="msg" :class="item.role">
          <div class="avatar">
            <UserOutlined v-if="item.role === 'user'" />
            <RobotOutlined v-else />
          </div>
          <div class="msg-body">
            <div class="msg-meta">
              <span>{{ item.role === 'user' ? '你' : 'MovieRec AI' }}</span>
              <span v-if="item.modeName">{{ item.modeName }}</span>
            </div>
            <div class="bubble">
              <div v-if="item.streaming && !item.content" class="inline-loading">
                <i></i><i></i><i></i>
              </div>
              <div v-else v-html="renderMessage(item.content)"></div>
            </div>
            <div v-if="item.references && item.references.length" class="ref-strip">
              <button
                v-for="ref in item.references"
                :key="`${ref.type}-${ref.id || ref.title}`"
                type="button"
                @click="openReference(ref)"
              >
                {{ ref.typeLabel }} · {{ ref.title }}
              </button>
            </div>
            <div
              v-if="item.role === 'assistant' && item.modeId === 'recommend' && recommendationRefs(item).length"
              class="recommend-card-grid"
            >
              <button
                v-for="ref in recommendationRefs(item)"
                :key="`recommend-${ref.id}`"
                type="button"
                class="recommend-card"
                @click="openReference(ref)"
              >
                <img v-if="ref.image" :src="imageUrl(ref.image)" :alt="ref.title" />
                <div>
                  <span>{{ ref.meta }}</span>
                  <strong>{{ ref.title }}</strong>
                  <p>{{ ref.description || '点击查看电影详情。' }}</p>
                </div>
              </button>
            </div>
            <button
              v-if="item.role === 'assistant' && index === messages.length - 1 && !loading"
              type="button"
              class="regen"
              @click="regenerateLast"
            >
              <ReloadOutlined /> 重新生成
            </button>
          </div>
        </article>

        <article v-if="loading && retrieving" class="msg assistant">
          <div class="avatar"><RobotOutlined /></div>
          <div class="msg-body">
            <div class="msg-meta">
              <span>MovieRec AI</span>
              <span>{{ retrieving ? '检索数据' : '生成回答' }}</span>
            </div>
            <div class="bubble loading-bubble">
              <i></i><i></i><i></i>
            </div>
          </div>
        </article>
      </main>

      <footer class="ai-input">
        <a-textarea
          v-model:value="inputContent"
          :placeholder="activeMode.placeholder"
          :auto-size="{ minRows: 1, maxRows: 5 }"
          :disabled="loading"
          class="chat-input"
          @pressEnter="handleEnter"
        />
        <button
          type="button"
          class="send-btn"
          :class="{ stop: loading }"
          @click="loading ? stopRequest() : sendMessage()"
        >
          <StopOutlined v-if="loading" />
          <SendOutlined v-else />
        </button>
      </footer>
    </section>

    <aside class="ai-context">
      <span class="eyebrow">上下文</span>
      <h2>数据线索</h2>
      <div class="mode-note">
        <component :is="activeMode.icon" />
        <div>
          <strong>{{ activeMode.name }}</strong>
          <p>{{ activeMode.panelText }}</p>
        </div>
      </div>

      <div v-if="currentMode === 'chart'" class="chart-embed">
        <div class="chart-embed-head">
          <strong>图表解读数据</strong>
          <button type="button" @click="openChartModal">完整视图</button>
        </div>
        <div class="chart-tabs">
          <button
            v-for="item in chartTabs"
            :key="item.id"
            type="button"
            :class="{ active: chartView === item.id }"
            @click="chartView = item.id"
          >
            {{ item.name }}
          </button>
        </div>
        <div ref="chartPanelRef" class="mini-chart"></div>
        <div v-if="chartLoading" class="chart-loading-note">图表加载中...</div>
      </div>

      <div v-if="latestReferences.length" class="context-list">
        <button
          v-for="ref in latestReferences"
          :key="`${ref.type}-${ref.id || ref.title}`"
          type="button"
          class="context-card"
          @click="openReference(ref)"
        >
          <img v-if="ref.image" :src="imageUrl(ref.image)" :alt="ref.title" />
          <div>
            <span>{{ ref.typeLabel }}</span>
            <h3>{{ ref.title }}</h3>
            <p>{{ ref.meta }}</p>
            <p v-if="ref.description" class="desc">{{ ref.description }}</p>
          </div>
        </button>
      </div>
      <div v-else class="context-empty">
        <FileSearchOutlined />
        <p>{{ activeMode.emptyContext }}</p>
      </div>
    </aside>
    </div>

    <div v-if="chartModalOpen" class="chart-modal-mask" @click.self="closeChartModal">
      <section class="chart-modal">
        <header class="chart-modal-head">
          <div>
            <span class="eyebrow">图表解读</span>
            <h2>数据可视化完整视图</h2>
          </div>
          <button type="button" class="chart-modal-close" @click="closeChartModal">关闭</button>
        </header>
        <div class="chart-modal-body">
          <ChartMod />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import axios from "axios";
import { message } from "ant-design-vue";
import router from "@/router/router";
import ChartMod from "@/components/ChartMod";
import {
  BarChartOutlined,
  BulbOutlined,
  DatabaseOutlined,
  DeleteOutlined,
  FileSearchOutlined,
  MessageOutlined,
  PlusOutlined,
  ReloadOutlined,
  RobotOutlined,
  SendOutlined,
  StopOutlined,
  UserOutlined,
} from "@ant-design/icons-vue";
import MarkdownIt from "markdown-it";
import mk from "markdown-it-katex";
import "katex/dist/katex.min.css";

const md = new MarkdownIt({ html: false, linkify: true, typographer: true });
md.use(mk);

const STORAGE_KEY = "movierec-ai-sessions-v1";
const chartTabs = [
  { id: "popular", name: "年度热片" },
  { id: "trend", name: "类型趋势" },
  { id: "genre", name: "类型结构" },
];

const modes = [
  {
    id: "ask",
    apiMode: 1,
    name: "智能问答",
    title: "和 MovieRec 讨论电影",
    icon: RobotOutlined,
    emptyTitle: "从一个电影问题开始",
    emptyText: "可以聊观影选择、剧情理解、类型偏好，也可以继续追问。",
    placeholder: "问一个电影相关的问题...",
    panelText: "适合开放式电影问答，回答会尽量清晰、可追问。",
    emptyContext: "当前模式不强制检索数据库。",
    system: "适合开放式电影问答。回答时围绕电影、影人、推荐、评论和观影体验展开。",
    prompts: [
      { kicker: "选片", text: "我今晚想看一部节奏快、情绪不压抑的电影，有什么建议？" },
      { kicker: "比较", text: "悬疑片和犯罪片在观影体验上有什么区别？" },
      { kicker: "理解", text: "一部电影的评分高但热度低，通常可能说明什么？" },
    ],
  },
  {
    id: "knowledge",
    apiMode: 2,
    name: "知识库",
    title: "查询电影与影人资料",
    icon: DatabaseOutlined,
    emptyTitle: "用数据库线索回答问题",
    emptyText: "输入电影名、影人名或具体问题，页面会先召回相关条目。",
    placeholder: "查询电影、影人、剧情、主演、导演...",
    panelText: "会先搜索电影和影人数据，再把召回结果作为回答依据。",
    emptyContext: "提问后会在这里显示召回的电影或影人。",
    system: "适合数据库知识问答。必须优先使用提供的电影、影人和评论线索。",
    prompts: [
      { kicker: "电影", text: "《阿甘正传》的导演、主演和类型是什么？" },
      { kicker: "影人", text: "成龙的代表作品和常见类型倾向是什么？" },
      { kicker: "口碑", text: "帮我总结《这个杀手不太冷》的评论区主要观点。" },
    ],
  },
  {
    id: "recommend",
    apiMode: 1,
    name: "推荐助手",
    title: "把偏好转成推荐理由",
    icon: BulbOutlined,
    emptyTitle: "说出你的观影偏好",
    emptyText: "越具体越容易得到有解释的推荐，比如地区、年代、类型、情绪。",
    placeholder: "描述你想看的电影...",
    panelText: "适合把自然语言偏好转成推荐方向，并解释为什么适合。",
    emptyContext: "提问后会优先召回匹配电影作为候选线索。",
    system: "适合推荐解释。要把用户偏好拆成类型、情绪、节奏、地区、年代等维度。",
    prompts: [
      { kicker: "偏好", text: "推荐几部高分日本悬疑片，最好节奏紧凑一点。" },
      { kicker: "情绪", text: "我想看温暖但不幼稚的电影，可以怎么选？" },
      { kicker: "解释", text: "为什么喜欢科幻片的人可能也会喜欢悬疑片？" },
    ],
  },
  {
    id: "chart",
    apiMode: 1,
    name: "图表解读",
    title: "解释可视化背后的趋势",
    icon: BarChartOutlined,
    emptyTitle: "让图表变成结论",
    emptyText: "适合分析年度热片、类型数量、用户画像等可视化结果。",
    placeholder: "问一个和图表趋势有关的问题...",
    panelText: "适合把图表结果解释成自然语言结论，后续可直接接入图表数据。",
    emptyContext: "当前会提供图表模块的结构线索。",
    system: "适合图表解读。回答要用趋势、对比、异常点和可能原因组织，不要编造具体数值。",
    prompts: [
      { kicker: "趋势", text: "如果某一年喜剧片数量明显增加，可能有哪些解释？" },
      { kicker: "画像", text: "用户画像里的高频类型可以怎样转化为推荐理由？" },
      { kicker: "异常", text: "年度最受欢迎电影和类型数量之间可以如何关联分析？" },
    ],
  },
];

const currentMode = ref("ask");
const inputContent = ref("");
const loading = ref(false);
const retrieving = ref(false);
const messagesContainer = ref(null);
const chartPanelRef = ref(null);
const abortController = ref(null);
const currentReferences = ref([]);
const currentContextText = ref("");
const currentSessionId = ref(1);
const nextSessionId = ref(2);
const sessions = reactive([{ id: 1, title: "新对话", modeId: "" }]);
const sessionStore = reactive({ 1: { messages: [], references: [], contextText: "", modeId: "" } });
const messages = ref([]);
const chartView = ref("popular");
const chartLoading = ref(false);
const chartModalOpen = ref(false);
let chartInstance = null;
let chartRenderRetry = 0;

const activeMode = computed(() => modes.find((item) => item.id === currentMode.value) || modes[0]);
const currentSession = computed(() => sessions.find((item) => item.id === currentSessionId.value) || null);
const lockedModeId = computed(() => {
  const state = sessionStore[currentSessionId.value] || {};
  return currentSession.value?.modeId || state.modeId || inferModeFromMessages(messages.value);
});
const isModeLocked = computed(() => Boolean(lockedModeId.value && messages.value.length));
const latestReferences = computed(() => {
  for (let index = messages.value.length - 1; index >= 0; index -= 1) {
    const item = messages.value[index];
    if (item.modeId === currentMode.value && item.references?.length) {
      return item.references;
    }
  }
  if (currentMode.value === "chart") {
    return currentReferences.value.length ? currentReferences.value : chartReferences();
  }
  return [];
});

onMounted(() => {
  loadStoredSessions();
  window.addEventListener("beforeunload", persistSessions);
  window.addEventListener("resize", resizeChart);
  if (currentMode.value === "chart") {
    renderChartPanel();
  }
});

onBeforeUnmount(() => {
  persistSessions();
  window.removeEventListener("beforeunload", persistSessions);
  window.removeEventListener("resize", resizeChart);
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});

watch([currentMode, chartView], () => {
  if (currentMode.value === "chart") {
    renderChartPanel();
  }
});

const renderMessage = (content) => md.render(content || "");
const normalize = (value) => (value || "").replace(/\s+/g, " ").trim();
const truncate = (value, size) => {
  const text = normalize(value);
  return text.length > size ? `${text.slice(0, size)}...` : text;
};

const cloneMessages = (items) =>
  items.map((item) => ({
    ...item,
    streaming: false,
    references: item.references ? item.references.map((ref) => ({ ...ref })) : [],
  }));

const modeById = (modeId) => modes.find((item) => item.id === modeId);

const inferModeFromMessages = (items = []) => {
  const match = items.find((item) => modeById(item.modeId));
  return match?.modeId || "";
};

const normalizeSessionState = (state = {}) => ({
  messages: cloneMessages(state.messages || []),
  references: Array.isArray(state.references) ? state.references.map((item) => ({ ...item })) : [],
  contextText: state.contextText || "",
  modeId: modeById(state.modeId) ? state.modeId : inferModeFromMessages(state.messages || []),
});

const writeCurrentSession = () => {
  const modeId =
    (currentSession.value && modeById(currentSession.value.modeId) ? currentSession.value.modeId : "") ||
    (modeById(sessionStore[currentSessionId.value]?.modeId) ? sessionStore[currentSessionId.value].modeId : "") ||
    inferModeFromMessages(messages.value);
  if (currentSession.value) {
    currentSession.value.modeId = modeId;
  }
  sessionStore[currentSessionId.value] = {
    messages: cloneMessages(messages.value),
    references: currentReferences.value.map((item) => ({ ...item })),
    contextText: currentContextText.value,
    modeId,
  };
};

const saveSession = () => {
  writeCurrentSession();
  persistSessions();
};

const persistSessions = () => {
  writeCurrentSession();
  const payload = {
    currentMode: currentMode.value,
    currentSessionId: currentSessionId.value,
    nextSessionId: nextSessionId.value,
    sessions: sessions.map((item) => ({ ...item })),
    sessionStore: JSON.parse(JSON.stringify(sessionStore)),
  };
  localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
};

const loadStoredSessions = () => {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return;
  try {
    const payload = JSON.parse(raw);
    if (!Array.isArray(payload.sessions) || !payload.sessions.length) return;
    Object.keys(sessionStore).forEach((key) => delete sessionStore[key]);
    const normalizedSessions = payload.sessions.map((item) => {
      const state = normalizeSessionState(payload.sessionStore?.[item.id] || {});
      const modeId = modeById(item.modeId) ? item.modeId : state.modeId;
      sessionStore[item.id] = { ...state, modeId };
      return { ...item, modeId };
    });
    sessions.splice(0, sessions.length, ...normalizedSessions);
    const storedSessionId = payload.currentSessionId || sessions[0].id;
    currentSessionId.value = sessions.some((item) => item.id === storedSessionId) ? storedSessionId : sessions[0].id;
    nextSessionId.value = payload.nextSessionId || Math.max(...sessions.map((item) => item.id)) + 1;
    const state = normalizeSessionState(sessionStore[currentSessionId.value] || {});
    sessionStore[currentSessionId.value] = state;
    if (currentSession.value) currentSession.value.modeId = state.modeId;
    currentMode.value = state.modeId || (modeById(payload.currentMode) ? payload.currentMode : "ask");
    messages.value = cloneMessages(state.messages || []);
    currentReferences.value = state.references.length ? state.references : currentMode.value === "chart" ? chartReferences() : [];
    currentContextText.value = state.contextText || "";
  } catch (error) {
    localStorage.removeItem(STORAGE_KEY);
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const changeMode = (modeId) => {
  if (isModeLocked.value && modeId !== lockedModeId.value) {
    currentMode.value = lockedModeId.value;
    message.warning("当前对话已锁定模式，请新建对话后再切换。");
    return;
  }
  currentMode.value = modeId;
  currentReferences.value = modeId === "chart" ? chartReferences() : [];
  currentContextText.value = "";
};

const startNewChat = () => {
  if (loading.value) stopRequest();
  saveSession();
  const id = nextSessionId.value;
  nextSessionId.value += 1;
  sessions.unshift({ id, title: "新对话", modeId: "" });
  sessionStore[id] = { messages: [], references: [], contextText: "", modeId: "" };
  currentSessionId.value = id;
  messages.value = [];
  inputContent.value = "";
  currentReferences.value = currentMode.value === "chart" ? chartReferences() : [];
  currentContextText.value = "";
  persistSessions();
  message.success("已开启新对话");
};

const switchSession = (id) => {
  if (id === currentSessionId.value) return;
  if (loading.value) stopRequest();
  saveSession();
  currentSessionId.value = id;
  const state = normalizeSessionState(sessionStore[id] || {});
  sessionStore[id] = state;
  messages.value = cloneMessages(state.messages);
  if (state.modeId) {
    currentMode.value = state.modeId;
  }
  currentReferences.value = state.references?.length
    ? state.references.map((item) => ({ ...item }))
    : currentMode.value === "chart"
      ? chartReferences()
      : [];
  currentContextText.value = state.contextText || "";
  scrollToBottom();
};

const deleteSession = (id) => {
  if (loading.value) stopRequest();
  const index = sessions.findIndex((item) => item.id === id);
  if (index < 0) return;

  sessions.splice(index, 1);
  delete sessionStore[id];

  if (sessions.length === 0) {
    const nextId = nextSessionId.value;
    nextSessionId.value += 1;
    sessions.push({ id: nextId, title: "新对话", modeId: "" });
    sessionStore[nextId] = { messages: [], references: [], contextText: "", modeId: "" };
    currentSessionId.value = nextId;
    messages.value = [];
    currentReferences.value = currentMode.value === "chart" ? chartReferences() : [];
    currentContextText.value = "";
  } else if (currentSessionId.value === id) {
    currentSessionId.value = sessions[0].id;
    const state = normalizeSessionState(sessionStore[currentSessionId.value] || {});
    sessionStore[currentSessionId.value] = state;
    messages.value = cloneMessages(state.messages || []);
    if (state.modeId) {
      currentMode.value = state.modeId;
    }
    currentReferences.value = state.references?.length
      ? state.references.map((item) => ({ ...item }))
      : currentMode.value === "chart"
        ? chartReferences()
        : [];
    currentContextText.value = state.contextText || "";
  }

  persistSessions();
  message.success("已删除对话");
};

const usePrompt = (text) => {
  inputContent.value = text;
  sendMessage();
};

const handleEnter = (event) => {
  if (event.shiftKey) return;
  event.preventDefault();
  sendMessage();
};

const sendMessage = async () => {
  const content = inputContent.value.trim();
  if (!content || loading.value) return;
  if (isModeLocked.value && lockedModeId.value !== currentMode.value) {
    currentMode.value = lockedModeId.value;
    message.warning("当前对话已锁定模式，请继续使用原模式。");
    return;
  }
  const mode = activeMode.value;
  lockCurrentSessionMode(mode.id);
  messages.value.push({
    role: "user",
    content,
    modeId: mode.id,
    modeName: mode.name,
    references: [],
  });
  inputContent.value = "";
  updateSessionTitle(content);
  scrollToBottom();
  await requestAssistant(content, mode.id);
};

const lockCurrentSessionMode = (modeId) => {
  if (!modeById(modeId)) return;
  if (currentSession.value) {
    currentSession.value.modeId = modeId;
  }
  const state = sessionStore[currentSessionId.value] || {};
  sessionStore[currentSessionId.value] = { ...state, modeId };
  currentMode.value = modeId;
};

const requestAssistant = async (content, modeId) => {
  const mode = modes.find((item) => item.id === modeId) || activeMode.value;
  const controller = new AbortController();
  abortController.value = controller;
  loading.value = true;
  retrieving.value = true;

  try {
    const references = await retrieveContext(content, mode, controller.signal);
    currentReferences.value = references;
    retrieving.value = false;
    const payload = {
      mode: mode.apiMode,
      modeKey: mode.id,
      modeName: mode.name,
      contextText: currentContextText.value,
      messages: messages.value.map((item) => ({ role: item.role, content: item.content })),
    };
    const assistantMessage = {
      role: "assistant",
      content: "",
      modeId: mode.id,
      modeName: mode.name,
      references,
      streaming: true,
    };
    messages.value.push(assistantMessage);
    await streamAssistantResponse(payload, assistantMessage, controller.signal);
    assistantMessage.streaming = false;
    if (!assistantMessage.content.trim()) {
      assistantMessage.content = "抱歉，服务器没有返回有效回复。";
    }
  } catch (error) {
    const last = messages.value[messages.value.length - 1];
    if (last?.role === "assistant") {
      last.streaming = false;
    }
    if (axios.isCancel(error) || error.name === "CanceledError" || error.name === "AbortError") {
      if (last?.role === "assistant" && !last.content.trim()) {
        last.content = "已停止生成。";
      } else if (!last || last.role !== "assistant") {
        messages.value.push({
          role: "assistant",
          content: "已停止生成。",
          modeId: mode.id,
          modeName: mode.name,
          references: currentReferences.value,
        });
      }
    } else {
      console.error(error);
      messages.value.push({
        role: "assistant",
        content: "请求失败，请检查后端服务或模型配置。",
        modeId: mode.id,
        modeName: mode.name,
        references: currentReferences.value,
      });
    }
  } finally {
    loading.value = false;
    retrieving.value = false;
    abortController.value = null;
    saveSession();
    scrollToBottom();
  }
};

const streamAssistantResponse = async (payload, targetMessage, signal) => {
  const response = await fetch("http://localhost:8080/api/chat/stream", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
    signal,
  });
  if (!response.ok || !response.body) {
    throw new Error("stream failed");
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";
  let eventName = "";
  let eventData = "";

  const flushEvent = () => {
    if (!eventData) {
      eventName = "";
      return;
    }
    let data = {};
    try {
      data = JSON.parse(eventData);
    } catch (error) {
      data = { content: eventData };
    }
    if (eventName === "chunk" && data.content) {
      targetMessage.content += normalizeModelAnswer(data.content);
      scrollToBottom();
    }
    if (eventName === "error") {
      targetMessage.content += data.message || "模型服务暂时不可用。";
    }
    eventName = "";
    eventData = "";
  };

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split(/\r?\n/);
    buffer = lines.pop() || "";
    for (const line of lines) {
      if (line.startsWith("event:")) {
        eventName = line.slice(6).trim();
      } else if (line.startsWith("data:")) {
        eventData += line.slice(5).trim();
      } else if (line === "") {
        flushEvent();
      }
    }
  }
  if (buffer) {
    flushEvent();
  }
};

const stopRequest = () => {
  if (abortController.value) abortController.value.abort();
  loading.value = false;
  retrieving.value = false;
};

const regenerateLast = async () => {
  if (loading.value) return;
  let assistantIndex = -1;
  for (let index = messages.value.length - 1; index >= 0; index -= 1) {
    if (messages.value[index].role === "assistant") {
      assistantIndex = index;
      break;
    }
  }
  if (assistantIndex < 0) return;

  let userMessage = null;
  for (let index = assistantIndex - 1; index >= 0; index -= 1) {
    if (messages.value[index].role === "user") {
      userMessage = messages.value[index];
      break;
    }
  }
  if (!userMessage) return;
  messages.value.splice(assistantIndex, 1);
  lockCurrentSessionMode(userMessage.modeId || currentMode.value);
  await requestAssistant(userMessage.content, currentMode.value);
};

const retrieveContext = async (content, mode, signal) => {
  const response = await axios.post(
    "http://localhost:8080/api/ai/retrieve",
    {
      query: content,
      mode: mode.id,
      nickname: sessionStorage.getItem("nickname") || "",
      limit: 5,
    },
    { signal }
  );
  const data = response.data?.data || {};
  currentContextText.value = data.contextText || "";
  return Array.isArray(data.references) ? data.references : [];
};

const updateSessionTitle = (content) => {
  const session = sessions.find((item) => item.id === currentSessionId.value);
  if (session && session.title === "新对话") session.title = truncate(content, 18);
};

const chartReferences = () => [
  { type: "chart", typeLabel: "图表", title: "历年最受欢迎电影", meta: "年度趋势", description: "可用于分析年份、热度与代表影片之间的关系。", route: "/chart" },
  { type: "chart", typeLabel: "图表", title: "类型数量分布", meta: "类型对比", description: "可用于观察某一年不同电影类型的数量结构。", route: "/chart" },
  { type: "chart", typeLabel: "画像", title: "我的画像", meta: "用户偏好", description: "可用于把高频类型解释成个性化推荐理由。", route: "/chart" },
];

const renderChartPanel = async () => {
  await nextTick();
  const target = chartPanelRef.value;
  if (!target || currentMode.value !== "chart") return;
  if (!target.clientWidth || !target.clientHeight) {
    if (chartRenderRetry < 5) {
      chartRenderRetry += 1;
      window.setTimeout(renderChartPanel, 80);
    }
    return;
  }
  chartRenderRetry = 0;
  chartLoading.value = true;
  try {
    const echarts = await import("echarts");
    if (!chartInstance || chartInstance.getDom() !== target) {
      if (chartInstance) chartInstance.dispose();
      chartInstance = echarts.init(target);
    }
    if (chartView.value === "popular") {
      await renderPopularChart();
    } else if (chartView.value === "trend") {
      await renderTrendChart();
    } else {
      await renderGenreChart();
    }
    resizeChart();
    window.requestAnimationFrame(resizeChart);
  } catch (error) {
    console.error(error);
  } finally {
    chartLoading.value = false;
  }
};

const renderPopularChart = async () => {
  const response = await axios.post("http://localhost:8080/chart/chart1", {});
  const rows = Array.isArray(response.data) ? response.data : [];
  const sample = rows.slice(-12);
  chartInstance.setOption({
    grid: { top: 20, left: 36, right: 14, bottom: 38 },
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: sample.map((item) => item.year), axisLabel: { fontSize: 10, interval: 1 } },
    yAxis: { type: "value", axisLabel: { fontSize: 10 } },
    series: [{
      type: "bar",
      data: sample.map((item) => Number.parseInt(item.popular, 10) || 0),
      itemStyle: { color: "#c43b45", borderRadius: [3, 3, 0, 0] },
    }],
  }, true);
};

const renderTrendChart = async () => {
  const response = await axios.post("http://localhost:8080/chart/chart2", { tag: "喜剧" });
  const rows = Array.isArray(response.data) ? response.data : [];
  const sample = rows.slice(-18);
  chartInstance.setOption({
    grid: { top: 20, left: 36, right: 14, bottom: 38 },
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: sample.map((item) => item.year), axisLabel: { fontSize: 10, interval: 2 } },
    yAxis: { type: "value", axisLabel: { fontSize: 10 } },
    series: [{
      name: "喜剧发行数",
      type: "line",
      smooth: true,
      symbolSize: 5,
      data: sample.map((item) => Number.parseInt(item.movieID, 10) || 0),
      lineStyle: { color: "#0f7f83", width: 3 },
      itemStyle: { color: "#0f7f83" },
      areaStyle: { color: "rgba(15, 127, 131, 0.12)" },
    }],
  }, true);
};

const renderGenreChart = async () => {
  const response = await axios.post("http://localhost:8080/chart/chart3", { year: "2010" });
  const rows = Array.isArray(response.data) ? response.data : [];
  const pieData = rows
    .map((item) => ({ name: item.genre, value: Number.parseInt(item.movieID, 10) || 0 }))
    .filter((item) => item.value > 0);
  chartInstance.setOption({
    tooltip: { trigger: "item" },
    legend: { type: "scroll", bottom: 0, textStyle: { fontSize: 10 } },
    series: [{
      type: "pie",
      radius: ["35%", "62%"],
      center: ["50%", "42%"],
      label: { show: false },
      data: pieData,
    }],
  }, true);
};

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize();
  } else if (currentMode.value === "chart" && chartPanelRef.value?.clientWidth) {
    renderChartPanel();
  }
};

const openChartModal = () => {
  chartModalOpen.value = true;
};

const closeChartModal = () => {
  chartModalOpen.value = false;
};

const recommendationRefs = (item) =>
  (item.references || []).filter((ref) => ref.type === "movie");

const imageUrl = (url) => (url ? `http://localhost:8080/image?url=${encodeURIComponent(url)}` : "");
const openReference = (ref) => {
  if (ref.type === "chart") {
    openChartModal();
    return;
  }
  if (ref.route) router.push(ref.route);
};

const normalizeModelAnswer = (answer) => {
  if (answer.startsWith("Error calling AI service")) return "模型服务暂时不可用，请检查 API Key、模型地址或网络配置。";
  if (answer.startsWith("No response from AI service")) return "模型服务没有返回内容，请稍后再试。";
  return answer;
};
</script>

<style scoped>
.ai-shell {
  position: relative;
}

.ai-page {
  display: grid;
  grid-template-columns: 250px minmax(0, 1fr) 380px;
  min-height: calc(100vh - 104px);
  background: #f5f7f9;
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  overflow: hidden;
  box-shadow: var(--movie-shadow-sm);
}

button {
  font: inherit;
}

.ai-side {
  padding: 20px 14px;
  background: #111820;
  color: #eef4f6;
  overflow: hidden;
}

.side-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 6px 16px;
  font-size: 18px;
  font-weight: 800;
}

.side-brand .anticon,
.session-item .anticon {
  color: #55b8ba;
}

.new-chat,
.session-open,
.session-delete,
.mode-tab,
.prompt-card,
.send-btn,
.ref-strip button,
.regen,
.context-card {
  border: 0;
  cursor: pointer;
}

.new-chat {
  width: 100%;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: var(--movie-radius);
  color: #fff;
  background: #c43b45;
}

.session-list {
  margin-top: 16px;
}

.session-item {
  width: 100%;
  min-height: 42px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px;
  margin-bottom: 4px;
  border-radius: var(--movie-radius);
  color: rgba(238, 244, 246, 0.76);
  background: transparent;
  text-align: left;
}

.session-item:hover,
.session-item.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.session-open {
  min-width: 0;
  flex: 1;
  min-height: 34px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px;
  color: inherit;
  background: transparent;
  text-align: left;
}

.session-open span {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.session-delete {
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  color: rgba(238, 244, 246, 0.58);
  background: transparent;
  opacity: 0;
  transition: opacity 0.2s ease, color 0.2s ease, background 0.2s ease;
}

.session-item:hover .session-delete,
.session-item.active .session-delete {
  opacity: 1;
}

.session-delete:hover {
  color: #fff;
  background: rgba(196, 59, 69, 0.72);
}

.ai-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #f8fafb;
}

.ai-header {
  padding: 22px 28px 18px;
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid var(--movie-line);
}

.eyebrow {
  display: block;
  margin-bottom: 5px;
  color: #687784;
  font-size: 13px;
  font-weight: 800;
}

.ai-header h1,
.ai-context h2 {
  margin: 0;
  color: #111820;
  font-size: 26px;
  line-height: 1.2;
  font-weight: 800;
}

.mode-tabs {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-top: 18px;
}

.mode-tab {
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: var(--movie-radius);
  color: #53616d;
  background: #eef3f5;
  border: 1px solid transparent;
}

.mode-tab:hover,
.mode-tab.active {
  color: #0f7f83;
  background: #e3f2f2;
  border-color: rgba(15, 127, 131, 0.22);
}

.mode-tab.disabled,
.mode-tab.disabled:hover {
  color: #95a2ac;
  background: #eef2f4;
  border-color: transparent;
  cursor: not-allowed;
  opacity: 0.64;
}

.mode-lock-note {
  display: inline-flex;
  margin-top: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  color: #61707c;
  background: #f4f7f8;
  border: 1px solid #dfe7ec;
  font-size: 12px;
  line-height: 1.45;
}

.chat-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 28px;
}

.empty-state {
  max-width: 860px;
  min-height: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: center;
}

.empty-icon {
  width: 58px;
  height: 58px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #0f7f83;
  background: #e3f2f2;
  font-size: 28px;
}

.empty-state h2 {
  margin: 0 0 10px;
  color: #111820;
  font-size: 28px;
  font-weight: 800;
}

.empty-state p {
  margin: 0;
  color: #687784;
}

.prompt-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 28px;
}

.prompt-card {
  min-height: 126px;
  padding: 18px;
  border-radius: var(--movie-radius);
  color: #1f2d37;
  background: #fff;
  border: 1px solid #dbe4e9;
  text-align: left;
  line-height: 1.55;
  box-shadow: 0 8px 24px rgba(17, 24, 32, 0.05);
}

.prompt-card:hover {
  border-color: rgba(15, 127, 131, 0.36);
  box-shadow: 0 14px 30px rgba(17, 24, 32, 0.08);
}

.prompt-card span {
  display: block;
  margin-bottom: 10px;
  color: #c43b45;
  font-size: 13px;
  font-weight: 800;
}

.msg {
  display: flex;
  gap: 14px;
  max-width: 880px;
  margin: 0 auto 22px;
}

.msg.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #6c7a86;
  background: #e9eff2;
  margin-top: 24px;
}

.assistant .avatar {
  color: #0f7f83;
  background: #e3f2f2;
}

.user .avatar {
  color: #c43b45;
  background: #f8e8ea;
}

.msg-body {
  min-width: 0;
  max-width: 76%;
}

.user .msg-body {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.msg-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 7px;
  color: #788692;
  font-size: 12px;
  font-weight: 800;
}

.bubble {
  padding: 14px 16px;
  border-radius: var(--movie-radius);
  color: #17222b;
  background: #fff;
  border: 1px solid #dbe4e9;
  line-height: 1.7;
  box-shadow: 0 6px 20px rgba(17, 24, 32, 0.04);
  overflow-wrap: break-word;
}

.user .bubble {
  color: #fff;
  background: #c43b45;
  border-color: #c43b45;
}

:deep(.bubble p) {
  margin-bottom: 0.6em;
}

:deep(.bubble p:last-child) {
  margin-bottom: 0;
}

:deep(.bubble ul),
:deep(.bubble ol) {
  padding-left: 1.35em;
}

:deep(.bubble pre) {
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  background: #f1f5f7;
}

.ref-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.ref-strip button {
  max-width: 240px;
  min-height: 30px;
  padding: 5px 10px;
  border-radius: 999px;
  color: #0f7f83;
  background: #e3f2f2;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.recommend-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.recommend-card {
  display: flex;
  gap: 10px;
  min-height: 104px;
  padding: 10px;
  border: 1px solid #dbe4e9;
  border-radius: var(--movie-radius);
  color: inherit;
  background: #fff;
  text-align: left;
  box-shadow: 0 6px 18px rgba(17, 24, 32, 0.04);
}

.recommend-card:hover {
  border-color: rgba(15, 127, 131, 0.34);
  background: #f8fbfb;
}

.recommend-card img {
  width: 54px;
  height: 78px;
  flex: 0 0 auto;
  border-radius: 6px;
  object-fit: cover;
  background: #e7edf1;
}

.recommend-card span {
  display: block;
  margin-bottom: 3px;
  color: #6d7a85;
  font-size: 11px;
  line-height: 1.35;
}

.recommend-card strong {
  display: block;
  color: #111820;
  font-size: 14px;
  line-height: 1.35;
}

.recommend-card p {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin: 5px 0 0;
  overflow: hidden;
  color: #596875;
  font-size: 12px;
  line-height: 1.45;
}

.regen {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 4px 0;
  color: #6b7883;
  background: transparent;
}

.regen:hover {
  color: #0f7f83;
}

.loading-bubble,
.inline-loading {
  width: 74px;
  display: flex;
  justify-content: center;
  gap: 5px;
  padding: 18px 16px;
}

.inline-loading {
  padding: 4px 0;
}

.loading-bubble i,
.inline-loading i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #0f7f83;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-bubble i:nth-child(1),
.inline-loading i:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-bubble i:nth-child(2),
.inline-loading i:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.ai-input {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 18px 28px 24px;
  background: rgba(255, 255, 255, 0.92);
  border-top: 1px solid var(--movie-line);
}

.chat-input {
  border-radius: var(--movie-radius);
  padding: 12px 14px;
  resize: none;
  border-color: #d7e1e6;
  box-shadow: none;
}

.chat-input:hover,
.chat-input:focus {
  border-color: rgba(15, 127, 131, 0.55);
  box-shadow: 0 0 0 3px rgba(15, 127, 131, 0.1);
}

.send-btn {
  width: 44px;
  height: 44px;
  flex: 0 0 auto;
  border-radius: var(--movie-radius);
  color: #fff;
  background: #0f7f83;
  box-shadow: 0 10px 24px rgba(15, 127, 131, 0.18);
}

.send-btn.stop {
  background: #c43b45;
}

.ai-context {
  min-width: 0;
  padding: 24px 20px;
  background: #fff;
  border-left: 1px solid var(--movie-line);
  overflow-y: auto;
}

.ai-context h2 {
  margin-bottom: 18px;
  font-size: 22px;
}

.mode-note {
  display: flex;
  gap: 12px;
  padding: 14px;
  margin-bottom: 18px;
  border-radius: var(--movie-radius);
  background: #f5f8fa;
  border: 1px solid #dfe7ec;
}

.mode-note .anticon {
  color: #0f7f83;
  font-size: 22px;
  margin-top: 2px;
}

.mode-note strong {
  color: #111820;
}

.mode-note p {
  margin: 5px 0 0;
  color: #687784;
  font-size: 13px;
  line-height: 1.55;
}

.chart-embed {
  padding: 12px;
  margin-bottom: 14px;
  border: 1px solid #dfe7ec;
  border-radius: var(--movie-radius);
  background: #f8fafb;
}

.chart-embed-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.chart-embed-head strong {
  color: #111820;
  font-size: 15px;
}

.chart-embed-head button {
  border: 0;
  color: #0f7f83;
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  font-weight: 800;
}

.chart-tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
  margin-bottom: 10px;
}

.chart-tabs button {
  min-height: 30px;
  border: 1px solid #d9e4e9;
  border-radius: 6px;
  color: #60707c;
  background: #fff;
  cursor: pointer;
  font-size: 12px;
}

.chart-tabs button.active {
  color: #0f7f83;
  border-color: rgba(15, 127, 131, 0.28);
  background: #e8f4f5;
}

.mini-chart {
  width: 100%;
  height: 240px;
  border-radius: 6px;
  background: #fff;
}

.chart-loading-note {
  margin-top: 8px;
  color: #687784;
  font-size: 12px;
  text-align: center;
}

.context-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.context-card {
  width: 100%;
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: var(--movie-radius);
  color: inherit;
  background: #fff;
  border: 1px solid #dfe7ec;
  text-align: left;
}

.context-card:hover {
  border-color: rgba(15, 127, 131, 0.34);
  background: #f8fbfb;
}

.context-card img {
  width: 48px;
  height: 68px;
  flex: 0 0 auto;
  border-radius: 6px;
  object-fit: cover;
  background: #e7edf1;
}

.context-card span {
  color: #c43b45;
  font-size: 12px;
  font-weight: 800;
}

.context-card h3 {
  margin: 4px 0 5px;
  color: #111820;
  font-size: 15px;
  line-height: 1.35;
}

.context-card p {
  margin: 0;
  color: #6c7a86;
  font-size: 12px;
  line-height: 1.45;
}

.context-card .desc {
  margin-top: 6px;
  color: #394854;
}

.context-empty {
  min-height: 170px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #7a8791;
  text-align: center;
  border: 1px dashed #d5e0e6;
  border-radius: var(--movie-radius);
  background: #f8fafb;
}

.context-empty .anticon {
  color: #9aa7b0;
  font-size: 30px;
}

.context-empty p {
  max-width: 220px;
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
}

.chart-modal-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px;
  background: rgba(17, 24, 32, 0.48);
  backdrop-filter: blur(4px);
}

.chart-modal {
  width: min(1180px, calc(100vw - 56px));
  max-height: calc(100vh - 56px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: var(--movie-radius);
  background: #f5f7f9;
  box-shadow: 0 24px 70px rgba(17, 24, 32, 0.26);
}

.chart-modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 22px;
  background: #fff;
  border-bottom: 1px solid var(--movie-line);
}

.chart-modal-head h2 {
  margin: 0;
  color: #111820;
  font-size: 22px;
  font-weight: 800;
}

.chart-modal-close {
  min-width: 70px;
  height: 36px;
  border: 1px solid #d9e3e8;
  border-radius: var(--movie-radius);
  color: #53616d;
  background: #fff;
  cursor: pointer;
}

.chart-modal-close:hover {
  color: #c43b45;
  border-color: rgba(196, 59, 69, 0.32);
  background: #fff5f6;
}

.chart-modal-body {
  min-height: 0;
  overflow: auto;
  padding: 18px;
  background: #f5f7f9;
}

.chart-modal-body :deep(.chart-container) {
  margin: 0;
  max-width: none;
  min-height: auto;
  border: 0;
  box-shadow: none;
}

@media (max-width: 1180px) {
  .ai-page {
    grid-template-columns: 250px minmax(0, 1fr);
  }

  .ai-context {
    display: none;
  }
}

@media (max-width: 860px) {
  .ai-page {
    grid-template-columns: minmax(0, 1fr);
    min-height: calc(100vh - 92px);
  }

  .ai-side {
    display: none;
  }

  .ai-header,
  .chat-scroll,
  .ai-input {
    padding-left: 16px;
    padding-right: 16px;
  }

  .mode-tabs,
  .prompt-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .recommend-card-grid {
    grid-template-columns: 1fr;
  }

  .msg-body {
    max-width: 86%;
  }
}

@media (max-width: 560px) {
  .mode-tabs,
  .prompt-grid {
    grid-template-columns: 1fr;
  }

  .chart-modal-mask {
    padding: 12px;
  }

  .chart-modal {
    width: calc(100vw - 24px);
    max-height: calc(100vh - 24px);
  }
}
</style>
