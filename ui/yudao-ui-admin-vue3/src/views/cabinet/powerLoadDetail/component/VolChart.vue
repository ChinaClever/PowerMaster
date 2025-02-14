<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
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

if(props.curChartData != null){
  L1Data.value = props.curChartData.aPathVc.map((item) => item.volValue);
  L2Data.value = props.curChartData.bPathVc.map((item) => item.volValue);
  L3Data.value = props.curChartData.aPathVc.map((item) => item.volValuel);
  L4Data.value = props.curChartData.bPathVc.map((item) => item.volValuel);
  L5Data.value = props.curChartData.aPathVc.map((item) => item.volValuell);
  L6Data.value = props.curChartData.bPathVc.map((item) => item.volValuell);
  createTimeData.value = props.curChartData.aPathVc.map((item) => item.createTime);
}

const chartOptions = {
  title: { text: ''},
  legend: { orient: 'horizontal', right: '25'},
  dataZoom:[{type: "inside"}],
  xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
  yAxis: { 
    type: 'value',
    axisLabel: {
      formatter: function(value) {
          return value + ' V';
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
    {name: 'A-L1', type: 'line', symbol: 'none', data: L1Data.value },
    {name: 'A-L2', type: 'line', symbol: 'none', data: L2Data.value},
    {name: 'A-L3', type: 'line', symbol: 'none', data: L3Data.value},
    {name: 'B-L1', type: 'line', symbol: 'none', data: L4Data.value},
    {name: 'B-L2', type: 'line', symbol: 'none', data: L5Data.value},
    {name: 'B-L3', type: 'line', symbol: 'none', data: L6Data.value},
  ],
}
</script>

<style lang="less" scope>
  
</style>