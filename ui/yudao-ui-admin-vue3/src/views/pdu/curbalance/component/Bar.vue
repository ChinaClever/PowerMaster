<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script setup lang="ts">
import 'echarts'
import { reactive, watch, defineProps, onUnmounted } from 'vue';

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
  { value: props.max.L1, name: 'A相电流', percent: ((props.max.L1 / total) * 100).toFixed(2) + '%' ,itemStyle: { color: '#E5B849' }},
    { value: props.max.L2, name: 'B相电流', percent: ((props.max.L2 / total) * 100).toFixed(2) + '%' ,itemStyle: { color: '#C8603A' }},
    { value: props.max.L3, name: 'C相电流', percent: ((props.max.L3 / total) * 100).toFixed(2) + '%' ,itemStyle: { color: '#AD3762' }}
  ];
});

// 图表配置
const echartsOption = reactive({
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


<style lang="scss" scoped>

</style>