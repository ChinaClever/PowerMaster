<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  max: {
    type: Object,
    required: true
  },
  height: {
    type: [Number,String],
  },
  width: {
    type: [Number,String],
  }
});

// 使用 ref 来获取 DOM 元素
const chartDom = ref<HTMLDivElement | null>(null);
let myChart: echarts.ECharts | null = null;

// 计算每个数据项的百分比
const dataWithPercent = computed(() => {
  const total = props.max.L1 + props.max.L2 + props.max.L3;
  return [
    
    { value: props.max.L1, name: 'A相电压',itemStyle: { color: '#075F71' } },
    { value: props.max.L2, name: 'B相电压' ,itemStyle: { color: '#119CB5' }},
    { value: props.max.L3, name: 'C相电压',itemStyle: { color: '#45C0C9' } }
  ];
});

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
        position: 'inside', // 将标签显示在饼图内部
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
      data: dataWithPercent.value
    }
  ]
});

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