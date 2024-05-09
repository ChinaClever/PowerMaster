<template>
  <div>
    <el-card class="card" shadow="never">
      <template #header>
        <CardTitle title="机房" />
      </template>
      <div class="room">
        <el-card class="power" shadow="hover">
          <Echart :height="300" :options="echartsOption1" />
        </el-card>
        <el-card class="percent" shadow="hover">
          <div class="percentContainer">
            <div>实时负载百分比</div>
            <div class="square-continer">
              <div class="squareBox1"></div>
              <div class="squareBox2"></div>
              <div class="squareBox3"></div>
              <div class="squareBox4"></div>
              <div class="squareBox5"></div>
              <div class="pointer"></div>
            </div>
            <div class="leftPercent">0</div>
            <div class="topPercent">55%</div>
            <div class="rightPercent">100%</div>
          </div>
        </el-card>
      </div>
    </el-card>
    <el-card class="card" shadow="never">
      <template #header>
        <CardTitle title="MDC1" />
      </template>
      <div class="mdc">
        <el-card class="mdc-card" shadow="hover">
          <div class="percent">
            <div>
              <div class="title">SourceA</div>
              <div>输入电压：382.1V</div>
              <div>视在功率：200KW</div>
              <div>有功功率：201KVA</div>
              <div>电&emsp;&emsp;能：10.112kWh</div>
            </div>
            <div class="progressContainer">
              <div class="progress">
                <div class="left" :style="`flex: ${53}`">{{53}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${100 - 53}`">{{100 - 53}}%</div>
              </div>
            </div>
            <div>
              <div class="title">SourceB</div>
              <div>输入电压：378.8V</div>
              <div>视在功率：190KW</div>
              <div>有功功率：192KVA</div>
              <div>电&emsp;&emsp;能：8.996kWh</div>
            </div>
          </div>
        </el-card>
        <el-card shadow="hover">
          <Echart :height="280" :options="echartsOption2" />
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { isNumber } from '@/utils/is';
import { EChartsOption } from 'echarts'
const echartsOption1 = reactive<EChartsOption>({
  title: {
    text: '机房总功率'
  },
  tooltip: {
    trigger: 'none',
  },
  legend: {
    bottom: '0%' // 图例距离容器顶部的距离
  },
  xAxis: [{
    type: 'category',
    axisTick: {
      alignWithLabel: true
    },
    axisLine: {
      onZero: false,
    },
    data: ['10:00', '10:30', '11:00', '11:30', '12:00', '12:30']
  }],
  yAxis: {
    type: 'value',
    max: 600
  },
  series: [
    {
      name: '总功率',
      type: 'line',
      symbol: '',
      data: [
       350, 400, 410, 480, 500, 350
      ]
    }
  ]
}) as EChartsOption

const echartsOption2 = reactive<EChartsOption>({
  title: {
    text: '实时功率走势'
  },
  tooltip: {
    trigger: 'none',
  },
  legend: {
    top: '50%',
    right: '5%', // 图例距离容器顶部的距离
    orient: 'vertical' // 设置为水平布局
  },
  grid: {
    left: '4%',
    right: '15%'
  },
  xAxis: [{
    type: 'category',
    axisTick: {
      alignWithLabel: true
    },
    axisLine: {
      onZero: false,
    },
    data: ['10:00', '10:30', '11:00', '11:30', '12:00', '12:30']
  }],
  yAxis: {
    type: 'value',
    max: 500
  },
  series: [
    {
      name: 'SourceA',
      type: 'line',
      symbol: '',
      data: [
       450, 400, 410, 310, 500, 350
      ]
    },
    {
      name: 'SourceB',
      type: 'line',
      symbol: '',
      data: [
       250, 200, 200, 100, 310, 180
      ]
    }
  ],
  axisLabel: {
    formatter: function(value) {
      if (value === 500 || !isNumber(value)) {
        return value;
      } else {
        return '';
      }
    }
  }
}) as EChartsOption
</script>

<style lang="scss" scoped>
.card {
  margin-bottom: 16px;
}
.room {
  width: 100%;
  display: flex;
  .power{
    flex: 1;
    margin-right: 16px;
  }
  .percent{
    flex: 1;
  }
  .percentContainer {
    width: 100%;
    height: 100%;
    position: relative;
    padding: 0 5px;
    font-weight: bold;
    font-size: 18px;
    color: #464646;
    .leftPercent {
      position: absolute;
      top: 254px;
      left: calc(50% - 180px);
    }
    .topPercent {
      position: absolute;
      top: 26px;
      left: calc(50% - 13px);
    }
    .rightPercent {
      position: absolute;
      top: 254px;
      left: calc(50% + 155px);
    }
  }
  .square-continer {
    left: calc(50% - 200px);
    top: 50px;
    width: 400px;
    height: 200px;
    overflow: hidden;
    position: absolute;
    .squareBox1 {
      width: 400px;
      height: 200px;
      box-sizing: border-box;
      border-radius: 200px 200px 0px 0px;
      border: 50px solid rgb(39, 196, 0);
      border-bottom: none;
      transform-origin: 50% 100%;
      transform: rotate(-70deg);
      position: absolute;
      top: 0;
      left: 0;
      z-index: 9;
    }
    .squareBox2 {
      width: 400px;
      height: 200px;
      box-sizing: border-box;
      border-radius: 200px 200px 0px 0px;
      border: 51px solid #fff;
      border-bottom: none;
      transform-origin: 200px 199px;
      transform: rotate(-67deg);
      position: absolute;
      top: 0;
      left: 0;
      z-index: 8;
    }
    .squareBox3 {
      width: 400px;
      height: 200px;
      box-sizing: border-box;
      border-radius: 200px 200px 0px 0px;
      border: 50px solid rgb(250, 239, 93);
      border-bottom: none;
      transform-origin: 50% 100%;
      transform: rotate(-30deg);
      position: absolute;
      top: 0;
      left: 0;
      z-index: 7;
    }
    .squareBox4 {
      width: 400px;
      height: 200px;
      box-sizing: border-box;
      border-radius: 200px 200px 0px 0px;
      border: 51px solid #fff;
      border-bottom: none;
      transform-origin: 200px 199px;
      transform: rotate(-27deg);
      position: absolute;
      top: 0;
      left: 0;
      z-index: 6;
    }
    .squareBox5 {
      width: 400px;
      height: 200px;
      box-sizing: border-box;
      border-radius: 200px 200px 0px 0px;
      border: 50px solid rgb(255, 145, 2);
      border-bottom: none;
      transform-origin: 50% 100%;
      position: absolute;
      top: 0;
      left: 0;
      z-index: 5;
    }
    .pointer {
      position: absolute;
      bottom: 0;
      left: 70px;
      z-index: 10;
      width: 130px;
      height: 10px;
      background-color: rgb(250, 239, 93);
      transform-origin: 100% 50%;
      transform: rotate(90deg);
    }
  }
}
.mdc {
  .mdc-card {
    margin-bottom: 16px;
  }
  .percent {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
    line-height: 1.7;
    .title {
      font-weight: bold;
    }
  }
}
.progressContainer {
  display: flex;
  justify-content: center;
  align-items: center;
  .progress {
    width: 400px;
    display: flex;
    align-items: center;
    font-size: 14px;
    color: #eee;
    box-sizing: border-box;
    position: relative;
    display: flex;
    justify-content: center;
    .line {
      width: 3px;
      height: 36px;
      background-color: #000;
    }
    .left {
      text-align: center;
      box-sizing: border-box;
      background-color: #3b8bf5;
      // border-right: 1px solid #000;
    }
    .right {
      text-align: center;
      background-color:  #f86f13;
    }
  }
}
:deep(.el-card__body) {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}
</style>