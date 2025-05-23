<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
import { ref } from 'vue';
import { formatDate } from '@/utils/formatTime'

const props = defineProps({
  curChartData: Object,
  timeRadio: String,
  avgMaxMin:String
});

const a1Cur=ref([])
const a2Cur=ref([])
const a3Cur=ref([])
const b1Cur=ref([])
const b2Cur=ref([])
const b3Cur=ref([])

const a1AvgCur=ref([])
const a1MaxCur=ref([])
const a1MaxCurTime=ref([])
const a1MinCur=ref([])
const a1MinCurTime=ref([])

const a2AvgCur=ref([])
const a2MaxCur=ref([])
const a2MaxCurTime=ref([])
const a2MinCur=ref([])
const a2MinCurTime=ref([])

const a3AvgCur=ref([])
const a3MaxCur=ref([])
const a3MaxCurTime=ref([])
const a3MinCur=ref([])
const a3MinCurTime=ref([])

const b1AvgCur=ref([])
const b1MaxCur=ref([])
const b1MaxCurTime=ref([])
const b1MinCur=ref([])
const b1MinCurTime=ref([])

const b2AvgCur=ref([])
const b2MaxCur=ref([])
const b2MaxCurTime=ref([])
const b2MinCur=ref([])
const b2MinCurTime=ref([])

const b3AvgCur=ref([])
const b3MaxCur=ref([])
const b3MaxCurTime=ref([])
const b3MinCur=ref([])
const b3MinCurTime=ref([])

const createTimeData=ref([])

const chartOptions = ref({
  title: { text: '' },
  legend: { orient: 'horizontal', right: '25' },
  tooltip: {
    trigger: 'axis', // 触发方式：坐标轴触发
    formatter: function (params) {
      // params 是一个数组，包含当前悬停点的所有系列信息
      let result = `${params[0].axisValue}<br>`; // 显示时间
      params.forEach((item) => {
        result += `${item.seriesName}: ${item.value}<br>`; // 显示系列名称和值
      });
      return result;
    },
  },
  dataZoom: [{ type: 'inside' }],
  xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: function (value) {
        return value + ' A';
      },
    },
  },
  grid: {
    left: '5%', // 设置左侧边距
    right: '5%', // 设置右侧边距
    top: '10%', // 设置上侧边距
    bottom: '10%', // 设置下侧边距
  }
});

function buKongGe(value,du){
  value=Number(value);
  console.log(value);
  if(value<100&&value>=10) return "&nbsp;&nbsp;&nbsp;"+value.toFixed(du);
  if(value<10) return "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+value.toFixed(du);
  return "&nbsp;"+value.toFixed(du);
}

