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


// 设置饼图的选项
const echartsOption = reactive({
  tooltip: {
      trigger: 'item'
  },
  series: [{
      type: 'pie',
      radius: ['40%', '90%'],
      center: ['50%', '70%'],
      startAngle: 180,
      endAngle: 360,
      data: [{
          value: props.loadFactor,
          name:  "负载率(%)"
      },
      {
        value: (100 - props.loadFactor).toFixed(2), // 计算剩余百分比
        name: "其他(%)" // 剩余部分的标签
      }]
  }]
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