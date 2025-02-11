<template>
  <Echart :options="echartsOptions" />
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, defineProps } from 'vue';
import 'echarts';

const prop = defineProps({
  list: {
    type: Object as () => { loop: { lineId: number; cur: number[]; dateTimes: string[] }[]; dateTimes: string[]; },
    required: true
  }
});

const lineidDateTimes = ref([] as string[]);
lineidDateTimes.value = prop.list.dateTimes;

const newArray = prop.list.loop.map((item, index) => ({
  name: String(index + 1),
  type: 'line',
  data: item.cur,
  symbol: 'circle',
  symbolSize: 4
}));

const legendData = Array.from({ length: prop.list.loop.length }, (_, i) => (i + 1).toString());

const echartsOptions = computed(() => ({
  title: { text: '' },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = params[0].name + '<br>';
      params.forEach(param => {
        result += `${param.marker}${param.seriesName}: &nbsp;&nbsp;&nbsp;&nbsp${param.value.toFixed(2)}A<br>`;
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