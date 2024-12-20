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
        { value: props.loadFactor.ia || 0, name: 'Ia', itemStyle: { color: '#075F71' } },
        { value: props.loadFactor.ib || 0, name: 'Ib', itemStyle: { color: '#119CB5' } },
        { value: props.loadFactor.ic || 0, name: 'Ic', itemStyle: { color: '#45C0C9' } },
      ]
    }
  ]
});

watch(() => props.loadFactor, (newVal) => {
  echartsOption.series[0].data = [
    { value: newVal.ua, name: 'Ia' },
    { value: newVal.ub, name: 'Ib' },
    { value: newVal.uc, name: 'Ic' },
  ];
}, { deep: true }); // 添加 deep: true 以深度监听 loadFactor 对象的变化

onUnmounted(() => {
  console.log('onUnmounted******')
})
</script>

<style lang="scss" scoped>

</style>