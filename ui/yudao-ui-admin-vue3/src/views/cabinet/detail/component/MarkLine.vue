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
const factorA = prop.list.factorA;
const factorB = prop.list.factorB;
const factorTotal = prop.list.factorTotal;
const days = prop.list.day;
let timeOnlyArray = days.map(dateTimeString => {
  // 使用正则表达式匹配小时和分钟部分（不包括后面的冒号和秒）
  let match = dateTimeString.match(/(\d{2}:\d{2})(?=:)/);
  return match ? match[1] : null; // 如果匹配成功，返回小时和分钟部分，否则返回 null
});
const series = ref()
const time = ref()
const legendList = ref()
// 设置饼图的选项
const echartsOption = reactive({
  dataZoom: [{ type: "inside" }],
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: timeOnlyArray
  },
  yAxis: {
    type: "value",
    min: 0,
    max: 100,
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
    {
      name: 'Factor A',
      type: 'line', // 或 'bar' 等其他类型，根据您的需求选择
      data: factorA
    },
    {
      name: 'Factor B',
      type: 'line', // 同上
      data: factorB
    },
    {
      name: 'Factor Total',
      type: 'line', // 同上
      data: factorTotal
    }
  ]
});
watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  series.value = prop.list.series;
  if(  series.value != null && series.value?.length > 0){
    legendList.value =  series.value?.map(item => item.name)
  }

  time.value = prop.list.time;

});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>