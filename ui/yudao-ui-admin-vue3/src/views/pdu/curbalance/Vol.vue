<template>
  <div ref="chartDom" style="width: 400px; height: 300px;margin-left: 30px;"></div>
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
    { value: props.max.L1, name: 'A相电压', percent: ((props.max.L1 / total) * 100).toFixed(2) + '%' ,itemStyle: { color: '#E5B849' }},
    { value: props.max.L2, name: 'B相电压', percent: ((props.max.L2 / total) * 100).toFixed(2) + '%' ,itemStyle: { color: '#C8603A' }},
    { value: props.max.L3, name: 'C相电压', percent: ((props.max.L3 / total) * 100).toFixed(2) + '%' ,itemStyle: { color: '#AD3762' }}
  ];
});

// 图表配置
const option = {
  tooltip: {
    trigger: 'item',
    formatter: function (params: any) {
      return `${params.name}: ${params.value} V`;
    }
  },
  series: [
    {
      name: 'Access From',
      type: 'pie',
      radius: ['40%', '100%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 40,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
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