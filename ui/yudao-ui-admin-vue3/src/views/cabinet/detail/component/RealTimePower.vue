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
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        position: 'inside', // 将标签显示在饼图内部
        formatter: (params) => {
          // 返回具体的数值
          return `${params.value}KVA`;
        },
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