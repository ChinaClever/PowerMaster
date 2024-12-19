<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts';
import { reactive, watch, defineProps, onUnmounted } from 'vue';

// 定义组件接收的属性
const props = defineProps({
  loadFactor: {
    type: Object as () => { ua: number; ub: number; uc: number; ia?: number; ib?: number; ic?: number },
    required: true
  },
  height: {
    type: [Number, String],
    default: 400
  },
  width: {
    type: [Number, String],
    default: '100%'
  }
});

// 初始化ECharts配置
const echartsOption = reactive({
  tooltip: {
    trigger: 'item',
    formatter: (params) => `${params.name}:`+`${params.value}V`,
  },
  series: [
    {
      type: 'pie',
      radius: ['30%', '60%'],
      avoidLabelOverlap: false,
      label: {
        show: true,
        position: 'inside',
        formatter: (params) => {
          // 根据params.dataIndex获取对应的数据项，并返回具体数值
          const dataItem = echartsOption.series[0].data[params.dataIndex];
          return `${dataItem.value}V`;
        },
        fontSize: 14,
        fontWeight: 'bold'
      },
      labelLine: {
        show: false
      },
      data: [
        { value: props.loadFactor.ua || 0, name: 'Ua', itemStyle: { color: '#E5B849' } },
        { value: props.loadFactor.ub || 0, name: 'Ub', itemStyle: { color: '#C8603A' } },
        { value: props.loadFactor.uc || 0, name: 'Uc', itemStyle: { color: '#AD3762' } },
        // 如果ia, ib, ic存在，您可能希望将它们也添加到数据中
        // { value: props.loadFactor.ia || 0, name: 'Ia', itemStyle: { /* color */ } },
        // { value: props.loadFactor.ib || 0, name: 'Ib', itemStyle: { /* color */ } },
        // { value: props.loadFactor.ic || 0, name: 'Ic', itemStyle: { /* color */ } },
      ]
    }
  ]
});

// 监听loadFactor对象内各属性的变化，并更新ECharts数据
watch(() => props.loadFactor.ua, (newVal) => {
  echartsOption.series[0].data[0].value = newVal;
}, { immediate: true });

watch(() => props.loadFactor.ub, (newVal) => {
  echartsOption.series[0].data[1].value = newVal;
}, { immediate: true });

watch(() => props.loadFactor.uc, (newVal) => {
  echartsOption.series[0].data[2].value = newVal;
}, { immediate: true });

// 如果ia, ib, ic也需要监听，可以添加以下watch
// watch(() => props.loadFactor.ia, (newVal) => {
//   if (echartsOption.series[0].data.length < 4) {
//     echartsOption.series[0].data.push({ value: newVal || 0, name: 'Ia', itemStyle: { /* color */ } });
//   } else {
//     echartsOption.series[0].data[3].value = newVal;
//   }
// }, { immediate: true });
// 类似地添加对ib和ic的监听...

// 组件卸载时执行的逻辑
onUnmounted(() => {
  console.log('组件已卸载******');
});
</script>

<style lang="scss" scoped>
/* 样式定义 */
</style>