<template>
  <Echart :options="echartsOptions" />
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, defineProps } from 'vue';
import 'echarts';

const prop = defineProps({
  list: {
    type: Object,
    required: true
  }
});

console.log('6666666666666677777777777777', prop.list);

const curvolAData = ref({ curValueList: [] as number[] });
const curvolBData = ref({ curValueList: [] as number[] });
const curvolCData = ref({ curValueList: [] as number[] });
const lineidDateTimes = ref([] as string[]);

const updateChartData = () => {
  lineidDateTimes.value = prop.list.dateTimes;
  prop.list.l.forEach(item => curvolAData.value.curValueList.push(item.cur_avg_value.toFixed(2)));
  prop.list.ll.forEach(item => curvolBData.value.curValueList.push(item.cur_avg_value.toFixed(2)));
  prop.list.lll.forEach(item => curvolCData.value.curValueList.push(item.cur_avg_value.toFixed(2)));
};

updateChartData(); // 初始数据填充

const echartsOptions = computed(() => ({
  title: { text: '' },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = params[0].name + '<br>';
      params.forEach(param => {
        result += `${param.marker}${param.seriesName}: &nbsp;&nbsp;&nbsp;&nbsp${param.value}`;
        if (param.seriesName === 'A相电流' || param.seriesName === 'B相电流' || param.seriesName === 'C相电流') {
          result += 'A';
        }
        result += '<br>';
      });
      return result.trimEnd();
    }
  },
  
  legend: {
    data: ['A相电流', 'B相电流', 'C相电流'],
    selectedMode: 'multiple'
  },
  grid: { left: '3%', right: '4%', bottom: '5%', containLabel: true },
  toolbox: { feature: { saveAsImage: {}, dataView: {}, dataZoom: {}, restore: {} } },
  xAxis: {
    type: 'category', nameLocation: 'end', boundaryGap: false, data: lineidDateTimes.value
  },
  yAxis: { type: 'value' },
  series: [
    { name: 'A相电流', type: 'line', data: curvolAData.value.curValueList, symbol: 'circle', symbolSize: 4 },
    { name: 'B相电流', type: 'line', data: curvolBData.value.curValueList, symbol: 'circle', symbolSize: 4 },
    { name: 'C相电流', type: 'line', data: curvolCData.value.curValueList, symbol: 'circle', symbolSize: 4 }
  ]
}));

watchEffect(() => {
  // 清空 curvolAData 中的数组
  curvolAData.value = {
    curValueList: []
  };
  
  // 清空 curvolBData 中的数组
  curvolBData.value = {
    curValueList: []
  };
  
  // 清空 curvolCData 中的数组
  curvolCData.value = {
    curValueList: []
  };
  
  // 清空 lineidDateTimes 数组
  lineidDateTimes.value = [];

  updateChartData(); // 当 prop.list 变化时更新图表数据
});

console.log("Initial list", prop.list);
</script>

<style lang="scss" scoped>
/* 样式 */
</style>
