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
console.log('list2222221111111111111',prop.list);

time.value = prop.list.time;
series.value = [
  {name:'A相电流谐波',type: 'line',data:prop.list.lineOne},
  {name:'B相电流谐波',type: 'line',data:prop.list.linetwe},
  {name:'C相电流谐波',type: 'line',data:prop.list.linethree}
];

//设置饼图的选项
const echartsOption = ref({
  dataZoom: [{ type: "inside" }],
  legend: {
    data: legendList,
    type: 'scroll',
    orient: 'horizontal',
    width: 1000
  },
  tooltip: { 
    trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
        result += '<br>';
      }
      return result;
    } 
   },
  xAxis: { type: 'category', boundaryGap: false, data: time.value },
  yAxis: { type: 'value' },
  toolbox: {
    feature: {
      saveAsImage: {},
      dataView: {},
      dataZoom: {},
      restore: {}
    }
  },
  series: series.value,
});
//watchEffect(() => {
//  // 直接访问即可，watchEffect会自动跟踪变化
//
//  series.value = prop.list.series;
//  if(  series.value != null && series.value?.length > 0){
//    legendList.value =  series.value?.map(item => item.name)
//  }
//
//  time.value = prop.list.time;
//
//});
watch(() => prop.list, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    time.value = newVal.time;
    series.value = [
      { name: 'A相电流谐波', type: 'line', symbol: 'circle', symbolSize: 4,data: newVal.lineOne },
      { name: 'B相电流谐波', type: 'line', symbol: 'circle', symbolSize: 4,data: newVal.linetwe },
      { name: 'C相电流谐波', type: 'line', symbol: 'circle', symbolSize: 4,data: newVal.linethree }
    ];
    legendList.value = series.value.map(item => item.name);
  }
}, { deep: true }); // 使用 deep: true 来深度监听对象的变化


onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>