<template>
  <a-list :loading="loading">
    <div id="graph">
    </div>
  </a-list>
  <a-segmented v-model:value="value" :options="data" @change="onChange()">
  </a-segmented>
  <br/>
  <a-segmented v-if="value==='历年各种类型电影数量（柱状图）'" v-model:value="value2"
               :options="type" @change="onChange()">
  </a-segmented>
  <div v-if="value === '历年各种类型电影数量（饼图）'">
    <div v-for="(group,index) in groupedOptions" :key="index">
      <a-segmented v-model:value="value3" :options="group"
                   @change="onChange()">
      </a-segmented>
    </div>
  </div>
  <a-segmented v-if="value==='我的画像' && this.nickname" v-model:value="value4"
               :options="shapes" @change="onChange()">
  </a-segmented>
  <a-segmented v-else-if="value==='我的画像'" :options="value5"
               @change="onChange()" @click="login()">
  </a-segmented>
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
    }
  },
  computed: {
    groupedOptions() {
      const groupSize = 15;
      const options = this.year;
      return Array.from({length: Math.ceil(options.length / groupSize)}, (_, index) => {
        const start = index * groupSize;
        return options.slice(start, start + groupSize);
      });
    }
  },
  methods: {
    async onChange() {
      this.loading = true;
      await this.getGraph();
      this.loading = false;
    },
    async getGraph() {
      this.category = [];
      this.lineData = [];
      this.barData = [];
      this.pieData = [];
      this.movieName = [];
      this.keyData = [];
      const echarts = await import('echarts');
      await import('echarts-wordcloud');
      let myChart = echarts.init(document.getElementById('graph'));
      myChart.clear();
      if (this.value === '历年最受欢迎电影') {
        try {
          const response = await axios.post('http://localhost:8080/chart/chart1',
              {}).then(response => {
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
        console.log(this.movieName);
        let movieName = this.movieName;
        myChart.setOption({
          backgroundColor: '#fff',
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            formatter: function (params) {
              let tooltip = params[0].name + '年<br>';
              for (let i = 0; i < params.length; i++) {
                tooltip += params[i].marker + ' ' + params[i].seriesName
                    + ': ' + '&nbsp;&nbsp;<strong>' + params[i].value + '</strong>' + '<br>';
                tooltip += movieName[params[i].dataIndex] + '<br>';
              }
              return tooltip;
            },
          },
          legend: {
            data: ['观看人数'],
            textStyle: {
              color: '#ccc'
            }
          },
          xAxis: {
            data: this.category,
            axisLine: {
              lineStyle: {
                color: '#333'
              }
            }
          },
          yAxis: {
            splitLine: {show: false},
            axisLine: {
              lineStyle: {
                color: '#333'
              }
            }
          },
          series: [
            {
              name: '观看人数',
              type: 'bar',
              barWidth: 10,
              itemStyle: {
                borderRadius: 5,
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {offset: 0, color: '#14c8d4'},
                  {offset: 1, color: '#43eec6'}
                ])
              },
              data: this.barData,
            },
          ],
          title: {
            text: '1911-2015年最受欢迎的电影及其观看人数',
            textStyle: {
              verticalAlign: 'bottom',
            }
          }
        });
      } else if (this.value === '历年各种类型电影数量（柱状图）') {
        try {
          const response = await axios.post('http://localhost:8080/chart/chart2',
              {tag: (this.value2 === '全部' ? '' : this.value2)}).then(response => {
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
        myChart.setOption({
          backgroundColor: '#fff',
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
          },
          legend: {
            data: ['发行数'],
            textStyle: {
              color: '#ccc'
            }
          },
          xAxis: {
            data: this.category,
            axisLine: {
              lineStyle: {
                color: '#333'
              }
            }
          },
          yAxis: {
            splitLine: {show: false},
            axisLine: {
              lineStyle: {
                color: '#333'
              }
            }
          },
          series: [
            {
              name: '发行数',
              type: 'bar',
              barWidth: 10,
              itemStyle: {
                borderRadius: 5,
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {offset: 0, color: '#14c8d4'},
                  {offset: 1, color: '#43eec6'}
                ])
              },
              data: this.barData,
            },
          ],
          title: {
            text: '1911-2015年' + this.value2 + ((this.value2 === '全部') ? '' : '类') + '电影发行总数',
            textStyle: {
              verticalAlign: 'bottom',
            }
          }
        });
      } else if (this.value === '历年各种类型电影数量（饼图）') {
        try {
          const response = await axios.post('http://localhost:8080/chart/chart3',
              {year: this.value3}).then(response => {
            let len = response.data.length;
            console.log(response);
            for (let i = 0; i < len; i++) {
              // this.category.push(response.data[i].genre);
              this.pieData.push({
                name: response.data[i].genre,
                value: parseInt(response.data[i].movieID)
              });
            }
          }, error => {
          })
        } catch (error) {
        }
        console.log(this.category);
        console.log(this.pieData);
        myChart.setOption({
          backgroundColor: '#fff',
          tooltip: {
            trigger: 'item', // You can customize the trigger type as needed
            formatter: '{b}: {c} ({d}%)', // Customize the tooltip content
          },
          legend: {
            data: this.pieData,
            textStyle: {
              color: '#ccc',
              fontSize: 12,
            }
          },
          series: [
            {
              type: 'pie',
              data: this.pieData,
              label: {
                show: true,
                formatter: '{b}: {c} ({d}%)',
              }
            },
          ],
          title: {
            text: (this.value3 === '全部' ? '历' : this.value3) + '年各类型电影发行总数',
            textStyle: {
              verticalAlign: 'bottom',
            }
          }
        });
      } else if (this.value === '我的画像') {
        try {
          const response = await axios.post('http://localhost:8080/chart/figure',
              {nickname: sessionStorage.getItem('nickname')}).then(response => {
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
                return 'rgb(' + [
                  Math.round(Math.random() * 160),
                  Math.round(Math.random() * 160),
                  Math.round(Math.random() * 160)
                ].join(',') + ')';
              }
            },
            emphasis: {
              // focus: 'self',
              textStyle: {
                textShadowBlur: 5,
                textShadowColor: '#333'
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
    await this.getGraph();
    this.loading = false;
  }
}
</script>
<style scoped>
#graph {
  width: 1200px;
  height: 500px;
  margin-left: auto;
  margin-right: auto;
}

.demo-table-expand {
  font-size: 0;
}

.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}

.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}
</style>