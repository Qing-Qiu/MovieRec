import {defineStore} from 'pinia'
import {ref} from "vue";

export const useCounterStore = defineStore('counter', {
    state: () => {
        return {
            music_url: ref(''),
            music_lrc: ref([]),
            music_rid: ref(null),
            music_playing: ref(false),
            playback_signal: ref(0),
        }
    },
    actions: {
        change(url, lrc, rid = null) {
            this.music_url = url;
            this.music_lrc = lrc || [];
            this.music_rid = rid;
            this.music_playing = Boolean(url);
        },
        setLyrics(lrc) {
            this.music_lrc = lrc || [];
        },
        setPlaying(isPlaying) {
            this.music_playing = isPlaying;
        },
        togglePlayback() {
            this.playback_signal += 1;
        }
    }
})
