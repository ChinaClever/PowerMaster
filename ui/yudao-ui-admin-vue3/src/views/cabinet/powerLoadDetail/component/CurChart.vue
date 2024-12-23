<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
const props = defineProps({
  curChartData: {
    type: Array,
    required: true
  },
  createTimeData:{
    
    type: Array,
    required: true
  }
})

const L1Data = ref()
const L2Data = ref()
const L3Data = ref()

console.log('curChartData',props.curChartData.value)
console.log('createTimeData',props.createTimeData)

if(props.curChartData.value != null){
  L1Data.value = props.curChartData.value.L1.map((item) => item.cur_value.toFixed(2));
  L2Data.value = props.curChartData.value.L2.map((item) => item.cur_value.toFixed(2));
  L3Data.value = props.curChartData.value.L3.map((item) => item.cur_value.toFixed(2));
}

console.log('L1Data',L1Data.value)
console.log('L2Data',L2Data.value)
console.log('L3Data',L3Data.value)

const chartOptions = {
  title: { text: ''},
  legend: { orient: 'horizontal', right: '25'},
  dataZoom:[{type: "inside"}],
  xAxis: {type: 'category', boundaryGap: false, data:props.createTimeData},
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
    {name: 'A-L1', type: 'line', symbol: 'none', data: L1Data.value },
    {name: 'A-L2', type: 'line', symbol: 'none', data: L2Data.value},
    {name: 'A-L3', type: 'line', symbol: 'none', data: L3Data.value},
    {name: 'B-L1', type: 'line', symbol: 'none', data: L1Data.value },
    {name: 'B-L2', type: 'line', symbol: 'none', data: L2Data.value},
    {name: 'B-L3', type: 'line', symbol: 'none', data: L3Data.value},
  ],
}
</script>

<style lang="less" scope>
  
</style>