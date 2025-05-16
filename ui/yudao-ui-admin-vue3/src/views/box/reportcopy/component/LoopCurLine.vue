<!-- <template>
  <Echart :options="echartsOptions" />
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, defineProps } from 'vue';
import 'echarts';

const prop = defineProps({
  list: {
    type: Object as () => { loop: { lineId: number; cur: number[]; dateTimes: string[] }[]; dateTimes: string[]; },
    required: true
  }
});

const lineidDateTimes = ref([] as string[]);
lineidDateTimes.value = prop.list.dateTimes;

const newArray = prop.list.loop.map((item, index) => ({
  name: String(index + 1),
  type: 'line',
  data: item.cur,
  symbol: 'circle',
  symbolSize: 4
}));

const legendData = Array.from({ length: prop.list.loop.length }, (_, i) => (i + 1).toString());

const echartsOptions = computed(() => ({
  title: { text: '' },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let result = params[0].name + '<br>';
      params.forEach(param => {
        result += `${param.marker}${param.seriesName}: &nbsp;&nbsp;&nbsp;&nbsp${param.value.toFixed(2)}A<br>`;
      });
      return result.trimEnd();
    }
  },
  legend: {
    data: legendData,
    selectedMode: 'multiple'
  },
  grid: { left: '3%', right: '4%', bottom: '5%', containLabel: true },
  toolbox: { feature: { saveAsImage: {}, dataView: {}, dataZoom: {}, restore: {} } },
  xAxis: {
    type: 'category', nameLocation: 'end', boundaryGap: false,
    data: lineidDateTimes.value
  },
  yAxis: { type: 'value' },
  series: newArray
}));

// 注意：这里的 console.log 仅在调试时使用，生产环境中应移除
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
  height: {
    type: [Number,String],
    default: 60
  },
  width: {
    type: [Number,String],
    default: 60
  },
  dataType:{
      type : Number,
      default : 1
  }
})

const series = ref()
const time = ref()
const legendList = ref()
const happenTime = ref()

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
        if(prop.dataType == 1){
        result +=  params[i].marker + params[i].seriesName + '最大电流：' ;
        }
             if(prop.dataType == 0){
        result +=  params[i].marker + params[i].seriesName + '平均电流：' ;
        }
             if(prop.dataType == -1){
        result +=  params[i].marker + params[i].seriesName + '最小电流：' ;
        }
        
        if(prop.dataType !=0){
         
          result += params[i].value.toFixed(2) +  ' A'+' &nbsp&nbsp&nbsp发生时间：'+happenTime.value[i][params[i].dataIndex]+' &nbsp&nbsp&nbsp;'; 
      
          // result +=' &nbsp&nbsp&nbsp发生时间:'+happenTime.value[i][params[i].dataIndex]+' &nbsp&nbsp&nbsp;'+ params[i].value.toFixed(3) + ' kW';
    
          // result +=' &nbsp&nbsp&nbsp发生时间:'+happenTime.value[i][params[i].dataIndex]+' &nbsp&nbsp&nbsp;'+ params[i].value.toFixed(3) + ' kVar';
        
        }else{
        
       
          result += params[i].value.toFixed(3) +  ' A&nbsp&nbsp&nbsp;发生时间：'+params[0].name; 
     
  
        
        }


        result += '<br>';
      }
      return result;
    } 
  },
  xAxis: {type: 'category', boundaryGap: false, data : time},
  grid:{left:'3%',right:'3%'},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {}, dataView: {
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
  console.log("loopline.value",  prop.list)
  series.value = prop.list?.series;
  console.log("series.value",  series.value)
  if(  series.value != null && series.value?.length > 0){
    legendList.value =  series.value?.map(item => item.name)
  }

  time.value = prop.list?.time;
  happenTime.value = prop.list?.series.map(item => item.happenTime);
});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>