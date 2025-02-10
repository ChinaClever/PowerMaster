<template>
  <Echart :height="height" :width="width" :options="echartsOptions" />
</template>

<script lang="ts" setup>
import { defineProps, ref, computed, onUnmounted } from 'vue';
import 'echarts';

const prop = defineProps({
  list: {
    type: Object as () => Record<string, any>,
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

const legendList = computed(() => {
  const items = [];
  if (prop.list['1'] !== undefined) {
    items.push('输出位1功率因数');
  }
  if (prop.list['2'] !== undefined) {
    items.push('输出位2功率因数');
  }
  if (prop.list['3'] !== undefined) {
    items.push('输出位3功率因数');
  }
  return items; // 根据prop.list中的非null键返回相应的图例数组
});

const echartsOptions = computed(() => {
  return {
    legend: { data: legendList.value },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        let result = params[0].name + '<br>';
        for (let i = 0; i < params.length; i++) {
          result += params[i].marker + params[i].seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + params[i].value.toFixed(2);
          result += '<br>';
        }
        return result;
      }
    },
    xAxis: { type: 'category', boundaryGap: false, data: prop.list.time },
    yAxis: { type: 'value' },
    toolbox: { feature: { saveAsImage: {}, dataView: {}, dataZoom: {}, restore: {} } },
    series: [
      { name: '输出位1功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: prop.list['1'] },
      { name: '输出位2功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: prop.list['2'] },
      { name: '输出位3功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: prop.list['3'] }
    ]
  };
});

onUnmounted(() => {
  console.log('onUnmounted******');
});
</script>

<style lang="scss" scoped>
</style>