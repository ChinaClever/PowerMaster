<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts';
const prop = defineProps({
  list: {
    type: Object,
    required: true,
  },
  height: {
    type: [Number, String],
    default: 60,
  },
  width: {
    type: [Number, String],
    default: 60,
  },
});

const series = ref();
const time = ref();
const legendList = ref();

const echartsOption = ref({
  dataZoom: [{ type: "inside" }],
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result += params[i].marker + "消耗电能" + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(1) + ' kWh';
        result += '<br>';
      }
      return result;
    },
  },
  grid: { left: '5%', right: '5%', top: '5%', bottom: '5%' },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: time },
  toolbox: { feature: { saveAsImage: {}, dataView: {}, dataZoom: {}, restore: {} } },
  series: series,
});

watchEffect(() => {
  series.value = prop.list?.series;
  if (series.value != null && series.value?.length > 0) {
    legendList.value = series.value?.map(item => item.name);
    series.value.forEach(item => {
      item.label = {
        show: true,
        position: 'right',  // 关键修改：标签显示在右侧
        // formatter: '{c} kWh',
        formatter: (params) => `${params.value.toFixed(1)} kWh`, // 保留1位小数
        color: '#333',
        fontSize: 12,
      };
    });
  }
  time.value = prop.list?.time;
});

onUnmounted(() => {
  console.log('onUnmounted******');
});
</script>

<style lang="scss" scoped></style>