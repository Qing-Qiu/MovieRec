<template>
  <div class="chart-container">
    <div class="header-section">
      <h2 class="chart-title">数据可视化仪表盘</h2>
      
      <div class="controls-wrapper">
        <!-- Main Chart Type Selector -->
        <a-segmented 
          v-model:value="value" 
          :options="data" 
          @change="onChange()" 
          class="main-chart-selector"
          size="large"
        >
          <template #label="{ value: val }">
            <span style="padding: 0 8px">{{ val }}</span>
          </template>
        </a-segmented>
        
        <!-- Bar Chart Filters (Type Selection) -->
        <div v-if="value==='历年各种类型电影数量（柱状图）'" class="filter-section">
           <span class="filter-label">选择电影类型:</span>
           <div class="pill-selector">
             <div 
                v-for="item in type" 
                :key="item"
                class="pill-item"
                :class="{ active: value2 === item }"
                @click="setBarType(item)"
             >
               {{ item }}
             </div>
           </div>
        </div>

        <!-- Pie Chart Filters (Year Selection) -->
        <div v-if="value === '历年各种类型电影数量（饼图）'" class="filter-section">
           <span class="filter-label">选择年份:</span>
           <div class="year-selector">
              <a-select
                  v-model:value="value3"
                  show-search
                  placeholder="选择年份"
                  style="width: 140px"
                  size="large"
                  @change="onChange()"
                  class="custom-select"
              >
                <a-select-option v-for="y in year" :key="y" :value="y">{{ y }}</a-select-option>
              </a-select>
           </div>
        </div>

        <!-- Word Cloud Filters (Shape Selection) -->
        <div v-if="value==='我的画像' && nickname" class="filter-section">
             <span class="filter-label">词云形状:</span>
             <a-segmented v-model:value="value4" :options="shapes" @change="onChange()" size="middle"></a-segmented>
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
<script setup>
import HomePage from "@/views/HomePage";
</script>
<script>
import axios from "axios";
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
      data: ['历年最受欢迎电影', '历年各种类型电影数量（柱状图）', '历年各种类型电影数量（饼图）', '我的画像'],
      value: '历年最受欢迎电影',
      type: ['全部', '动作', '动画', '喜剧', '犯罪', '科幻', '历史', '音乐', '爱情', '悬疑', '惊悚', '其它'],
      year: [],
      value2: '全部',
      value3: '全部',
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
    }
  },
  computed: {
  },
  methods: {
    async onChange() {
      // If switching to My Profile and not logged in, do not trigger loading or API call
      if (this.value === '我的画像' && !this.nickname) {
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
    async getGraph(id) {
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
      
      await import('echarts-wordcloud');
      if (id !== this.requestId) return; // Check after import
      
      let myChart = echarts.init(document.getElementById('graph'));
      myChart.clear();
      
      if (this.value === '历年最受欢迎电影') {
        try {
          const response = await axios.post('http://localhost:8080/chart/chart1',
              {}).then(response => {
            if (id !== this.requestId) return; // Check inside promise if needed, but safer after await
            let len = response.data.length;
            for (let i = 0; i < len; i++) {
              this.category.push(response.data[i].year);
              this.barData.push(parseInt(response.data[i].popular));
              this.movieName.push(response.data[i].name);
            }
          }, error => {
          })
        } catch (error) {
        }
        
        if (id !== this.requestId) return; // Check before drawing
        
        console.log(this.movieName);
        let movieName = this.movieName;
        myChart.setOption({
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            borderColor: '#eee',
            borderWidth: 1,
            textStyle: { color: '#333' },
            formatter: function (params) {
              let tooltip = `<div style="font-weight:bold;margin-bottom:5px;">${params[0].name}年</div>`;
              for (let i = 0; i < params.length; i++) {
                tooltip += `<div style="display:flex;justify-content:space-between;align-items:center;">
                              <span>${params[i].marker} ${params[i].seriesName}</span>
                              <span style="font-weight:bold;margin-left:10px;">${params[i].value}</span>
                            </div>`;
                tooltip += `<div style="font-size:12px;color:#666;margin-top:2px;">Most Popular: ${movieName[params[i].dataIndex]}</div>`;
              }
              return tooltip;
            },
          },
          grid: {
            top: '80px',
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          legend: {
            data: ['观看人数'],
            top: 40,
            textStyle: { color: '#666' }
          },
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
                  { offset: 0, color: '#7EB6FF' },
                  { offset: 1, color: '#5F89FF' }
                ])
              },
              emphasis: {
                 itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                      { offset: 0, color: '#5F89FF' },
                      { offset: 1, color: '#7EB6FF' }
                    ])
                 }
              },
              data: this.barData,
            },
          ],
          title: {
            text: '1911-2015年最受欢迎电影及观看人数',
            left: 'center',
            textStyle: {
              color: '#333',
              fontSize: 18,
              fontWeight: 600
            }
          }
        });
      } else if (this.value === '历年各种类型电影数量（柱状图）') {
        try {
          const response = await axios.post('http://localhost:8080/chart/chart2',
              {tag: (this.value2 === '全部' ? '' : this.value2)}).then(response => {
            if (id !== this.requestId) return;
            let len = response.data.length;
            console.log(response);
            for (let i = 0; i < len; i++) {
              this.category.push(response.data[i].year);
              this.barData.push(parseInt(response.data[i].movieID));
            }
          }, error => {
          })
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
            top: '80px',
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          legend: {
            data: ['发行数'],
            top: 40,
            textStyle: { color: '#666' }
          },
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
                  { offset: 0, color: '#FF9A9E' },
                  { offset: 1, color: '#FECFEF' }
                ])
              },
               emphasis: {
                 itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                      { offset: 0, color: '#FECFEF' },
                      { offset: 1, color: '#FF9A9E' }
                    ])
                 }
              },
              data: this.barData,
            },
          ],
          title: {
            text: '1911-2015年' + this.value2 + ((this.value2 === '全部') ? '' : '类') + '电影发行总数',
            left: 'center',
            textStyle: {
              color: '#333',
              fontSize: 18,
              fontWeight: 600
            }
          }
        });
      } else if (this.value === '历年各种类型电影数量（饼图）') {
        try {
          const response = await axios.post('http://localhost:8080/chart/chart3',
              {year: this.value3}).then(response => {
            if (id !== this.requestId) return;
            let len = response.data.length;
            console.log(response);
            for (let i = 0; i < len; i++) {
              let count = parseInt(response.data[i].movieID);
              if (count > 0) {
                this.pieData.push({
                  name: response.data[i].genre,
                  value: count
                });
              }
            }
          }, error => {
          })
        } catch (error) {
        }
        
        if (id !== this.requestId) return;

        console.log(this.category);
        console.log(this.pieData);
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
          title: {
            text: (this.value3 === '全部' ? '历' : this.value3) + '年各类型电影发行占比',
            left: 'center',
             textStyle: {
              color: '#333',
              fontSize: 18,
              fontWeight: 600
            }
          }
        });
      } else if (this.value === '我的画像') {
        try {
          const response = await axios.post('http://localhost:8080/chart/figure',
              {nickname: sessionStorage.getItem('nickname')}).then(response => {
            if (id !== this.requestId) return;
            for (let i in response.data) {
              this.keyData.push({
                name: i,
                value: response.data[i]
              });
            }
          }, error => {
          })
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
                  '#FF9A9E', '#FECFEF', '#7EB6FF', '#5F89FF',
                  '#a18cd1', '#fbc2eb', '#8fd3f4', '#84fab0'
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
    }
  },
  async beforeMount() {
    this.nickname = sessionStorage.getItem('nickname');
    try {
      const response = await axios.post('http://localhost:8080/chart/year',
          {}).then(response => {
        let len = response.data.length;
        console.log(response);
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
  }
}
</script>
<style scoped>
.chart-container {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  padding: 32px;
  max-width: 1400px;
  margin: 24px auto;
  min-height: 650px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.header-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
  width: 100%;
}

.chart-title {
  font-size: 28px;
  color: #1f1f1f;
  margin-bottom: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #1f1f1f 0%, #434343 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.controls-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  width: 100%;
  max-width: 1000px;
}

/* Main Chart Selector Style Override */
.main-chart-selector {
  background-color: #f5f7fa;
  border: 1px solid #edf0f5;
  padding: 4px;
  border-radius: 12px;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
}

:deep(.ant-segmented-item-selected) {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08) !important;
  background-color: #fff !important;
  color: #1890ff !important;
  border-radius: 8px !important;
}

:deep(.ant-segmented-item) {
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.filter-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
  animation: fadeIn 0.4s ease-out;
}

.filter-label {
  font-size: 14px;
  color: #8c8c8c;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* Pill Selector for Movie Types */
.pill-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  padding: 8px;
}

.pill-item {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  color: #555;
  background-color: #f7f9fc;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  user-select: none;
}

.pill-item:hover {
  background-color: #e6f7ff;
  color: #1890ff;
  transform: translateY(-1px);
}

.pill-item.active {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  font-weight: 500;
}

/* Custom Select Styling */
:deep(.ant-select-selector) {
  border-radius: 8px !important;
  border-color: #d9d9d9 !important;
  box-shadow: 0 2px 0 rgba(0,0,0,0.02) !important;
}
:deep(.ant-select-focused .ant-select-selector) {
  border-color: #40a9ff !important;
  box-shadow: 0 0 0 2px rgba(24,144,255,0.2) !important;
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
  position: relative;
  background: #ffffff;
  border-radius: 12px;
  /* border: 1px solid #f0f0f0; */
}

.graph-content {
  width: 100%;
  height: 600px;
  border-radius: 12px;
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
  height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
  border-radius: 12px;
  border: 2px dashed #e8e8e8;
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
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.guest-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 12px;
}

.guest-desc {
  font-size: 14px;
  color: #8c8c8c;
  line-height: 1.6;
  margin-bottom: 32px;
}

.guest-btn {
  padding: 0 32px;
  border-radius: 20px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  height: 40px;
}

.guest-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}
</style>