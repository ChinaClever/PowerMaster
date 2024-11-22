<template>
   <div class="homeContainer">
    <div class="left">
      <ContentWrap>
        <div class="progress">
          <!--<el-progress type="dashboard" :percentage="powerInfo.powApparent && powerInfo.powApparent.toFixed()">-->
          <el-progress type="dashboard" :percentage="80">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}kVA</span>
              <span class="percentage-label">总视在功率</span>
            </template>
          </el-progress>
        </div>
      </ContentWrap>
      <!--<el-card shadow="never">
        <template #header>
          <div>空间管理</div>
        </template>
        <div>未用空间：{{spaceInfo.freeSpace}}u</div>
        <div>已用空间：{{spaceInfo.usedSpace}}u</div>
        <div>总空间：{{spaceInfo.totalSpace}}u</div>
        <div>机柜数：{{spaceInfo.cabNum}}</div>
      </el-card>-->
      <el-card shadow="never">
        <template #header>
          <div>环境数据</div>
          <el-link @click="updateChart(); toggleTable = !toggleTable" type="primary" style="margin-left:12vw;">{{toggleTable?'湿度':'温度'}}</el-link>
        </template>
        <div ref="lineidChartContainer" id="lineidChartContainer" style="width:14vw;height:25vh;"></div>
        <!--<div>当前平均温度：{{envInfo.temAvg}}°C</div>
        <div>当前最高温度：{{envInfo.temMax}}°C</div>
        <div>当前最低温度：{{envInfo.temMin}}°C</div>
        <div>最近更新时间：{{envInfo.updateTime}}</div>-->
      </el-card>
      <el-card shadow="never" style="margin-bottom: 15px">
        <!--<template #header>
          <div>当前用能</div>
        </template>
        <div>今日：{{EqInfo.todayEq && EqInfo.todayEq.toFixed(2)}}kW·h</div>
        <div>本周：{{EqInfo.thisWeekEq && EqInfo.thisWeekEq.toFixed(2)}}kW·h</div>
        <div>本月：{{EqInfo.thisMonthEq  && EqInfo.thisMonthEq.toFixed(2)}}kW·h</div>-->
        <div style="display: flex; align-items: center; margin-bottom:2vh; margin-top:1vh;">
          <span>今日用能：</span>
          <span 
            style="display: inline-block; position: relative; width: 5vw;"
          >
            <el-progress :stroke-width="26" :format="format"  :percentage="38" style="width: 9vw;" />
            <div style="position: absolute; bottom: 10%; left: 77%; transform: translateX(-50%); color: #000; border-radius: 3px; white-space: nowrap;">
              150kw
            </div>
          </span>
          <span style="margin-left:3vh;">390kw</span>
        </div>
        <div style="display: flex; align-items: center; margin-bottom:2vh; margin-top:1vh;">
          <span>本周用能：</span>
          <span 
            style="display: inline-block; position: relative; width: 5vw;"
          >
            <el-progress :stroke-width="26" :format="format"  :percentage="42" style="width: 9vw;" />
            <div style="position: absolute; bottom: 10%; left: 77%; transform: translateX(-50%); color: #000; border-radius: 3px; white-space: nowrap;">
              290kw
            </div>
          </span>
          <span style="margin-left:3vh;">690kw</span>
        </div>
        <div style="display: flex; align-items: center; margin-bottom:2vh; margin-top:1vh;">
          <span>本月用能：</span>
          <span 
            style="display: inline-block; position: relative; width: 5vw;"
          >
            <el-progress  :stroke-width="26" :format="format" :percentage="46" style="width: 9vw;" />
            <div style="position: absolute; bottom: 10%; left: 77%; transform: translateX(-50%); color: #000; border-radius: 3px; white-space: nowrap;">
              759kw
            </div>
          </span>
          <span style="margin-left:3vh;">1650kw</span>
        </div>
      </el-card>
    </div>
    <div class="center" id="center">
      <CabTopology :containerInfo="containerInfo" :isFromHome="true" @back-data="handleBackData" @getroomid="handleGetRoomId" />
      <ContentWrap class="CabEchart">
        <Echart :options="echartOptionsPower" height="30vh" width="100%" />
        <div class="btns">
          <el-button class="btn" size="small" :plain="!(radioBtn == 'pow')" type="primary" @click="switchTrend('pow')">功率</el-button>
          <el-button class="btn" size="small" :plain="!(radioBtn == 'ele')" type="primary" @click="switchTrend('ele')">用能</el-button>
        </div>
      </ContentWrap>
    </div>
    <div class="right">
      <el-card shadow="never">
        <template #header>
          <div style="margin-left:45%">PUE</div>
        </template>
        <div style="margin-left:45%">1.03</div>
      </el-card>
      <!--<el-card shadow="never">
        <template #header>
          <div>用能</div>
        </template>
        <div>昨日用能：{{energyInfo.yesterdayEq ? energyInfo.yesterdayEq.toFixed(2) : '0.00'}}kW·h</div>
        <div>上周用能：{{energyInfo.lastWeekEq ? energyInfo.lastWeekEq.toFixed(2) : '0.00'}}kW·h</div>
        <div>上月用能：{{energyInfo.lastMonthEq ? energyInfo.lastMonthEq.toFixed(2) : '0.00'}}kW·h</div>
      </el-card>
      <ContentWrap>
        <div class="progress">
          <el-progress type="dashboard" :percentage="powerInfo.powActive && powerInfo.powActive.toFixed()">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}kW</span>
              <span class="percentage-label">总有功功率</span>
            </template>
          </el-progress>
        </div>
      </ContentWrap>-->
      <el-card shadow="never" class="mb-8px">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>告警统计</span>
          </div>
        </template>
        <el-table :data="tableData" style="width: 100%;height:25vh" border class="text-12px">
          <el-table-column prop="error" label="告警内容"  />
          <el-table-column prop="box" label="告警设备" />
          <el-table-column prop="time" label="告警时间" />
        </el-table>
      </el-card>
      <el-card shadow="never">
        <template #header>
          <div class="h-3 flex justify-between">
            <span> 设备统计</span>
          </div>
        </template>
          <el-table :data="tableData" style="width: 100%;height:25vh" border class="text-12px">
            <el-table-column prop="name" label=""  width="100" />
            <el-table-column prop="all" label="总数" width="56" />
            <el-table-column prop="on" label="在线" width="56" />
            <el-table-column prop="off" label="离线" width="56" />
          </el-table>
      </el-card>
      <!--<el-card shadow="never" class="mb-8px">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>设备统计</span>
          </div>
        </template>
        <el-table :data="tableData" style="width: 15vw;height:25vh" border class="text-12px">
          <el-table-column prop="name" label=""  />
          <el-table-column prop="all" label="总数"  />
          <el-table-column prop="on" label="在线" />
          <el-table-column prop="off" label="离线" />
        </el-table>
      </el-card>
      <el-card shadow="never">
        <template #header>
          <div>环境数据</div>
        </template>
        <div>当前平均湿度：{{envInfo.humAvg}}%</div>
        <div>当前最高湿度：{{envInfo.humMax}}%</div>
        <div>当前最低湿度：{{envInfo.humMin}}%</div>
        <div>最近更新时间：{{envInfo.updateTime}}</div>
      </el-card>-->
    </div>
  </div>
