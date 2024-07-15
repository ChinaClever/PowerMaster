<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
const prop = defineProps({
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
const L1 = ref();
const L2 = ref();
const L3 = ref();
// 设置饼图的选项
const echartsOption = reactive({
  series: [
    {
      type: 'pie',
      data: [
        { name: 'L1', value: L1 },
        { name: 'L2', value: L2 },
        { name: 'L3', value: L3 }
      ],
      label: {
        show: false // 设置为false，不显示饼图上的名称
      },
      radius: ['40%', '65%'],
      emphasis:{disabled:false,scale:false,scaleSize:0,},
      tooltip: {
        trigger: 'item',
      }
    }
  ]
})

watchEffect(() =>  {
  L1.value = prop.max.L1
  L2.value = prop.max.L2
  L3.value = prop.max.L3
})
onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>