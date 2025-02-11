<template>
  <Echart :options="echartsOptions" />
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, defineProps } from 'vue';
import 'echarts';

const prop = defineProps({
  list: {
    type: Object as () => { loop: { lineId: number; vol: number[]; dateTimes: string[] }[]; dateTimes: string[]; },
    required: true
  }
});

const lineidDateTimes = ref([] as string[]);
lineidDateTimes.value = prop.list.dateTimes;

// 用于存储所有曲线的电压数据
const allVolData = ref([] as number[][]); 

const updateChartData = () => {
  lineidDateTimes.value = prop.list.dateTimes;
  prop.list.loop.forEach(item => {
    allVolData.value.push(item.vol);
  });
};

updateChartData(); // 初始数据填充

// 构建 Echarts 系列数据
const newArray = allVolData.value.map((volValues, index) => ({
  name: String(index + 1),
  type: 'line',
  data: volValues,
  symbol: 'circle',
  symbolSize: 4
}));

const legendData = Array.from({ length: allVolData.value.length }, (_, i) => (i + 1).toString());

const echartsOptions = computed(() => ({
  title: { text: '' },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = params[0].name + '<br>';
      params.forEach(param => {
        result += `${param.marker}${param.seriesName}: &nbsp;&nbsp;&nbsp;&nbsp${param.value.toFixed(1)}V<br>`;
      });
      return result.trimEnd();
    }
  },
  legend: {
    data: legendData,
    selectedMode: 'multiple'
  },
  grid: { left: '3%', right: '4%', bottom: '5%', containLabel: true },
  toolbox: { feature: { saveAsImage: {}, dataView: {}, dataZoom: {}, restore: {} } },
  xAxis: {
    type: 'category', nameLocation: 'end', boundaryGap: false,
    data: lineidDateTimes.value
  },
  yAxis: { type: 'value' },
  series: newArray
}));

// 注意：这里的 console.log 仅在调试时使用，生产环境中应移除
console.log("Initial list", prop.list);
</script>

<style lang="scss" scoped>
/* 样式 */
</style>