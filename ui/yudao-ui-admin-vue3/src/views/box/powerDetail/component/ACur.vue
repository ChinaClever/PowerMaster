<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
import { reactive, watch, defineProps, onUnmounted } from 'vue';
const props = defineProps({
  loadFactor: {
    type: Number,
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
const echartsOption = reactive({
  tooltip: {
    trigger: 'item',
    formatter: '{b} : {c}'
  },
  series: [
    {
      type: 'pie',
      radius: [20, 80],
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
        { value: props.loadFactor.lineCurValue[0], name: 'Ia', itemStyle: { color: '#075F71' } },
        { value: props.loadFactor.lineCurValue[1], name: 'Ib', itemStyle: { color: '#119CB5' } },
        { value: props.loadFactor.lineCurValue[2], name: 'Ic', itemStyle: { color: '#45C0C9' } },
      ]
    }
  ]
});

onUnmounted(() => {
  console.log('onUnmounted******')
})
</script>

<style lang="scss" scoped>

</style>