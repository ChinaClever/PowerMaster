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

const L1Data = ref();
const L2Data = ref();
const createTimeData = ref();
//const L3Data = ref()

//if(props.curChartData.value != null){
//  if(props.timeRadio === '近一小时'){
//    L1Data.value = props.curChartData.value.L1.map((item) => item.vol_value.toFixed(1))
//    L2Data.value = props.curChartData.value.L2.map((item) => item.vol_value.toFixed(1))
//    L3Data.value = props.curChartData.value.L3.map((item) => item.vol_value.toFixed(1))
//  }else{
//    L1Data.value = props.curChartData.value.L1.map((item) => item.vol_value_avg_value.toFixed(1));
//    L2Data.value = props.curChartData.value.L2.map((item) => item.vol_value_avg_value.toFixed(1));
//    L3Data.value = props.curChartData.value.L3.map((item) => item.vol_value_avg_value.toFixed(1));
//  }
//}

if(props.curChartData != null){
  L1Data.value = props.curChartData.a.map((item) => item.volValue);
  L2Data.value = props.curChartData.b.map((item) => item.volValue);
  createTimeData.value = props.curChartData.a.map((item) => item.createTime);
  //L3Data.value = props.curChartData.value.L3.map((item) => item.curValue.toFixed(2));
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
    //{name: 'A-L3', type: 'line', symbol: 'none', data: L3Data.value},
    //{name: 'B-L1', type: 'line', symbol: 'none', data: L1Data.value },
    //{name: 'B-L2', type: 'line', symbol: 'none', data: L2Data.value},
    //{name: 'B-L3', type: 'line', symbol: 'none', data: L3Data.value},
  ],
}
</script>

<style lang="less" scope>
  
</style>