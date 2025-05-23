<template>
  <Echart :height="height" :width="width" :options="chartOptions" />
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { toRefs } from 'vue';
import { formatDate } from '@/utils/formatTime'

const props = defineProps({
  curChartData: Object,
  timeRadio: String,
  avgMaxMin:String
});


const a1Vol=ref([])
const a2Vol=ref([])
const a3Vol=ref([])
const b1Vol=ref([])
const b2Vol=ref([])
const b3Vol=ref([])

const a1AvgVol=ref([])
const a1MaxVol=ref([])
const a1MaxVolTime=ref([])
const a1MinVol=ref([])
const a1MinVolTime=ref([])

const a2AvgVol=ref([])
const a2MaxVol=ref([])
const a2MaxVolTime=ref([])
const a2MinVol=ref([])
const a2MinVolTime=ref([])

const a3AvgVol=ref([])
const a3MaxVol=ref([])
const a3MaxVolTime=ref([])
const a3MinVol=ref([])
const a3MinVolTime=ref([])

const b1AvgVol=ref([])
const b1MaxVol=ref([])
const b1MaxVolTime=ref([])
const b1MinVol=ref([])
const b1MinVolTime=ref([])

const b2AvgVol=ref([])
const b2MaxVol=ref([])
const b2MaxVolTime=ref([])
const b2MinVol=ref([])
const b2MinVolTime=ref([])

