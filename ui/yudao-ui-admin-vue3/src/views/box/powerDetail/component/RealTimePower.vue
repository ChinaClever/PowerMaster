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
console.log('loadFactor',props.loadFactor)

const gaugeData = [
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
];

// 设置饼图的选项
const echartsOption = reactive({
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
      data: gaugeData,
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
});;

onUnmounted(() => {
  console.log('onUnmounted******')
})

watch(() => props.loadFactor, (newVal) => {
  echartsOption.series[0].data[0].value = newVal;
});

</script>

<style lang="scss" scoped>

</style>