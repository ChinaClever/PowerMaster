<template>
  <Echart :height="height" :width="width" :options="echartsOptions" />
</template>

<script lang="ts" setup>
import { defineProps, ref, computed, onUnmounted } from 'vue';
import 'echarts';

const prop = defineProps({
  list: {
    type: Object as () => Record<string, any>,
    required: true
  },
  height: {
    type: [Number, String],
    default: 400
  },
  width: {
    type: [Number, String],
    default: '100%'
  }
});

const pow_factor1 = ref()
const pow_factor2 = ref()
const pow_factor3 = ref()
const time = ref()
const legendList = ref()
const seriesList = ref()
// computed(() => {
//   const items = [];
//   if (prop.list['1'] !== undefined) {
//     items.push('输出位1功率因数');
//   }
//   if (prop.list['2'] !== undefined) {
//     items.push('输出位2功率因数');
//   }
//   if (prop.list['3'] !== undefined) {
//     items.push('输出位3功率因数');
//   }
//   return items; // 根据prop.list中的非null键返回相应的图例数组
// });

const echartsOptions = ref({
    legend: { data: legendList },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        let result = params[0].name + '<br>';
        for (let i = 0; i < params.length; i++) {
          result +=  params[i].marker + params[i].seriesName;
          if(prop.list[params[i].dataIndex]?.powerFactorTime?.[i]) {
            result += '&nbsp发生时间:' + prop.list[params[i].dataIndex].powerFactorTime[i]
          }
          result += '&nbsp&nbsp' + params[i].value
          result += '<br>';
        }
        return result;
      }
    },
    xAxis: { type: 'category', boundaryGap: false, data: time },
    yAxis: { type: 'value' },
    toolbox: { feature: { saveAsImage: {}, dataView: {}, dataZoom: {}, restore: {} } },
    series: seriesList
});

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化

  pow_factor1.value = prop.list.map(obj => obj.pow_factor_value1);
  pow_factor2.value = prop.list.map(obj => obj.pow_factor_value2);
  pow_factor3.value = prop.list.map(obj => obj.pow_factor_value3);
  time.value = prop.list.map(obj => obj.time);
  if(prop.list[0].outletNum == 3){
    legendList.value =  ["输出位1功率因数","输出位2功率因数","输出位3功率因数"]
    seriesList.value = [
      { name: '输出位1功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: pow_factor1 },
      { name: '输出位2功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: pow_factor2 },
      { name: '输出位3功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: pow_factor3 }
    ]
  } else if(prop.list[0].outletNum == 2) {
    legendList.value =  ["输出位1功率因数","输出位2功率因数"]
    seriesList.value = [
      { name: '输出位1功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: pow_factor1 },
      { name: '输出位2功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: pow_factor2 }
    ]
  } else if(prop.list[0].outletNum == 1) {
    legendList.value =  ["输出位1功率因数"]
    seriesList.value = [
      { name: '输出位1功率因数', type: 'line', symbol: 'circle', symbolSize: 4, data: pow_factor1 }
    ]
  } else {
    legendList.value = []
    seriesList.value = []
  }
});


onUnmounted(() => {
  console.log('onUnmounted******');
});
</script>

<style lang="scss" scoped>
</style>