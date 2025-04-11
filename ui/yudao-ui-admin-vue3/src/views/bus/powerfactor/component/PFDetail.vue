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

// 设置饼图的选项
const echartsOption = ref({
  legend: { data: legendList},
  tooltip: { 
    trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName;
        if(prop.list[params[i].dataIndex]?.powerFactorTime?.[i]) {
          result += '&nbsp发生时间:' + prop.list[params[i].dataIndex].powerFactorTime[i]
        }
        result += '&nbsp&nbsp' + params[i].value
        result += '<br>';
      }
      return result;
    } 
   },
  xAxis: {type: 'category', boundaryGap: false, data : time},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: [
    {name: 'A相功率因素', type: 'line', symbol: 'circle', symbolSize: 4, data: powerFactorA},
    {name: 'B相功率因素', type: 'line', symbol: 'circle', symbolSize: 4, data: powerFactorB},
    {name: 'C相功率因素', type: 'line', symbol: 'circle', symbolSize: 4, data: powerFactorC},
  ]
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  powerFactorA.value = prop.list.map(obj => obj.powerFactorValueA);
  powerFactorB.value = prop.list.map(obj => obj.powerFactorValueB);
  powerFactorC.value = prop.list.map(obj => obj.powerFactorValueC);
  time.value = prop.list.map(obj => obj.time);
  if(powerFactorA.value?.length > 0){

    legendList.value =  ["A相功率因素","B相功率因素","C相功率因素"]
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