</template>

<script lang="ts" setup>
import CabTopology from "../topology/index.vue"
import { MachineRoomApi } from '@/api/cabinet/room'
import { EChartsOption } from 'echarts'

import * as echarts from 'echarts'

const format = (percentage) => ( ``)  //用来自定义进度条的内容

const echartOptionsPower = ref<EChartsOption>({}) //用来存储功率曲线图表的配置选项
const environmentOptions = ref<EChartsOption>({}) //用来存储环境图表的配置选项
const roomId = ref<number>(0)
const radioBtn = ref('pow')
const containerInfo = reactive({
  width: 1000,
}) // 机房拓扑容器信息
const deviceInfo = reactive({}) // 设备信息
const energyInfo = reactive({}) // 用能信息
const powerInfo = reactive({}) // 功率信息
const spaceInfo = reactive({}) // 空间信息
const envInfo = reactive({}) // 空间信息
const echartInfo = reactive<any>({}) //配置图表的数据系列
const toggleTable = ref(false)
const tableData = [
  {
    name: 'PDU',
    all: 35,
    on: 15,
    off: 20,
    error:'温度超阀值上限',
    box:'温湿度01',
    time:'2022-11-07 12:15:46'
  },{
    name: '始端箱',
    all: 35,
    on: 15,
    off: 20,
    error:'制冷压力超阀值上限',
    box:'精密空调',
    time:'2022-11-07 12:15:46'
  },{
    name: '插接箱',
    all: 35,
    on: 15,
    off: 20,
    error:'检测到有水',
    box:'水浸',
    time:'2022-11-07 12:15:46'
  },{
    name: 'PDU-1',
    all: 35,
    on: 15,
    off: 20,
    error:'开门时间异常',
    box:'东门门禁',
    time:'2022-11-07 12:15:46'
  },{
    name: '始端箱-1',
    all: 35,
    on: 15,
    off: 20,
    error:'设备离线',
    box:'摄像机1',
    time:'2022-11-07 12:15:46'
  },{
    name: '插接箱-1',
    all: 35,
    on: 15,
    off: 20,
    error:'检测到有人',
    box:'红外1',
    time:'2022-11-07 12:15:46'
  },{
    name: 'PDU-2',
    all: 35,
    on: 15,
    off: 20,
    error:'温度超阀值上限',
    box:'温湿度02',
    time:'2022-11-07 12:15:46'
  },{
    name: '始端箱-2',
    all: 35,
    on: 15,
    off: 20,
    error:'温度超阀值上限',
    box:'温湿度03',
    time:'2022-11-07 12:15:46'
  },{
    name: '插接箱-2',
    all: 35,
    on: 15,
    off: 20,
    error:'温度超阀值上限',
    box:'温湿度04',
    time:'2022-11-07 12:15:46'
  }
]

