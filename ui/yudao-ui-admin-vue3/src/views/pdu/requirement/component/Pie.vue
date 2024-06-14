<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
const {max} = defineProps({
  max: {
    type: Object,
    required: true
  },
  height: {
    type: Number,
    default: 60
  },
  width: {
    type: Number,
    default: 60
  }
})
console.log("max",max)
// 设置饼图的选项
const echartsOption = reactive({
  series: [
    {
      type: 'pie',
      data: [
        { name: 'L1', value: max.L1 },
        { name: 'L2', value: max.L2 },
        { name: 'L3', value: max.L3 }
      ],
      label: {
        show: false // 设置为false，不显示饼图上的名称
      },
      radius: '100%'
    }
  ]
})
onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>