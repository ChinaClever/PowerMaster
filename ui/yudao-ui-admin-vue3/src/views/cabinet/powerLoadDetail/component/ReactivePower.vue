<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  curChartData: {
    type: Array,
    required: true,
  },
  timeRadio: {
    required: true,
  }
});

const L1Data = ref([]);
const L2Data = ref([]);
const L3Data = ref([]);
const createTimeData = ref([]);

const chartOptions = ref({
  title: { text: '' },
  legend: { orient: 'horizontal', right: '25' },
  dataZoom: [{ type: "inside" }],
  xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: function (value) {
        return value + ' KW';
      }
    }
  },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = '';
      params.forEach(item => {
        result += `${item.seriesName}: ${item.value} kVar<br/>`;
      });
      return result;
    }
  },
  grid: {
    left: '5%',   // 设置左侧边距
    right: '5%',  // 设置右侧边距
    top: '10%',    // 设置上侧边距
    bottom: '10%', // 设置下侧边距
  },
  series: [
    { name: '总无功功率', type: 'line', symbol: 'none', data: L1Data.value },
    { name: 'A路无功功率', type: 'line', symbol: 'none', data: L2Data.value },
    { name: 'B路无功功率', type: 'line', symbol: 'none', data: L3Data.value },
  ],
});

const updateChartData = () => {
  if (props.curChartData && props.curChartData.aPath) {
    L1Data.value = props.curChartData.aPath.map((item) => item.powReactiveTotal.toFixed(3));
    L2Data.value = props.curChartData.aPath.map((item) => item.powReactiveA.toFixed(3));
    L3Data.value = props.curChartData.aPath.map((item) => item.powReactiveB.toFixed(3));
    createTimeData.value = props.curChartData.aPath.map((item) => item.createTime);

    chartOptions.value = {
      ...chartOptions.value,
      xAxis: { ...chartOptions.value.xAxis, data: createTimeData.value },
      series: [
        { ...chartOptions.value.series[0], data: L1Data.value },
        { ...chartOptions.value.series[1], data: L2Data.value },
        { ...chartOptions.value.series[2], data: L3Data.value },
      ],
    };
  }
};

// 初始化时更新一次数据
updateChartData();

watch(
  () => props.curChartData,
  (newData) => {
    if (newData && newData.aPath) { // 确保 newData 和 newData.aPath 存在
      updateChartData();
    }
  },
  { deep: true }
);
</script>

<style lang="less" scope>
  
</style>