<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
import { timelineItemProps } from 'element-plus';
import { max } from 'lodash-es';
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
  choose:String
})
console.log("prop.list===========",prop.list);
// const factorA = prop.list.factorA;
// const factorB = prop.list.factorB;
// const factorTotal = prop.list.factorTotal;
const factorTotal = ref([])
const maxLoadRateTime=ref([])
const minLoadRateTime=ref([])
// const days = prop.list.day;
// console.log('days.value',days)
// console.log('list22222',prop.list)
// let timeOnlyArray = days.map(dateTimeString => {
//   // 使用正则表达式匹配小时和分钟部分（不包括后面的冒号和秒）
//   let match = dateTimeString.match(/(\d{2}:\d{2})(?=:)/);
//   return match ? match[1] : null; // 如果匹配成功，返回小时和分钟部分，否则返回 null
// });
 
// console.log(timeOnlyArray);
const time = ref([])
// const model = ref()
//const newSeriesObject = {...prop.list.series[0]};
//newSeriesObject.name = '负载率曲线'
//model.value = [newSeriesObject];
// model.value = prop.list.series
// 设置饼图的选项
const echartsOption = computed(() => ({
  dataZoom: [{ type: "inside" }],
  tooltip: { trigger: 'axis',formatter:function(params){
    if(prop.choose=='平均'){
      return "平均负载率："+factorTotal.value[params[0].dataIndex]+"% 记录时间："+time.value[params[0].dataIndex];
    }else if(prop.choose=='最大'){
      return "最大负载率："+factorTotal.value[params[0].dataIndex]+"% 发生时间："+ maxLoadRateTime.value[params[0].dataIndex];
    }else if(prop.choose=='最小'){
      return "最小负载率："+factorTotal.value[params[0].dataIndex]+"% 发生时间："+ minLoadRateTime.value[params[0].dataIndex];
    }else if(prop.choose=='实时'){
      return "负载率："+factorTotal.value[params[0].dataIndex]+"% 发生时间："+ time.value[params[0].dataIndex];
    }
  } },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: time.value
  },
  yAxis: {
    type: "value",
    // min: 0,
    // max: 100,
    name: "%",
    axisLine: {
      show: false,
      lineStyle: {
        color: "#000"
      }
    },
    axisLabel: {
      show: true
    }
  },
  series: [
    // {
    //   name: 'A路负载率',
    //   type: 'line', // 或 'bar' 等其他类型，根据您的需求选择
    //   data: factorA
    // },
    // {
    //   name: 'B路负载率',
    //   type: 'line', // 同上
    //   data: factorB
    // },
    {
      name: '负载率',
      type: 'line', // 同上
      data: factorTotal.value,
      symbol: 'none',
    }
  ]
}));
// watchEffect(() => {
//   // 直接访问即可，watchEffect会自动跟踪变化
//   // console.log("watchprop.list===========",prop.list);
//   // series.value = prop.list.series;
//   // if(  series.value != null && series.value?.length > 0){
//   //   legendList.value =  series.value?.map(item => item.name)
//   // }
//   // console.log("watchprop.list===========",prop.list);
//   // time.value = prop.list.time;
//   console.log("prop.list===============================",prop.list)
//   if(prop.list!=null){
//     if(prop.choose=='实时'){
//       factorTotal.value=prop.list.factorTotal;
//     }else if(prop.choose=='最大'){
//       factorTotal.value=prop.list.maxLoadRate;
//       time.value=prop.list.maxLoadRateTime;
//     }else if(prop.choose=='最小'){
//       factorTotal.value=prop.list.minLoadRate;
//       time.value=prop.list.minLoadRateTime;
//     }else if(prop.choose=='平均'){
//       factorTotal.value=prop.list.avgLoadRate;
//       time.value=prop.list.avgLoadRateTime;
//     }
//   }
  
// });
watch(()=>[prop.list,prop.choose],()=>{
  console.log("prop.list===============================",prop.list)
  if(prop.list!=null){
    if(prop.choose=='实时'){
      factorTotal.value=prop.list.factorTotal;
    }else if(prop.choose=='最大'){
      factorTotal.value=prop.list.maxLoadRate;
      maxLoadRateTime.value=prop.list.maxLoadRateTime;
    }else if(prop.choose=='最小'){
      factorTotal.value=prop.list.minLoadRate;
      minLoadRateTime.value=prop.list.minLoadRateTime;
    }else if(prop.choose=='平均'){
      factorTotal.value=prop.list.avgLoadRate;
    }
    time.value=prop.list.day;
  }
},{deep:true})



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>