const b3AvgVol=ref([])
const b3MaxVol=ref([])
const b3MaxVolTime=ref([])
const b3MinVol=ref([])
const b3MinVolTime=ref([])
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
        return value + ' V';
      },
    },
  },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = '';
      params.forEach(item => {
        result += `${item.seriesName}: ${item.value} V<br/>`;
      });
      return result;
    },
  },
  grid: {
    left: '5%',
    right: '5%',
    top: '10%',
    bottom: '10%',
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
  () =>[props.curChartData,props.avgMaxMin],
  (newDatas) => {
    let newData=newDatas[0]
    if (newData != null) {
      a1Vol.value=newData?.aPathVc?.map((item) => item.volValue?.toFixed(1))||[];
      a2Vol.value=newData?.aPathVc?.map((item) => item.volValuell?.toFixed(1))||[];
      a3Vol.value=newData?.aPathVc?.map((item) => item.volValuelll?.toFixed(1))||[];
      b1Vol.value=newData?.bPathVc?.map((item) => item.volValue?.toFixed(1))||[];
      b2Vol.value=newData?.bPathVc?.map((item) => item.volValuell?.toFixed(1))||[];
      b3Vol.value=newData?.bPathVc?.map((item) => item.volValuelll?.toFixed(1))||[];

      a1AvgVol.value=newData?.aPathVc?.map((item) => item.volValue?.toFixed(1))||[];
      a1MaxVol.value=newData?.aPathVc?.map((item) => item.maxVolValue?.toFixed(1))||[];
      a1MaxVolTime.value=newData?.aPathVc?.map((item) => formatDate(item.maxVolValueTime,'YYYY-MM-DD HH:mm:ss'))||[];
      a1MinVol.value=newData?.aPathVc?.map((item) => item.minVolValue?.toFixed(1))||[];
      a1MinVolTime.value=newData?.aPathVc?.map((item) => formatDate(item.minVolValueTime,'YYYY-MM-DD HH:mm:ss'))||[];

      a2AvgVol.value=newData?.aPathVc?.map((item) => item.volValuell?.toFixed(1))||[];
      a2MaxVol.value=newData?.aPathVc?.map((item) => item.maxVolValuell?.toFixed(1))||[];
      a2MaxVolTime.value=newData?.aPathVc?.map((item) => formatDate(item.maxVolValuellTime,'YYYY-MM-DD HH:mm:ss'))||[];
      a2MinVol.value=newData?.aPathVc?.map((item) => item.minVolValuell?.toFixed(1))||[];
      a2MinVolTime.value=newData?.aPathVc?.map((item) => formatDate(item.minVolValuellTime,'YYYY-MM-DD HH:mm:ss'))||[];

      a3AvgVol.value=newData?.aPathVc?.map((item) => item.volValuelll?.toFixed(1))||[];
      a3MaxVol.value=newData?.aPathVc?.map((item) => item.maxVolValuelll?.toFixed(1))||[];
      a3MaxVolTime.value=newData?.aPathVc?.map((item) => formatDate(item.maxVolValuelllTime,'YYYY-MM-DD HH:mm:ss'))||[];
      a3MinVol.value=newData?.aPathVc?.map((item) => item.minVolValuelll?.toFixed(1))||[];
      a3MinVolTime.value=newData?.aPathVc?.map((item) => formatDate(item.minVolValuelllTime,'YYYY-MM-DD HH:mm:ss'))||[];

      b1AvgVol.value=newData?.bPathVc?.map((item) => item.volValue?.toFixed(1))||[];
      b1MaxVol.value=newData?.bPathVc?.map((item) => item.maxVolValue?.toFixed(1))||[];
      b1MaxVolTime.value=newData?.bPathVc?.map((item) => formatDate(item.maxVolValueTime,'YYYY-MM-DD HH:mm:ss'))||[];
      b1MinVol.value=newData?.bPathVc?.map((item) => item.minVolValue?.toFixed(1))||[];
      b1MinVolTime.value=newData?.bPathVc?.map((item) => formatDate(item.minVolValueTime,'YYYY-MM-DD HH:mm:ss'))||[];

      b2AvgVol.value=newData?.bPathVc?.map((item) => item.volValuell?.toFixed(1))||[];
      b2MaxVol.value=newData?.bPathVc?.map((item) => item.maxVolValuell?.toFixed(1))||[];
      b2MaxVolTime.value=newData?.bPathVc?.map((item) => formatDate(item.maxVolValuellTime,'YYYY-MM-DD HH:mm:ss'))||[];
      b2MinVol.value=newData?.bPathVc?.map((item) => item.minVolValuell?.toFixed(1))||[];
      b2MinVolTime.value=newData?.bPathVc?.map((item) => formatDate(item.minVolValuellTime,'YYYY-MM-DD HH:mm:ss'))||[];

      b3AvgVol.value=newData?.bPathVc?.map((item) => item.volValuelll?.toFixed(1))||[];
      b3MaxVol.value=newData?.bPathVc?.map((item) => item.maxVolValuelll?.toFixed(1))||[];
      b3MaxVolTime.value=newData?.bPathVc?.map((item) => formatDate(item.maxVolValuelllTime,'YYYY-MM-DD HH:mm:ss'))||[];
      b3MinVol.value=newData?.bPathVc?.map((item) => item.minVolValuelll?.toFixed(1))||[];
      b3MinVolTime.value=newData?.bPathVc?.map((item) => formatDate(item.minVolValuelllTime,'YYYY-MM-DD HH:mm:ss'))||[];
      if(props.timeRadio=='近一小时'){
        createTimeData.value = props.curChartData.aPathVc?.map((item) => formatDate(item.createTime, 'YYYY-MM-DD HH:mm:ss'));
        if(createTimeData.value==null||createTimeData.value.length==0){
          createTimeData.value = props.curChartData.bPathVc?.map((item) => formatDate(item.createTime, 'YYYY-MM-DD HH:mm:ss'));
        }
      }else if(props.timeRadio=='近一月'){
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
      //   L1Data.value = props.curChartData.aPathVc.map((item) => item.volValue);
      //   L2Data.value = props.curChartData.aPathVc.map((item) => item.volValuell || 0);
      //   L3Data.value = props.curChartData.aPathVc.map((item) => item.volValuelll || 0);
      //   createTimeData.value = props.curChartData.aPathVc.map((item) => item.createTime);
      // }

      // if(!props.curChartData?.bPathVc || !props.curChartData?.bPathVc.length) {
      //   L4Data.value = 0
      //   L5Data.value = 0
      //   L6Data.value = 0
      // }
      // else if (props.curChartData?.bPathVc) {
      //   L4Data.value = props.curChartData.bPathVc.map((item) => item.volValue);
      //   L5Data.value = props.curChartData.bPathVc.map((item) => item.volValuell || 0);
      //   L6Data.value = props.curChartData.bPathVc.map((item) => item.volValuelll || 0);
      //   createTimeData.value = props.curChartData.bPathVc.map((item) => item.createTime);
      // }


      let circle={symbol:'none'};
    if(createTimeData.value.length==1){
      circle={symbol: 'circle'}
    }
      let colors=[];
      let series=[];
      if(props.timeRadio=='近一小时'){

        if(haveData(a1Vol.value)){
          series.push({ name: 'A-L1', type: 'line', ...circle, data: a1Vol.value,...colors.shift()});
        }
        if(haveData(a2Vol.value)){
          series.push({ name: 'A-L2', type: 'line', ...circle, data: a2Vol.value,...colors.shift()});
        }
        if(haveData(a3Vol.value)){
          series.push({name: 'A-L3', type: 'line', ...circle, data: a3Vol.value,...colors.shift()});
        }

        if(haveData(b1Vol.value)){
          series.push({ name: 'B-L1', type: 'line', ...circle, data: b1Vol.value,...colors.shift() });
        }
        if(haveData(b2Vol.value)){
          series.push({ name: 'B-L2', type: 'line', ...circle, data: b2Vol.value,...colors.shift()});
        }
        if(haveData(b3Vol.value)){
          series.push({name: 'B-L3', type: 'line', ...circle, data: b3Vol.value,...colors.shift()});
        }

        chartOptions.value = {
          title: { text: '' },
          legend: { orient: 'horizontal', right: '25' },
          tooltip: {
            trigger: 'axis', // 触发方式：坐标轴触发
            formatter: function (params) {
              // params 是一个数组，包含当前悬停点的所有系列信息
              let result = ''; // 显示时间
              params.forEach((item) => {
                result += `${item.seriesName}: ${item.value} V<br>`; // 显示系列名称和值
              });
              result +=`时间：${params[0].axisValue}<br>`;
              return result;
            },
          },
          dataZoom: [{ type: 'inside' }],
          xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
          yAxis: {
            type: 'value',
            axisLabel: {
              formatter: function (value) {
                return value + ' V';
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
      if(haveData(a1AvgVol.value)){
        if(props.avgMaxMin=='平均'){
          series.push( { name: 'A-L1', type: 'line', ...circle, data: a1AvgVol.value ,...colors.shift()});
        }
        if(props.avgMaxMin=='最大'){
          series.push({ name: 'A-L1', type: 'line', ...circle, data: a1MaxVol.value,...colors.shift()});
        }
        if(props.avgMaxMin=='最小'){
          series.push({ name: 'A-L1', type: 'line', ...circle, data: a1MinVol.value, ...colors.shift()});
        }
      }
      if(haveData(a2AvgVol.value)){
        if(props.avgMaxMin=='平均'){
          series.push( { name: 'A-L2', type: 'line', ...circle, data: a2AvgVol.value,...colors.shift() });
        }
        if(props.avgMaxMin=='最大'){
          series.push({ name: 'A-L2', type: 'line', ...circle, data: a2MaxVol.value,...colors.shift()});
        }
        if(props.avgMaxMin=='最小'){
          series.push({ name: 'A-L2', type: 'line', ...circle, data: a2MinVol.value, ...colors.shift()});
        }
      }
      if(haveData(a3AvgVol.value)){
        if(props.avgMaxMin=='平均'){
          series.push( { name: 'A-L3', type: 'line', ...circle, data: a3AvgVol.value,...colors.shift() });
        }
        if(props.avgMaxMin=='最大'){
          series.push({  name: 'A-L3', type: 'line', ...circle, data: a3MaxVol.value,...colors.shift()});
        }
        if(props.avgMaxMin=='最小'){
          series.push({ name: 'A-L3', type: 'line', ...circle, data: a3MinVol.value,  ...colors.shift()});
        }
      }

      if(haveData(b1AvgVol.value)){
        if(props.avgMaxMin=='平均'){
          series.push( {  name: 'B-L1', type: 'line', ...circle, data: b1AvgVol.value ,...colors.shift()});
        }
        if(props.avgMaxMin=='最大'){
          series.push({ name: 'B-L1', type: 'line', ...circle, data: b1MaxVol.value,...colors.shift()});
        }
        if(props.avgMaxMin=='最小'){
          series.push({ name: 'B-L1', type: 'line', ...circle, data: b1MinVol.value, ...colors.shift()});
        }
      }
      if(haveData(b2AvgVol.value)){
        if(props.avgMaxMin=='平均'){
          series.push( { name: 'B-L2', type: 'line', ...circle, data: b2AvgVol.value,...colors.shift() });
        }
        if(props.avgMaxMin=='最大'){
          series.push({ name: 'B-L2', type: 'line', ...circle, data: b2MaxVol.value,...colors.shift()});
        }
        if(props.avgMaxMin=='最小'){
          series.push({ name: 'B-L2', type: 'line', ...circle, data: b2MinVol.value, ...colors.shift()});
        }
      }
      if(haveData(b3AvgVol.value)){
        if(props.avgMaxMin=='平均'){
          series.push( { name: 'B-L3', type: 'line', ...circle, data: b3AvgVol.value,...colors.shift() });
        }
        if(props.avgMaxMin=='最大'){
          series.push({  name: 'B-L3', type: 'line', ...circle, data: b3MaxVol.value,...colors.shift()});
        }
        if(props.avgMaxMin=='最小'){
          series.push({ name: 'B-L3', type: 'line', ...circle, data: b3MinVol.value,  ...colors.shift()});
        }
      }
      chartOptions.value = {
        title: { text: '' },
        legend: { orient: 'horizontal', right: '25'},
        dataZoom: [{ type: "inside" }],
        xAxis: { type: 'category', boundaryGap: false, data: createTimeData.value },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: function (value) {
              return value + ' V';
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
                  tooltipContent += item.marker + item.seriesName + ':' + buKongGe(item.value,1) +'V 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' +  '   <br/>';
                  break;
                }
              }
              if(props.avgMaxMin=='最大'){
                switch (item.seriesName) { 
                  case "A-L1":
                    tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) + 'V 发生时间: ' +a1MaxVolTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "A-L2":
                  tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) +  'V 发生时间: ' +a2MaxVolTime.value[item.dataIndex] + ' ' + '   <br/>';
                  break;
                  case "A-L3":
                    tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) + 'V 发生时间: ' +a3MaxVolTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                    case "B-L1":
                    tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) +  'V 发生时间: ' +b1MaxVolTime.value[item.dataIndex] + ' ' + '  <br/>';
                    break;
                  case "B-L2":
                    tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) + 'V 发生时间: ' +b2MaxVolTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "B-L3":
                  tooltipContent += item.marker + item.seriesName + ': ' + buKongGe(item.value,1) +'V 发生时间: ' +b3MaxVolTime.value[item.dataIndex] + ' ' +  '   <br/>';
                  break;
                }
              }
              if(props.avgMaxMin=='最小'){
                switch (item.seriesName) { 
                  case "A-L1":
                    tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) +  'V 发生时间: ' +a1MinVolTime.value[item.dataIndex] + ' ' + '   <br/>';
                    break;
                  case "A-L2":
                    tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) + 'V 发生时间: ' +a2MinVolTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "A-L3":
                    tooltipContent += item.marker + item.seriesName + ': ' + buKongGe(item.value,1) + 'V 发生时间: ' +a3MinVolTime.value[item.dataIndex] + ' ' + '   <br/>';
                    break;
                  case "B-L1":
                    tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) +  'V 发生时间: ' +b1MinVolTime.value[item.dataIndex] + ' ' + '   <br/>';
                    break;
                  case "B-L2":
                    tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) + 'V 发生时间: ' +b2MinVolTime.value[item.dataIndex] + ' ' +  '   <br/>';
                    break;
                  case "B-L3":
                    tooltipContent += item.marker +item.seriesName + ': ' + buKongGe(item.value,1) +  'V 发生时间: ' +b3MinVolTime.value[item.dataIndex] + ' ' + '   <br/>';
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