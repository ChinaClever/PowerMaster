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
  dataZoom:[{ type:"inside"}],
  legend: { data: legendList,
    type: 'scroll', // 设置为 'single' 或 'multiple'
    orient: 'horizontal', // 设置为 'horizontal' 或 'vertical'
    width:1000
  },
  tooltip: {
    trigger: 'axis',
    tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      var result = '记录时间：' + params[0].name + '<br/>';
        params.forEach(function (item) {
            // 根据系列名称设置颜色
            var color;
            switch (item.seriesName) {
                case '系列1':
                    color = 'blue';
                    break;
                case '系列2':
                    color = 'green';
                    break;
                case '系列3':
                    color = 'yellow';
                    break;
                // 可以添加更多系列名称和颜色的映射
                default:
                    color = 'black'; // 默认颜色，或者根据需求设置
            }
  
            // 创建一个颜色点和间隔
            result += '<span style="display: inline-block; width: 10px; height: 10px; background-color: ' + color + '; border-radius: 50%; margin-right: 8px;"></span>';
  
            // 添加系列名称和对应的值
            result += item.seriesName + '：' + item.value + '<br/>';
        });
      return result;
      }
    }
  },
  xAxis: {type: 'category', boundaryGap: false, data : time.value},
  yAxis: { type: 'value'},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: series.value,
})

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
      { name: 'A相电流谐波', type: 'line', data: newVal.lineOne },
      { name: 'B相电流谐波', type: 'line', data: newVal.linetwe },
      { name: 'C相电流谐波', type: 'line', data: newVal.linethree }
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