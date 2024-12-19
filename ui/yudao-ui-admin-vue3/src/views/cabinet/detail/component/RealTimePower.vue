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
    trigger: 'item',
    formatter: (params) => `${params.name}:`+`${params.value}KVA`,
  },
  series: [
    {
      type: 'pie',
      radius: '50%',
      label: {
        show: true,
        position: 'inside',
        formatter: (params) => `${params.value}`+'KVA', // 确保返回数值
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        { 
          value: props.loadFactor.s, 
          name: '现在功率',
          itemStyle: {
            color: '#E5B849'
          }
        },
        { 
          value: props.loadFactor.p, 
          name: '有功功率',
          itemStyle: {
            color: '#C8603A'
          }
        },
        { 
          value: props.loadFactor.q,
          name: '无功功率',
          itemStyle: {
            color: '#AD3762'
          }
        },
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
});

onUnmounted(() => {
  console.log('onUnmounted******')
})

watch(() => props.loadFactor, (newVal) => {
  echartsOption.series[0].data[0].value = newVal;
});

</script>

<style lang="scss" scoped>

</style>