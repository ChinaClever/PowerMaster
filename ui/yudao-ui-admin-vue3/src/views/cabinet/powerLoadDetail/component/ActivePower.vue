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
  },
  avgMaxMin:String
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

function buKongGe(value,du){
  value=Number(value);
  console.log(value);
  if(value<100&&value>=10) return "&nbsp;&nbsp;"+value.toFixed(du);
  if(value<10) return "&nbsp;&nbsp;&nbsp;&nbsp;"+value.toFixed(du);
  return "&nbsp;"+value.toFixed(du);
}
const updateChartData = () => {
  if (props.curChartData && props.curChartData.aPath) {
    totalActivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total, 3));
    aActivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_a, 3));
    bActivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_b, 3));
    totalActivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total_avg_value, 3));
    totalActivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total_max_value, 3));
    totalActivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_total_max_time, 'YYYY-MM-DD HH:mm:ss'));
    totalActivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total_min_value, 3));
    totalActivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_total_min_time, 'YYYY-MM-DD HH:mm:ss'));

    aActivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_a_avg_value, 3));
    aActivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_a_max_value, 3));
    aActivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_a_max_time, 'YYYY-MM-DD HH:mm:ss'));
    aActivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_a_min_value, 3));
    aActivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_a_min_time, 'YYYY-MM-DD HH:mm:ss'));
    
    bActivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_b_avg_value, 3));
    bActivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_b_max_value, 3));
    bActivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_b_max_time, 'YYYY-MM-DD HH:mm:ss'));
    bActivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_b_min_value, 3));
    bActivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_b_min_time, 'YYYY-MM-DD HH:mm:ss'));
     if(props.timeRadio=='近一月'){
      createTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
    }else {
      createTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH:mm:ss'));
    }
    

    let circle={symbol:'none'};
    if(createTimeData.value.length==1){
      circle={symbol: 'circle'}
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
            return result+"时间："+createTimeData.value[params[0].dataIndex];
          }
        },
        grid: {
          left: '5%',   // 设置左侧边距
          right: '5%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
        series: [
          { name: '总有功功率', type: 'line', ...circle, data:  totalActivePowData.value},
          { name: 'A路有功功率', type: 'line', ...circle, data: aActivePowData.value },
          { name: 'B路有功功率', type: 'line', ...circle, data: bActivePowData.value },
        ],
      };
    }else{
      let series=[];
      if(props.avgMaxMin=='最大'){
        series=[
          { name: '总有功功率', type: 'line', ...circle, data: totalActivePowMaxValueData.value},
          { name: 'A路有功功率', type: 'line', ...circle, data: aActivePowMaxValueData.value},
          { name: 'B路有功功率', type: 'line', ...circle, data: bActivePowMaxValueData.value},
        ]
      }
      if(props.avgMaxMin=='最小'){
        series=[
          { name: '总有功功率', type: 'line', ...circle, data: totalActivePowMinValueData.value,},
          { name: 'A路有功功率', type: 'line', ...circle, data: aActivePowMinValueData.value, },
          { name: 'B路有功功率', type: 'line', ...circle, data: bActivePowMinValueData.value, }, 
        ]
      }
      if(props.avgMaxMin=='平均'){
        series=[
          { name: '总有功功率', type: 'line', ...circle, data: totalActivePowAvgValueData.value ,},
          { name: 'A路有功功率', type: 'line', ...circle, data: aActivePowAvgValueData.value,},
          { name: 'B路有功功率', type: 'line', ...circle, data: bActivePowAvgValueData.value,},
        ]
      }
      chartOptions.value = {
        title: { text: '' },
        legend: { orient: 'horizontal', right: '25',data: ['总有功功率','A路有功功率','B路有功功率'],
          selected:  { '总有功功率': true, 'A路有功功率': true, 'B路有功功率': true }
         },
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
              if(props.avgMaxMin=='平均'){
                switch (item.seriesName) { 
                  case "总有功功率":
                    tooltipContent += item.marker + item.seriesName + ':&nbsp;&nbsp;' + buKongGe(item.value,3) +'kW 记录时间: ' +createTimeData.value[item.dataIndex] +  '<br/>';
                    break;
                  case "A路有功功率":
                  case "B路有功功率":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) +'kW 记录时间: ' +createTimeData.value[item.dataIndex] +  '<br/>';
                    break;
                }
              }
              if(props.avgMaxMin=='最大'){
                  switch (item.seriesName) { 
                    case "总有功功率":
                    tooltipContent += item.marker + item.seriesName + ':&nbsp;&nbsp;' + buKongGe(item.value,3) +'kW 发生时间: ' +totalActivePowMaxTimeData.value[item.dataIndex] +  '<br/>';
                    break;
                    case "A路有功功率":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) +'kW 发生时间: ' +aActivePowMaxTimeData.value[item.dataIndex] +'<br/>';
                    break;
                    case "B路有功功率":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,3) + 'kW 发生时间: ' +bActivePowMaxTimeData.value[item.dataIndex] + '<br/>';
                    break;
                }
              }
              if(props.avgMaxMin=='最小'){
                switch (item.seriesName) { 
                  case "总有功功率":
                    tooltipContent += item.marker +item.seriesName + ':&nbsp;&nbsp;' + buKongGe(item.value,3) +  'kW 发生时间: ' +totalActivePowMinTimeData.value[item.dataIndex] + '<br/>';
                    break;
                  case "A路有功功率":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,3) +  'kW 发生时间: ' +aActivePowMinTimeData.value[item.dataIndex] + '<br/>';
                    break;
                  case "B路有功功率":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,3) + 'kW 发生时间: ' +bActivePowMinTimeData.value[item.dataIndex] + '<br/>';
                    break;
              }
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
        series: series
      };

    }
    
  }
};

// 初始化时更新一次数据
updateChartData();

watch(
  () => [props.curChartData,props.avgMaxMin],
  () => {
    // if (newData && newData.aPath) { // 确保 newData 和 newData.aPath 存在
      updateChartData();
    // }
  },
  { deep: true }
);
</script>

<style lang="less" scope>
  
</style>