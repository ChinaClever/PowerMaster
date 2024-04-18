<template>
  <div class="descriptionContainer">
    <el-card class="card" shadow="never">
      <template #header>
        <CardTitle title="A路" />
      </template>
      <el-descriptions class="ABRord" direction="vertical" :column="2" >
        <el-descriptions-item label="输入电压">381.1V</el-descriptions-item>
        <el-descriptions-item label="A视在功率">200KW</el-descriptions-item>
        <el-descriptions-item label="A有功功率">201KVA</el-descriptions-item>
        <el-descriptions-item label="A电能">10.112kWh</el-descriptions-item>
        <el-descriptions-item label="A平衡度">20%</el-descriptions-item>
        <el-descriptions-item label="A负载百分比">65.5%</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-card class="card" shadow="never">
      <template #header>
        <CardTitle title="总参数" />
      </template>
      <el-descriptions class="ABRord" direction="vertical" :column="2" >
        <el-descriptions-item label="输入电压">378.8V</el-descriptions-item>
        <el-descriptions-item label="视在功率">190KW</el-descriptions-item>
        <el-descriptions-item label="有功功率">192KVA</el-descriptions-item>
        <el-descriptions-item label="电能">8.996kWh</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-card class="card" shadow="never">
      <template #header>
        <CardTitle title="B路" />
      </template>
      <el-descriptions class="ABRord" direction="vertical" :column="2" >
        <el-descriptions-item label="输入电压">381.1V</el-descriptions-item>
        <el-descriptions-item label="B视在功率">200KW</el-descriptions-item>
        <el-descriptions-item label="B有功功率">201KVA</el-descriptions-item>
        <el-descriptions-item label="B电能">10.112kWh</el-descriptions-item>
        <el-descriptions-item label="B平衡度">80%</el-descriptions-item>
        <el-descriptions-item label="B负载百分比">34.5%</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
  <el-card class="card" shadow="never">
    <template #header>
      <CardTitle title="拓扑展示" />
    </template>
    <TopologyShow />
  </el-card>
  <el-card class="card" shadow="never">
    <template #header>
      <CardTitle title="实时曲线" />
    </template>
    <Echart :height="500" :options="echartsOption" />
  </el-card>
  <el-card class="card" shadow="never">
    <template #header>
      <CardTitle title="智能分析" />
    </template>
  </el-card>
</template>

<script lang="ts" setup>
import TopologyShow from "./TopologyShow.vue"
import { EChartsOption } from 'echarts'

const xData = []

const echartsOption = reactive<EChartsOption>({
  title: {
    text: '机柜列实时功率走势'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {},
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00']
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value} W'
    }
  },
  series: [
    {
      // name: 'sourceA',
      type: 'line',
      data: [200, 110, 330, 410, 420, 320, 390],
      markPoint: {
        data: [
          { type: 'max', name: 'Max' },
          { type: 'min', name: 'Min' }
        ]
      },
      markLine: {
        data: [{ type: 'average', name: 'Avg' }]
      }
    },
    // {
    //   name: 'sourceB',
    //   type: 'line',
    //   data: [300, 360, 230, 300, 320, 280, 290],
    //   markPoint: {
    //     data: [{ name: '周最低', value: -2, xAxis: 1, yAxis: -1.5 }]
    //   },
    //   markLine: {
    //     data: [
    //       { type: 'average', name: 'Avg' },
    //       [
    //         {
    //           symbol: 'none',
    //           x: '90%',
    //           yAxis: 'max'
    //         },
    //         {
    //           symbol: 'circle',
    //           label: {
    //             position: 'start',
    //             formatter: 'Max'
    //           },
    //           type: 'max',
    //           name: '最高点'
    //         }
    //       ]
    //     ]
    //   }
    // }
  ]
}) as EChartsOption
</script>

<style lang="scss" scoped>

.descriptionContainer {
  display: flex;
  justify-content: space-between;
  .card {
    flex: 1;
    // margin-right: 15px;
    .ABRord {
      padding-left: 50px;
    }
  }
  .card:nth-child(2) {
    margin: 0 16px 16px;
  }
}
.card {
  margin-bottom: 16px
}
</style>