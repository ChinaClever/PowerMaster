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
        position: 'insideRight',
        formatter: (params) => {
          const unitMap = ['KVAR', 'KW', 'KVA'];
          return `${-(params.value)}${unitMap[params.dataIndex]}`;
        },
        color: '#fff', // 字体颜色为白色
        textBorderColor: 'auto', // 轮廓颜色自动跟随柱形图颜色
        textBorderWidth: 2, // 轮廓宽度
        fontSize: 14,
        fontWeight: 'bold',
      },
      data: [
        {value:-(props.loadFactor.powReactiveA),itemStyle: { color: '#800080' }}, 
        {value:-(props.loadFactor.powActiveA),itemStyle: { color: '#91cc75' }}, 
        {value:-(props.loadFactor.powApparentA),itemStyle: { color: '#5470c6' }}]
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
