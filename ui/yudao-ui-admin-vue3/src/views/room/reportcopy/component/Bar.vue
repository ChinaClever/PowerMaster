<!-- <template>
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
  }
})

const series = ref()
const time = ref()
const legendList = ref()

// 设置饼图的选项
const echartsOption = ref({
  dataZoom:[{ type:"inside"}],  
  tooltip: { trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + "消耗电能" + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(1) + ' kWh' ;
        result += '<br>';
      }
      return result;
    } 
  },
  xAxis: {type: 'category', boundaryGap: false, data : time},
  yAxis: { type: 'value' , name : "kWh"},
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
  // color: ['#C8603A'],
  
  series: series,
  
})

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  series.value = prop.list.series;
  series.value[0].itemStyle = {color: '#C8603A'}
  if(  series.value != null && series.value?.length > 0){
    legendList.value =  series.value?.map(item => item.name)
  }
  time.value = prop.list.time;
});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

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
  }
})

const series = ref()
const time = ref()
const legendList = ref()

// 设置饼图的选项
const echartsOption = ref({
  dataZoom:[{ type:"inside"}],
  tooltip: { trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + "消耗电能" + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(1) + ' kWh' ;
        result += '<br>';
      }
      return result;
    } 
  },
  grid:{left:'4%',right:'3%'},
  xAxis: {type: 'category', boundaryGap: true, data : time},
  yAxis: { type: 'value' , name : "kWh"},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: series,
})

watchEffect(() => {
  series.value = prop.list.series;
  if (series.value != null && series.value?.length > 0) {
    legendList.value = series.value?.map(item => item.name);
    // series.value.forEach(item => {
    //   item.label = {
    //     show: true,
    //     position: 'top',
    //     formatter: (params) => `${params.value.toFixed(1)} `, // 保留1位小数
    //     color: '#333',
    //     fontSize: 12,
    //   };
    // });
  }
  time.value = prop.list.time;
  console.log('Bartime.value',time.value)
});
onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>