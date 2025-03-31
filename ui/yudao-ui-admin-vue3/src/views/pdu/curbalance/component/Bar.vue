<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script setup lang="ts">
import * as echarts from 'echarts';
import { ref, watch, defineProps, onMounted, onUnmounted, nextTick } from 'vue';

const props = defineProps({
  max: {
    type: Object,
    required: true
  },
  height: {
    type: [Number, String],
    default: '300px'
  },
  width: {
    type: [Number, String],
    default: '300px'
  }
});

console.log('maxdata111111111', props.max);
// 使用 ref 来获取 DOM 元素
const chartDom = ref<HTMLDivElement | null>(null);
let myChart: echarts.ECharts | null = null;

// 计算每个数据项的百分比
const dataWithPercent = ref([
  { value: 0, name: 'A相电流', percent: '0%', itemStyle: { color: '#E5B849' } },
  { value: 0, name: 'B相电流', percent: '0%', itemStyle: { color: '#C8603A' } },
  { value: 0, name: 'C相电流', percent: '0%', itemStyle: { color: '#AD3762' } }
]);

// 图表配置
const echartsOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: function (params: any) {
      return `${params.name}: ${params.value} A`;
    }
  },
  series: [
    {
      name: 'Nightingale Chart',
      type: 'pie',
      radius: [55, 120],
      center: ['50%', '50%'],
      roseType: 'area',
      label: {
        show: true,
        position: 'inside', // 将标签显示在饼图内部
        formatter: (params) => {
          return `${params.value}A`;
        },
        fontSize: 14,
        fontWeight: 'bold'
      },
      itemStyle: {
        borderRadius: 7 // 设置圆角半径为10
      },
      padAngle: 1,
      data: [
        {
          value: props.max.L1,
          name: 'A相电流',
          itemStyle: { color: '#E5B849' }
        },
        {
          value: props.max.L2,
          name: 'B相电流',
          itemStyle: { color: '#C8603A' }
        },
        {
          value: props.max.L3,
          name: 'C相电流',
          itemStyle: { color: '#AD3762' }
        }
      ]
    }
  ]
}));


// 组件卸载时销毁图表
onUnmounted(() => {

});
</script>

<style lang="scss" scoped>
/* 图表容器样式 */
</style>