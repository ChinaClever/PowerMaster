<template>
  <div ref="chartDom" :style="{ height, width }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, reactive } from 'vue';
import * as echarts from 'echarts';

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

// 使用 ref 来获取 DOM 元素
const chartDom = ref<HTMLDivElement | null>(null);
let myChart: echarts.ECharts | null = null;

// 图表配置
const echartsOption = reactive({
  tooltip: {
    trigger: 'item'
  },
  series: [
    {
      type: 'pie',
      radius: ['40%', '90%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 7,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        position: 'inside',
        formatter: (params) => {
          return `${params.value}V`;
        },
        fontSize: 14,
        fontWeight: 'bold'
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 40,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      padAngle: 1,
      data: []
    }
  ]
});

// 初始化图表
const initChart = () => {
  if (chartDom.value) {
    myChart = echarts.init(chartDom.value);
    myChart.setOption(echartsOption);
  }
};

// 更新图表数据
const updateChart = () => {
  if (myChart) {
    myChart.setOption({
      series: [
        {
          data: echartsOption.series[0].data
        }
      ]
    });
  }
};

// 计算每个数据项的百分比
watch(
  () => props.max,
  (newMax) => {
    const total = newMax.L1 + newMax.L2 + newMax.L3;
    echartsOption.series[0].data = [
      { value: newMax.L1, name: 'A相电压', itemStyle: { color: '#075F71' } },
      { value: newMax.L2, name: 'B相电压', itemStyle: { color: '#119CB5' } },
      { value: newMax.L3, name: 'C相电压', itemStyle: { color: '#45C0C9' } }
    ];
    updateChart();
  },
  { deep: true }
);

// 组件挂载时初始化图表
onMounted(() => {
  initChart();
  // 初始化数据
  watch(
    () => props.max,
    { immediate: true }
  );
});

// 组件卸载时销毁图表
onUnmounted(() => {
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
});
</script>