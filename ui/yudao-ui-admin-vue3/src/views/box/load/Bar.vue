<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import { log } from 'console';
import 'echarts'
import { reactive, defineProps, onUnmounted } from 'vue';

const { max } = defineProps({
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

// 直接使用传入的数值，假设这些数值是占比的基础
const data = [
  max.L1, // 对应于 L1
  max.L2, // 对应于 L2
  max.L3  // 对应于 L3
];

console.log("max", max);

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
    axisLabel:false  
  },
  series: [
    {
      type: 'bar', // 设置为柱状图
      data: data, // 使用传入的数据
      itemStyle: {
        color: (params) => getColor(params.value) // 动态设置颜色
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
})
const getColor = (value) => {
  if (value < 60) {
    return 'green' // 低于60时为绿色色
  } else if (value < 90) {
    return '#ffc402' // 60到90之间为黄色
  } else {
    return '#fa3333' // 高于90时为红色
  }
};

</script>

<style lang="scss" scoped>
</style>
