<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script lang="ts" setup>
import { defineProps, computed, reactive, onUnmounted } from 'vue';
import 'echarts';

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
console.log('loadFactor',props)


// 设置饼图的选项
// 设置饼图的选项
const chartOptions = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b} : {c}'
  },
  series: [
    {
      type: 'pie',
      radius: ['30%', '70%'],
      center: ['50%', '50%'],
      roseType: 'radius',
      itemStyle: {
        borderRadius: 5
      },
      label: {
        show: true,
        position: 'inside', // 将标签显示在饼图内部
        formatter: (params) => {
          return `${params.value}A`;
        },
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        { value: props.loadFactor.curA == null ? -1 : props.loadFactor.curA[0], name: 'Ia', itemStyle: { color: '#E5B849' } },
        { value: props.loadFactor.curA == null ? -1 : props.loadFactor.curA[1], name: 'Ib', itemStyle: { color: '#C8603A' } },
        { value: props.loadFactor.curA == null ? -1 : props.loadFactor.curA[2], name: 'Ic', itemStyle: { color: '#AD3762' } },
      ]
    }
  ]
}));
onUnmounted(() => {
  console.log('组件已卸载******');
});
// onMounted(() => {
//   window.addEventListener('resize', () => {
//     chartOptions.resize();
//   });
// });
</script>

<style lang="scss" scoped>

</style>
