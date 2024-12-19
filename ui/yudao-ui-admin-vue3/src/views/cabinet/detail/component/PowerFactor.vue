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
      radius: '60%',
      label: {
        show: true,
        position: 'inside',
        formatter: (params) => `${params.value}`, // 确保返回数值
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        { value: props.loadFactor.finstalledCapacity, name: '频率',itemStyle: {
            color: '#E5B849'
          }},
        { value: props.loadFactor.s, name: '功率因数',
          itemStyle: {
            color: '#C8603A'
          } },
        { value: props.loadFactor.p, name: '三相电压不平衡度',
          itemStyle:{
            color: '#AD3762'
          } },
        { value: props.loadFactor.q, name: '三相电流不平衡度',
          itemStyle:{
            color: '#B47660'
          } },
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