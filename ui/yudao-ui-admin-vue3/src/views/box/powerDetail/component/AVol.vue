<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script lang="ts" setup>
import { defineProps, computed, reactive, onUnmounted } from 'vue';
import 'echarts';

// 定义组件接收的属性
const props = defineProps({
  loadFactor: {
    type: Object as () => { ua: number; ub: number; uc: number; ia?: number; ib?: number; ic?: number },
    required: true
  },
  height: {
    type: [Number, String],
    default: 400
  },
  width: {
    type: [Number, String],
    default: '100%'
  }
});

// 使用 computed 属性来创建 ECharts 配置
const chartOptions = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: (params) => `${params.name}: ${params.value}V`
  },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        position: 'inside',
        formatter: (params) => `${params.value}V`,
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        { value: props.loadFactor.lineVolValue[0], name: 'Ua', itemStyle: { color: '#075F71' } },
        { value: props.loadFactor.lineVolValue[1], name: 'Ub', itemStyle: { color: '#119CB5' } },
        { value: props.loadFactor.lineVolValue[2], name: 'Uc', itemStyle: { color: '#45C0C9' } },
      ]
    }
  ]
}));

onUnmounted(() => {
  console.log('组件已卸载******');
});
</script>

<style lang="scss" scoped>
/* 样式定义 */
</style>