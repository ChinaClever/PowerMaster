<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
import { reactive, watch, defineProps, onUnmounted } from 'vue';
const props = defineProps({
  loadFactor: {
    type: Object as () => { ua: number; ub: number; uc: number; ia?: number; ib?: number; ic?: number },
    required: true
  },
  height: {
    type: [Number,String],
  },
  width: {
    type: [Number,String],
  }
})
console.log('loadFactor',props.loadFactor)


// 设置饼图的选项
// 设置饼图的选项
const echartsOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: (params) => `${params.name}: ${params.value}A`
  },
  series: [
    {
      type: 'pie',
      radius: ['25%', '75%'],
      center: ['50%', '40%'],
      roseType: 'radius',
      itemStyle: {
        borderRadius: 5
      },
      label: {
        show: true,
        position: 'inside', // 将标签显示在饼图内部
        formatter: (params) => {
          return `${params.value}A`;
        },
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        { value: props.loadFactor.lineCurValue[0].toFixed(2), name: 'Ia', itemStyle: { color: '#E5B849' } },
        { value: props.loadFactor.lineCurValue[1].toFixed(2), name: 'Ib', itemStyle: { color: '#C8603A' } },
        { value: props.loadFactor.lineCurValue[2].toFixed(2), name: 'Ic', itemStyle: { color: '#AD3762' } },
      ]
    }
  ]
}));

onUnmounted(() => {
  console.log('onUnmounted******')
})
</script>

<style lang="scss" scoped>

</style>