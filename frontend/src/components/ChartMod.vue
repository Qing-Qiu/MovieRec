<template>
  <div class="chart-container">
    <div class="header-section">
      <h2 class="chart-title">数据可视化仪表盘</h2>
      <div class="controls-wrapper">
        <a-segmented v-model:value="value" :options="data" @change="onChange()" class="main-segment"></a-segmented>
        
        <div v-if="value==='历年各种类型电影数量（柱状图）'" class="sub-controls">
           <span class="label">类型筛选:</span>
           <a-segmented v-model:value="value2" :options="type" @change="onChange()" size="small"></a-segmented>
        </div>

        <div v-if="value === '历年各种类型电影数量（饼图）'" class="sub-controls">
           <span class="label">年份筛选:</span>
           <div class="year-groups">
              <a-select
                  v-model:value="value3"
                  show-search
                  placeholder="选择年份"
                  style="width: 120px"
                  @change="onChange()"
              >
                <a-select-option v-for="y in year" :key="y" :value="y">{{ y }}</a-select-option>
              </a-select>
           </div>
        </div>

        <div v-if="value==='我的画像'" class="sub-controls">
           <template v-if="nickname">
             <span class="label">形状:</span>
             <a-segmented v-model:value="value4" :options="shapes" @change="onChange()" size="small"></a-segmented>
           </template>
           <template v-else>
             <a-button type="primary" @click="login()">登录查看画像</a-button>
           </template>
        </div>
      </div>
    </div>

    <div class="chart-wrapper">
      <a-spin :spinning="loading" style="width: 100%">
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
    async getGraph(id) {
      this.category = [];
      this.lineData = [];
      this.barData = [];
      this.pieData = [];
      this.movieName = [];
      this.keyData = [];
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
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 24px;
  max-width: 1400px;
  margin: 20px auto;
  min-height: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.chart-title {
  font-size: 24px;
  color: #1a1a1a;
  margin-bottom: 20px;
  font-weight: 600;
}

.controls-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.main-segment {
  /* box-shadow: 0 2px 8px rgba(0,0,0,0.04); */
  font-weight: 500;
}

.sub-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  animation: fadeIn 0.3s ease;
}

.label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.year-groups {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.graph-content {
  width: 100%;
  max-width: 1200px;
  height: 550px;
  border-radius: 8px;
  margin: 0 auto;
  /* background: #f9f9f9; */
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-5px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Customizing segmented controls if needed, usually Ant Design default is okay */
:deep(.ant-segmented) {
  background-color: #f0f2f5;
}
:deep(.ant-segmented-item-selected) {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08); 
}

.chart-wrapper {
  width: 100%;
}
</style>