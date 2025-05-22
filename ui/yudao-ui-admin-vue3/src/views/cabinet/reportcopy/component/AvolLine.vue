<!-- <template>
  <Echart :options="echartsOptions" />
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, defineProps } from 'vue';
import 'echarts';

const prop = defineProps({
  list: {
    type: Object,
    required: true
  }
});

console.log('66666666666666', prop.list);

const curvolAData = ref({ volValueList: [] as number[] });
const curvolBData = ref({ volValueList: [] as number[] });
const curvolCData = ref({ volValueList: [] as number[] });
const lineidDateTimes = ref([] as string[]);

const updateChartData = () => {
  lineidDateTimes.value = prop.list.dateTimes;
  prop.list.l.forEach(item => curvolAData.value.volValueList.push(item.vol_avg_value.toFixed(2)));
  prop.list.ll.forEach(item => curvolBData.value.volValueList.push(item.vol_avg_value.toFixed(2)));
  prop.list.lll.forEach(item => curvolCData.value.volValueList.push(item.vol_avg_value.toFixed(2)));
};

updateChartData(); // 初始数据填充

const echartsOptions = computed(() => ({
  title: { text: '' },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = params[0].name + '<br>';
      params.forEach(param => {
        result += `${param.marker}${param.seriesName}: &nbsp;&nbsp;&nbsp;&nbsp${param.value}`;
        if (param.seriesName === 'A相电压' || param.seriesName === 'B相电压' || param.seriesName === 'C相电压') {
          result += 'V';
        }
        result += '<br>';
      });
      return result.trimEnd();
    }
  },
  
  legend: {
    data: ['A相电压', 'B相电压', 'C相电压'],
    selectedMode: 'multiple'
  },
  grid: { left: '3%', right: '4%', bottom: '5%', containLabel: true },
  toolbox: { feature: { saveAsImage: {}, dataView: {}, dataZoom: {}, restore: {} } },
  xAxis: {
    type: 'category', nameLocation: 'end', boundaryGap: false, data: lineidDateTimes.value
  },
  yAxis: { type: 'value' },
  series: [
    { name: 'A相电压', type: 'line', data: curvolAData.value.volValueList, symbol: 'circle', symbolSize: 4 },
    { name: 'B相电压', type: 'line', data: curvolBData.value.volValueList, symbol: 'circle', symbolSize: 4 },
    { name: 'C相电压', type: 'line', data: curvolCData.value.volValueList, symbol: 'circle', symbolSize: 4 }
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
</style> -->
<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'

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

const series = ref()
const time = ref()
const legendList = ref()
const happenTime = ref()
var dataTypeName = '';


// 设置饼图的选项
const echartsOption = ref({
  dataZoom:[{ type:"inside"}],
  color:['#E5B849','#C8603A','#5337A9','#614E43','#94B159'],
  legend: { data: legendList,
    type: 'scroll', // 设置为 'single' 或 'multiple'
    orient: 'horizontal', // 设置为 'horizontal' 或 'vertical'
    width:1000
  },
  tooltip: { trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        if(prop.dataType == 0){
          result +=  params[i].marker + params[i].seriesName+dataTypeName+'：'+ params[i].value.toFixed(2)+' V' + '&nbsp&nbsp&nbsp&nbsp'+'发生时间：'+ params[0].name  ;
        }else{
          result +=  params[i].marker + params[i].seriesName +dataTypeName+'：'+ + params[i].value.toFixed(2)+' V'+ '&nbsp&nbsp&nbsp&nbsp'+'发生时间：'+happenTime.value[i][params[i].dataIndex] ;
        }
        result += '<br>';
      }
      return result;
    } 
  },
  xAxis: {type: 'category', boundaryGap: false, data : time},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},  
  dataView: {
      optionToContent: function(opt) {
        const axisData = opt.xAxis[0].data;
        const series = opt.series;
        
        let table = `
          <style>
            .data-view-table {
              width: 100%;
              font-size: 12px;
              border-collapse: collapse;
            }
            .data-view-table th, .data-view-table td {
              padding: 4px 8px;
              border: 1px solid #eee;
              text-align: center;
            }
            .data-view-table th {
              background: #fafafa;
            }
          </style>
          <table class="data-view-table">
            <tr><th>时间</th>
        `;

        series.forEach(item => table += `<th>${item.name}</th>`);
        table += '</tr>';

        for (let i = 0; i < axisData.length; i++) {
          table += `<tr><td>${axisData[i]}</td>`;
          series.forEach(item => {
            table += `<td>${item.data[i]?.toFixed(3) || '-'}</td>`;
          });
          table += '</tr>';
        }

        return table + '</table>';
      }
    }
  ,dataZoom :{},restore :{}, }},
  series: series,
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  series.value = prop.list.series;
  console.log("curList.series.value",  series.value)
  if(  series.value != null && series.value?.length > 0){
    legendList.value =  series.value?.map(item => item.name)
  } 
  time.value = prop.list.time;

  happenTime.value = prop.list.series.map(item => item.happenTime);
  if(prop.dataType == 1){
    dataTypeName = "最大电压";
  }else if(prop.dataType == 0){
    dataTypeName = "平均电压";
  }else if(prop.dataType == -1){
    dataTypeName = "最小电压";
  }
  
});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>