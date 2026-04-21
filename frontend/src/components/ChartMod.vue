<template>
  <div class="chart-container">
    <div class="header-section">
      <div class="controls-wrapper">
        <div class="main-chart-selector" role="tablist" aria-label="图表视图">
          <button
            v-for="item in chartOptions"
            :key="item.value"
            type="button"
            class="chart-option"
            :class="{ active: value === item.value }"
            role="tab"
            :aria-selected="value === item.value"
            @click="setChartType(item.value)"
          >
            <span>{{ item.kicker }}</span>
            <strong>{{ item.label }}</strong>
          </button>
        </div>
        
        <div v-if="value==='历年各种类型电影数量（柱状图）'" class="filter-section">
          <div class="filter-head">
            <span>筛选条件</span>
            <strong>电影类型</strong>
          </div>
          <div class="pill-selector">
            <button
              v-for="item in type"
              :key="item"
              type="button"
              class="pill-item"
              :class="{ active: value2 === item }"
              @click="setBarType(item)"
            >
              {{ item }}
            </button>
          </div>
        </div>

        <div v-if="value === '历年各种类型电影数量（饼图）'" class="filter-section">
          <div class="filter-head">
            <span>筛选条件</span>
            <strong>发行年份</strong>
          </div>
          <div class="year-selector">
            <button
              v-for="item in featuredYears"
              :key="item"
              type="button"
              class="year-chip"
              :class="{ active: value3 === item }"
              @click="setPieYear(item)"
            >
              {{ item }}
            </button>
            <div class="custom-year-query">
              <input
                v-model.trim="customYear"
                type="text"
                inputmode="numeric"
                maxlength="4"
                placeholder="输入年份"
                @keydown.enter="applyCustomYear"
              />
              <button type="button" @click="applyCustomYear">查询</button>
            </div>
          </div>
        </div>

        <!-- Word Cloud Filters (Shape Selection) -->
        <div v-if="value==='我的画像' && nickname" class="filter-section">
          <div class="filter-head">
            <span>筛选条件</span>
            <strong>词云形状</strong>
          </div>
          <div class="shape-selector">
            <button
              v-for="item in shapes"
              :key="item"
              type="button"
              class="shape-chip"
              :class="{ active: value4 === item }"
              @click="setCloudShape(item)"
            >
              {{ item }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Chart Display Area -->
    <div class="chart-display-area">
      <!-- Logic: 
           1. If My Profile + No Login -> Show Guest UI 
           2. Else -> Show Chart (with loading spinner)
      -->
      <div v-if="value === '我的画像' && !nickname" class="guest-state">
        <div class="guest-content">
           <div class="guest-icon">
             <!-- Simple SVG User Icon -->
             <svg viewBox="0 0 24 24" width="64" height="64" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round" style="color: #bfbfbf;">
               <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
               <circle cx="12" cy="7" r="4"></circle>
             </svg>
           </div>
           <h3 class="guest-title">查看您的专属画像</h3>
           <p class="guest-desc">登录后，我们将根据您的观影偏好为您以此生成个性化词云。</p>
           <a-button type="primary" size="large" @click="login()" class="guest-btn">
             立即登录
           </a-button>
        </div>
      </div>

      <a-spin v-else :spinning="loading" tip="正在加载数据..." wrapperClassName="chart-loading">
        <div id="graph" class="graph-content"></div>
      </a-spin>
    </div>
  </div>
</template>
<script>
import axios from "axios";
import { message } from "ant-design-vue";
import router from "@/router/router";

export default {
  data() {
    return {
      nickname: '',
      category: [],
      lineData: [],
      barData: [],
      pieData: [],
      movieName: [],
      keyData: [],
      chart: null,
      option: Object,
      loading: true,
      chartOptions: [
        { value: '历年最受欢迎电影', kicker: '热度', label: '年度热门' },
        { value: '历年各种类型电影数量（柱状图）', kicker: '趋势', label: '类型柱图' },
        { value: '历年各种类型电影数量（饼图）', kicker: '结构', label: '类型占比' },
        { value: '我的画像', kicker: '偏好', label: '我的画像' },
      ],
      value: '历年最受欢迎电影',
      type: ['全部', '剧情', '动作', '动画', '喜剧', '犯罪', '科幻', '历史', '音乐', '爱情', '悬疑', '惊悚', '奇幻', '冒险', '家庭', '战争', '传记', '恐怖', '纪录片', '古装', '武侠', '运动', '其它'],
      year: [],
      value2: '全部',
      value3: '全部',
      customYear: '',
      value4: '圆形',
      shapes: ['圆形', '心形', '菱形', '三角', '星形'],
      map: {
        '圆形': 'circle',
        '心形': 'cardioid',
        '菱形': 'diamond',
        '三角': 'triangle',
        '星形': 'star'
      },
      value5: ['当前未登录，请登录后查看'],
      requestId: 0,
      chartCache: {
        chart1: null,
        chart2: {},
        chart3: {},
        figure: {},
      },
    }
  },
  computed: {
    featuredYears() {
      const presets = ['全部', '2015', '2014', '2013', '2012', '2011', '2010', '2008', '2005', '2000', '1995', '1990', '1985', '1980', '1975', '1970', '1960', '1950'];
      const availableYears = new Set(this.year.map((item) => String(item)));
      return presets.filter((item) => availableYears.has(item));
    },
  },
  methods: {
    async onChange() {
      // If switching to My Profile and not logged in, do not trigger loading or API call
      if (this.value === '我的画像' && !this.nickname) {
        this.disposeChart();
        this.loading = false;
        return;
      }
      
      this.loading = true;
      this.requestId++; // Increment ID for new request
      const currentId = this.requestId;
      
      // Small delay to allow UI to update loading state
      setTimeout(async () => {
         await this.getGraph(currentId);
         if (currentId === this.requestId) {
            this.loading = false;
         }
      }, 100);
    },
    setBarType(type) {
      if (this.value2 === type) return;
      this.value2 = type;
      this.onChange();
    },
    setPieYear(year) {
      if (this.value3 === year) return;
      this.value3 = year;
      this.onChange();
    },
    applyCustomYear() {
      const year = String(this.customYear || '').trim();
      if (!/^\d{4}$/.test(year)) {
        message.warning('请输入 4 位年份');
        return;
      }
      const yearNumber = Number.parseInt(year, 10);
      if (yearNumber < 1911 || yearNumber > 2015) {
        message.warning('请输入 1911-2015 年之间的年份');
        return;
      }
      const availableYears = new Set(this.year.map((item) => String(item)));
      if (!availableYears.has(year)) {
        message.warning('该年份暂无可视化数据');
        return;
      }
      this.setPieYear(year);
    },
    setCloudShape(shape) {
      if (this.value4 === shape) return;
      this.value4 = shape;
      this.onChange();
    },
    setChartType(type) {
      if (this.value === type) return;
      this.value = type;
      this.onChange();
    },
    async getGraph(id) {
      await this.$nextTick();
      this.category = [];
      this.lineData = [];
      this.barData = [];
      this.pieData = [];
      this.movieName = [];
      this.keyData = [];
      
      // Safety check: if somehow we got here for My Profile without login, abort
      if (this.value === '我的画像' && !this.nickname) return;

      const echarts = await import('echarts');
      if (id !== this.requestId) return; // Check after import

      const graph = document.getElementById('graph');
      if (!graph) return;
      this.recreateChart(echarts, graph);
      let myChart = this.chart;
      
      if (this.value === '历年最受欢迎电影') {
        try {
          let rows = this.chartCache.chart1;
          if (!rows) {
            const response = await axios.post('http://localhost:8080/chart/chart1', {});
            rows = response.data || [];
            this.chartCache.chart1 = rows;
          }
          if (id !== this.requestId) return;
          for (let i = 0; i < rows.length; i++) {
            this.category.push(rows[i].year);
            this.barData.push(parseInt(rows[i].popular));
            this.movieName.push(rows[i].name);
          }
        } catch (error) {
        }
        
        if (id !== this.requestId) return; // Check before drawing
        
        let movieName = this.movieName;
        const formatMovieLabel = (value, size = 12) => {
          const text = String(value || '');
          return text.length > size ? `${text.slice(0, Math.max(1, size - 1))}...` : text;
        };
        myChart.setOption({
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            appendToBody: true,
            confine: false,
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            borderColor: '#eee',
            borderWidth: 1,
            extraCssText: 'max-width:320px;box-shadow:0 14px 32px rgba(17,24,32,.12);border-radius:8px;padding:10px 12px;z-index:3000;',
            textStyle: { color: '#333' },
            formatter: function (params) {
              const item = Array.isArray(params) ? params[0] : params;
              const escapeText = (value) => String(value ?? '')
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
              const name = escapeText(item?.name || '');
              const seriesName = escapeText(item?.seriesName || '观看人数');
              const value = escapeText(item?.value ?? 0);
              const movie = escapeText(movieName[item?.dataIndex] || '暂无片名');
              return `<div style="font-weight:bold;margin-bottom:6px;">${name}年</div>
                      <div style="display:flex;justify-content:space-between;gap:16px;align-items:center;">
                        <span>${item?.marker || ''} ${seriesName}</span>
                        <span style="font-weight:bold;">${value}</span>
                      </div>
                      <div style="font-size:12px;color:#666;margin-top:4px;">热门电影：${movie}</div>`;
            },
          },
          grid: {
            top: '28px',
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          legend: { show: false, selectedMode: false },
          xAxis: {
            data: this.category,
            axisLine: { lineStyle: { color: '#ccc' } },
            axisLabel: { color: '#666' },
            axisTick: { show: false }
          },
          yAxis: {
            splitLine: { lineStyle: { type: 'dashed', color: '#eee' } },
            axisLine: { show: false },
            axisLabel: { color: '#666' }
          },
          series: [
            {
              name: '观看人数',
              type: 'bar',
              barWidth: 20,
              itemStyle: {
                borderRadius: [4, 4, 0, 0],
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#d75760' },
                  { offset: 1, color: '#9f2f37' }
                ])
              },
              emphasis: {
                 label: {
                    show: true,
                    position: 'top',
                    distance: 10,
                    color: '#17222b',
                    fontSize: 11,
                    lineHeight: 19,
                    fontWeight: 800,
                    backgroundColor: 'rgba(255, 255, 255, 0.94)',
                    borderColor: 'rgba(15, 127, 131, 0.18)',
                    borderWidth: 1,
                    borderRadius: 8,
                    padding: [6, 9],
                    shadowBlur: 12,
                    shadowColor: 'rgba(17, 24, 32, 0.12)',
                    formatter: function (params) {
                      const year = params.name || '';
                      const title = formatMovieLabel(movieName[params.dataIndex], 16);
                      return `{year|${year} 年}  {value|${params.value}}\n{movie|${title}}`;
                    },
                    rich: {
                      year: {
                        color: '#0f7f83',
                        fontSize: 11,
                        fontWeight: 800,
                        lineHeight: 18,
                      },
                      value: {
                        color: '#6c7a86',
                        fontSize: 11,
                        fontWeight: 700,
                        lineHeight: 18,
                      },
                      movie: {
                        color: '#17222b',
                        fontSize: 12,
                        fontWeight: 800,
                        lineHeight: 20,
                      },
                    },
                 },
                 itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                      { offset: 0, color: '#9f2f37' },
                      { offset: 1, color: '#d75760' }
                    ])
                 }
              },
              data: this.barData,
            },
          ],
          title: { show: false }
        });
      } else if (this.value === '历年各种类型电影数量（柱状图）') {
        try {
          const cacheKey = this.value2 === '全部' ? '__all__' : this.value2;
          let rows = this.chartCache.chart2[cacheKey];
          if (!rows) {
            const response = await axios.post('http://localhost:8080/chart/chart2',
                {tag: (this.value2 === '全部' ? '' : this.value2)});
            rows = response.data || [];
            this.chartCache.chart2[cacheKey] = rows;
          }
          if (id !== this.requestId) return;
          for (let i = 0; i < rows.length; i++) {
            this.category.push(rows[i].year);
            this.barData.push(parseInt(rows[i].movieID));
          }
        } catch (error) {
        }
        
        if (id !== this.requestId) return;

        myChart.setOption({
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            textStyle: { color: '#333' }
          },
           grid: {
            top: '28px',
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          legend: { show: false, selectedMode: false },
          xAxis: {
            data: this.category,
            axisLine: { lineStyle: { color: '#ccc' } },
             axisLabel: { color: '#666' },
             axisTick: { show: false }
          },
          yAxis: {
             splitLine: { lineStyle: { type: 'dashed', color: '#eee' } },
            axisLine: { show: false },
             axisLabel: { color: '#666' }
          },
          series: [
            {
              name: '发行数',
              type: 'bar',
              barWidth: 20,
              itemStyle: {
                borderRadius: [4, 4, 0, 0],
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#36a5a4' },
                  { offset: 1, color: '#157f83' }
                ])
              },
               emphasis: {
                 itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                      { offset: 0, color: '#157f83' },
                      { offset: 1, color: '#36a5a4' }
                    ])
                 }
              },
              data: this.barData,
            },
          ],
          title: { show: false }
        });
      } else if (this.value === '历年各种类型电影数量（饼图）') {
        try {
          const cacheKey = this.value3 || '全部';
          let rows = this.chartCache.chart3[cacheKey];
          if (!rows) {
            const response = await axios.post('http://localhost:8080/chart/chart3', {year: this.value3});
            rows = response.data || [];
            this.chartCache.chart3[cacheKey] = rows;
          }
          if (id !== this.requestId) return;
          for (let i = 0; i < rows.length; i++) {
            let count = parseInt(rows[i].movieID);
            if (count > 0) {
              this.pieData.push({
                name: rows[i].genre,
                value: count
              });
            }
          }
        } catch (error) {
        }
        
        if (id !== this.requestId) return;

        myChart.setOption({
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c} ({d}%)',
             backgroundColor: 'rgba(255, 255, 255, 0.9)',
             textStyle: { color: '#333' }
          },
          legend: {
            type: 'scroll',
            orient: 'vertical',
            selectedMode: false,
            right: 10,
            top: 20,
            bottom: 20,
            data: this.pieData.map(item => item.name),
            textStyle: { color: '#666' }
          },
          series: [
            {
              name: '电影类型',
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['40%', '50%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '20',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: this.pieData,
            },
          ],
          title: { show: false }
        });
      } else if (this.value === '我的画像') {
        try {
          await import('echarts-wordcloud');
          if (id !== this.requestId) return;

          const cacheKey = this.nickname || sessionStorage.getItem('nickname') || '';
          let rows = this.chartCache.figure[cacheKey];
          if (!rows) {
            const response = await axios.post('http://localhost:8080/chart/figure',
                {nickname: cacheKey});
            rows = response.data || {};
            this.chartCache.figure[cacheKey] = rows;
          }
          if (id !== this.requestId) return;
          for (let i in rows) {
              this.keyData.push({
                name: i,
                value: rows[i]
              });
          }
        } catch (error) {
        }
        
        if (id !== this.requestId) return;

        myChart.setOption({
          series: [{
            type: 'wordCloud',
            shape: this.map[this.value4],
            keepAspect: false,
            left: 'center',
            top: '-15px',
            width: '98%',
            height: '100%',
            right: null,
            bottom: null,
            sizeRange: [18, 60],
            rotationRange: [-90, 90],
            rotationStep: 45,
            gridSize: 8,
            drawOutOfBound: false,
            layoutAnimation: true,
            textStyle: {
              fontFamily: 'sans-serif',
              fontWeight: 'bold',
              color: function () {
                // Soft pastel palette
                const colors = [
                  '#c43b45', '#157f83', '#b58a2f', '#65707c',
                  '#9f2f37', '#36a5a4', '#d0a74b', '#171b20'
                ];
                return colors[Math.floor(Math.random() * colors.length)];
              }
            },
            emphasis: {
              textStyle: {
                textShadowBlur: 10,
                textShadowColor: 'rgba(0,0,0,0.1)'
              }
            },
            data: this.keyData,
          }]
        })
      }
    },
    login() {
      sessionStorage.clear();
      this.nickname = '';
      router.push('/auth/login');
    },
    recreateChart(echarts, graph) {
      this.disposeChart();
      this.chart = echarts.init(graph);
    },
    disposeChart() {
      if (this.chart) {
        this.chart.dispose();
        this.chart = null;
      }
    }
  },
  async beforeMount() {
    this.nickname = sessionStorage.getItem('nickname');
    try {
      const response = await axios.post('http://localhost:8080/chart/year',
          {}).then(response => {
        let len = response.data.length;
        for (let i = 0; i < len; i++) {
          this.year.push(response.data[i]);
        }
        this.year.push("全部");
        this.year = this.year.reverse();
      }, error => {
      })
    } catch (error) {
    }
    this.loading = true;
    this.requestId++;
    await this.getGraph(this.requestId);
    this.loading = false;
  },
  beforeUnmount() {
    this.disposeChart();
  }
}
</script>
<style scoped>
.chart-container {
  background: var(--movie-surface);
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  box-shadow: var(--movie-shadow-sm);
  padding: 18px;
  max-width: 1400px;
  margin: 8px auto 32px;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.header-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  min-height: 0;
  margin-bottom: 14px;
  width: 100%;
}

