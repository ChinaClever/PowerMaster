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
const processedIndices = new Set();
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
  tooltip: { trigger: 'axis' },
  xAxis: {type: 'category', boundaryGap: true, data : time},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: series,
})

watchEffect(() => {
  const newSeries = [...prop.list.series];
 
  // 遍历新的series数组
  newSeries.forEach((item, index) => {
    // 检查这个索引是否已经被处理过
    if (!processedIndices.has(index)) {
      // 如果没有，处理这个series项
      item.data = item.data.map(value => Math.round(value / 100 * 100) / 100);
      // 将这个索引添加到已处理的集合中
      processedIndices.add(index);
    }
  });

  series.value = newSeries;
  legendList.value = series.value.map(item => item.name);
  time.value = prop.list.time;

});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>