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

const series = ref({});

const time = ref([]);
const legendList = ref([]);

const count = ref({});
const length = ref();

// 初始化 count 的每个属性为数组
for (let i = 1; i < Object.keys(prop.list).length; i++) {
  count.value[i] = ref([]); // 或者直接使用 []，如果不需要深度响应式
}
for (let i = 1; i < Object.keys(prop.list).length; i++) {
  count.value[i].push(...prop.list[i].map(item => item.pow_active_avg_value));
}

for (let i = 1; i < (Object.keys(prop.list).length); i++) {
  series.value[i - 1] = {
    name: `输出位${i}`,
    data: count.value[i],
    type: 'line',
  }
}

for (let i = 1; i < Object.keys(prop.list).length; i++) {
  legendList.value.push(series.value[i - 1].name);
}

console.log('prop.list.length',Object.keys(prop.list).length);
console.log('prop.list',prop.list);
console.log('countcountvcountc',count.value[2]);
console.log('series.value',series.value);
console.log('legendList.value',legendList.value);
console.log('time.value',time.value);

const echartsOption = ref({
  legend: { data:legendList.value },
  xAxis: {
    type: 'category',
    data: prop.list.time
  },
  yAxis: {
    type: 'value'
  },
  series: series.value
})

onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>