watch(
  () => [props.curChartData,props.avgMaxMin],
  (newDatas, oldData) => {
    let newData=newDatas[0];
    if (newData != null) {
      a1Cur.value=newData?.aPathVc?.map((item) => item.curValue?.toFixed(2))||[];
      a2Cur.value=newData?.aPathVc?.map((item) => item.curValuell?.toFixed(2))||[];
      a3Cur.value=newData?.aPathVc?.map((item) => item.curValuelll?.toFixed(2))||[];
      b1Cur.value=newData?.bPathVc?.map((item) => item.curValue?.toFixed(2))||[];
      b2Cur.value=newData?.bPathVc?.map((item) => item.curValuell?.toFixed(2))||[];
      b3Cur.value=newData?.bPathVc?.map((item) => item.curValuelll?.toFixed(2))||[];

      a1AvgCur.value=newData?.aPathVc?.map((item) => item.curValue?.toFixed(2))||[];
      a1MaxCur.value=newData?.aPathVc?.map((item) => item.maxCurValue?.toFixed(2))||[];
      a1MaxCurTime.value=newData?.aPathVc?.map((item) => formatDate(item.maxCurValueTime,'YYYY-MM-DD HH:mm:ss'))||[];
      a1MinCur.value=newData?.aPathVc?.map((item) => item.minCurValue?.toFixed(2))||[];
      a1MinCurTime.value=newData?.aPathVc?.map((item) => formatDate(item.minCurValueTime,'YYYY-MM-DD HH:mm:ss'))||[];

      a2AvgCur.value=newData?.aPathVc?.map((item) => item.curValuell?.toFixed(2))||[];
      a2MaxCur.value=newData?.aPathVc?.map((item) => item.maxCurValuell?.toFixed(2))||[];
      a2MaxCurTime.value=newData?.aPathVc?.map((item) => formatDate(item.maxCurValuellTime,'YYYY-MM-DD HH:mm:ss'))||[];
      a2MinCur.value=newData?.aPathVc?.map((item) => item.minCurValuell?.toFixed(2))||[];
      a2MinCurTime.value=newData?.aPathVc?.map((item) => formatDate(item.minCurValuellTime,'YYYY-MM-DD HH:mm:ss'))||[];

      a3AvgCur.value=newData?.aPathVc?.map((item) => item.curValuelll?.toFixed(2))||[];
      a3MaxCur.value=newData?.aPathVc?.map((item) => item.maxCurValuelll?.toFixed(2))||[];
      a3MaxCurTime.value=newData?.aPathVc?.map((item) => formatDate(item.maxCurValuelllTime,'YYYY-MM-DD HH:mm:ss'))||[];
      a3MinCur.value=newData?.aPathVc?.map((item) => item.minCurValuelll?.toFixed(2))||[];
      a3MinCurTime.value=newData?.aPathVc?.map((item) => formatDate(item.minCurValuelllTime,'YYYY-MM-DD HH:mm:ss'))||[];

      b1AvgCur.value=newData?.bPathVc?.map((item) => item.volValue?.toFixed(2))||[];
      b1MaxCur.value=newData?.bPathVc?.map((item) => item.maxVolValue?.toFixed(2))||[];
      b1MaxCurTime.value=newData?.bPathVc?.map((item) => formatDate(item.maxVolValueTime,'YYYY-MM-DD HH:mm:ss'))||[];
      b1MinCur.value=newData?.bPathVc?.map((item) => item.minVolValue?.toFixed(2))||[];
      b1MinCurTime.value=newData?.bPathVc?.map((item) => formatDate(item.minVolValueTime,'YYYY-MM-DD HH:mm:ss'))||[];

      b2AvgCur.value=newData?.bPathVc?.map((item) => item.volValuell?.toFixed(2))||[];
      b2MaxCur.value=newData?.bPathVc?.map((item) => item.maxVolValuell?.toFixed(2))||[];
      b2MaxCurTime.value=newData?.bPathVc?.map((item) => formatDate(item.maxVolValuellTime,'YYYY-MM-DD HH:mm:ss'))||[];
      b2MinCur.value=newData?.bPathVc?.map((item) => item.minVolValuell?.toFixed(2))||[];
      b2MinCurTime.value=newData?.bPathVc?.map((item) => formatDate(item.minVolValuellTime,'YYYY-MM-DD HH:mm:ss'))||[];

      b3AvgCur.value=newData?.bPathVc?.map((item) => item.volValuelll?.toFixed(2))||[];
      b3MaxCur.value=newData?.bPathVc?.map((item) => item.maxVolValuelll?.toFixed(2))||[];
      b3MaxCurTime.value=newData?.bPathVc?.map((item) => formatDate(item.maxVolValuelllTime,'YYYY-MM-DD HH:mm:ss'))||[];
      b3MinCur.value=newData?.bPathVc?.map((item) => item.minVolValuelll?.toFixed(2))||[];
      b3MinCurTime.value=newData?.bPathVc?.map((item) => formatDate(item.minVolValuelllTime,'YYYY-MM-DD HH:mm:ss'))||[];
      if(props.timeRadio=='近一月'){
        createTimeData.value = props.curChartData.aPathVc?.map((item) => formatDate(item.createTime, 'YYYY-MM-DD'));
        if(createTimeData.value==null||createTimeData.value.length==0){
          createTimeData.value = props.curChartData.bPathVc?.map((item) => formatDate(item.createTime, 'YYYY-MM-DD'));
        }
      }else{
        createTimeData.value = props.curChartData.aPathVc?.map((item) => formatDate(item.createTime, 'YYYY-MM-DD HH:mm:ss'));
        if(createTimeData.value==null||createTimeData.value.length==0){
          createTimeData.value = props.curChartData.bPathVc?.map((item) => formatDate(item.createTime, 'YYYY-MM-DD HH:mm:ss'));
        }
      }
      // if(!props.curChartData?.aPathVc || !props.curChartData?.aPathVc.length) {
      //   L1Data.value = 0
      //   L2Data.value = 0
      //   L3Data.value = 0
      // }
      // else if (props.curChartData?.aPathVc) {
      //   L1Data.value = props.curChartData.aPathVc.map((item) => item.curValue);
      //   L2Data.value = props.curChartData.aPathVc.map((item) => item.curValuell || 0);
      //   L3Data.value = props.curChartData.aPathVc.map((item) => item.curValuelll || 0);
      //   createTimeData.value = props.curChartData.aPathVc.map((item) => item.createTime);
      // }

      // if(!props.curChartData?.bPathVc || !props.curChartData?.bPathVc.length) {
      //   L4Data.value = 0
      //   L5Data.value = 0
      //   L6Data.value = 0
      // }
      // else if (props.curChartData?.bPathVc) {
      //   L4Data.value = props.curChartData.bPathVc.map((item) => item.curValue);
      //   L5Data.value = props.curChartData.bPathVc.map((item) => item.curValuell || 0);
      //   L6Data.value = props.curChartData.bPathVc.map((item) => item.curValuelll || 0);
      //   createTimeData.value = props.curChartData.bPathVc.map((item) => item.createTime);
      // }
      
    //   chartOptions.value = {
    //     ...chartOptions.value,
    //     xAxis: { ...chartOptions.value.xAxis, data: createTimeData.value },
    //     series: [
    //       { ...chartOptions.value.series[0], data: L1Data.value },
    //       { ...chartOptions.value.series[1], data: L2Data.value },
    //       { ...chartOptions.value.series[2], data: L3Data.value },
    //       { ...chartOptions.value.series[3], data: L4Data.value },
    //       { ...chartOptions.value.series[4], data: L5Data.value },
    //       { ...chartOptions.value.series[5], data: L6Data.value },
    //     ],
    //   };
    let circle={symbol:'none'};
    if(createTimeData.value?.length==1){
      circle={symbol: 'circle'}
    }
      let colors=[];
      let series=[];
      if(props.timeRadio=='近一小时'){

        if(haveData(a1Cur.value)){
          series.push({ name: 'A-L1', type: 'line', ...circle, data: a1Cur.value,...colors.shift()});
        }
        if(haveData(a2Cur.value)){
          series.push({ name: 'A-L2', type: 'line', ...circle, data: a2Cur.value,...colors.shift()});
        }
        if(haveData(a3Cur.value)){
          series.push({name: 'A-L3', type: 'line', ...circle, data: a3Cur.value,...colors.shift()});
        }

        if(haveData(b1Cur.value)){
          series.push({ name: 'B-L1', type: 'line', ...circle, data: b1Cur.value,...colors.shift() });
        }
        if(haveData(b2Cur.value)){
          series.push({ name: 'B-L2', type: 'line', ...circle, data: b2Cur.value,...colors.shift()});
        }
        if(haveData(b3Cur.value)){
          series.push({name: 'B-L3', type: 'line', ...circle, data: b3Cur.value,...colors.shift()});
        }

        chartOptions.value = {
          title: { text: '' },
          legend: { orient: 'horizontal', right: '25' },
          tooltip: {
            trigger: 'axis', // 触发方式：坐标轴触发
            formatter: function (params) {
              // params 是一个数组，包含当前悬停点的所有系列信息
              let result = ''
              params.forEach((item) => {
                result += `${item.seriesName}: ${item.value} A<br>`; // 显示系列名称和值
              });
              result+=`时间：${params[0].axisValue}<br>`; // 显示时间
              return result;
            },
          },
          dataZoom: [{ type: 'inside' }],
          xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
          yAxis: {
            type: 'value',
            axisLabel: {
              formatter: function (value) {
                return value + ' A';
              },
            },
          },
          grid: {
            left: '5%', // 设置左侧边距
            right: '5%', // 设置右侧边距
            top: '10%', // 设置上侧边距
            bottom: '10%', // 设置下侧边距
          },
          series
        }
    }else{
      let choose=newDatas[1];
      console.log(choose,"==============choose");
      if(haveData(a1AvgCur.value)){
        if(choose=='平均'){
          series.push( { name: 'A-L1', type: 'line', ...circle, data: a1AvgCur.value ,...colors.shift()});
        }
        if(choose=='最大'){
          series.push({ name: 'A-L1', type: 'line', ...circle, data: a1MaxCur.value,...colors.shift()});
        }
        if(choose=='最小'){
          series.push({ name: 'A-L1', type: 'line', ...circle, data: a1MinCur.value, ...colors.shift()});
        }
      }
      if(haveData(a2AvgCur.value)){
        if(choose=='平均'){
          series.push( { name: 'A-L2', type: 'line', ...circle, data: a2AvgCur.value,...colors.shift() });
        }
        if(choose=='最大'){
          series.push({ name: 'A-L2', type: 'line', ...circle, data: a2MaxCur.value,...colors.shift()});
        }
        if(choose=='最小'){
          series.push({ name: 'A-L2', type: 'line', ...circle, data: a2MinCur.value, ...colors.shift()});
        }
      }
      if(haveData(a3AvgCur.value)){
        if(choose=='平均'){
          series.push( { name: 'A-L3', type: 'line', ...circle, data: a3AvgCur.value,...colors.shift() });
        }
        if(choose=='最大'){
          series.push({  name: 'A-L3', type: 'line', ...circle, data: a3MaxCur.value,...colors.shift()});
        }
        if(choose=='最小'){
          series.push({ name: 'A-L3', type: 'line', ...circle, data: a3MinCur.value,  ...colors.shift()});
        }
      }

      if(haveData(b1AvgCur.value)){
        if(choose=='平均'){
          series.push( {  name: 'B-L1', type: 'line', ...circle, data: b1AvgCur.value ,...colors.shift()});
        }
        if(choose=='最大'){
          series.push({ name: 'B-L1', type: 'line', ...circle, data: b1MaxCur.value,...colors.shift()});
        }
        if(choose=='最小'){
          series.push({ name: 'B-L1', type: 'line', ...circle, data: b1MinCur.value, ...colors.shift()});
        }
      }
      if(haveData(b2AvgCur.value)){
        if(choose=='平均'){
          series.push( { name: 'B-L2', type: 'line', ...circle, data: b2AvgCur.value,...colors.shift() });
        }
        if(choose=='最大'){
          series.push({ name: 'B-L2', type: 'line', ...circle, data: b2MaxCur.value,...colors.shift()});
        }
        if(choose=='最小'){
          series.push({ name: 'B-L2', type: 'line', ...circle, data: b2MinCur.value, ...colors.shift()});
        }
      }
      if(haveData(b3AvgCur.value)){
        if(choose=='平均'){
          series.push( { name: 'B-L3', type: 'line', ...circle, data: b3AvgCur.value,...colors.shift() });
        }
        if(choose=='最大'){
          series.push({  name: 'B-L3', type: 'line', ...circle, data: b3MaxCur.value,...colors.shift()});
        }
        if(choose=='最小'){
          series.push({ name: 'B-L3', type: 'line', ...circle, data: b3MinCur.value,  ...colors.shift()});
        }
      }
      chartOptions.value = {
        title: { text: '' },
        legend: { orient: 'horizontal', right: '25',data: ['A-L1','A-L2','A-L3','B-L1','B-L2','B-L3'],
          selected:  { 'A-L1':true,'A-L2':true,'A-L3':true,'B-L1':true,'B-L2':true,'B-L3':true }
         },
        dataZoom: [{ type: "inside" }],
        xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: function (value) {
              return value + ' A';
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
                  case "A-L1":
                  case "A-L2":
                  case "A-L3":
                  case "B-L1":
                  case "B-L2":
                  case "B-L3":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,2) + 'A 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' + ' <br/>';
                    break;
                }
              }
              if(props.avgMaxMin=='最大'){
                switch (item.seriesName) { 
                  case "A-L1":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,2) +'A 发生时间: ' +a1MaxCurTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "A-L2":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) +  'A 发生时间: ' +a2MaxCurTime.value[item.dataIndex] + ' ' + '   <br/>';
                    break;
                  case "A-L3":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) + 'A 发生时间: ' +a3MaxCurTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "B-L1":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) + 'A 发生时间: ' +b1MaxCurTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "B-L2":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) + 'A 发生时间: ' +b2MaxCurTime.value[item.dataIndex] + ' ' +  '  <br/>';
                    break;
                  case "B-L3":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,2) + 'A 发生时间: ' +b3MaxCurTime.value[item.dataIndex] + ' ' + '   <br/>';
                    break;
                }
              }
              if(props.avgMaxMin=='最小'){
                switch (item.seriesName) { 
                  case "A-L1":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,2) +'A 发生时间: ' +a1MinCurTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "A-L2":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) + 'A 发生时间: ' +a2MinCurTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "A-L3":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) + 'A 发生时间: ' +a3MinCurTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "B-L1":
                    tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,2)+ 'A 发生时间: ' +b1MinCurTime.value[item.dataIndex] + ' '  + '   <br/>';
                    break;
                  case "B-L2":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) + 'A 发生时间: ' +b2MinCurTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "B-L3":
                    tooltipContent += item.marker +item.seriesName + ':' + buKongGe(item.value,2) + 'A 发生时间: ' +b3MinCurTime.value[item.dataIndex] + ' ' +  '   <br/>';
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
        series
      };
    }

    }
  },
  { deep: true,immediate: true }
);
function haveData(arr){
  if(arr==null||arr.length == 0){
    return false;
  }
  for(let i = 0; i < arr.length; i++) {
    if(arr[i]!=null){
      return true;
    }
  }
  return false;
}
</script>

<style lang="less" scoped>
</style>
