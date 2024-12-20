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
          return `${params.value}V`;
        },
        fontSize: 14,
        fontWeight: 'bold'
      },
      data: [
        { value: props.loadFactor.ua || 0, name: 'Ua', itemStyle: { color: '#E5B849' } },
        { value: props.loadFactor.ub || 0, name: 'Ub', itemStyle: { color: '#C8603A' } },
        { value: props.loadFactor.uc || 0, name: 'Uc', itemStyle: { color: '#AD3762' } },
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

onUnmounted(() => {
  console.log('组件已卸载******');
});
</script>

<style lang="scss" scoped>
/* 样式定义 */
</style>