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

// 设置饼图的选项
const echartsOption = ref({
  dataZoom:[{ type:"inside"}],
  tooltip: { trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + "消耗电能" + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(1) + ' kWh' ;
        result += '<br>';
      }
      return result;
    } 
  },
  grid:{left:'3%',right:'3%'},
  xAxis: {type: 'category', boundaryGap: true, data : time},
  yAxis: { type: 'value' , name : "kWh"},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: series,
})

// watchEffect(() => {
//   // 直接访问即可，watchEffect会自动跟踪变化

//   series.value = prop.list.series;
//   if(  series.value != null && series.value?.length > 0){
//     legendList.value =  series.value?.map(item => item.name)
//   }
//   time.value = prop.list.time;
// });
watchEffect(() => {
  series.value = prop.list.series;
  if (series.value != null && series.value?.length > 0) {
    legendList.value = series.value?.map(item => item.name);
    series.value.forEach(item => {
      item.label = {
        show: true,
        position: 'top',
        formatter: (params) => `${params.value.toFixed(1)} kWh`, // 保留1位小数
        color: '#333',
        fontSize: 12,
      };
    });
  }
  time.value = prop.list.time;
});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>