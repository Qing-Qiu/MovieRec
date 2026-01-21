<template>
  <div class="music-player-container" :class="{ 'is-expanded': isExpanded }" v-if="counter.music_url">
    <!-- Minimized Player (Floating Button) -->
    <div class="mini-player" @click="toggleExpand" v-if="!isExpanded">
      <div class="mini-disc" :class="{ 'rotating': isPlaying }">
        <CustomerServiceOutlined class="mini-icon" />
      </div>
    </div>

    <!-- Expanded Player (Bottom Bar) -->
    <div class="player-panel" v-else>
      <div class="player-controls">
        <!-- Close Button -->
        <div class="close-btn" @click="toggleExpand">
          <DownOutlined />
        </div>

        <!-- Lyrics Display -->
        <div class="lyrics-display">
          <p class="current-lyric">{{ currentLyric || 'Music Player' }}</p>
        </div>

        <!-- Audio Controls -->
        <div class="audio-wrapper">
           <audio 
              ref="audioRef"
              :src="counter.music_url"
              autoplay
              @timeupdate="updateCurrentTime"
              @play="onPlay"
              @pause="onPause"
              @ended="onEnded"
              controls
              controlsList="nodownload" 
           ></audio>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from "vue";
import { useCounterStore } from "@/store/store";
import { CustomerServiceOutlined, DownOutlined } from "@ant-design/icons-vue";

const counter = useCounterStore();
const audioRef = ref(null);
const currentLyric = ref('Listening to music...');
const isExpanded = ref(true); // Default valid player is expanded
const isPlaying = ref(false);
const lyricsMap = ref([]);

// Parse LRC format
const parseLyrics = (lrcText) => {
  if (!lrcText) return [];
  const lines = lrcText.split('\n');
  const result = [];
  const timeRegex = /\[(\d{2}):(\d{2})(\.\d{2,3})?\]/g;
  
  for (const line of lines) {
    const matches = [...line.matchAll(timeRegex)];
    if (matches.length > 0) {
      const content = line.replace(timeRegex, '').trim();
      for (const match of matches) {
        const min = parseInt(match[1]);
        const sec = parseInt(match[2]);
        const ms = match[3] ? parseFloat(match[3]) : 0;
        const time = min * 60 + sec + ms;
        result.push({ time, content });
      }
    } else if (line.trim().length > 0) {
       // Handle lines without timestamps if any, though usually ignored or treated as 0
    }
  }
  return result.sort((a, b) => a.time - b.time);
};

// Update lyrics based on time
const updateCurrentTime = () => {
  if (!audioRef.value) return;
  
  const currentTime = audioRef.value.currentTime;
  
  // Find the active lyric line
  // We look for the last line that has a start time <= current time
  let activeLine = '';
  for (let i = 0; i < lyricsMap.value.length; i++) {
    if (lyricsMap.value[i].time <= currentTime) {
      activeLine = lyricsMap.value[i].content;
    } else {
      break; 
    }
  }
  
  if (activeLine) {
    currentLyric.value = activeLine;
  }
};

const onPlay = () => {
  isPlaying.value = true;
};

const onPause = () => {
  isPlaying.value = false;
};

const onEnded = () => {
  isPlaying.value = false;
};

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value;
};

// Watch for store changes to re-parse lyrics
watch(() => counter.music_lrc, (newLrc) => {
  // Reset current lyric state whenever song/lyrics change
  currentLyric.value = 'Loading lyrics...';
  lyricsMap.value = [];

  if (newLrc && newLrc.length > 0) {
    if (Array.isArray(newLrc)) {
        lyricsMap.value = newLrc.map(item => ({
            time: parseFloat(item.time),
            content: item.lineLyric
        })).sort((a, b) => a.time - b.time);
    } else {
        // Fallback if it is a string
        lyricsMap.value = parseLyrics(newLrc);
    }
    // Set initial lyric
    if (lyricsMap.value.length > 0) {
        currentLyric.value = lyricsMap.value[0].content;
    }
  } else {
    currentLyric.value = '纯音乐 / 暂无歌词';
  }
}, { immediate: true });

onMounted(() => {
  // If there's already a URL, verify lyrics are loaded
  if (counter.music_lrc) {
      // trigger watch manually if needed, but immediate: true handles it
  }
});
</script>

<style scoped>
.music-player-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  transition: all 0.3s ease;
}

/* Expanded Player Stlyes */
.player-panel {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  width: 320px;
  padding: 16px;
  animation: slideIn 0.3s ease;
  border: 1px solid rgba(0,0,0,0.05);
}

.player-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.close-btn {
  position: absolute;
  top: 8px;
  right: 12px;
  cursor: pointer;
  color: #888;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #333;
}

.lyrics-display {
  text-align: center;
  margin-bottom: 8px;
  height: 48px; /* Fixed height for two lines or one padded line */
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.current-lyric {
  font-size: 14px;
  color: #1890ff;
  font-weight: 500;
  margin: 0;
  transition: all 0.3s ease;
  line-height: 1.4;
}

.audio-wrapper audio {
  width: 100%;
  height: 36px;
  outline: none;
}

/* Mini Player Styles */
.mini-player {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
  transition: transform 0.3s ease;
}

.mini-player:hover {
  transform: scale(1.1);
}

.mini-disc {
  color: #fff;
  font-size: 24px;
}

.rotating {
  animation: rotate 6s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Dark mode tweaks if needed */
@media (prefers-color-scheme: dark) {
  .player-panel {
    background: rgba(30, 30, 30, 0.95);
    color: #fff;
  }
  .current-lyric {
    color: #40a9ff;
  }
}
</style>
