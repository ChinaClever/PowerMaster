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
    type: [Number, String],
    default: 60
  },
  width: {
    type: [Number, String],
    default: 60
  },
    dataType:{
      type : Number,
      default : 1
  }
})

const series = ref([]);
const time = ref([]);
const legendList = ref([]);
const count = ref([]);

// 检查 prop.list 是否为空
if (Object.keys(prop.list).length === 0) {
  console.warn('prop.list is empty');
} else {
  const keys = Object.keys(prop.list);
    let length = keys.length;
  if(keys.length > 3){
  length = 3
  }


  // 初始化 count 数组
  for (let i = 0; i < length; i++) {
    count.value.push([]);
    if (Array.isArray(prop.list[keys[i]])) {
      count.value[i].push(...prop.list[keys[i]].map(item => item.eq_value));
    }
  }

  // 构建 series 和 legendList
  for (let i = 0; i < length; i++) {
    series.value.push({
      name: `输出位${i + 1}`,
      data: count.value[i],
      type: 'line',
      symbol: 'circle',
      symbolSize: 4
    });
    legendList.value.push(`输出位${i + 1}`);
  }

  // 初始化时间轴数据
  if (prop.list.time) {
    time.value = prop.list.time;
  }
}

const echartsOption = computed(() => ({
  legend: { data: legendList.value },
  xAxis: {
    type: 'category',
    data: time.value
  },
  tooltip: { trigger: 'axis',
    formatter: function(params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(3) + 'kWh' ;
        result += '<br>';
      }
      return result;
    } 
  },
  yAxis: {
    type: 'value'
  },
  series: series.value
}));

onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>