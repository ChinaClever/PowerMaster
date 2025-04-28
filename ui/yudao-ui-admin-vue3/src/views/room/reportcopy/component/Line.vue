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
  color:['#E5B849','#C8603A','#AD3762'],
  legend: { data: legendList,
    type: 'scroll', // 设置为 'single' 或 'multiple'
    orient: 'horizontal', // 设置为 'horizontal' 或 'vertical'
    width:1000
  },
  tooltip: { trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName + ' &nbsp&nbsp&nbsp' ;
        if (params[i].seriesName.includes("视在功率")) {
          result +='发生时间:'+params[0].name +' &nbsp&nbsp&nbsp'+params[i].value.toFixed(3) +  ' kVA'; 
        } else if (params[i].seriesName.includes("有功功率")) {
          result +='发生时间:'+params[0].name +' &nbsp&nbsp&nbsp'+ params[i].value.toFixed(3) + ' kW';
        } else if (params[i].seriesName.includes("无功功率")) {
          result += '发生时间:'+params[0].name +' &nbsp&nbsp&nbsp'+params[i].value.toFixed(3) + ' kVar';
        }
        result += '<br>';
      }
      return result;
    } 
  },
  xAxis: {type: 'category', boundaryGap: false, data : time},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},
  // dataView:{}
    // dataView: {
  //     optionToContent: function(opt) {
  //       // opt 是整个 echarts 的 option 对象
  //       const axisData = opt.xAxis[0].data; // 获取x轴数据
  //       const series = opt.series; // 获取所有系列数据
        
  //       // 构建HTML表格
  //       let table = '<table style="width:100%;text-align:center"><tbody><tr><th>时间</th>';
        
  //       // 添加表头
  //       series.forEach(item => {
  //         table += `<th>${item.name}</th>`;
  //       });
  //       table += '</tr>';
        
  //       // 添加数据行
  //       for (let i = 0; i < axisData.length; i++) {
  //         table += `<tr><td>${axisData[i]}</td>`;
  //         series.forEach(item => {
  //           const value = item.data[i] !== undefined ? item.data[i].toFixed(3) : '-';
  //           table += `<td>${value}</td>`;
  //         });
  //         table += '</tr>';
  //       }
        
  //       table += '</tbody></table>';
  //       return table;
  //     }
  //   },
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
  if(series.value != null && series.value?.length > 0){
    legendList.value =  series.value?.map(item => item.name)
  }

  time.value = prop.list.time;

});



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>