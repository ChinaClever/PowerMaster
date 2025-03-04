<template>
  <div ref="chartDom" style="width: 1290px; height: 500px;"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';

// 写死的数据
const _rawData = [
  { Year: 1930, Country: 'Germany', Income: 80 },
  { Year: 1935, Country: 'Germany', Income: 90 },
  { Year: 1940, Country: 'Germany', Income: 100 },
  { Year: 1945, Country: 'Germany', Income: 70 },
  { Year: 1950, Country: 'Germany', Income: 150 },
  { Year: 1955, Country: 'Germany', Income: 180 },
  { Year: 1960, Country: 'Germany', Income: 200 },
  { Year: 1930, Country: 'France', Income: 75 },
  { Year: 1935, Country: 'France', Income: 85 },
  { Year: 1940, Country: 'France', Income: 90 },
  { Year: 1945, Country: 'France', Income: 65 },
  { Year: 1950, Country: 'France', Income: 130 },
  { Year: 1955, Country: 'France', Income: 150 },
  { Year: 1960, Country: 'France', Income: 170 }
];

// 使用 ref 来获取 DOM 元素
const chartDom = ref<HTMLDivElement | null>(null);
let myChart: echarts.ECharts | null = null;

// 图表配置
const option = {
  dataset: [
    {
      id: 'dataset_raw',
      source: _rawData
    },
    {
      id: 'dataset_of_germany',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
            { dimension: 'Country', '=': 'Germany' }
          ]
        }
      }
    },
    {
      id: 'dataset_of_france',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
            { dimension: 'Country', '=': 'France' }
          ]
        }
      }
    }
  ],
  title: {
    text: 'Income of Germany and France'
  },
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    nameLocation: 'middle'
  },
  yAxis: {
    name: 'Income'
  },
  legend: {
    data: ['Germany', 'France']
  },
  series: [
    {
      type: 'line',
      datasetId: 'dataset_of_germany',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'Germany'
    },
    {
      type: 'line',
      datasetId: 'dataset_of_france',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'France'
    }
  ]
};

// 组件挂载时初始化图表
onMounted(() => {
  if (chartDom.value) {
    myChart = echarts.init(chartDom.value);
    myChart.setOption(option);
  }
});

// 组件卸载时销毁图表
onUnmounted(() => {
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
});
</script>