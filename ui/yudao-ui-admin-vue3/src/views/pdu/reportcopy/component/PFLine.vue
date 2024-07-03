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
  legend: { data: legendList,
    type: 'scroll', // 设置为 'single' 或 'multiple'
    orient: 'horizontal', // 设置为 'horizontal' 或 'vertical'
    width:1000
  },
  tooltip: { trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' ;
        if (params[i].seriesName.includes("视在功率")) {
          result += params[i].value.toFixed(3) +  ' kVA'; 
        } else if (params[i].seriesName.includes("有功功率")) {
          result += params[i].value.toFixed(3) + ' kW';
        }
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

  series.value = prop.list.series;
  console.log("series.value",  series.value)
  if(  series.value != null && series.value?.length > 0){
    legendList.value =  series.value?.map(item => item.name)
  }
  series.value.forEach(item => {  
    // 检查 item 是否已经有 markPoint，如果没有则添加  
    if (!item.markPoint) {  
      item.markPoint = {  
        data: [  
          {  
            type: 'max',  
            name: 'Max',
            symbol : "circle",
            symbolSize : 10,  
            label: {  
              show: true,  
              position: 'top',  
              formatter: '{b}: {c}'  
            }
          },  
          {  
            type: 'min',  
            name: 'Min',
            symbol : "circle",
            symbolSize : 10,
            label: {  
              show: true,  

              formatter: '{b}: {c}'  
            }    
            // 自定义样式和其他属性  
          }  
        ]  
      };  
    }  
    // 如果 item 已经有 markPoint，但你想更新它（比如样式），可以在这里做  
  });  
  time.value = prop.list.time;
});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>