console.log('111',tableData)

let lineidChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainer = ref<HTMLElement | null>(null);

//获取数据
const handleGetRoomId = (data) => {
  roomId.value = data
  getRoomDataDetail()
  getRoomDevData()
  getRoomPowData()
  getRoomEchartData()
  getRoomEnvData()
  getRoomEqData()
  console.log('handleGetRoomId', data)
}
// 获取机房数据详情
const getRoomDataDetail = async() => {
  const res = await MachineRoomApi.getRoomDataDetail({id: roomId.value})
  console.log('***getRoomDataDetail', res)
}
// 获取机房主页面设备数据
const getRoomDevData = async() => {
  const res = await MachineRoomApi.getRoomDevData({id: roomId.value})
  Object.assign(deviceInfo, res)
  console.log('***获取机房主页面设备数据', res)
}
// 获取机房主页面功率数据
const getRoomPowData = async() => {
  const res = await MachineRoomApi.getRoomPowData({id: roomId.value})
  Object.assign(powerInfo, res)
  console.log('***获取机房主页面功率数据', res)
}
// 获取机房主页面曲线数据
const getRoomEchartData = async() => {
  const res = await MachineRoomApi.getRoomEchartData({id: roomId.value})
  console.log('***获取机房主页面曲线数据', res)
  Object.assign(echartInfo, {
    powList: res.powList || [],
    eqList: res.eqList || []
  })
  switchTrend(radioBtn.value, true)
  console.log('....', echartInfo)
}
// 获取机房主页面环境数据
const getRoomEnvData = async() => {
  const res = await MachineRoomApi.getRoomEnvData({id: roomId.value})
  Object.assign(envInfo, res)
  console.log('***获取机房主页面环境数据', envInfo)
}
// 获取机房主页面用能
const getRoomEqData = async() => {
  const res = await MachineRoomApi.getRoomEqData({id: roomId.value})
  Object.assign(energyInfo, res)
  console.log('***获取机房主页面用能', res)
}
// 获取空间信息 ?需不需要刷新
const handleBackData = (data) => {
  console.log('***',data)
  Object.assign(spaceInfo, data)
}

const initChart = () => {
  lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
  lineidChart.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === '冷通道平均温度' || param.seriesName === '热通道平均温度') {
            result += '℃';
          }else{
            result += '%'
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {
        data: ['冷通道平均温度', '热通道平均温度'], // 图例项
        selected: false
      },
      xAxis: {
        type: 'category',nameLocation: 'end',
        boundaryGap: false,
        data:['周一','周二','周三','周四','周五']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '冷通道平均温度',
          type: 'line',
          data: [31,31,31,31,31],
          symbol: 'circle',
          symbolSize: 4
        },
        {
          name: '热通道平均温度',
          type: 'line',
          data: [33,34,35,36,37],
          symbol: 'circle',
          symbolSize: 4,
          lineStyle:{type: 'dashed'}
        }
      ]
  })
}

const updateChart = () => {
  lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
  if(toggleTable.value === true){
    lineidChart.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === '冷通道平均温度' || param.seriesName === '热通道平均温度') {
            result += '℃';
          }else{
            result += '%'
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {
        data: ['冷通道平均温度', '热通道平均温度'], // 图例项
        selected: false
      },
      xAxis: {
        type: 'category',nameLocation: 'end',
        boundaryGap: false,
        data:['周一','周二','周三','周四','周五']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '冷通道平均温度',
          type: 'line',
          data: [31,31,31,31,31],
          symbol: 'circle',
          symbolSize: 4
        },
        {
          name: '热通道平均温度',
          type: 'line',
          data: [33,34,35,36,37],
          symbol: 'circle',
          symbolSize: 4,
          lineStyle:{type: 'dashed'}
        }
      ]
  })
  }else if(toggleTable.value === false){
    lineidChart.setOption( {
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === '冷通道平均温度' || param.seriesName === '热通道平均温度') {
            result += '℃';
          }else{
            result += '%'
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {
        data: ['冷通道平均湿度','热通道平均湿度'], // 图例项
        selected: false
      },
      xAxis: {
        type: 'category',nameLocation: 'end',
        boundaryGap: false,
        data:['周一','周二','周三','周四','周五']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '冷通道平均湿度',
          type: 'line',
          data: [81,81,81,81,81],
          symbol: 'circle',
          symbolSize: 4
        },
        {
          name: '热通道平均湿度',
          type: 'line',
          data: [82,83,84,85,86],
          symbol: 'circle',
          symbolSize: 4,
          lineStyle:{type: 'dashed'}
        }
      ]
  })
  }
}

