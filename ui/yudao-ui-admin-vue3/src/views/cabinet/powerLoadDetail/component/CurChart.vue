<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  curChartData: Object,
});

const L1Data = ref([]);
const L2Data = ref([]);
const L3Data = ref([]);
const L4Data = ref([]);
const L5Data = ref([]);
const L6Data = ref([]);
const createTimeData = ref([]);

console.log(props.curChartData?.aPathVc,!props.curChartData?.aPathVc,props.curChartData?.bPathVc)
if(!props.curChartData?.aPathVc || !props.curChartData?.aPathVc.length) {
  L1Data.value = 0
  L2Data.value = 0
  L3Data.value = 0
}
else if (props.curChartData?.aPathVc) {
  L1Data.value = props.curChartData.aPathVc.map((item) => item.curValue);
  L2Data.value = props.curChartData.aPathVc.map((item) => item.curValuell || 0);
  L3Data.value = props.curChartData.aPathVc.map((item) => item.curValuelll || 0);
  createTimeData.value = props.curChartData.aPathVc.map((item) => item.createTime);
}

if(!props.curChartData?.bPathVc || !props.curChartData?.bPathVc.length) {
  L4Data.value = 0
  L5Data.value = 0
  L6Data.value = 0
}
else if (props.curChartData?.bPathVc) {
  L4Data.value = props.curChartData.bPathVc.map((item) => item.curValue || 0);
  L5Data.value = props.curChartData.bPathVc.map((item) => item.curValuell || 0);
  L6Data.value = props.curChartData.bPathVc.map((item) => item.curValuelll || 0);
  createTimeData.value = props.curChartData.bPathVc.map((item) => item.createTime);
}
console.log(L1Data.value,L2Data.value,L3Data.value,L4Data.value)

const chartOptions = ref({
  title: { text: '' },
  legend: { orient: 'horizontal', right: '25' },
  tooltip: {
    trigger: 'axis', // 触发方式：坐标轴触发
    formatter: function (params) {
      // params 是一个数组，包含当前悬停点的所有系列信息
      let result = `${params[0].axisValue}<br>`; // 显示时间
      params.forEach((item) => {
        result += `${item.seriesName}: ${item.value}<br>`; // 显示系列名称和值
      });
      return result;
    },
  },
  dataZoom: [{ type: 'inside' }],
  xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: function (value) {
        return value + ' A';
      },
    },
  },
  grid: {
    left: '5%', // 设置左侧边距
    right: '5%', // 设置右侧边距
    top: '10%', // 设置上侧边距
    bottom: '10%', // 设置下侧边距
  },
  series: [
    { name: 'A-L1', type: 'line', symbol: 'none', data: L1Data.value },
    { name: 'A-L2', type: 'line', symbol: 'none', data: L2Data.value },
    { name: 'A-L3', type: 'line', symbol: 'none', data: L3Data.value },
    { name: 'B-L1', type: 'line', symbol: 'none', data: L4Data.value },
    { name: 'B-L2', type: 'line', symbol: 'none', data: L5Data.value },
    { name: 'B-L3', type: 'line', symbol: 'none', data: L6Data.value },
  ],
});


watch(
  () => props.curChartData,
  (newData, oldData) => {
    if (newData != null) {
      if(!props.curChartData?.aPathVc || !props.curChartData?.aPathVc.length) {
        L1Data.value = 0
        L2Data.value = 0
        L3Data.value = 0
      }
      else if (props.curChartData?.aPathVc) {
        L1Data.value = props.curChartData.aPathVc.map((item) => item.curValue);
        L2Data.value = props.curChartData.aPathVc.map((item) => item.curValuell || 0);
        L3Data.value = props.curChartData.aPathVc.map((item) => item.curValuelll || 0);
        createTimeData.value = props.curChartData.aPathVc.map((item) => item.createTime);
      }

      if(!props.curChartData?.bPathVc || !props.curChartData?.bPathVc.length) {
        L4Data.value = 0
        L5Data.value = 0
        L6Data.value = 0
      }
      else if (props.curChartData?.bPathVc) {
        L4Data.value = props.curChartData.bPathVc.map((item) => item.curValue);
        L5Data.value = props.curChartData.bPathVc.map((item) => item.curValuell || 0);
        L6Data.value = props.curChartData.bPathVc.map((item) => item.curValuelll || 0);
        createTimeData.value = props.curChartData.bPathVc.map((item) => item.createTime);
      }
      chartOptions.value = {
        ...chartOptions.value,
        xAxis: { ...chartOptions.value.xAxis, data: createTimeData.value },
        series: [
          { ...chartOptions.value.series[0], data: L1Data.value },
          { ...chartOptions.value.series[1], data: L2Data.value },
          { ...chartOptions.value.series[2], data: L3Data.value },
          { ...chartOptions.value.series[3], data: L4Data.value },
          { ...chartOptions.value.series[4], data: L5Data.value },
          { ...chartOptions.value.series[5], data: L6Data.value },
        ],
      };
    }
  },
  { deep: true }
);
</script>

<style lang="less" scoped>
</style>
