<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts';
import { reactive, defineProps, watchEffect } from 'vue';

const props = defineProps({
  max: {
    type: Object,
    required: true
  },
  height: {
    type: Number,
    default: 60
  },
  width: {
    type: Number,
    default: 60
  }
});

// 设置柱状图的选项
const echartsOption = reactive({
  tooltip: {
    trigger: 'item',
  },
  xAxis: {
    type: 'category',
    data: ['A相', 'B相', 'C相'], // x轴分类
    axisLabel: {
      fontSize: 9, // 设置字体大小
    }
  },
  yAxis: {
    type: 'value', // y轴为数值型
    max: 100, // 设置y轴最大值为100%
    axisLabel: false  
  },
  series: [
    {
      type: 'bar', // 设置为柱状图
      data: [
        props.max.L1 || 0, // 使用 props.max.L1 或默认值0
        props.max.L2 || 0, // 使用 props.max.L2 或默认值0
        props.max.L3 || 0  // 使用 props.max.L3 或默认值0
      ],
      itemStyle: {
        color: (params) => getColor(params.value), // 动态设置颜色
      },  
      emphasis: {
        focus: 'series'
      },
      label: {
        show: true, // 可以显示每个柱子的数值
        position: 'top' // 显示位置
      }
    }
  ]
});

// 根据值动态获取颜色
const getColor = (value) => {
  if (value < 60) {
    return 'green'; // 低于60时为绿色
  } else if (value < 90) {
    return '#ffc402'; // 60到90之间为黄色
  } else {
    return '#fa3333'; // 高于90时为红色
  }
};

// 监听 max 的变化并更新图表数据
watchEffect(() => {
  const data = [
    props.max.L1 || 0, // 使用 props.max.L1 或默认值0
    props.max.L2 || 0, // 使用 props.max.L2 或默认值0
    props.max.L3 || 0  // 使用 props.max.L3 或默认值0
  ];

  // 更新图表数据
  echartsOption.series[0].data = data;
});

</script>

<style lang="scss" scoped>
</style>
