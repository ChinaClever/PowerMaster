<template>
  <el-card class="card" shadow="never">
    <template #header>
      <CardTitle title="机柜列连接" />
    </template>
    <ConnetShow />
  </el-card>
  <el-card class="card" shadow="never">
    <template #header>
      <CardTitle title="AB路信息" />
    </template>
    <div class="descriptionContainer">
        <el-descriptions class="ABRord" title="A路 " :column="1" size="medium" >
          <el-descriptions-item label="输入电压">381.1V</el-descriptions-item>
          <el-descriptions-item label="视在功率">200KW</el-descriptions-item>
          <el-descriptions-item label="有功功率">201KVA</el-descriptions-item>
          <el-descriptions-item label="有功电能">10.112kWh</el-descriptions-item>
        </el-descriptions>
        <el-descriptions class="ABRord" title="B路 " :column="1" >
          <el-descriptions-item label="输入电压">378.8V</el-descriptions-item>
          <el-descriptions-item label="视在功率">190KW</el-descriptions-item>
          <el-descriptions-item label="有功功率">192KVA</el-descriptions-item>
          <el-descriptions-item label="有功电能">8.996kWh</el-descriptions-item>
        </el-descriptions>
      </div>
  </el-card>
  <el-card class="card" shadow="never">
    <template #header>
      <CardTitle title="机柜列实时功率走势" />
    </template>
    <Echart :height="500" :options="echartsOption" />
  </el-card>
</template>

<script lang="ts" setup>
import ConnetShow from "./ConnectShow.vue"
import { EChartsOption } from 'echarts'

const echartsOption = reactive<EChartsOption>({
  title: {
    text: '机柜列实时功率走势'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {},
  toolbox: {
    show: true,
    feature: {  
      dataZoom: {
        yAxisIndex: 'none'
      },
      dataView: { readOnly: false },
      magicType: { type: ['line', 'bar'] },
      restore: {},
      saveAsImage: {}
    }
  },
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
      name: 'sourceA',
      type: 'line',
      data: [400, 410, 330, 410, 420, 320, 390],
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
    {
      name: 'sourceB',
      type: 'line',
      data: [300, 360, 230, 300, 320, 280, 290],
      markPoint: {
        data: [{ name: '周最低', value: -2, xAxis: 1, yAxis: -1.5 }]
      },
      markLine: {
        data: [
          { type: 'average', name: 'Avg' },
          [
            {
              symbol: 'none',
              x: '90%',
              yAxis: 'max'
            },
            {
              symbol: 'circle',
              label: {
                position: 'start',
                formatter: 'Max'
              },
              type: 'max',
              name: '最高点'
            }
          ]
        ]
      }
    }
  ]
}) as EChartsOption
</script>

<style lang="scss" scoped>
.card {
  margin-bottom: 15px;
}

.descriptionContainer {
  display: flex;
  justify-content: space-around;
  .ABRord {
    flex: 1;
    padding-left: 50px;
  }
}
</style>