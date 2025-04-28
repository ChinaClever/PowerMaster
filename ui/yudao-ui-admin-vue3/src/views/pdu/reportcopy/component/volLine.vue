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

const curvolAData = ref({ volValueList: [] as number[] });
const curvolBData = ref({ volValueList: [] as number[] });
const curvolCData = ref({ volValueList: [] as number[] });
const lineidDateTimes = ref([] as string[]);

const updateChartData = () => {
  lineidDateTimes.value = prop.list.dateTimes;
  if(prop.dataType == 1){
    
    lineAName.value = 'A相最大电压'
    lineBName.value = 'B相最大电压'
    lineCName.value = 'C相最大电压'
    prop.list.l.forEach(item => curvolAData.value.volValueList.push(item.vol_max_value.toFixed(2)));
  prop.list.ll.forEach(item => curvolBData.value.volValueList.push(item.vol_max_value.toFixed(2)));
  prop.list.lll.forEach(item => curvolCData.value.volValueList.push(item.vol_max_value.toFixed(2)));
  }else if(prop.dataType == 0){
    lineAName.value = 'A相平均电压'
    lineBName.value = 'B相平均电压'
    lineCName.value = 'C相平均电压'
    prop.list.l.forEach(item => curvolAData.value.volValueList.push(item.vol_avg_value.toFixed(2)));
  prop.list.ll.forEach(item => curvolBData.value.volValueList.push(item.vol_avg_value.toFixed(2)));
  prop.list.lll.forEach(item => curvolCData.value.volValueList.push(item.vol_avg_value.toFixed(2)));
  }else if(prop.dataType == -1){
    lineAName.value = 'A相最小电压'
    lineBName.value = 'B相最小电压'
    lineCName.value = 'C相最小电压'
    prop.list.l.forEach(item => curvolAData.value.volValueList.push(item.vol_min_value.toFixed(2)));
  prop.list.ll.forEach(item => curvolBData.value.volValueList.push(item.vol_min_value.toFixed(2)));
  prop.list.lll.forEach(item => curvolCData.value.volValueList.push(item.vol_min_value.toFixed(2)));
  }
  
  
};

updateChartData(); // 初始数据填充

const echartsOptions = computed(() => ({
  title: { text: '' },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = params[0].name + '<br>';
      params.forEach(param => {
        result += `${param.marker}${param.seriesName}: &nbsp;&nbsp;&nbsp;&nbsp发生时间:${params[0].name}&nbsp;&nbsp;&nbsp;&nbsp${param.value}V`;
        // if (param.seriesName === 'A相电压' || param.seriesName === 'B相电压' || param.seriesName === 'C相电压') {
        //   result += 'V';
        // }
   
        
        result += '<br>';
      });
      return result.trimEnd();
    }
  },
  color:['#E5B849','#C8603A','#AD3762'],
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
    { name: lineAName.value, type: 'line', data: curvolAData.value.volValueList, symbol: 'circle', symbolSize: 0 },
    { name: lineBName.value, type: 'line', data: curvolBData.value.volValueList, symbol: 'circle', symbolSize: 0 },
    { name: lineCName.value, type: 'line', data: curvolCData.value.volValueList, symbol: 'circle', symbolSize: 0 }
  ]
}));

watchEffect(() => {
  // 清空 curvolAData 中的数组
  curvolAData.value = {
    volValueList: []
  };
  
  // 清空 curvolBData 中的数组
  curvolBData.value = {
    volValueList: []
  };
  
  // 清空 curvolCData 中的数组
  curvolCData.value = {
    volValueList: []
  };
  
  // 清空 lineidDateTimes 数组
  lineidDateTimes.value = [];

  updateChartData(); // 当 prop.list 变化时更新图表数据
});

console.log("Initial list", prop.list);
</script>

<style lang="scss" scoped>
/* 样式 */
</style>
