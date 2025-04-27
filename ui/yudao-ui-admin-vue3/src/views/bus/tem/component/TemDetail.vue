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

const temValueA = ref();
const temValueB = ref();
const temValueC = ref();
const temValueN = ref();
const time = ref()
const legendList = ref() ;

// 设置饼图的选项
const echartsOption = reactive({
  legend: { data: legendList},
  tooltip: { 
    trigger: 'axis',
    formatter: function(params) {
      var result = '记录时间：' + params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName;
        if(prop.list[params[i].dataIndex]?.tem_time?.[i]) {
          result += '&nbsp发生时间:' + prop.list[params[i].dataIndex].tem_time[i]
        }
        result += '&nbsp&nbsp' + params[i].value + ' °C'
        result += '<br>';
      }
      return result;
    } 
   },
  xAxis: {type: 'category', boundaryGap: false, data : time},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: [
    {name: 'A相温度', type: 'line', symbol: 'circle', symbolSize: 4, data: temValueA},
    {name: 'B相温度', type: 'line', symbol: 'circle', symbolSize: 4, data: temValueB},
    {name: 'C相温度', type: 'line', symbol: 'circle', symbolSize: 4, data: temValueC},
    {name: 'N相温度', type: 'line', symbol: 'circle', symbolSize: 4, data: temValueN},
  ]
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  temValueA.value = prop.list.map(obj => obj.tem_a_value);
  temValueB.value = prop.list.map(obj => obj.tem_b_value);
  temValueC.value = prop.list.map(obj => obj.tem_c_value);
  temValueN.value = prop.list.map(obj => obj.tem_n_value);
  time.value = prop.list.map(obj => obj.time);
  if(temValueA.value?.length > 0){
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