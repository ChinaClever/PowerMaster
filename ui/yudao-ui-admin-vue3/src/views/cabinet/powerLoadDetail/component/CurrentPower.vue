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

const totalApparentPowData = ref([]);
const aApparentPowData = ref([]);
const bApparentPowData = ref([]);

const totalApparentPowAvgValueData = ref([]);
const totalApparentPowMaxValueData = ref([]);
const totalApparentPowMaxTimeData = ref([]);
const totalApparentPowMinValueData = ref([]);
const totalApparentPowMinTimeData = ref([]);

const aApparentPowAvgValueData = ref([]);
const aApparentPowMaxValueData = ref([]);
const aApparentPowMaxTimeData = ref([]);
const aApparentPowMinValueData = ref([]);
const aApparentPowMinTimeData = ref([]);

const bApparentPowAvgValueData = ref([]);
const bApparentPowMaxValueData = ref([]);
const bApparentPowMaxTimeData = ref([]);
const bApparentPowMinValueData = ref([]);
const bApparentPowMinTimeData = ref([]);
const createTimeData = ref([]);

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

const chartOptions = ref({
  title: { text: '' },
  legend: { orient: 'horizontal', right: '25' },
  dataZoom: [{ type: "inside" }],
  xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: function (value) {
        return value + ' KVA';
      }
    }
  },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = '';
      params.forEach(item => {
        result += `${item.seriesName}: ${item.value} kVA<br/>`;
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

const updateChartData = () => {
  if (props.curChartData && props.curChartData.aPath) {
    totalApparentPowData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_total, 3)); 
    aApparentPowData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_a, 3));
    bApparentPowData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_b, 3));

    totalApparentPowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_total_avg_value, 3));
    totalApparentPowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_total_max_value, 3));
    totalApparentPowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.apparent_total_max_time, 'YYYY-MM-DD HH:mm'));
    totalApparentPowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_total_min_value, 3));
    totalApparentPowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.apparent_total_min_time, 'YYYY-MM-DD HH:mm'));

    aApparentPowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_a_avg_value, 3));
    aApparentPowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_a_max_value, 3));
    aApparentPowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.apparent_a_max_time, 'YYYY-MM-DD HH:mm'));
    aApparentPowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_a_min_value, 3));
    aApparentPowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.apparent_a_min_time, 'YYYY-MM-DD HH:mm'));

    bApparentPowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_b_avg_value, 3));
    bApparentPowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_b_max_value, 3));
    bApparentPowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.apparent_b_max_time, 'YYYY-MM-DD HH:mm'));
    bApparentPowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_b_min_value, 3));
    bApparentPowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.apparent_b_min_time, 'YYYY-MM-DD HH:mm'));
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
              return value + ' KVA';
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            let result = '';
            params.forEach(item => {
              result += item.marker+`${item.seriesName}: ${item.value} KVA<br/>`;
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
          { name: '总视在功率', type: 'line', symbol: 'none', data:  totalApparentPowData.value},
          { name: 'A路视在功率', type: 'line', symbol: 'none', data: aApparentPowData.value },
          { name: 'B路视在功率', type: 'line', symbol: 'none', data: bApparentPowData.value },
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
              return value + ' KVA';
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            let tooltipContent = '';
            params.forEach(item => {
              switch (item.seriesName) { 
                case "总平均视在功率":
                case "A路平均视在功率":
                case "B路平均视在功率":
                  tooltipContent += item.marker + ' 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' KVA  <br/>';
                  break;
                case "总最大视在功率":
                  tooltipContent += item.marker + ' 记录时间: ' +totalApparentPowMaxTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' KVA  <br/>';
                  break;
                case "总最小视在功率":
                  tooltipContent += item.marker + ' 记录时间: ' +totalApparentPowMinTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' KVA  <br/>';
                  break;
                case "A路最大视在功率":
                  tooltipContent += item.marker + ' 记录时间: ' +aApparentPowMaxTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' KVA  <br/>';
                  break;
                case "A路最小视在功率":
                  tooltipContent += item.marker + ' 记录时间: ' +aApparentPowMinTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' KVA  <br/>';
                  break;
                case "B路最大视在功率":
                  tooltipContent += item.marker + ' 记录时间: ' +bApparentPowMaxTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' KVA  <br/>';
                  break;
                case "B路最小视在功率":
                  tooltipContent += item.marker + ' 记录时间: ' +bApparentPowMinTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' KVA  <br/>';
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
          { name: '总平均视在功率', type: 'line', symbol: 'none', data: totalApparentPowAvgValueData.value },
          { name: '总最大视在功率', type: 'line', symbol: 'none', data: totalApparentPowMaxValueData.value},
          { name: '总最小视在功率', type: 'line', symbol: 'none', data: totalApparentPowMinValueData.value },
          { name: 'A路平均视在功率', type: 'line', symbol: 'none', data: aApparentPowAvgValueData.value },
          { name: 'A路最大视在功率', type: 'line', symbol: 'none', data: aApparentPowMaxValueData.value},
          { name: 'A路最小视在功率', type: 'line', symbol: 'none', data: aApparentPowMinValueData.value },
          { name: 'B路平均视在功率', type: 'line', symbol: 'none', data: bApparentPowAvgValueData.value },
          { name: 'B路最大视在功率', type: 'line', symbol: 'none', data: bApparentPowMaxValueData.value},
          { name: 'B路最小视在功率', type: 'line', symbol: 'none', data: bApparentPowMinValueData.value }, 
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