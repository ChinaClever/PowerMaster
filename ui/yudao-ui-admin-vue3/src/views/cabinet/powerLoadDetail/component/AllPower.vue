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


const totalActivePowData = ref([]);
const totalReactivePowData = ref([]);
const totalApparentPowData = ref([]);

const totalActivePowAvgValueData = ref([]);
const totalActivePowMaxValueData = ref([]);
const totalActivePowMaxTimeData = ref([]);
const totalActivePowMinValueData = ref([]);
const totalActivePowMinTimeData = ref([]);

const totalReactivePowAvgValueData = ref([]);
const totalReactivePowMaxValueData = ref([]);
const totalReactivePowMaxTimeData = ref([]);
const totalReactivePowMinValueData = ref([]);
const totalReactivePowMinTimeData = ref([]);

const totalApparentPowAvgValueData = ref([]);
const totalApparentPowMaxValueData = ref([]);
const totalApparentPowMaxTimeData = ref([]);
const totalApparentPowMinValueData = ref([]);
const totalApparentPowMinTimeData = ref([]);

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
  if(value<100&&value>=10) return "&nbsp;&nbsp;&nbsp;"+value.toFixed(du);
  if(value<10) return "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+value.toFixed(du);
  return "&nbsp;"+value.toFixed(du);
}

const updateChartData = () => {
  if (props.curChartData && props.curChartData.aPath) {
    totalActivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total, 3));
    totalReactivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_total, 3));
    totalApparentPowData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_total, 3)); 

    totalActivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total_avg_value, 3));
    totalActivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total_max_value, 3));
    totalActivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_total_max_time, 'YYYY-MM-DD HH:mm:ss'));
    totalActivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.active_total_min_value, 3));
    totalActivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.active_total_min_time, 'YYYY-MM-DD HH:mm:ss'));

    totalReactivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_total_avg_value, 3));
    totalReactivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_total_max_value, 3));
    totalReactivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.reactive_total_max_time, 'YYYY-MM-DD HH:mm:ss'));
    totalReactivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_total_min_value, 3));
    totalReactivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.reactive_total_min_time, 'YYYY-MM-DD HH:mm:ss'));

    totalApparentPowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_total_avg_value, 3));
    totalApparentPowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_total_max_value, 3));
    totalApparentPowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.apparent_total_max_time, 'YYYY-MM-DD HH:mm:ss'));
    totalApparentPowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.apparent_total_min_value, 3));
    totalApparentPowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.apparent_total_min_time, 'YYYY-MM-DD HH:mm:ss'));

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
              return value + ' KW';
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            let result = '';
            params.forEach(item => {
              switch (item.seriesName) { 
                  case "总有功功率":
                    result += item.marker+`${item.seriesName}: ${item.value} KW<br/>`;
                    break;
                  case "总视在功率":
                    result += item.marker+`${item.seriesName}: ${item.value} KVA<br/>`;
                    break;
                  case "总无功功率":
                    result += item.marker+`${item.seriesName}: ${item.value} KVar<br/>`;
                    break;
              }
              
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
          { name: '总视在功率', type: 'line', ...circle, data:  totalApparentPowData.value},
          { name: '总无功功率', type: 'line', ...circle, data:  totalReactivePowData.value,},
        ],
      };
    }else{
      let circle={symbol:'none'};
      if(createTimeData.value.length==1){
        circle={symbol: 'circle'}
      }
      let series=[];
      if(props.avgMaxMin=='最大'){
        series=[
          { name: '总有功功率', type: 'line', ...circle, data: totalActivePowMaxValueData.value,},
          { name: '总视在功率', type: 'line', ...circle, data: totalApparentPowMaxValueData.value,},
          { name: '总无功功率', type: 'line', ...circle, data: totalReactivePowMaxValueData.value,},
        ]
      }
      if(props.avgMaxMin=='最小'){
        series=[
          { name: '总有功功率', type: 'line', ...circle, data: totalActivePowMinValueData.value, },
          { name: '总视在功率', type: 'line', ...circle, data: totalApparentPowMinValueData.value,},
          { name: '总无功功率', type: 'line', ...circle, data: totalReactivePowMinValueData.value,},
        ]
      }
      if(props.avgMaxMin=='平均'){
        series=[
          { name: '总有功功率', type: 'line', ...circle, data: totalActivePowAvgValueData.value },
          { name: '总视在功率', type: 'line', ...circle, data: totalApparentPowAvgValueData.value,},
          { name: '总无功功率', type: 'line', ...circle, data: totalReactivePowAvgValueData.value},
        ]
      }
      chartOptions.value = {
        title: { text: '' },
        legend: { orient: 'horizontal', right: '25',data: ['总有功功率','总视在功率','总无功功率'],
          selected:  { '总有功功率': true, '总视在功率': true, '总无功功率': true }
         },
        dataZoom: [{ type: "inside" }],
        xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
        yAxis: {
          type: 'value',
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            let tooltipContent = '';
            params.forEach(item => {
              if(props.avgMaxMin=='平均'){
                switch (item.seriesName) { 
                  case "总有功功率":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,3) +  'KW&nbsp;&nbsp;&nbsp;记录时间: ' +createTimeData.value[item.dataIndex]  + '<br/>';
                    break;
                  case "总视在功率":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) +  'kVA&nbsp;&nbsp;记录时间: ' +createTimeData.value[item.dataIndex] + '<br/>';
                    break;
                  case "总无功功率":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) + 'kVar&nbsp;记录时间: ' +createTimeData.value[item.dataIndex] + '<br/>';
                    break;
                }
              }
              if(props.avgMaxMin=='最大'){
                  switch (item.seriesName) { 
                    case "总有功功率":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) +  'kW&nbsp;&nbsp;&nbsp;发生时间: ' +totalActivePowMaxTimeData.value[item.dataIndex]  + '<br/>';
                    break;
                    case "总视在功率":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) +'KVA&nbsp;&nbsp;发生时间: ' +totalApparentPowMaxTimeData.value[item.dataIndex]  +  '<br/>';
                    break;
                    case "总无功功率":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) + 'KVar&nbsp;发生时间: ' +totalReactivePowMaxTimeData.value[item.dataIndex] + '<br/>';
                  break;
                }
              }
              if(props.avgMaxMin=='最小'){
                switch (item.seriesName) { 
                  case "总有功功率":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) + 'kW&nbsp;&nbsp;&nbsp;发生时间: ' +totalActivePowMinTimeData.value[item.dataIndex] +  '<br/>';
                    break;
                  case "总视在功率":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) +'KVA&nbsp;&nbsp;发生时间: ' +totalApparentPowMinTimeData.value[item.dataIndex] + '<br/>';
                    break;
                  case "总无功功率":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,3) + 'KVar&nbsp;发生时间: ' +totalReactivePowMinTimeData.value[item.dataIndex] + '<br/>';
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