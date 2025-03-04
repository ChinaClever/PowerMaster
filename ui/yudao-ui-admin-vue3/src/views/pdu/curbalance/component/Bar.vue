<template>
  <div ref="chartDom" :style="{height: height, width: width}"></div>
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
const echartsOption = {
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
      data: dataWithPercent.value
    }
  ]
};

// 初始化图表
const initChart = () => {
  if (chartDom.value) {
    myChart = echarts.init(chartDom.value);
    updateData(props.max); // 在初始化时更新数据
    myChart.setOption(echartsOption);
  }
  console.log('123123123', dataWithPercent.value);
};

// 更新数据
const updateData = (newMax: any) => {
  const total = newMax.L1 + newMax.L2 + newMax.L3;
  dataWithPercent.value = [
    {
      value: newMax.L1,
      name: 'A相电流',
      percent: ((newMax.L1 / total) * 100).toFixed(2) + '%',
      itemStyle: { color: '#E5B849' }
    },
    {
      value: newMax.L2,
      name: 'B相电流',
      percent: ((newMax.L2 / total) * 100).toFixed(2) + '%',
      itemStyle: { color: '#C8603A' }
    },
    {
      value: newMax.L3,
      name: 'C相电流',
      percent: ((newMax.L3 / total) * 100).toFixed(2) + '%',
      itemStyle: { color: '#AD3762' }
    }
  ];
  echartsOption.series[0].data = dataWithPercent.value;
};

// 更新图表数据
const updateChart = () => {
  if (myChart) {
    myChart.setOption({
      series: [
        {
          data: dataWithPercent.value
        }
      ]
    });
  }
};

// 监听 max 变化并更新数据
watch(() => props.max, (newMax) => {
  updateData(newMax);
  updateChart();
}, { deep: true });

// 组件挂载时初始化图表
onMounted(() => {
  nextTick(() => {
    initChart();
  });
});

// 组件卸载时销毁图表
onUnmounted(() => {
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
});
</script>

<style lang="scss" scoped>
/* 图表容器样式 */
</style>