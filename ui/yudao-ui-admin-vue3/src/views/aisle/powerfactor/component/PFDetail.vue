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

const powerFactorTotal = ref()
const powerFactorA = ref()
const powerFactorB = ref()
const time = ref()
const legendList = ref() ;

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
    {name: '总功率因素', type: 'line', symbol: 'circle', symbolSize: 4, data: powerFactorTotal},
    {name: 'A路功率因素', type: 'line', symbol: 'circle', symbolSize: 4, data: powerFactorA},
    {name: 'B路功率因素', type: 'line', symbol: 'circle', symbolSize: 4, data: powerFactorB},
  ]
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  powerFactorTotal.value = prop.list.factorTotalAvgValue;
  powerFactorA.value = prop.list.factorAAvgValue;
  powerFactorB.value = prop.list.factorBAvgValue;
  time.value = prop.list.time;
  if(prop.list.factorTotalAvgValue?.length > 0){
    legendList.value =  ["总功率因素","A路功率因素","B路功率因素"]
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