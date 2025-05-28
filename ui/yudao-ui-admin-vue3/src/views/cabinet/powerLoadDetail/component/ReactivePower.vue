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



function buKongGe(value,du){
  value=Number(value);
  console.log(value);
  if(value<100&&value>=10) return "&nbsp;&nbsp;&nbsp;"+value.toFixed(du);
  if(value<10) return "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+value.toFixed(du);
  return "&nbsp;"+value.toFixed(du);
}
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

const totalReactivePowData = ref([]);
const aReactivePowData = ref([]);
const bReactivePowData = ref([]);

const totalReactivePowAvgValueData = ref([]);
const totalReactivePowMaxValueData = ref([]);
const totalReactivePowMaxTimeData = ref([]);
const totalReactivePowMinValueData = ref([]);
const totalReactivePowMinTimeData = ref([]);

const aReactivePowAvgValueData = ref([]);
const aReactivePowMaxValueData = ref([]);
const aReactivePowMaxTimeData = ref([]);
const aReactivePowMinValueData = ref([]);
const aReactivePowMinTimeData = ref([]);

const bReactivePowAvgValueData = ref([]);
const bReactivePowMaxValueData = ref([]);
const bReactivePowMaxTimeData = ref([]);
const bReactivePowMinValueData = ref([]);
const bReactivePowMinTimeData = ref([]);

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
        return value + ' KVar';
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
  }
});

const updateChartData = () => {
  if (props.curChartData && props.curChartData.aPath) {
    totalReactivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_total, 3));
    aReactivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_a, 3));
    bReactivePowData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_b, 3));
    totalReactivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_total_avg_value, 3));
    totalReactivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_total_max_value, 3));
    totalReactivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.reactive_total_max_time, 'YYYY-MM-DD HH:mm:ss'));
    totalReactivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_total_min_value, 3));
    totalReactivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.reactive_total_min_time, 'YYYY-MM-DD HH:mm:ss'));

    aReactivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_a_avg_value, 3));
    aReactivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_a_max_value, 3));
    aReactivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.reactive_a_max_time, 'YYYY-MM-DD HH:mm:ss'));
    aReactivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_a_min_value, 3));
    aReactivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.reactive_a_min_time, 'YYYY-MM-DD HH:mm:ss'));
    
    bReactivePowAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_b_avg_value, 3));
    bReactivePowMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_b_max_value, 3));
    bReactivePowMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.reactive_b_max_time, 'YYYY-MM-DD HH:mm:ss'));
    bReactivePowMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.reactive_b_min_value, 3));
    bReactivePowMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.reactive_b_min_time, 'YYYY-MM-DD HH:mm:ss'));
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
              return value + ' KVar';
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            let result = '';
            params.forEach(item => {
              result += item.marker+`${item.seriesName}: ${item.value} kVar<br/>`;
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
          { name: '总无功功率', type: 'line', ...circle, data:  totalReactivePowData.value,},
          { name: 'A路无功功率', type: 'line', ...circle, data: aReactivePowData.value, },
          { name: 'B路无功功率', type: 'line', ...circle, data: bReactivePowData.value, },
        ],
      };
    }else{
      let series=[];
      if(props.avgMaxMin=='平均'){
        series=[
          { name: '总无功功率', type: 'line', ...circle, data: totalReactivePowAvgValueData.value },
          { name: 'A路无功功率', type: 'line', ...circle, data: aReactivePowAvgValueData.value, },
          { name: 'B路无功功率', type: 'line', ...circle, data: bReactivePowAvgValueData.value,},
        ]
      }
      if(props.avgMaxMin=='最大'){
        series=[
          { name: '总无功功率', type: 'line', ...circle, data: totalReactivePowMaxValueData.value,},
          { name: 'A路无功功率', type: 'line', ...circle, data: aReactivePowMaxValueData.value,},
          { name: 'B路无功功率', type: 'line', ...circle, data: bReactivePowMaxValueData.value,},
        ]
      }
      if(props.avgMaxMin=='最小'){
        series=[
          { name: '总无功功率', type: 'line', ...circle, data: totalReactivePowMinValueData.value, },
          { name: 'A路无功功率', type: 'line', ...circle, data: aReactivePowMinValueData.value,},
          { name: 'B路无功功率', type: 'line', ...circle, data: bReactivePowMinValueData.value,}, 
        ]
      }
      chartOptions.value = {
        title: { text: '' },
        legend: { orient: 'horizontal', right: '25',data:['总无功功率',"A路无功功率",'B路无功功率'],
          selected:{"总无功功率":true,"A路无功功率":true,"B路无功功率":true}
         },
        dataZoom: [{ type: "inside" }],
        xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: function (value) {
              return value + ' KVar';
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
                case "总无功功率":
                  tooltipContent += item.marker + item.seriesName + ':&nbsp;&nbsp;' + buKongGe(item.value,3) + 'KVar 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' + '   <br/>';
                  break;
                case "A路无功功率":
                case "B路无功功率":
                  tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3) + 'KVar 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' + '   <br/>';
                  break;
                } 
              }
              if(props.avgMaxMin=='最大'){
                switch (item.seriesName) {
                  case "总无功功率":
                  tooltipContent += item.marker +item.seriesName + ':&nbsp;&nbsp;' + buKongGe(item.value,3) + 'KVar 发生时间: ' +totalReactivePowMaxTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
                  break;
                  case "A路无功功率":
                  tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,3)+ 'KVar 发生时间: ' +aReactivePowMaxTimeData.value[item.dataIndex] + ' '  + '   <br/>';
                  break;
                  case "B路无功功率":
                  tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,3) + 'KVar 发生时间: ' +bReactivePowMaxTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
                  break;
                } 
              }
              if(props.avgMaxMin=='最小'){
                switch (item.seriesName) { 
                case "总无功功率":
                  tooltipContent += item.marker +item.seriesName + ':&nbsp;&nbsp;' + buKongGe(item.value,3) +  'KVar 发生时间: ' +totalReactivePowMinTimeData.value[item.dataIndex] + ' ' + '   <br/>';
                  break;
                case "A路无功功率":
                  tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,3) + 'KVar 发生时间: ' +aReactivePowMinTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
                  break;
                case "B路无功功率":
                  tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,3) + 'KVar 发生时间: ' +bReactivePowMinTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
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
  (newData) => {
    // if (newData && newData.aPath) { // 确保 newData 和 newData.aPath 存在
      updateChartData();
    // }
  },
  { deep: true }
);
</script>

<style lang="less" scope>
  
</style>