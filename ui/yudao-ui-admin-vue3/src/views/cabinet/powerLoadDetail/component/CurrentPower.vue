<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
const props = defineProps({
  curChartData: {
    type: Array,
    required: true,
  },
  createTimeData:{
    type: Array,
    required: true,
  },
  timeRadio:{
    required: true,
  }
})

const L1Data = ref()
const L2Data = ref()
const L3Data = ref()

if(props.curChartData.value != null){
  if(props.timeRadio === '近一小时'){
    L1Data.value = props.curChartData.value.L1.map((item) => item.pow_apparent.toFixed(3))
    L2Data.value = props.curChartData.value.L2.map((item) => item.pow_apparent.toFixed(3))
    L3Data.value = props.curChartData.value.L3.map((item) => item.pow_apparent.toFixed(3))
  }else{
    L1Data.value = props.curChartData.value.L1.map((item) => item.pow_apparent_avg_value.toFixed(3));
    L2Data.value = props.curChartData.value.L2.map((item) => item.pow_apparent_avg_value.toFixed(3));
    L3Data.value = props.curChartData.value.L3.map((item) => item.pow_apparent_avg_value.toFixed(3));
  }
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
          return value + ' KVA';
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