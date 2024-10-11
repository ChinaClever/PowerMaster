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

// 用于获取颜色的函数
const getColor = (index) => {
  const value = props.max[`L${index + 4}`]; // L4, L5, L6映射
  return value === 0 ? 'green' : '#fa3333'; // 根据条件返回颜色
};

// 设置柱状图的选项
const echartsOption = reactive({
  tooltip: {
    trigger: 'item',
  },
  xAxis: {
    type: 'category',
    data: ['A相', 'B相', 'C相'], // x轴分类
    axisLabel: {
      fontSize: 9,
    }
  },
  yAxis: {
    type: 'value',
    max: Math.max(props.max.L1, props.max.L2, props.max.L3), // 设置y轴最大值为传入的最大值
    axisLabel: false  
  },
  series: [
    {
      type: 'bar',
      data: [props.max.L1, props.max.L2, props.max.L3], // 使用传入的数据
      itemStyle: {
        color: (params) => getColor(params.dataIndex),
      },
      emphasis: {
        focus: 'series'
      },
      label: {
        show: true,
        position: 'top',
        fontSize: 8,
      }
    }
  ]
});

// 监听max的变化
watchEffect(() => {
  const newMax = props.max;

  // 更新柱状图的数据
  echartsOption.series[0].data = [newMax.L1, newMax.L2, newMax.L3];

  // 更新y轴的最大值
  echartsOption.yAxis.max = Math.max(newMax.L1, newMax.L2, newMax.L3);
});
</script>

<style lang="scss" scoped>
/* 可以添加样式 */
</style>
