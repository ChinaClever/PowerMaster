<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
import { ref, watch } from 'vue';
import { formatDate } from '@/utils/formatTime'
const props = defineProps({
  curChartData: {
    type: Array,
    required: true,
  },
  timeRadio: {
    required: true,
  }
});
console.log('props============>', props)
console.log('timeRadio============>', props.timeRadio)
const totalActivePowData = ref([]);
const aActivePowData = ref([]);
const bActivePowData = ref([]);
const totalActivePowAvgValueData = ref([]);
const totalActivePowMaxValueData = ref([]);
const totalActivePowMaxTimeData = ref([]);
const totalActivePowMinValueData = ref([]);
const totalActivePowMinTimeData = ref([]);

const aActivePowAvgValueData = ref([]);
const aActivePowMaxValueData = ref([]);
const aActivePowMaxTimeData = ref([]);
const aActivePowMinValueData = ref([]);
const aActivePowMinTimeData = ref([]);

const bActivePowAvgValueData = ref([]);
const bActivePowMaxValueData = ref([]);
const bActivePowMaxTimeData = ref([]);
const bActivePowMinValueData = ref([]);
const bActivePowMinTimeData = ref([]);
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
        result += `${item.seriesName}: ${item.value} KW<br/>`;
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
});

function formatNumber(value, decimalPlaces) {
  try{
    if (!isNaN(value)) {
        return Number(value).toFixed(decimalPlaces);
    } else {
        return null; // 或者其他默认值
    }
  }catch(e){
    return null;
  }
}


const updateChartData = () => {
  if (props.curChartData && props.curChartData.aPath) {
    totalActivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total, 3));
    aActivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_a, 3));
    bActivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_b, 3));
    totalActivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total_avg_value, 3));
    totalActivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total_max_value, 3));
    totalActivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_total_max_time, 'YYYY-MM-DD HH:mm'));
    totalActivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total_min_value, 3));
    totalActivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_total_min_time, 'YYYY-MM-DD HH:mm'));

    aActivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_a_avg_value, 3));
    aActivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_a_max_value, 3));
    aActivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_a_max_time, 'YYYY-MM-DD HH:mm'));
    aActivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_a_min_value, 3));
    aActivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_a_min_time, 'YYYY-MM-DD HH:mm'));
    
    bActivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_b_avg_value, 3));
    bActivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_b_max_value, 3));
    bActivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_b_max_time, 'YYYY-MM-DD HH:mm'));
    bActivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_b_min_value, 3));
    bActivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_b_min_time, 'YYYY-MM-DD HH:mm'));
    if(props.timeRadio=='近一小时'){
      createTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH:mm:ss'));
    }else if(props.timeRadio=='近一月'){
      createTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
    }else{
      createTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH'));   
    }
    

    if(props.timeRadio=='近一小时'){

      chartOptions.value = {
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
              result += item.marker+`${item.seriesName}: ${item.value} KW<br/>`;
            });
            return result+"记录时间："+createTimeData.value[params[0].dataIndex];
          }
        },
        grid: {
          left: '5%',   // 设置左侧边距
          right: '5%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
        series: [
          { name: '总有功功率', type: 'line', symbol: 'none', data:  totalActivePowData.value},
          { name: 'A路有功功率', type: 'line', symbol: 'none', data: aActivePowData.value },
          { name: 'B路有功功率', type: 'line', symbol: 'none', data: bActivePowData.value },
        ],
      };
    }else{
      chartOptions.value = {
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
            let tooltipContent = '';
            params.forEach(item => {
              switch (item.seriesName) { 
                case "总平均有功功率":
                case "A路平均有功功率":
                case "B路平均有功功率":
                  tooltipContent += item.marker + ' 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' kW  <br/>';
                  break;
                case "总最大有功功率":
                  tooltipContent += item.marker + ' 记录时间: ' +totalActivePowMaxTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' kW  <br/>';
                  break;
                case "总最小有功功率":
                  tooltipContent += item.marker + ' 记录时间: ' +totalActivePowMinTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' kW  <br/>';
                  break;
                case "A路最大有功功率":
                  tooltipContent += item.marker + ' 记录时间: ' +aActivePowMaxTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' kW  <br/>';
                  break;
                case "A路最小有功功率":
                  tooltipContent += item.marker + ' 记录时间: ' +aActivePowMinTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' kW  <br/>';
                  break;
                case "B路最大有功功率":
                  tooltipContent += item.marker + ' 记录时间: ' +bActivePowMaxTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' kW  <br/>';
                  break;
                case "B路最小有功功率":
                  tooltipContent += item.marker + ' 记录时间: ' +bActivePowMinTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' kW  <br/>';
                  break;
              }
            });
            return tooltipContent;
          }
        },
        grid: {
          left: '5%',   // 设置左侧边距
          right: '5%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
        series: [
          { name: '总平均有功功率', type: 'line', symbol: 'none', data: totalActivePowAvgValueData.value },
          { name: '总最大有功功率', type: 'line', symbol: 'none', data: totalActivePowMaxValueData.value},
          { name: '总最小有功功率', type: 'line', symbol: 'none', data: totalActivePowMinValueData.value },
          { name: 'A路平均有功功率', type: 'line', symbol: 'none', data: aActivePowAvgValueData.value },
          { name: 'A路最大有功功率', type: 'line', symbol: 'none', data: aActivePowMaxValueData.value},
          { name: 'A路最小有功功率', type: 'line', symbol: 'none', data: aActivePowMinValueData.value },
          { name: 'B路平均有功功率', type: 'line', symbol: 'none', data: bActivePowAvgValueData.value },
          { name: 'B路最大有功功率', type: 'line', symbol: 'none', data: bActivePowMaxValueData.value},
          { name: 'B路最小有功功率', type: 'line', symbol: 'none', data: bActivePowMinValueData.value }, 
        ],
      };

    }
    
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