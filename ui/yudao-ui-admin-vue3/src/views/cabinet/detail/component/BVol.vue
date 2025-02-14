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
        { value: props.loadFactor.volB[0], name: 'Ua', itemStyle: { color: '#075F71' } },
        { value: props.loadFactor.volB[1], name: 'Ub', itemStyle: { color: '#119CB5' } },
        { value: props.loadFactor.volB[2], name: 'Uc', itemStyle: { color: '#45C0C9' } },
      ]
    }
  ]
});

onUnmounted(() => {
  console.log('组件已卸载******');
});
</script>

<style lang="scss" scoped>
/* 样式定义 */
</style>