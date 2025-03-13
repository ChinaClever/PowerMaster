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
    bottom: '100%'
  },
  xAxis: {
    type: 'value',
    boundaryGap: [0, 0.01],
    max: (!props.loadFactor.first && !props.loadFactor.second && !props.loadFactor.third) || (props.loadFactor.first == "0" && props.loadFactor.second == "0" && props.loadFactor.third == "0") ? -1 : 0,
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
        show: false, // 隐藏 ECharts 自带的标签
        margin: 0
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
        position: 'insideRight',
        formatter: (params) => {
          const unitMap = props.loadFactor.unit;
          return `${-(params.value)}${unitMap[params.dataIndex]}`;
        },
        color: '#fff', // 字体颜色为白色
        textBorderColor: 'auto', // 轮廓颜色自动跟随柱形图颜色
        textBorderWidth: 2, // 轮廓宽度
        fontSize: 10,
        fontWeight: 'bold'
      },
      data: [
        {value:-(props.loadFactor.first ? props.loadFactor.first : 0),itemStyle: { color: props.loadFactor.color[0] }}, 
        {value:-(props.loadFactor.second ? props.loadFactor.second : 0),itemStyle: { color: props.loadFactor.color[1] }}, 
        {value:-(props.loadFactor.third ? props.loadFactor.third : 0),itemStyle: { color: props.loadFactor.color[2] }}]
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
