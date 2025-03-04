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
    formatter: (params) => {
      let unit;
      switch (params.name) {
        case '有功功率':
          unit = 'kW';
          break;
        case '无功功率':
          unit = 'kVar';
          break;
        case '现在功率':
          unit = 'kVA';
          break;
        // case '功率因数':
        //   return `${params.name}: ${params.value.toFixed(2)}`;
      }
      return `${params.name}: ${params.value}${unit}`;
    }
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
        formatter: (params) => `${params.value}`,
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        { value: props.loadFactor.linePowActive[0].toFixed(3), name: '有功功率', itemStyle: { color: '#E5B849' } },
        { value: props.loadFactor.linePowReactive[0].toFixed(3), name: '无功功率', itemStyle: { color: '#C8603A' } },
        { value: props.loadFactor.linePowApparent[0].toFixed(3), name: '现在功率', itemStyle: { color: '#AD3762' } },
        // { value: props.loadFactor.linePowerFactor[0].toFixed(2), name: '功率因数', itemStyle: { color: '#E5B849' } },
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