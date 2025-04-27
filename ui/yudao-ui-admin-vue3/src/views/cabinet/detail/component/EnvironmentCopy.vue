<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import { LEFT } from '@jsplumb/browser-ui';
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


const echartsOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: function (params) {
      if(params[0].name === '视在功率'){
        return `${params[0].name}: ${Math.abs(params[0].value)}KVAR`
      }else if(params[0].name === '有功功率'){
        return `${params[0].name}: ${Math.abs(params[0].value)}KW`
      }else{  
        return `${params[0].name}: ${Math.abs(params[0].value)}KVA`
      }
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
    data: ['无功功率', '有功功率', '视在功率']
  },
  series: [
    {
      name: '2011',
      type: 'bar',
      label: {
        show: true,
        position: 'insideLeft',
        formatter: (params) => {
          const unitMap = ['KVAR', 'KW', 'KVA'];
          return `${params.value}${unitMap[params.dataIndex]}`;
        },
        color: '#fff', // 字体颜色为白色
        textBorderColor: 'auto', // 轮廓颜色自动跟随柱形图颜色
        textBorderWidth: 2, // 轮廓宽度
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        {value:props.loadFactor.powReactiveB,itemStyle: { color: '#800080'}},
        {value:props.loadFactor.powActiveB,itemStyle: { color: '#91cc75' }},
        {value:props.loadFactor.powApparentB,itemStyle: { color: '#5470c6' }}]
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
