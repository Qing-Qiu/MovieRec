import {defineStore} from 'pinia'
import {ref} from "vue";

export const useCounterStore = defineStore('counter', {
    state: () => {
        return {
            music_url: ref(''),
            music_lrc: ref(''),
        }
    },
    actions: {
        change(url, lrc) {
            this.music_url = url;
            this.music_lrc = lrc;
        }
    }
})
