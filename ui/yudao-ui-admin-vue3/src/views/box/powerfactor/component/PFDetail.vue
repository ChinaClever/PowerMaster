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
    default: 60
  },
  width: {
    type: [Number,String],
    default: 60
  }
})

const powerFactorA = ref()
const powerFactorB = ref()
const powerFactorC = ref()
const time = ref()
const legendList = ref() ;

console.log('prop.list[]',prop.list['1']);

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
  xAxis: {type: 'category', boundaryGap: false, data : time},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: [
    {name: 'A相功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: powerFactorA.value},
    {name: 'B相功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: powerFactorB.value},
    {name: 'C相功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: powerFactorC.value},
  ]
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  powerFactorA.value = prop.list['1'];
  powerFactorB.value = prop.list['2'];
  powerFactorC.value = prop.list['3'];
  time.value = prop.list['1'].create_time;
  if(prop.list.powerFactorAvgValueA?.length > 0){

    legendList.value =  ["A相功率因数","B相功率因数","C相功率因数"]
  }else {

    legendList.value = []
  }
});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>