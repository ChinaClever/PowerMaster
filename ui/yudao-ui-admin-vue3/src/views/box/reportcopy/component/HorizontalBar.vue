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

const series = ref(Object.keys(prop.list).map(key => ({
    name: '', // 可以根据需要使用 key 或其他默认值
    data: [],
})));

const time = ref([]);
const legendList = ref([]);

const count = ref({});
const length = ref();

const echartsOption = ref({
  dataZoom:[{ type:"inside"}],
  tooltip: { trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker  + "消耗电能" + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(1) + ' kWh' ;
        result += '<br>';
      }
      return result;
    } 
  },
  grid:{left:'7%',right:'7%'},
  xAxis: { type: 'value' },
  yAxis: {type: 'category' , data : legendList.value},
  toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  series: series.value
})

watchEffect(() => {
  // 初始化 count 的每个属性为数组
  for (let i = 1; i < Object.keys(prop.list).length; i++) {
    count.value[i] = ref([]); // 或者直接使用 []，如果不需要深度响应式
  }

  for (let i = 1; i < Object.keys(prop.list).length; i++) {
    count.value[i].push(...prop.list[i].map(item => item.pow_active_avg_value));
  }

  time.value = prop.list.time;

  for (let i = 0; i < series.value.length - 1; i++) {
    series.value[i].name = `输出位${i + 1}`; // 使用 i + 1 来从 1 开始编号
    series.value[i].data = count.value[i+1];
    legendList.value.push(series.value[i].name)
    //series.value[i].type = 'line';
  }

  //series.value.pop();
  //legendList.value = series.value.map(item => item.name);
  time.value = legendList.value
  //legendList.value.pop();

  console.log('prop.list.length',Object.keys(prop.list).length);
  console.log('prop.list',prop.list);
  console.log('countcountvcountc',count.value[2]);
  console.log('series.value',series.value);
  console.log('legendList.value',legendList.value);
  console.log('time.value',time.value);
});

onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>