.controls-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: 100%;
  max-width: 1100px;
  min-height: 0;
}

.main-chart-selector {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.chart-option {
  height: 52px;
  padding: 8px 12px;
  border: 1px solid var(--movie-line);
  border-radius: var(--movie-radius);
  color: #566572;
  background: linear-gradient(180deg, #ffffff 0%, #f7fafb 100%);
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease, color 0.2s ease;
}

.chart-option:hover {
  color: #0f7f83;
  border-color: rgba(15, 127, 131, 0.28);
  box-shadow: 0 10px 24px rgba(17, 24, 32, 0.06);
  transform: translateY(-1px);
}

.chart-option.active {
  color: #0f7f83;
  border-color: rgba(15, 127, 131, 0.36);
  background: #eef8f8;
  box-shadow: 0 12px 26px rgba(15, 127, 131, 0.1);
}

.chart-option span {
  display: block;
  margin-bottom: 4px;
  color: #8a98a3;
  font-size: 12px;
  font-weight: 700;
}

.chart-option strong {
  display: block;
  color: inherit;
  font-size: 15px;
  line-height: 1.3;
  font-weight: 800;
}

.filter-section {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  align-items: start;
  gap: 12px;
  width: 100%;
  min-height: 0;
  padding: 12px;
  border: 1px solid #dfe7ec;
  border-radius: var(--movie-radius);
  background: #f8fbfc;
  animation: fadeIn 0.4s ease-out;
}

.filter-head {
  padding: 4px 0;
}

.filter-head span {
  display: block;
  margin-bottom: 4px;
  color: #8a98a3;
  font-size: 12px;
  font-weight: 700;
}

.filter-head strong {
  display: block;
  color: #17222b;
  font-size: 15px;
  font-weight: 800;
}

.pill-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-start;
}

.pill-item {
  min-height: 30px;
  padding: 4px 11px;
  border-radius: 999px;
  font-size: 13px;
  color: #4f5f6b;
  background-color: #fff;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid var(--movie-line);
  user-select: none;
}

.pill-item:hover {
  background-color: #eef8f8;
  border-color: rgba(15, 127, 131, 0.24);
  color: #0f7f83;
}

.pill-item.active {
  background: #0f7f83;
  border-color: #0f7f83;
  color: #fff;
  box-shadow: 0 8px 18px rgba(15, 127, 131, 0.18);
  font-weight: 800;
}

.year-selector {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.year-chip {
  min-width: 58px;
  min-height: 30px;
  padding: 4px 10px;
  border: 1px solid var(--movie-line);
  border-radius: 999px;
  color: #4f5f6b;
  background: #fff;
  cursor: pointer;
  font-weight: 700;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.year-chip:hover {
  color: #0f7f83;
  border-color: rgba(15, 127, 131, 0.24);
  background: #eef8f8;
}

.year-chip.active {
  color: #fff;
  border-color: #0f7f83;
  background: #0f7f83;
  box-shadow: 0 8px 18px rgba(15, 127, 131, 0.18);
}

.custom-year-query {
  display: inline-grid;
  grid-template-columns: minmax(86px, 112px) auto;
  align-items: center;
  gap: 6px;
  margin-left: 4px;
}

.custom-year-query input {
  width: 100%;
  height: 30px;
  padding: 4px 10px;
  border: 1px solid var(--movie-line);
  border-radius: 999px;
  color: #17222b;
  background: #fff;
  outline: none;
  font-weight: 700;
}

.custom-year-query input:focus {
  border-color: rgba(15, 127, 131, 0.42);
  box-shadow: 0 0 0 3px rgba(15, 127, 131, 0.08);
}

.custom-year-query button {
  min-height: 30px;
  padding: 4px 12px;
  border: 1px solid #0f7f83;
  border-radius: 999px;
  color: #fff;
  background: #0f7f83;
  cursor: pointer;
  font-size: 13px;
  font-weight: 800;
  transition: background 0.2s ease, box-shadow 0.2s ease;
}

.custom-year-query button:hover {
  background: #0c6f73;
  box-shadow: 0 8px 18px rgba(15, 127, 131, 0.18);
}

.shape-selector {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.shape-chip {
  min-width: 66px;
  min-height: 30px;
  padding: 4px 10px;
  border: 1px solid var(--movie-line);
  border-radius: 999px;
  color: #4f5f6b;
  background: #fff;
  cursor: pointer;
  font-weight: 700;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.shape-chip:hover {
  color: #0f7f83;
  border-color: rgba(15, 127, 131, 0.24);
  background: #eef8f8;
}

.shape-chip.active {
  color: #fff;
  border-color: #0f7f83;
  background: #0f7f83;
  box-shadow: 0 8px 18px rgba(15, 127, 131, 0.18);
}

/* Login Prompt */
.login-prompt {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fffbe6;
  padding: 12px 24px;
  border-radius: 8px;
  border: 1px solid #ffe58f;
}

.chart-display-area {
  width: 100%;
  flex: 1;
  min-height: 0;
  position: relative;
  background: #ffffff;
  border-radius: var(--movie-radius);
}

.graph-content {
  width: 100%;
  height: 460px;
  border-radius: var(--movie-radius);
  margin: 0 auto;
}

:deep(.ant-spin-nested-loading) {
  height: 100%;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Guest State Styles */
.guest-state {
  width: 100%;
  height: 460px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--movie-surface-soft);
  border-radius: var(--movie-radius);
  border: 1px dashed var(--movie-line);
}

.guest-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 400px;
  padding: 40px;
  animation: fadeIn 0.5s ease-out;
}

.guest-icon {
  margin-bottom: 24px;
  padding: 24px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border-radius: var(--movie-radius);
}

.guest-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--movie-ink);
  margin-bottom: 12px;
}

.guest-desc {
  font-size: 14px;
  color: var(--movie-muted);
  line-height: 1.6;
  margin-bottom: 32px;
}

.guest-btn {
  padding: 0 32px;
  border-radius: var(--movie-radius);
  background: var(--movie-accent);
  border-color: var(--movie-accent);
  font-weight: 500;
  box-shadow: 0 12px 22px rgba(196, 59, 69, 0.2);
  height: 40px;
}

.guest-btn:hover {
  transform: translateY(-1px);
  background: var(--movie-accent-dark) !important;
  border-color: var(--movie-accent-dark) !important;
  box-shadow: 0 14px 26px rgba(196, 59, 69, 0.24);
}

@media (max-width: 768px) {
  .chart-container {
    padding: 20px 16px;
  }

  .main-chart-selector {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-section {
    grid-template-columns: 1fr;
  }

  .graph-content,
  .guest-state {
    height: 480px;
  }
}

@media (max-width: 520px) {
  .main-chart-selector {
    grid-template-columns: 1fr;
  }
}
</style>
