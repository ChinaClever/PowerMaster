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
          result += `${params[i].marker} ${params[i].seriesName}：${params[i].value}%RH &nbsp;&nbsp;&nbsp;&nbsp;发生时间：&nbsp;&nbsp;&nbsp;&nbsp;${happenTime.value[i][params[i].dataIndex]} `;
        }else{
          result += `${params[i].marker} ${params[i].seriesName}：${params[i].value}%RH &nbsp;&nbsp;&nbsp;&nbsp;发生时间：${params[0].name}`;
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

  series.value = prop.list.series;

  
  if(series.value != null && series.value?.length > 0){
    legendList.value = series.value?.map(item => item.name)
  }
  
  time.value = prop.list.time;
  happenTime.value = prop.list.series.map(item => item.happenTime);
  console.log(happenTime.value)
 

  
  
});
</script>