<template>
  <Echart :options="echartsOptions" />
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, defineProps } from 'vue';
import 'echarts';

const prop = defineProps({
  list: {
    type: Object as () => { A: any[]; B: any[]; C: any[]; dateTimes: string[]; },
    required: true
  }
});

const lineidDateTimes = ref([] as string[]);
lineidDateTimes.value = prop.list.dateTimes;

const vol = prop.list.vol;

const newArray = vol.map((value, index) => {
  const obj = {};
  obj['name'] = String(index + 1);
  obj['type'] = 'line';
  obj['data'] = value;
  obj['symbol'] = 'circle';
  obj['symbolSize'] = 4;
  return obj;
});

const legendData = Array.from({ length: vol.length }, (_, i) => (i + 1).toString());
console.log('legendData',legendData);

const echartsOptions = computed(() => ({
  title: { text: '' },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = params[0].name + '<br>';
      params.forEach(param => {
        result += `${param.marker}${param.seriesName}: &nbsp;&nbsp;&nbsp;&nbsp${param.value.toFixed(1)}`;
        result += 'V';
        result += '<br>';
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
    type: 'category', nameLocation: 'end', boundaryGap: false, data: lineidDateTimes.value
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