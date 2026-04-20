<template>
  <div class="music-player-container" :class="{ 'is-expanded': isExpanded }" v-if="counter.music_url">
    <!-- Minimized Player (Floating Button) -->
    <div class="mini-player" @click="toggleExpand" v-show="!isExpanded">
      <div class="mini-disc" :class="{ 'rotating': isPlaying }">
        <CustomerServiceOutlined class="mini-icon" />
      </div>
    </div>

    <!-- Expanded Player (Bottom Bar) -->
    <div class="player-panel" v-show="isExpanded">
      <div class="player-controls">
        <button type="button" class="close-btn" @click="toggleExpand" aria-label="收起播放器">
          <DownOutlined />
        </button>

        <div class="player-top">
          <div class="player-disc" :class="{ rotating: isPlaying }">
            <CustomerServiceOutlined />
          </div>
          <div class="player-text">
            <span class="player-status">{{ isPlaying ? '播放中' : '已暂停' }}</span>
            <p class="current-lyric">{{ currentLyric || 'Music Player' }}</p>
          </div>
        </div>

        <div class="timeline-row">
          <span>{{ formatTime(currentTime) }}</span>
          <input
            class="progress-slider"
            type="range"
            min="0"
            :max="duration || 0"
            step="0.1"
            :value="currentTime"
            @input="seekAudio"
          />
          <span>{{ formatTime(duration) }}</span>
        </div>

        <div class="player-actions">
          <button type="button" class="main-toggle" @click="counter.togglePlayback()">
            <PauseCircleFilled v-if="isPlaying" />
            <PlayCircleFilled v-else />
          </button>
          <span class="helper-text">{{ isPlaying ? '点击暂停' : '点击继续' }}</span>
        </div>

        <audio
          ref="audioRef"
          :src="counter.music_url"
          autoplay
          preload="metadata"
          @loadedmetadata="syncDuration"
          @durationchange="syncDuration"
          @timeupdate="updateCurrentTime"
          @play="onPlay"
          @pause="onPause"
          @ended="onEnded"
          controlsList="nodownload"
        ></audio>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import { useCounterStore } from "@/store/store";
import {
  CustomerServiceOutlined,
  DownOutlined,
  PauseCircleFilled,
  PlayCircleFilled
} from "@ant-design/icons-vue";

const counter = useCounterStore();
const audioRef = ref(null);
const currentLyric = ref('Listening to music...');
const isExpanded = ref(true); // Default valid player is expanded
const isPlaying = ref(false);
const lyricsMap = ref([]);
const currentTime = ref(0);
const duration = ref(0);

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
  
  currentTime.value = audioRef.value.currentTime || 0;
  syncDuration();
  
  // Find the active lyric line
  // We look for the last line that has a start time <= current time
  let activeLine = '';
  for (let i = 0; i < lyricsMap.value.length; i++) {
    if (lyricsMap.value[i].time <= currentTime.value) {
      activeLine = lyricsMap.value[i].content;
    } else {
      break; 
    }
  }
  
  if (activeLine) {
    currentLyric.value = activeLine;
  }
};

const syncDuration = () => {
  if (!audioRef.value) return;
  const nextDuration = audioRef.value.duration;
  duration.value = Number.isFinite(nextDuration) ? nextDuration : 0;
};

const seekAudio = (event) => {
  if (!audioRef.value) return;
  const nextTime = Number(event.target.value);
  audioRef.value.currentTime = nextTime;
  currentTime.value = nextTime;
  updateCurrentTime();
};

const formatTime = (seconds) => {
  if (!Number.isFinite(seconds) || seconds <= 0) return '0:00';
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60).toString().padStart(2, '0');
  return `${mins}:${secs}`;
};

const onPlay = () => {
  isPlaying.value = true;
  counter.setPlaying(true);
};

const onPause = () => {
  isPlaying.value = false;
  counter.setPlaying(false);
};

const onEnded = () => {
  isPlaying.value = false;
  counter.setPlaying(false);
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

watch(() => counter.music_url, () => {
  if (counter.music_url) {
    isExpanded.value = true;
    currentTime.value = 0;
    duration.value = 0;
  }
});

watch(() => counter.playback_signal, async () => {
  if (!audioRef.value || !counter.music_url) return;

  if (audioRef.value.paused) {
    try {
      await audioRef.value.play();
    } catch (error) {
      console.warn('Audio play failed:', error);
      counter.setPlaying(false);
    }
  } else {
    audioRef.value.pause();
  }
});

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

.music-player-container.is-expanded {
  right: auto;
  left: 50%;
  width: min(720px, calc(100vw - 32px));
  transform: translateX(-50%);
}

.player-panel {
  width: 100%;
  padding: 12px 18px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(216, 223, 232, 0.88);
  border-radius: var(--movie-radius);
  box-shadow: 0 18px 40px rgba(18, 24, 33, 0.13);
  animation: slideIn 0.3s ease;
}

.player-controls {
  position: relative;
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(180px, 240px) auto;
  align-items: center;
  gap: 14px;
}

.close-btn {
  position: absolute;
  top: -8px;
  right: -10px;
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  cursor: pointer;
  background: transparent;
  border: 0;
  color: var(--movie-muted);
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: var(--movie-accent);
}

.player-top {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  padding-right: 26px;
}

.player-disc {
  flex: 0 0 46px;
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--movie-teal);
  background: linear-gradient(135deg, rgba(21, 127, 131, 0.1), rgba(196, 59, 69, 0.08));
  border: 1px solid rgba(21, 127, 131, 0.18);
  border-radius: 50%;
  font-size: 22px;
}

.player-text {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.player-status {
  color: var(--movie-muted);
  font-size: 12px;
  font-weight: 700;
}

.current-lyric {
  font-size: 14px;
  color: var(--movie-ink);
  font-weight: 650;
  margin: 0;
  transition: all 0.3s ease;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.timeline-row {
  display: grid;
  grid-template-columns: 38px 1fr 38px;
  align-items: center;
  gap: 10px;
  color: var(--movie-muted);
  font-size: 12px;
  font-weight: 650;
}

.progress-slider {
  width: 100%;
  height: 4px;
  padding: 0;
  accent-color: var(--movie-teal);
  cursor: pointer;
}

.player-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-right: 18px;
}

.main-toggle {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  color: #fff;
  background: var(--movie-teal);
  border: 0;
  border-radius: 50%;
  box-shadow: 0 10px 22px rgba(21, 127, 131, 0.2);
  cursor: pointer;
  font-size: 25px;
  transition: background 0.2s ease, transform 0.2s ease;
}

.main-toggle:hover {
  background: #0f6f73;
  transform: translateY(-1px);
}

.helper-text {
  color: var(--movie-muted);
  font-size: 13px;
  font-weight: 650;
}

audio {
  display: none;
}

.mini-player {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--movie-teal);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(21, 127, 131, 0.24);
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
    background: rgba(21, 25, 31, 0.95);
    color: #fff;
  }
  .current-lyric {
    color: #f7fafc;
  }
}

@media (max-width: 520px) {
  .music-player-container {
    right: 12px;
    bottom: 12px;
  }

  .music-player-container.is-expanded {
    left: 12px;
    right: 12px;
    width: auto;
    transform: none;
  }

  .player-panel {
    width: 100%;
    padding: 16px;
  }

  .player-controls {
    display: flex;
    flex-direction: column;
    align-items: stretch;
  }

  .player-actions {
    padding-right: 0;
  }
}
</style>
