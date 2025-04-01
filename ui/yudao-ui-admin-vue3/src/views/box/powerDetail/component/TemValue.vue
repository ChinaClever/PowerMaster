<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script lang="ts" setup>
import { defineProps, computed, reactive, onUnmounted } from 'vue';
import 'echarts';

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

// 使用 computed 属性来创建 ECharts 配置
const chartOptions = computed(() => ( {
  series: [
    {
      type: 'gauge',
      anchor: {
        show: true,
        showAbove: true,
        size: 18,
        itemStyle: {
          color: '#FAC858'
        }
      },
      pointer: {
        icon: 'path://M2.9,0.7L2.9,0.7c1.4,0,2.6,1.2,2.6,2.6v115c0,1.4-1.2,2.6-2.6,2.6l0,0c-1.4,0-2.6-1.2-2.6-2.6V3.3C0.3,1.9,1.4,0.7,2.9,0.7z',
        width: 8,
        length: '80%',
        offsetCenter: [0, '8%']
      },
      progress: {
        show: true,
        overlap: true,
        roundCap: true
      },
      axisLine: {
        roundCap: true
      },
      data: [
        {
          value: props.loadFactor.temValue[0],
          name: 'A',
          itemStyle: { color: '#B47660' },
          title: {
            offsetCenter: ['-60%', '80%']
          },
          detail: {
            offsetCenter: ['-60%', '95%']
          }
        },
        {
          value: props.loadFactor.temValue[1],
          name: 'B',
          itemStyle: { color: '#C8603A' },
          title: {
            offsetCenter: ['-20%', '80%']
          },
          detail: {
            offsetCenter: ['-20%', '95%']
          }
        },
        {
          value: props.loadFactor.temValue[2],
          name: 'C',
          itemStyle: { color: '#AD3762' },
          title: {
            offsetCenter: ['20%', '80%']
          },
          detail: {
            offsetCenter: ['20%', '95%']
          }
        },
        {
          value: props.loadFactor.temValue[3],
          name: 'N',
          itemStyle: { color: '#E5B849' },
          title: {
            offsetCenter: ['60%', '80%']
          },
          detail: {
            offsetCenter: ['60%', '95%']
          }
        }
      ],
      title: {
        fontSize: 12
      },
      detail: {
        width: 20,
        height: 10,
        fontSize: 12,
        color: '#fff',
        backgroundColor: 'inherit',
        borderRadius: 3,
        formatter: '{value}℃'
      }
    }
  ]
}));

onUnmounted(() => {
  console.log('组件已卸载******');
});
</script>

<style lang="scss" scoped>
/* 样式定义 */
</style>