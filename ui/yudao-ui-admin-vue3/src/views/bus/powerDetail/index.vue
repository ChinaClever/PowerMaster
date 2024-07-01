<template>
  <div>
    <el-row>
      <el-col :span="7">
        <div ref="gaugeChartContainer" id="gaugeChartContainer" style="width: 400px; height: 400px"></div>
     </el-col>
      <el-col :span="17">
        <el-row>
          <el-col :span="6">1</el-col>
          <el-col :span="6">2</el-col>
          <el-col :span="6">3</el-col>
          <el-col :span="6">4</el-col>
        </el-row>
        <el-row>1</el-row>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts';
import { ref } from 'vue'

const instance = getCurrentInstance();
const gaugeChartContainer = ref<HTMLElement | null>(null);
let gaugeChart = null as echarts.ECharts | null; 
const initGaugeChart = () => {
  if (gaugeChartContainer.value && instance) {
    gaugeChart = echarts.init(gaugeChartContainer.value);
    gaugeChart.setOption(
      {
        series: [
          {
            type: 'gauge',
            axisLine: {
              lineStyle: {
                width: 10,
                color: [
                  [0.4, '#67e0e3'],
                  [0.7, '#37a2da'],
                  [1, '#fd666d']
                ]
              }
            },
            pointer: {
              itemStyle: {
                color: 'auto'
              }
            },
            axisLabel: {
              color: 'inherit',
              fontSize: 16
            },
            detail: {
              valueAnimation: true,
              formatter: '{value} %',
              color: 'inherit'
            },
            data: [
              {
                value: 70,
                name: '负载率',
              }
            ]
          }
        ]
      }
    );
    instance.appContext.config.globalProperties.gaugeChart = gaugeChart;
  }
}


   
/** 初始化 **/
onMounted(async () => {
  // await getDetailData();
  // await getLineChartData();
  initGaugeChart();
  // initChart1();
  // initChart2();


})


</script>


<style scoped>

</style>