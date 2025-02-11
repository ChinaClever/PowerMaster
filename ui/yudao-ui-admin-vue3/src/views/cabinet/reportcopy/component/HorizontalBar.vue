<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'

const height = 400 // 自行调整合适高度
const width = 800  // 自行调整合适宽度

// 写死的数据
const time = ['机架1', '机架2', '机架3', '机架4', '机架5', '机架6', '机架7', '机架8', '机架9', '机架10', '机架11', '机架12']
const series = [
  {
    name: '消耗电能',
    type: 'bar',
    data: [38.8, 38.8, 38.8, 38.8, 38.8, 38.8, 38.8, 38.8, 38.8, 38.8, 38.8, 38.8],
    itemStyle: {
      color: '#007aff'
    }
  }
]

// 设置饼图的选项
const echartsOption = {
  dataZoom: [{ type: "inside" }],
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      var result = params[0].name + '<br>';
      for (var i = 0; i < params.length; i++) {
        result += params[i].marker + "消耗电能" + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value.toFixed(1) + 'kWh';
        result += '<br>';
      }
      return result;
    }
  },
  grid: { left: '7%', right: '7%' },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: time },
  toolbox: { feature: { saveAsImage: {}, dataView: {}, dataZoom: {}, restore: {} } },
  series: series
};
</script>

<style lang="scss" scoped>

</style>