<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
import { reactive, watch, defineProps, onUnmounted } from 'vue';
const props = defineProps({
  loadFactor: {
    type: Number,
    required: true
  },
  height: {
    type: [Number,String],
  },
  width: {
    type: [Number,String],
  }
})


// 设置饼图的选项
const echartsOption = reactive({
  tooltip: {
      trigger: 'item'
  },
  series: [{
      type: 'pie',
      radius: ['60%', '120%'],
      center: ['50%', '70%'],
      label: {
        show: true,
        position: 'inside', // 将标签显示在饼图内部
        formatter: (params) => {
          // 返回显示的字符串，包括百分比
          return `${params.percent.toFixed(1)}%`;
        },
        fontSize: 14, // 可以调整字体大小
        fontWeight: 'bold' // 可以设置字体加粗
      },
      startAngle: 180,
      endAngle: 360,
      data: [{
          value: props.loadFactor,
          name:  "负载率(%)",
          itemStyle: {
            color: '#E5B849'
          }
      },
      {
        value: (100 - props.loadFactor).toFixed(2),
        itemStyle: {
          color: '#ccc'
        }
      }]
  }]
})

onUnmounted(() => {
  console.log('onUnmounted******')
})

watch(() => props.loadFactor, (newVal) => {
  echartsOption.series[0].data[0].value = newVal;
});

</script>

<style lang="scss" scoped>

</style>