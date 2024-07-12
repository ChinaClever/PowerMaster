<template>
  <div class="homeContainer">
    <div class="left">
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>用能情况</div>
        </template>
        <div>今日：10kW·h</div>
        <div>本周：70kW·h</div>
        <div>本月：220kW·h</div>
      </el-card>
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>A路</div>
        </template>
        <div>电流：Ia: 0.20、Ib: 0.31、Ic: 0.33</div>
        <div>电压：Ua: 9.20、Ub: 8.31、Uc: 1.33</div>
      </el-card>
      <ContentWrap>
        <div class="progress">
          <el-progress type="dashboard" :percentage="50">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}%</span>
              <span class="percentage-label">总视在功率</span>
            </template>
          </el-progress>
        </div>
      </ContentWrap>
      <ContentWrap>
        <Echart :options="echartsOptionA" :height="300" />
      </ContentWrap>
    </div>
    <div class="center">
      <Topology />
    </div>
    <div class="right">
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>用能情况</div>
        </template>
        <div>昨日：12kW·h</div>
        <div>上周：80kW·h</div>
        <div>上月：230kW·h</div>
      </el-card>
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>B路</div>
        </template>
        <div>电流：Ia: 0.20、Ib: 0.31、Ic: 0.33</div>
        <div>电压：Ua: 9.20、Ub: 8.31、Uc: 1.33</div>
      </el-card>
      <ContentWrap>
        <div class="progress">
          <el-progress type="dashboard" :percentage="50">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}%</span>
              <span class="percentage-label">总有功功率</span>
            </template>
          </el-progress>
        </div>
      </ContentWrap>
      <ContentWrap>
        <Echart :options="echartsOptionB" :height="300" />
      </ContentWrap>
    </div>
  </div>
</template>

<script lang="ts" setup>
import Topology from './component/Topology.vue'
import { EChartsOption } from 'echarts'
import { Right } from '@element-plus/icons-vue/dist/types'

const echartsOptionA = ref<EChartsOption>({
  title: {
    text: 'A路功率'
  },
  legend: {
    data: ['视在功率', '有功功率'],
    top: 0,
    right: 0,
  },
  grid: {
    bottom: 0,
    top: 0,
  },
  radar: {
    indicator: [
      { name: 'Sales', max: 6500 },
      { name: 'Administration', max: 16000 },
      { name: 'Information Technology', max: 30000 },
      { name: 'Customer Support', max: 38000 },
      { name: 'Development', max: 52000 },
      { name: 'Marketing', max: 25000 }
    ]
  },
  series: [
    {
      name: 'Budget vs spending',
      type: 'radar',
      data: [
        {
          value: [4200, 3000, 20000, 35000, 50000, 18000],
          name: '视在功率'
        },
        {
          value: [5000, 14000, 28000, 26000, 42000, 21000],
          name: '有功功率',
          areaStyle: {
            color: 'rgba(206, 255, 171, 0.6)'
          }
        }
      ]
    }
  ]
})
const echartsOptionB = ref<EChartsOption>({
  title: {
    text: 'B路功率'
  },
  legend: {
    data: ['视在功率', '有功功率'],
    top: 0,
    right: 0,
  },
  grid: {
    bottom: 0,
    top: 0,
  },
  radar: {
    indicator: [
      { name: 'Sales', max: 6500 },
      { name: 'Administration', max: 16000 },
      { name: 'Information Technology', max: 30000 },
      { name: 'Customer Support', max: 38000 },
      { name: 'Development', max: 52000 },
      { name: 'Marketing', max: 25000 }
    ]
  },
  series: [
    {
      name: 'Budget vs spending',
      type: 'radar',
      data: [
        {
          value: [4200, 3000, 20000, 35000, 50000, 18000],
          name: '视在功率'
        },
        {
          value: [5000, 14000, 28000, 26000, 42000, 21000],
          name: '有功功率',
          areaStyle: {
            color: 'rgba(206, 255, 171, 0.6)'
          }
        }
      ]
    }
  ]
})
</script>

<style lang="scss" scoped>
.homeContainer {
  box-sizing: border-box;
  width: 100%;
  height: calc(100vh - 120px);
  min-height: 550px;
  box-sizing: border-box;
  // background-color: #999;
  display: flex;
  .center {
    flex: 1;
    height: 100%;
    box-sizing: border-box;
    overflow: hidden;
    background-color: bisque;
    margin: 0 15px;
  }
  .left {
    width: 300px;
    height: 100%;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
  }
  .right {
    width: 300px;
    height: 100%;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
  }
}
.progress {
  display: flex;
  align-items: center;
  justify-content: center;
  .percentage-value {
    display: block;
    margin-top: 10px;
    font-size: 28px;
  }
  .percentage-label {
    display: block;
    margin-top: 10px;
    font-size: 12px;
  }
}
:deep(.el-card__header) {
  padding: 15px;
}
:deep(.el-card__body) {
  padding: 15px;
}
</style>