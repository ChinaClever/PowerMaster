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
      if(params[0].name === props.loadFactor.label[0]){
        return `${params[0].name}: ${Math.abs(params[0].value)}${props.loadFactor.unit[0]}`
      }else if(params[0].name === props.loadFactor.label[1]){
        return `${params[0].name}: ${Math.abs(params[0].value)}${props.loadFactor.unit[1]}`
      }else{  
        return `${params[0].name}: ${Math.abs(params[0].value)}${props.loadFactor.unit[2]}`
      }
    }
  },
  grid: {
    left: '1%',
    bottom: '90%',
    containLabel: true
  },
  xAxis: {
    type: 'value',
    boundaryGap: [0, 0.01],
    axisLabel: {
        show: false // 隐藏 ECharts 自带的标签
    },
    axisTick: {
        show: false // 隐藏 Y 轴的刻度线
    },
    axisLine: {
        show: false // 隐藏 Y 轴的轴线
    }
  },
  yAxis: {
    type: 'category',
    data: props.loadFactor.label,
    axisLabel: {
        padding: [0, 0, 0, 0],
        fontSize: 12, // 统一字体大小
        fontFamily: 'Arial' // 统一字体家族
    },
    axisTick: {
        show: false // 隐藏 Y 轴的刻度线
    },
    axisLine: {
        show: false // 隐藏 Y 轴的轴线
    }
  },
  series: [
    {
      name: '2011',
      type: 'bar',
      label: {
        show: true,
        position: 'insideLeft',
        formatter: (params) => {
          const unitMap = props.loadFactor.unit;
          return `${params.value}${unitMap[params.dataIndex]}`;
        },
        color: '#fff', // 字体颜色为白色
        textBorderColor: 'auto', // 轮廓颜色自动跟随柱形图颜色
        textBorderWidth: 2, // 轮廓宽度
        fontSize: 10,
        fontWeight: 'bold'
      },
      data: [
        {value:props.loadFactor.first ? props.loadFactor.first : 0,itemStyle: { color: '#800080'}},
        {value:props.loadFactor.second ? props.loadFactor.second : 0,itemStyle: { color: '#91cc75' }},
        {value:props.loadFactor.third ? props.loadFactor.third : 0,itemStyle: { color: '#5470c6' }}]
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
