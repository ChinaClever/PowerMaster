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

const echartsOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: function (params) {
      if(params[0].name === '视在功率'){
        return `${params[0].name}: ${Math.abs(params[0].value)}KVA`
      }else if(params[0].name === '有功功率'){
        return `${params[0].name}: ${Math.abs(params[0].value)}KW`
      }else{  
        return `${params[0].name}: ${Math.abs(params[0].value)}KVAR`
      }
    }
  },
  grid: {
    left: '-22%',
    bottom: '-8%',
    containLabel: true
  },
  xAxis: {
    type: 'value',
    boundaryGap: [0, 0.01]
  },
  yAxis: {
    type: 'category',
    data: ['无功功率', '有功功率', '视在功率']
  },
  series: [
    {
      name: '2011',
      type: 'bar',
      label: {
        show: true,
        position: 'inside',
        formatter: (params) => {
          const unitMap = ['KVAR', 'KW', 'KVA'];
          return `${-(params.value)}${unitMap[params.dataIndex]}`;
        },
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        {value:-(props.loadFactor.powReactiveA),itemStyle: { color: 'purple' }}, 
        {value:-(props.loadFactor.powActiveA),itemStyle: { color: 'green' }}, 
        {value:-(props.loadFactor.powApparentA),itemStyle: { color: 'blue' }}]
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
