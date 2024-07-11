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

const temAvgValueA = ref();
const temAvgValueB = ref();
const temAvgValueC = ref();
const temAvgValueN = ref();
const temAvgTime = ref();
const legendList = ref() ;

// 设置饼图的选项
const echartsOption = reactive({
  legend: { data: legendList},
  tooltip: { 
    trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(0) + ' °C';
        result += '<br>';
      }
      return result;
    } 
   },
  xAxis: {type: 'category', boundaryGap: false, data : temAvgTime},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: [
    {name: 'A相温度', type: 'line', symbol: 'circle', symbolSize: 4, data: temAvgValueA},
    {name: 'B相温度', type: 'line', symbol: 'circle', symbolSize: 4, data: temAvgValueB},
    {name: 'C相温度', type: 'line', symbol: 'circle', symbolSize: 4, data: temAvgValueC},
    {name: 'N相温度', type: 'line', symbol: 'circle', symbolSize: 4, data: temAvgValueN},
  ]
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化
  console.log(1)
  temAvgValueA.value = prop.list.temAvgValueA;
  temAvgValueB.value = prop.list.temAvgValueB;
  temAvgValueC.value = prop.list.temAvgValueC;
  temAvgValueN.value = prop.list.temAvgValueN;
  temAvgTime.value = prop.list.temAvgTime;
  if(prop.list.temAvgValueA?.length > 0){
    console.log(2)
    legendList.value = ["A相温度","B相温度","C相温度","N相温度"]; 
  }else {
    console.log(3)
    legendList.value = []
  }
});
onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>