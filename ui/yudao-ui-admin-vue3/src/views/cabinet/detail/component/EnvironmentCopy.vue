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

console.log('loadFactor1111',props.loadFactor)


const echartsOption = reactive({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
     formatter: function (params) {
      return `${params[0].seriesName}<br/>${params[0].name}: ${Math.abs(params[0].data)}KVA`;
    }
  },
  grid: {
    left: '1%',
    right:'1%',
    bottom: '-7%',
    containLabel: true
  },
  xAxis: {
    type: 'value',
    boundaryGap: [0, 0.01]
  },
  yAxis: {
    type: 'category',
    data: ['现在功率', '有功功率', '无功功率']
  },
  series: [
    {
      name: '2011',
      type: 'bar',
      label: {
        show: true,
        position: 'inside',
        formatter: (params) => `${params.value}`+'KVA',
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [props.loadFactor.powApparentB, props.loadFactor.powActiveB, props.loadFactor.powReactiveB]
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