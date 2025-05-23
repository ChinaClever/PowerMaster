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

const factorTotalData = ref([]);
const factorAData = ref([]);
const factorBData = ref([]);

const factorTotalAvgValueData = ref([]);
const factorTotalMaxValueData = ref([]);
const factorTotalMinValueData = ref([]);
const factorTotalMaxTimeData = ref([]);
const factorTotalMinTimeData = ref([]);

const factorAAvgValueData = ref([]);
const factorAMaxValueData = ref([]);
const factorAMinValueData = ref([]);
const factorAMaxTimeData = ref([]);
const factorAMinTimeData = ref([]);

const factorBAvgValueData = ref([]);
const factorBMaxValueData = ref([]);
const factorBMinValueData = ref([]);
const factorBMaxTimeData = ref([]);
const factorBMinTimeData = ref([]);
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
        return value + ' ';
      }
    }
  },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = '';
      params.forEach(item => {
        result += `${item.seriesName}: ${item.value} <br/>`;
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
  if(value<100&&value>=10) return ""+value.toFixed(du);
  if(value<10) return "&nbsp;&nbsp;"+value.toFixed(du);
  return ""+value.toFixed(du);
}

const updateChartData = () => {
  if (props.curChartData && props.curChartData.aPath) {
    factorTotalData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_total, 2));
    factorAData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_a, 2));
    factorBData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_b, 2));
    factorTotalAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_total_avg_value, 2));
    factorTotalMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_total_max_value, 2));
    factorTotalMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.factor_total_max_time, 'YYYY-MM-DD HH:mm:ss'));
    factorTotalMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_total_min_value, 2));
    factorTotalMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.factor_total_min_time, 'YYYY-MM-DD HH:mm:ss'));

    factorAAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_a_avg_value, 2));
    factorAMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_a_max_value, 2));
    factorAMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.factor_a_max_time, 'YYYY-MM-DD HH:mm:ss'));
    factorAMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_a_min_value, 2));
    factorAMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.factor_a_min_time, 'YYYY-MM-DD HH:mm:ss'));
    
    factorBAvgValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_b_avg_value, 2));
    factorBMaxValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_b_max_value, 2));
    factorBMaxTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.factor_b_max_time, 'YYYY-MM-DD HH:mm:ss'));
    factorBMinValueData.value = props.curChartData.aPath.map((item) => formatNumber(item.factor_b_min_value, 2));
    factorBMinTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.factor_b_min_time, 'YYYY-MM-DD HH:mm:ss'));
    if(props.timeRadio=='近一小时'){
      createTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH:mm:ss'));
    }else if(props.timeRadio=='近一月'){
      createTimeData.value = props.curChartData.aPath.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
    }else{
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
              return value + ' ';
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            let result = '';
            params.forEach(item => {
              result += item.marker+`${item.seriesName}: ${item.value} <br/>`;
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
          { name: '总功率因素', type: 'line', ...circle, data:  factorTotalData.value,},
          { name: 'A路功率因素', type: 'line', ...circle, data: factorAData.value, },
          { name: 'B路功率因素', type: 'line', ...circle, data: factorBData.value,},
        ],
      };
    }else{
      let series=[]
      if(props.avgMaxMin=='最大'){
        series=[
          { name: '总功率因素', type: 'line', ...circle, data: factorTotalMaxValueData.value,},
          { name: 'A路功率因素', type: 'line', ...circle, data: factorAMaxValueData.value,},
          { name: 'B路功率因素', type: 'line', ...circle, data: factorBMaxValueData.value,},
        ]
      }
      if(props.avgMaxMin=='最小'){
        series=[
          { name: '总功率因素', type: 'line', ...circle, data: factorTotalMinValueData.value, },
          { name: 'A路功率因素', type: 'line', ...circle, data: factorAMinValueData.value,} ,
          { name: 'B路功率因素', type: 'line', ...circle, data: factorBMinValueData.value, }, 
        ]
      }
      if(props.avgMaxMin=='平均'){
        series=[
          { name: '总功率因素', type: 'line', ...circle, data: factorTotalAvgValueData.value ,},
          { name: 'A路功率因素', type: 'line', ...circle, data: factorAAvgValueData.value, },
          { name: 'B路功率因素', type: 'line', ...circle, data: factorBAvgValueData.value, },
        ]
      }
      chartOptions.value = {
        title: { text: '' },
        legend: { orient: 'horizontal', right: '25',data: ['总功率因素','A路功率因素','B路功率因素',]
          // selected:  { '总平均功率因素': false, 'A路平均功率因素': false, 'B路平均功率因素': false, '总最小功率因素': false, 'A路最小功率因素': false, 'B路最小功率因素': false, "总最大功率因素": true, "A路最大功率因素": true, "B路最大功率因素": true }
         },
        dataZoom: [{ type: "inside" }],
        xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: function (value) {
              return value + ' ';
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
                  case "总功率因素":
                    tooltipContent += item.marker +item.seriesName + ':&nbsp;&nbsp;' + buKongGe(item.value,2) + ' 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "A路功率因素":
                  case "B路功率因素":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) + ' 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                }
              }
              if(props.avgMaxMin=='最大'){
                switch (item.seriesName) { 
                  case "总功率因素":
                  tooltipContent += item.marker +item.seriesName + ':&nbsp;&nbsp;' + buKongGe(item.value,2) +  ' 发生时间: ' +factorTotalMaxTimeData.value[item.dataIndex] + ' ' + '   <br/>';
                  break;
                  case "A路功率因素":
                  tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,2) + ' 发生时间: ' +factorAMaxTimeData.value[item.dataIndex] + ' ' + '   <br/>';
                  break;
                  case "B路功率因素":
                  tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,2) +' 发生时间: ' +factorBMaxTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
                  break;
                }
              }
              if(props.avgMaxMin=='最小'){
                switch (item.seriesName) { 
                  case "总功率因素":
                    tooltipContent += item.marker +item.seriesName + ':&nbsp;&nbsp;' + buKongGe(item.value,2) +  ' 发生时间: ' +factorTotalMinTimeData.value[item.dataIndex] + ' ' + '   <br/>';
                    break;
                  case "A路功率因素":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) + ' 发生时间: ' +factorAMinTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "B路功率因素":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) +' 发生时间: ' +factorBMinTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
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