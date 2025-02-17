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
    value: props.loadFactor.powApparentTotal,
    detail: {
      valueAnimation: true,
      offsetCenter: ['0%', '-20%']
    },
    //itemStyle:{
    //  color:'#C8603A'
    //}
  },
  {
    value: props.loadFactor.powActiveTotal,
    detail: {
      valueAnimation: true,
      offsetCenter: ['0%', '10%']
    },
    //itemStyle:{
    //  color:'#AD3762'
    //}
  },
  {
    value: props.loadFactor.powReactiveTotal,
    detail: {
      valueAnimation: true,
      offsetCenter: ['0%', '40%']
    },
    itemStyle:{
      color:'purple'
    }
  }
];

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
      data: gaugeData,
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

// watch(() => props.loadFactor, (newVal) => {
//   echartsOption.series[0].data[0].value = newVal;
// });

</script>

<style lang="scss" scoped>

</style>
