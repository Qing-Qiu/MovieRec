<template>
  <audio controls ref="audioPlayer" @timeupdate="updateCurrentTime"
         :src="counter.music_url">
  </audio>
  <h1 style="margin-left: auto;margin-right: auto"></h1>
</template>

<script>
import {ref, onMounted, onBeforeUnmount} from "vue";
import {useCounterStore} from "@/store/store";

export default {
  setup() {
    const counter = useCounterStore();
    const currentTime = ref(0);
    let audioElement, textElement;

    onMounted(() => {
      audioElement = document.querySelector("audio");
      textElement = document.querySelector("h1");
    });

    const updateCurrentTime = () => {
      if (audioElement) {
        let lyric = '';
        currentTime.value = audioElement.currentTime;
        for (let i = 0; i < counter.music_lrc.length; i++) {
          if (parseFloat(counter.music_lrc[i].time) <= currentTime.value) {
            lyric = counter.music_lrc[i].lineLyric;
          } else break;
        }
        console.log(lyric);
        textElement.textContent = lyric;
      }
    };

    onBeforeUnmount(() => {
      if (audioElement) {
        audioElement.removeEventListener("timeupdate", updateCurrentTime);
      }
    });

    return {counter, currentTime, updateCurrentTime};
  },
};
</script>
