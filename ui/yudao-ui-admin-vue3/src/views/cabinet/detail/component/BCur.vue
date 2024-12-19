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
// 设置饼图的选项
const echartsOption = reactive({
  tooltip: {
    trigger: 'item',
    formatter: (params) => `${params.name}:`+`${params.value}A`,
  },
  series: [
    {
      type: 'pie',
      radius: '60%',
      label: {
        show: true,
        position: 'inside', // 将标签显示在饼图内部
        formatter: (params) => {
          // 返回具体的数值
          return `${params.value}A`;
        },
        fontSize: 14, // 可以调整字体大小
        fontWeight: 'bold' // 可以设置字体加粗
      },
      data: [
        { value: props.loadFactor.ia || 0, name: 'Ia', itemStyle: { color: '#075F71' } },
        { value: props.loadFactor.ib || 0, name: 'Ib', itemStyle: { color: '#119CB5' } },
        { value: props.loadFactor.ic || 0, name: 'Ic', itemStyle: { color: '#45C0C9' } },
      ].sort((a, b) => b.value - a.value), // 根据值的大小排序
      roseType: 'radius', // 饼图南丁格尔图类型，使饼图呈现为半径不同的圆环
      itemStyle: {
        shadowBlur: 100, // 阴影模糊大小
        shadowColor: 'rgba(0, 0, 0, 0.3)' // 阴影颜色
      },
      animationType: 'scale', // 动画类型
      animationEasing: 'elasticOut', // 动画缓动效果
      animationDelay: (idx) => Math.random() * 200, // 动画延迟，使每个扇区动画不同步
      emphasis: {
        itemStyle: {
          shadowBlur: 20, // 强调项的阴影模糊大小
          shadowOffsetX: 0, // 阴影水平偏移
          shadowColor: 'rgba(0, 0, 0, 0.7)' // 强调项的阴影颜色
        }
      }
    }
  ]
});

watch(() => props.loadFactor, (newVal) => {
  echartsOption.series[0].data = [
    { value: newVal.ua, name: 'Ia' },
    { value: newVal.ub, name: 'Ib' },
    { value: newVal.uc, name: 'Ic' },
  ];
}, { deep: true }); // 添加 deep: true 以深度监听 loadFactor 对象的变化

onUnmounted(() => {
  console.log('onUnmounted******')
})
</script>

<style lang="scss" scoped>

</style>