<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
import { reactive, watch, defineProps, onUnmounted } from 'vue';
const props = defineProps({
  loadFactor: {
    type: Object as () => { ua: number; ub: number; uc: number; ia?: number; ib?: number; ic?: number },
    required: true
  },
  height: {
    type: [Number,String],
  },
  width: {
    type: [Number,String],
  }
})
console.log('loadFactor',props.loadFactor)

// 设置饼图的选项
const echartsOption = computed(() => ({
  series: [
    {
      type: 'gauge',
      startAngle: 90,
      endAngle: -270,
      pointer: {
        show: false
      },
      progress: {
        show: true,
        overlap: false,
        roundCap: true,
        clip: false,
        itemStyle: {
          borderWidth: 1,
          borderColor: '#464646'
        }
      },
      axisLine: {
        lineStyle: {
          width: 40
        }
      },
      splitLine: {
        show: false,
        distance: 0,
        length: 10
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        show: false,
        distance: 50
      },
      data: [
        {
          value: props.loadFactor.totalPowApparent.toFixed(3),
          itemStyle: { color: '#C8603A' },
          detail: {
            valueAnimation: true,
            offsetCenter: ['0%', '-20%']
          }
        },
        {
          value: props.loadFactor.totalPowActive.toFixed(3),
          itemStyle: { color: '#AD3762' },
          detail: {
            valueAnimation: true,
            offsetCenter: ['0%', '10%']
          }
        },
        {
          value: props.loadFactor.totalPowReactive.toFixed(3),
          itemStyle: { color: '#E5B849' },
          detail: {
            valueAnimation: true,
            offsetCenter: ['0%', '40%']
          }
        }
      ],
      max: 1000, // 设置最大值为1000
      title: {
        fontSize: 14
      },
      detail: {
        width: 50,
        height: 14,
        fontSize: 14,
        color: 'inherit',
        borderColor: 'inherit',
        borderRadius: 20,
        borderWidth: 1,
        formatter: '{value}'
      }
    }
  ]
}));

onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>