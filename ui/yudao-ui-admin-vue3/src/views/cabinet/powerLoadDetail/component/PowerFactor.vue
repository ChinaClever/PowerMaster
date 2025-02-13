<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
const props = defineProps({
  curChartData: {
    type: Array,
    required: true,
  },
  timeRadio:{
    required: true,
  }
})

console.log('props',props.curChartData);
const L1Data = ref([]);
const L2Data = ref([]);
const L3Data = ref([]);
const createTimeData = ref([]);

if(props.curChartData != null){
  L1Data.value = props.curChartData.aPath.map((item) => item.powerFactorA);
  L2Data.value = props.curChartData.aPath.map((item) => item.powerFactorB);
  L3Data.value = props.curChartData.aPath.map((item) => item.powerFactorTotal);
  createTimeData.value = props.curChartData.aPath.map((item) => item.createTime);
}

const chartOptions = {
  title: { text: ''},
  legend: { orient: 'horizontal', right: '25'},
  dataZoom:[{type: "inside"}],
  xAxis: {type: 'category', boundaryGap: false, data:props.createTimeData},
  yAxis: { 
    type: 'value',
    axisLabel: {
      formatter: function(value) {
          return value;
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
    {name: 'L1', type: 'line', symbol: 'none', data: L1Data.value },
    {name: 'L2', type: 'line', symbol: 'none', data: L2Data.value},
    {name: 'L3', type: 'line', symbol: 'none', data: L3Data.value},
  ],
}
</script>

<style lang="less" scope>
  
</style>