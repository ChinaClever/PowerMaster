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


// 设置饼图的选项
const echartsOption = reactive({
  tooltip: {
    trigger: 'item'
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      data: [
        { value: props.loadFactor.finstalledCapacity, name: '频率' },
        { value: props.loadFactor.s, name: '功率因数' },
        { value: props.loadFactor.p, name: '三相电压不平衡度' },
        { value: props.loadFactor.q, name: '三相电流不平衡度' },
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
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