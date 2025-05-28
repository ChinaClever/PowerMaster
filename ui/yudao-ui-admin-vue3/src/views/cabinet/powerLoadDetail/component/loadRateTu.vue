<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
import { ref, watch } from 'vue';
import { formatDate } from '@/utils/formatTime'
import { symbol } from 'vue-types';
const props = defineProps({
  curChartData: {
    type: Array,
    required: true,
  },
  timeRadio: {
    required: true,
  },
  avgMaxMin:String
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

const loadRate=ref([]);
const avgLoadRate=ref([]);
const maxLoadRate=ref([]);
const maxLoadRateTime=ref([]);
const minLoadRate=ref([]);
const minLoadRateTime=ref([]);
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
        return value + ' %';
      }
    }
  },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = '';
      params.forEach(item => {
        result += `${item.seriesName}: ${item.value} %<br/>`;
      });
      return result;
    }
  },
  grid: {
    left: '5%',   // 设置左侧边距
    right: '5%',  // 设置右侧边距
    top: '10%',    // 设置上侧边距
    bottom: '10%', // 设置下侧边距
  }
});


const updateChartData = () => {
  if (props.curChartData && props.curChartData.aPath) {
    loadRate.value = props.curChartData.aPath.map((item) => formatNumber(item.load_rate, 0));
    avgLoadRate.value = props.curChartData.aPath.map((item) => item.load_rate_total_avg_value?.toFixed(0));
    maxLoadRate.value = props.curChartData.aPath.map((item) => item.load_rate_max_value?.toFixed(0));
    maxLoadRateTime.value = props.curChartData.aPath.map((item) => formatDate(item.load_rate_max_time, 'YYYY-MM-DD HH:mm:ss'));
    minLoadRate.value = props.curChartData.aPath.map((item) => item.load_rate_min_value?.toFixed(0));
    minLoadRateTime.value = props.curChartData.aPath.map((item) => formatDate(item.load_rate_min_time, 'YYYY-MM-DD HH:mm:ss'));
    if(props.timeRadio=='近一月'){
      createTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
    }else {
      createTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH:mm:ss'));
    }


    if(props.timeRadio=='近一小时'){

      let circle={symbol:'none'};
      if(createTimeData.value.length==1){
        circle={symbol: 'circle'}
      }
      chartOptions.value = {
        title: { text: '' },
        legend: { orient: 'horizontal', right: '25' },
        dataZoom: [{ type: "inside" }],
        xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: function (value) {
              return value + ' %';
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            let result = '';
            params.forEach(item => {
              result += item.marker+` ${item.seriesName}: ${item.value}% `+" 时间："+createTimeData.value[params[0].dataIndex];
            });
            return result
          }
        },
        grid: {
          left: '5%',   // 设置左侧边距
          right: '5%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
        series: [
          { name: '负载率', type: 'line', ...circle, data: loadRate.value, },
        ],
      };
    }else{
      let circle={symbol:'none'};
      if( createTimeData.value.length==1){
        circle={symbol: 'circle'}
      }
      chartOptions.value = {
        title: { text: '' },
        legend: { orient: 'horizontal', right: '25',data: ['负载率'],
          selected:  { '负载率':true }
         },
        dataZoom: [{ type: "inside" }],
        xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: function (value) {
              return value + ' %';
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            let tooltipContent = '';
            params.forEach(item => {
              if(props.avgMaxMin=='最大'){
                tooltipContent += item.marker + item.seriesName + ': ' + item.value + '% 发生时间: ' +maxLoadRateTime.value[item.dataIndex] + ' '  + '   <br/>';
              }
              if(props.avgMaxMin=='最小'){
                tooltipContent += item.marker +  item.seriesName + ': ' + item.value + '% 发生时间: ' +minLoadRateTime.value[item.dataIndex] + ' ' + '   <br/>';
              }
              if(props.avgMaxMin=='平均'){
                tooltipContent += item.marker + item.seriesName + ': ' + item.value + '% 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' +  '  <br/>';
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
          props.avgMaxMin=='最大'?{ name: '负载率', type: 'line', ...circle, data: maxLoadRate.value}:props.avgMaxMin=='平均'?{ name: '负载率', type: 'line', ...circle, data: avgLoadRate.value}:{ name: '负载率', type: 'line', ...circle, data: minLoadRate.value }
        ],
      };
    }
    
  }
};

// 初始化时更新一次数据
updateChartData();

watch(
  () => [props.curChartData,props.avgMaxMin],
  () => {
    console.log("change",props.avgMaxMin)
    console.log("change",props.curChartData)
    // if (newData && newData.aPath) { // 确保 newData 和 newData.aPath 存在
      updateChartData();
    // }
  },
  { deep: true }
);
</script>

<style lang="less" scope>
  
</style>