<template>
  <Echart :options="echartsOptions" />
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, defineProps } from 'vue';
import 'echarts';

const prop = defineProps({
  list: {
    type: Object,
    required: true
  },
  dataType:{
      type : Number,
      default : 1
  }
});

const lineAName = ref('')
const lineBName = ref('')
const lineCName = ref('')
const happenATime = ref()
const happenBTime = ref()
const happenCTime = ref()



console.log('curLine666', prop.list);

const curvolAData = ref({ curValueList: [] as number[] });
const curvolBData = ref({ curValueList: [] as number[] });
const curvolCData = ref({ curValueList: [] as number[] });
const lineidDateTimes = ref([] as string[]);

const updateChartData = () => {

  lineidDateTimes.value = prop.list.dateTimes;
  if(prop.dataType==1){
    lineAName.value = 'A相最大电流'
    lineBName.value = 'B相最大电流'
    lineCName.value = 'C相最大电流'
    happenATime.value =  prop.list.l.map(item => item.cur_max_time);
  happenBTime.value =  prop.list.ll.map(item => item.cur_max_time);
  happenCTime.value =  prop.list.lll.map(item => item.cur_max_time);
    prop.list.l.forEach(item => curvolAData.value.curValueList.push(item.cur_max_value.toFixed(2)));
  prop.list.ll.forEach(item => curvolBData.value.curValueList.push(item.cur_max_value.toFixed(2)));
  prop.list.lll.forEach(item => curvolCData.value.curValueList.push(item.cur_max_value.toFixed(2)));
  }else if(prop.dataType == 0){
    lineAName.value = 'A相平均电流'
    lineBName.value = 'B相平均电流'
    lineCName.value = 'C相平均电流'
    prop.list.l.forEach(item => curvolAData.value.curValueList.push(item.cur_avg_value.toFixed(2)));
  prop.list.ll.forEach(item => curvolBData.value.curValueList.push(item.cur_avg_value.toFixed(2)));
  prop.list.lll.forEach(item => curvolCData.value.curValueList.push(item.cur_avg_value.toFixed(2)));
  }else if(prop.dataType == -1){
    happenATime.value =  prop.list.l.map(item => item.cur_min_time);
  happenBTime.value =  prop.list.ll.map(item => item.cur_min_time);
  happenCTime.value =  prop.list.lll.map(item => item.cur_min_time);
    lineAName.value = 'A相最小电流'
    lineBName.value = 'B相最小电流'
    lineCName.value = 'C相最小电流'
    prop.list.l.forEach(item => curvolAData.value.curValueList.push(item.cur_min_value.toFixed(2)));
  prop.list.ll.forEach(item => curvolBData.value.curValueList.push(item.cur_min_value.toFixed(2)));
  prop.list.lll.forEach(item => curvolCData.value.curValueList.push(item.cur_min_value.toFixed(2)));
  }


};

updateChartData(); // 初始数据填充

const echartsOptions = computed(() => ({
  title: { text: '' },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {      
      let result = params[0].name + '<br>';
             const dataIndex = params[0].dataIndex;
      if(prop.dataType != 0){
        for (var i = 0; i < params.length; i++) {
          result +=  params[i].marker + params[i].seriesName+': &nbsp&nbsp&nbsp&nbsp发生时间:'
        if (params[i].seriesName.includes("A")) {
          result += happenATime.value[dataIndex] +'&nbsp&nbsp&nbsp&nbsp' +params[i].value + ' A' ;
        }else if (params[i].seriesName.includes("B")) {
          result += happenBTime.value[dataIndex] +'&nbsp&nbsp&nbsp&nbsp' +params[i].value +  ' A'; 
        } else if (params[i].seriesName.includes("C")) {
          result += happenCTime.value[dataIndex] +'&nbsp&nbsp&nbsp&nbsp' +params[i].value + ' A';
        }

        result += '<br>';
      }
      }else{
        for (var i = 0; i < params.length; i++) {
          result +=  params[i].marker + params[i].seriesName
        if (params[i].seriesName.includes("A")) {
          result += '&nbsp&nbsp&nbsp&nbsp' +params[i].value + ' A' ;
        }else if (params[i].seriesName.includes("B")) {
          result += '&nbsp&nbsp&nbsp&nbsp' +params[i].value +  ' A'; 
        } else if (params[i].seriesName.includes("C")) {
          result += '&nbsp&nbsp&nbsp&nbsp' +params[i].value + ' A';
        }
        result += '<br>';
      }
      }

 
      return result.trimEnd();
    }
  },
  color:['#E5B849','#C8603A','#5337A9'],
  legend: {
    data: [lineAName.value, lineBName.value, lineCName.value],
    selectedMode: 'multiple'
  },
  grid: { left: '3%', right: '4%', bottom: '5%', containLabel: true },
  toolbox: { feature: { saveAsImage: {}, dataView: {}, dataZoom: {}, restore: {} } },
  xAxis: {
    type: 'category', nameLocation: 'end', boundaryGap: false, data: lineidDateTimes.value
  },
  yAxis: { type: 'value' },
  series: [
    { name: lineAName.value, type: 'line', data: curvolAData.value.curValueList, symbol: 'circle', symbolSize: 0 },
    { name: lineBName.value, type: 'line', data: curvolBData.value.curValueList, symbol: 'circle', symbolSize: 0 },
    { name: lineCName.value, type: 'line', data: curvolCData.value.curValueList, symbol: 'circle', symbolSize: 0 }
  ]
}));

watchEffect(() => {
  // 清空 curvolAData 中的数组
  curvolAData.value = {
    curValueList: []
  };
  
  // 清空 curvolBData 中的数组
  curvolBData.value = {
    curValueList: []
  };
  
  // 清空 curvolCData 中的数组
  curvolCData.value = {
    curValueList: []
  };
  
  // 清空 lineidDateTimes 数组
  lineidDateTimes.value = [];

  updateChartData(); // 当 prop.list 变化时更新图表数据

});


</script>

<style lang="scss" scoped>
/* 样式 */
</style>
