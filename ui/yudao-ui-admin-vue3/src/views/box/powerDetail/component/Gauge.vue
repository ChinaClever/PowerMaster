<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
import { reactive, watch, defineProps, onUnmounted } from 'vue';
const props = defineProps({
  data: {
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

console.log('1111',props.data)


// 设置饼图的选项
const echartsOption = reactive( {
  series: [
    {
      type: 'gauge',
      axisLine: {
        lineStyle: {
          width: 25,
          color: [
            [0.3, '#67e0e3'],
            [0.7, '#37a2da'],
            [1, '#fd666d']
          ]
        }
      },
      pointer: {
        itemStyle: {
          color: 'auto'
        }
      },
      axisTick: {
        distance: -30,
        length: 8,
        lineStyle: {
          color: '#fff',
          width: 2
        }
      },
      splitLine: {
        distance: -30,
        length: 30,
        lineStyle: {
          color: '#fff',
          width: 4
        }
      },
      axisLabel: {
        color: 'inherit',
        distance: 40,
        fontSize: 10,
      },
      detail: {
        valueAnimation: true,
        formatter: '{value}%',
        color: 'inherit'
      },
      data: [
        {
          value: props.data,
        }
      ]
    }
  ]
})

onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>