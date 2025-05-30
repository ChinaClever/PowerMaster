<template>
  <div ref="chartDom" style="width: 400px; height: 280px;margin-left:30px;"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  max: {
    type: Number,
    required: true
  },
  customColor: {
    type: String,
    default: ''
  },
  name:{
    type: String,
  }
});

// 使用 ref 来获取 DOM 元素
const chartDom = ref<HTMLDivElement | null>(null);
let myChart: echarts.ECharts | null = null;

// 创建响应式的 restPercentageValue
const percentageValue = computed(() => props.max);

// 创建响应式的 restPercentageValue
const restPercentageValue = computed(() => 100 - percentageValue.value);

// 动态生成图表配置
const option = computed(() => ({
  title: {
    text: props.name,
    left: 'left'
  },
  tooltip: {
    trigger: 'item',
    formatter: `${percentageValue.value}%`
  },
  legend: {
    show: false // 不显示图例
  },
  series: [
    {
      name: 'Access From',
      type: 'pie',
      radius: ['55%', '90%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      label: {
        show: true,
        position: 'center',
        // 修改 formatter 以显示 percentageValue 的值
        formatter: () => percentageValue.value +  '%',
        fontSize: 30,
        fontWeight: 'bold'
      },
      labelLine: {
        show: false
      },
      data: [
        {
          value: percentageValue.value,
          name: 'Percentage',
          itemStyle: {
            color: chooseColor(percentageValue.value)
          }
        },
        {
          value: restPercentageValue.value,
          name: 'Rest',
          itemStyle: { color: '#F6F4F4' }
        }
      ]
    }
  ]
}));
function chooseColor(value){
  if(value<=5){
    return "#00d26a";
  }else if(value<=10){
    return "#00a6ed"
  }else if(value<=20){
    return "#fcd53f"
  }else{
    return "#f8312f"
  }
}
// 组件挂载时初始化图表
onMounted(() => {
  if (chartDom.value) {
    myChart = echarts.init(chartDom.value);
    myChart.setOption(option.value);
  }
});

// 监听 restPercentageValue 的变化并更新图表
watch(restPercentageValue, () => {
  if (myChart) {
    myChart.setOption(option.value);
  }
});

// 组件卸载时销毁图表
onUnmounted(() => {
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
});
</script>