//配置ECharts图表
const switchTrend = (type, first = false) => {
  if (type == radioBtn.value && !first) return
  radioBtn.value = type
  if (type == 'pow') {
    echartOptionsPower.value = {
      title: {
        text: '功率曲线'
      },
      grid: {
        left: '0',
        right: '20',
        bottom: '0',
        top: '40',
        containLabel: true
      },
      legend: {},
      xAxis: {
        type: 'category',
        data: echartInfo.powList.map(item => item.dateTime)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '有功功率',
          data: echartInfo.powList.map(item => item.activePow ? item.activePow.toFixed(3) : '0.000'),
          type: 'line',
          smooth: true,
          symbol: 'none',
        },
        {
          name: '无功功率',
          data: echartInfo.powList.map(item => item.reactivePow ? item.reactivePow.toFixed(3) : '0.000'),
          type: 'line',
          smooth: true,
          symbol: 'none',
        },
        {
          name: '视在功率',
          data: echartInfo.powList.map(item => item.apparentPow ? item.apparentPow.toFixed(3) : '0.000'),
          type: 'line',
          smooth: true,
          symbol: 'none',
        },
      ]
    }
    console.log('echartOptionsPower', echartOptionsPower.value)
  } else if (type == 'ele') {
    echartOptionsPower.value = {
      title: {
        text: '用能曲线'
      },
      grid: {
        left: '0',
        right: '20',
        bottom: '0',
        top: '40',
        containLabel: true
      },
      legend: {selectedMode: 'single'},
      xAxis: {
        type: 'category',
        data: echartInfo.eqList.map(item => item.dateTime)
      },
      yAxis: {
        type: 'value'
      },
      series: [//配置图表的数据系列
        {
          name: '近一个月用能',
          data: echartInfo.eqList.map(item => item.eq),
          type: 'line',
          smooth: true,
          symbol: 'none',
        },
      ]
    }
  }
}

onMounted(() => {
  const centerEle = document.getElementById('center')
  containerInfo.width = centerEle?.offsetWidth as number
  console.log('centerEle', containerInfo.width, centerEle?.offsetWidth, centerEle?.offsetHeight)
  initChart()
})
</script>

<style lang="scss" scoped>
.homeContainer {
  box-sizing: border-box;
  width: 100%;
  // height: calc(100vh - 120px);
  min-height: 550px;
  max-height: calc(100vh - 120px);
  box-sizing: border-box;
  // background-color: #999;
  display: flex;
  .center {
    flex: 1;
    box-sizing: border-box;
    overflow: hidden;
    margin: 0 15px;
    padding-bottom: 15px;
    position: relative;
    display: flex;
    flex-direction: column;
    .CabEchart{
      flex: 1;
      min-height: 200px;
      position: relative;
      .btns {
        position: absolute;
        z-index: 9;
        right: 30px;
        top: 20px;
      }
    }
  }
  .left {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
  }
  .right {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
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
 
:deep(.progress .el-progress__text::after ){
  content: '60kW'; /* 要显示的文本 */
  position: absolute;
  top: 0; /* 放置在原元素的 */
  left: 100%;
  color: #000;
  padding: 5px;
  border-radius: 3px;
  white-space: nowrap;
  transform: translateX(5px); /* 稍微向右边移动一点以避免与进度条重叠 */
  opacity: 0; /* 默认隐藏 */
  transition: opacity 0.3s; /* 添加过渡效果 */
  pointer-events: none; /* 确保提示框不会干扰鼠标事件 */
}
 
:deep(.progress:hover .el-progress__text::after) {
  opacity: 1; /* 鼠标悬停时显示 */
}

:deep(.el-card) {
  margin-bottom: 15px;
}
:deep(.el-card__header) {
  padding: 15px;
}
:deep(.el-card__body) {
  padding: 15px;
}
:deep(.CabEchart .el-card__body) {
  width: 100%;
  height: 100%;
  & > div {
    width: 100%;
    height: 100%;
    box-sizing: border-box;
    padding: 5 20px;
  }
}

.responsive-table {
  table-layout: fixed;  /* 确保列宽按照设置的宽度显示 */
  word-wrap: break-word; /* 文本过长时换行 */
}
</style>