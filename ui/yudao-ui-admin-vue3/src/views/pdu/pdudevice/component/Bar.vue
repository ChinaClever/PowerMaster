<template>
  <div ref="chartDom" style="width: 100px; height: 100px;"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { number } from 'vue-types';

// 使用 ref 来获取 DOM 元素
const chartDom = ref<HTMLDivElement | null>(null);
let myChart: echarts.ECharts | null = null;

const props = defineProps({
  max: {
    type: number,
    required: true
  }
});

// 图表配置
const option = {
  series: [
    {
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      center: ['50%', '50%'], // 将中心位置调整到中间
      radius: '80%', // 减少半径以适应较小的空间
      min: 0,
      max: 1,
      splitNumber: 2, // 减少分割线的数量
      axisLine: {
        lineStyle: {
          width: 2, // 减少轴线的宽度
          color: [
            [0.5, '#FF6E76'],
            [1, '#7CFFB2']
          ] // 减少颜色段的数量
        }
      },
      pointer: {
        show: true, // 显示指针
        length: '50%', // 减少指针长度
        width: 1 // 减少指针宽度
      },
      axisTick: {
        show: true // 显示刻度线
      },
      splitLine: {
        show: false // 隐藏分割线
      },
      axisLabel: {
        show: false // 隐藏刻度标签
      },
      title: {
        show: true, // 显示标题
        fontSize: 10, // 减小标题字体大小
        offsetCenter: [0, '-30%'] // 调整标题位置，避免与数值重叠
      },
      detail: {
        fontSize: 12, // 减少字体大小以适应较小的空间
        offsetCenter: [0, '0%'], // 调整详细信息的位置
        valueAnimation: true,
        formatter: function (value: number) {
          return Math.round(value * 100) + '%'; // 显示百分比形式
        },
        color: 'inherit'
      },
      data: [
        {
          value: props.max,
          name: 'PF'
        }
      ]
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