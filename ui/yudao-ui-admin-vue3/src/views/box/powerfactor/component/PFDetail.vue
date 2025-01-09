<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
const prop = defineProps({
  list: {
    type: Object,
    required: true
  },
  height: {
    type: [Number,String],
  },
  width: {
    type: [Number,String],
  }
})

const legendList = ref() ;

console.log('prop.list[]',prop.list);

// 设置饼图的选项
const echartsOption = ref({
  legend: { data: legendList},
  tooltip: { 
    trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(2) ;
        result += '<br>';
      }
      return result;
    } 
   },
  xAxis: {type: 'category', boundaryGap: false, data : prop.list.time},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: [
    {name: '输出位1功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: prop.list['1']},
    {name: '输出位2功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: prop.list['2']},
    {name: '输出位3功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: prop.list['3']},
  ]
})

onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>