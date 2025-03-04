<template>
  <div ref="chartDom" style="width: 100px; height: 100px;"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  max: {
    type: Object,
    required: true
  }
});

// 使用 ref 来获取 DOM 元素
const chartDom = ref<HTMLDivElement | null>(null);
let myChart: echarts.ECharts | null = null;

// 计算每个数据项的百分比
const dataWithPercent = computed(() => {
  const total = props.max.L1 + props.max.L2 + props.max.L3;
  return [
    { value: props.max.L1, name: 'L1', percent: ((props.max.L1 / total) * 100).toFixed(2) + '%' },
    { value: props.max.L2, name: 'L2', percent: ((props.max.L2 / total) * 100).toFixed(2) + '%' },
    { value: props.max.L3, name: 'L3', percent: ((props.max.L3 / total) * 100).toFixed(2) + '%' }
  ];
});

// 图表配置
const option = {
  tooltip: {
    trigger: 'item',
    formatter: function (params: any) {
      return `${params.name}: ${params.value} (${params.percent}%)`;
    }
  },
  series: [
    {
      name: 'Nightingale Chart',
      type: 'pie',
      radius: [15, 33],
      center: ['50%', '50%'],
      roseType: 'area',
      label: {
        show: false // 不在线上显示标签
      },
      itemStyle: {
        borderRadius: 3// 设置圆角半径为10
      },
      padAngle: 10,
      data: dataWithPercent.value
    }
  ]
};

// 组件挂载时初始化图表
onMounted(() => {
  if (chartDom.value) {
    myChart = echarts.init(chartDom.value);
    myChart.setOption(option);
  }
});

// 组件卸载时销毁图表
onUnmounted(() => {
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
});
</script>