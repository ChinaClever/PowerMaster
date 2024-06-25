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

const harmonicRealTime = ref()
const times = ref()

// 设置饼图的选项
const echartsOption = ref({

  tooltip: { trigger: 'axis' },
  xAxis: {type: 'category', boundaryGap: false, data : times},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: [
    {type: 'bar', data: harmonicRealTime},

  ]
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  harmonicRealTime.value = prop.list.harmonicList;
  times.value = prop.list.times;

});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>