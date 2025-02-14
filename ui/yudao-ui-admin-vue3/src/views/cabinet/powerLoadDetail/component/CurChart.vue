<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
import { string } from "vue-types"

const props = defineProps({
  curChartData: {
    type: Array,
    required: true,
  },
  //createTimeData:{
  //  type: Array,
  //  required: true,
  //},
  timeRadio:{
    required: true,
  }
})

const L1Data = ref([]);
const L2Data = ref([]);
const L3Data = ref([]);
const L4Data = ref([]);
const L5Data = ref([]);
const L6Data = ref([]);
const createTimeData = ref([]);

console.log('curChartData',props.curChartData);

if(props.curChartData != null){
  L1Data.value = props.curChartData.aPathVc.map((item) => item.curValue);
  L2Data.value = props.curChartData.bPathVc.map((item) => item.curValue);
  L3Data.value = props.curChartData.aPathVc.map((item) => item.curValuel);
  L4Data.value = props.curChartData.bPathVc.map((item) => item.curValuel);
  L5Data.value = props.curChartData.aPathVc.map((item) => item.curValuell);
  L6Data.value = props.curChartData.bPathVc.map((item) => item.curValuell);
  createTimeData.value = props.curChartData.aPathVc.map((item) => item.createTime);
}

console.log('L1Data', L1Data.value);
console.log('L2Data', L2Data.value);
console.log('createTimeData', createTimeData.value);

const chartOptions = {
  title: { text: ''},
  legend: { orient: 'horizontal', right: '25'},
  dataZoom:[{type: "inside"}],
  xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
  yAxis: { 
    type: 'value',
    axisLabel: {
      formatter: function(value) {
          return value + ' A';
      }
    }
  },
  grid: {
    left: '5%',   // 设置左侧边距
    right: '5%',  // 设置右侧边距
    top: '10%',    // 设置上侧边距
    bottom: '10%', // 设置下侧边距
  },
  series: [
    {name: 'A-L1', type: 'line', symbol: 'none', data: L1Data.value},
    {name: 'A-L2', type: 'line', symbol: 'none', data: L2Data.value},
    {name: 'A-L3', type: 'line', symbol: 'none', data: L3Data.value},
    {name: 'B-L1', type: 'line', symbol: 'none', data: L4Data.value },
    {name: 'B-L2', type: 'line', symbol: 'none', data: L5Data.value},
    {name: 'B-L3', type: 'line', symbol: 'none', data: L6Data.value},
  ],
}


</script>

<style lang="less" scope>
  
</style>