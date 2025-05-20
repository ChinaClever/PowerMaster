<!-- <template>
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
  legend: { data: legendList,
    type: 'scroll', // 设置为 'single' 或 'multiple'
    orient: 'horizontal', // 设置为 'horizontal' 或 'vertical'
    width:1000
  },
  tooltip: { trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(0) + '°C' ;
        result += '<br>';
      }
      return result;
    } 
  },
  xAxis: {type: 'category', boundaryGap: false, data : time},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: series,
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化
  console.log(prop.list.series)
  series.value = prop.list.series;
  console.log(series.value)
  if(  series.value != null && series.value?.length > 0){
    legendList.value =  series.value?.map(item => item.name)
  }
      series.value.forEach(item => {  
    // 检查 item 是否已经有 markPoint，如果没有则添加  
                 item.symbol = "circle",
                 item.symbolSize = 4
  }); 
  time.value = prop.list.time;
});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style> -->

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
  },
  dataType:{
      type : Number,
      default : 1
  }
})

const series = ref()
const time = ref()
const happenTime = ref()
const legendList = ref()

// 设置饼图的选项
const echartsOption = ref({
  dataZoom: [{ type: "inside" }],
  color:['#E5B849','#C8603A','#5337A9','#B47660'],
  legend: { 
    data: legendList,
    type: 'scroll',
    orient: 'horizontal',
    width: 1000
  },
  tooltip: { 
    trigger: 'axis',
    formatter: function(params) {

      console.log('happenTime',happenTime);
      
      // 获取当前数据点的索引
      // const dataIndex = params[0].dataIndex;
      // 获取对应的 happenTime
      // const currentHappenTime = happenTime.value?.[dataIndex] || '';
      
      let result = `${params[0].name}<br>`;
      console.log('Tooltip params:', params);
      for (let i = 0; i < params.length; i++) {
        if(prop.dataType != 0){
          result += `${params[i].marker} ${params[i].seriesName}： ${params[i].value.toFixed(3)}kW &nbsp;&nbsp;&nbsp;&nbsp;发生时间：${happenTime.value[i][params[i].dataIndex]}`;

        }else{
          result += `${params[i].marker} ${params[i].seriesName}： ${params[i].value.toFixed(3)}kW &nbsp;&nbsp;&nbsp;&nbsp;发生时间：${params[0].name}`;

        }
        result += '<br>';
      }
      return result;
    } 
  },
  grid: { left: '5%', right: '5%' },
  xAxis: { 
    type: 'category', 
    boundaryGap: false, 
    data: time 
  },
  yAxis: { type: 'value' },
  toolbox: {
    feature: { 
      saveAsImage: {},
      dataView: {},
      dataZoom: {},
      restore: {},
    }
  },
  series: series,
})

watchEffect(() => {

  series.value = prop.list?.series;

  
  if(series.value != null && series.value?.length > 0){
    legendList.value = series.value?.map(item => item.name)
  }
  
  time.value = prop.list?.time;
  happenTime.value = prop.list?.series.map(item => item.happenTime);
  console.log(happenTime.value)
});
</script>