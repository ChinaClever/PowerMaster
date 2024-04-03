<template>
<ContentWrap>
  <div ref="wrapper" class="wrapper">
    <div class="room_girder">
      <div class="line">
        <div class="box"></div>
        <div class="box"></div>
        <div class="box"></div>
      </div>
    </div>
    <div id="roader" class="room_roader">
      <div class="roader"></div>
      <div class="roader"></div>
    </div>
    <div id="chart" class="room_chart">
      <div class="chart">
        <Echart :height="250" :options="echartsOption" />
      </div>
      <div class="chart"></div>
      <div class="chart"></div>
      <div class="chart"></div>
      <div class="chart"></div>
      <div class="chart"></div>
    </div>
    <!-- <div class="box1" ref="sourceElement">Source Element</div>
    <div class="boxList">
      <div class="box2" ref="targetElement1">Target Element</div>
      <div class="box2" ref="targetElement2">Target Element</div>
      <div class="box2" ref="targetElement3">Target Element</div>
    </div> -->
  </div>
</ContentWrap>
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector,ready, DotEndpoint } from '@jsplumb/browser-ui';
import {onMounted, ref, reactive} from 'vue'
import { EChartsOption } from 'echarts'
const wrapper = ref()
const roaderList = reactive([{ id: 0, name: '线路1', children: [{ id: 110, name: '图1' }, { id: 111, name: '图2' }, { id: 112, name: '图3' }] }, { id: 1, name: '线路2', children: [{ id: 113, name: '图4' }, { id: 114, name: '图5' }, { id: 115, name: '图6' }] }])
const chartList = reactive([{ id: 110, name: '图1' }, { id: 111, name: '图2' }, { id: 112, name: '图3' }, { id: 113, name: '图4' }, { id: 114, name: '图5' }, { id: 115, name: '图6' }])
const echartsOption = reactive<EChartsOption>({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: [
    {
      type: 'category',
      data: ['Mon', 'Tue'],
      axisTick: {
        alignWithLabel: true
      }
    }
  ],
  yAxis: [
    {
      type: 'value'
    }
  ],
  series: [
    {
      name: 'Direct',
      type: 'bar',
      barWidth: '100%',
      data: [10, 52]
    }
  ]
}) as EChartsOption
onMounted(() => {
  ready(() => {
    const roaders = document.querySelectorAll('#roader .roader')
    const charts = document.querySelectorAll('#chart .chart')
    console.log('roaders', roaders, charts)
    const instance = newInstance({
      container: wrapper.value
    })
    charts.forEach(item => {
      instance.addEndpoint(item, {
        target: true,
        endpoint: "Blank"
      })
    })
    let count = 0
    roaders.forEach((roader,index) => {
      instance.addEndpoint(roader, {
        source: true,
        endpoint: "Blank"
      })
      roaderList[index].children.forEach(() => {
        const connection = instance.connect({
          source: roader,
          target: charts[count],
          endpoint: "Blank",
          anchors: ['Bottom', 'Top'],
          // connector: {
          //   type: 'Bezier',
          //   options:{
          //     curviness: 50
          //   }
          // },
          paintStyle: {
            strokeWidth: 3, // 设置连接线的宽度为 2 像素
            stroke: 'rgba(0, 0, 0, 0)', // 设置连接线的颜色为黑色
            outlineWidth: 10,
          },
          hoverPaintStyle: {
            strokeWidth: 5,
            stroke: '#bfa',
            fill: 'red',
            outlineWidth: 10,
            outlineStroke: 'rgba(0, 0, 0, 0)'
          },
        })
        console.log('tesst', connection)
        count++
      })
      nextTick(() => {
        const test = document.querySelectorAll('.wrapper svg path')
        console.log('test', test)
      })
    })
    
    console.log('instance', instance)
    // instance.setDraggable(sourceElement.value, false)
  })
})
</script>

<style lang="scss" scoped>
.wrapper {
  box-sizing: border-box;
  overflow: hidden;
  height:700px;
  overflow: hidden;
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  .room_girder {
    width: 100%;
    height: 16px;
    display: flex;
    align-items: center;
    .line {
      width: 100%;
      height: 10px;
      background-color: blue;
      display: flex;
      justify-content: space-between;
      align-items: center;
      .box {
        width: 26px;
        height: 16px;
        background-color: #ccc;
      }
    }
  }
  .room_roader {
    width: 100%;
    display: flex;
    justify-content: space-around;
    margin-top: -3px;
    .roader {
      width: 80px;
      height: 40px;
      background-color: #ccc;
    }
  }
  .room_chart {
    svg .jtk-connector {
      stroke: red !important;
      stroke-width: 2px !important;
    }
    width: 100%;
    display: flex;
    justify-content: space-around;
    margin-top: 50px;
    .chart {
      width: 180px;
      height: 250px;
      border: 1px solid #555;
    }
  }
}
.box1 {
  width: 150px;
  height: 150px;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
  top: 100px;
  position:absolute;
}
.boxList {
  width: 100%;
  display: flex;
  justify-content: space-around;
  position: absolute;
  top: 200px;
}
.box2 {
  width: 150px;
  height: 150px;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
}
</style>