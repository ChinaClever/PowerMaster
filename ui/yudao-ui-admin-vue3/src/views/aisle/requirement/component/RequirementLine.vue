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

const series = ref()
const time = ref()
const legendList = ref()
const formatter = ref();

// 设置饼图的选项
const echartsOption = ref({
  dataZoom:[{ type:"inside"}],
  legend: { data: legendList,
    type: 'scroll', // 设置为 'single' 或 'multiple'
    orient: 'horizontal', // 设置为 'horizontal' 或 'vertical'
    width:1000,
    selected:{"A路最大有功功率":false,"B路最大有功功率":false,"A路最大视在功率":false,"B路最大视在功率":false}
  },
  tooltip: { 
    trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' ;
        if (params[i].seriesName.includes("视在功率")) {
          result += params[i].value.toFixed(3) +  ' KVA'; 
        } else if (params[i].seriesName.includes("有功功率")) {
          result += params[i].value.toFixed(3) + ' kW';
        }
        result += " &nbsp&nbsp&nbsp&nbsp发生时间:&nbsp" +  series.value[i].maxTime[params[0].dataIndex];
        result += '<br>';
      }
      return result;
    } 
  },
  xAxis: {type: 'category', boundaryGap: false, data : time},
  yAxis: { 
    type: 'value',
    axisLabel: {
      formatter: formatter
    }},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: series,
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  series.value = prop.list.series;
  if(  series.value != null && series.value?.length > 0){
    legendList.value =  series.value?.map(item => item.name)
  }
  formatter.value = prop.list.formatter;
  time.value = prop.list.time;

});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>