<template>
   <div class="homeContainer">
    <div class="left">
      <el-card shadow="never">
        <template #header>
          <div>用能</div>
        </template>
        <div>当日用能：{{energyInfo.todayEq ? energyInfo.todayEq.toFixed(2) : '0.00'}}kW·h</div>
        <div>本周用能：{{energyInfo.thisWeekEq ? energyInfo.thisWeekEq.toFixed(2) : '0.00'}}kW·h</div>
        <div>本月用能：{{energyInfo.thisMonthEq ? energyInfo.thisMonthEq.toFixed(2) : '0.00'}}kW·h</div>
      </el-card>
      <ContentWrap>
        <div class="progress">
          <el-progress type="dashboard" :percentage="powerInfo.powApparent && powerInfo.powApparent.toFixed()">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}kVA</span>
              <span class="percentage-label">总视在功率</span>
            </template>
          </el-progress>
        </div>
      </ContentWrap>
      <el-card shadow="never">
        <template #header>
          <div>空间管理</div>
        </template>
        <div>未用空间：{{spaceInfo.freeSpace}}u</div>
        <div>已用空间：{{spaceInfo.usedSpace}}u</div>
        <div>总空间：{{spaceInfo.totalSpace}}u</div>
        <div>机柜数：{{spaceInfo.cabNum}}</div>
      </el-card>
      <el-card shadow="never">
        <template #header>
          <div>环境数据</div>
        </template>
        <div>当前平均温度：{{envInfo.temAvg}}°C</div>
        <div>当前最高温度：{{envInfo.temMax}}°C</div>
        <div>当前最低温度：{{envInfo.temMin}}°C</div>
        <div>最近更新时间：{{envInfo.updateTime}}</div>
      </el-card>
    </div>
    <div class="center" id="center">
      <CabTopology :containerInfo="containerInfo" :isFromHome="true" @back-data="handleBackData" @getroomid="handleGetRoomId" />
      <ContentWrap class="CabEchart">
        <Echart :options="echartOptionsPower" height="100%" width="100%" />
        <div class="btns">
          <el-button class="btn" size="small" :plain="!(radioBtn == 'pow')" type="primary" @click="switchTrend('pow')">功率</el-button>
          <el-button class="btn" size="small" :plain="!(radioBtn == 'ele')" type="primary" @click="switchTrend('ele')">用能</el-button>
        </div>
      </ContentWrap>
    </div>
    <div class="right">
      <el-card shadow="never">
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
      </ContentWrap>
      <el-card shadow="never">
        <template #header>
          <div>设备数量</div>
        </template>
        <div>报警数量：{{deviceInfo.alarmNum}}</div>
        <div>正常数量：{{deviceInfo.normalNum}}</div>
        <div>离线数量：{{deviceInfo.offLineNum}}</div>
        <div>设备总数：{{deviceInfo.deviceNum}}</div>
      </el-card>
      <el-card shadow="never">
        <template #header>
          <div>环境数据</div>
        </template>
        <div>当前平均湿度：{{envInfo.humAvg}}%</div>
        <div>当前最高湿度：{{envInfo.humMax}}%</div>
        <div>当前最低湿度：{{envInfo.humMin}}%</div>
        <div>最近更新时间：{{envInfo.updateTime}}</div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import CabTopology from "../topology/index.vue"
import { MachineRoomApi } from '@/api/cabinet/room'
import { EChartsOption } from 'echarts'

const echartOptionsPower = ref<EChartsOption>({}) //用来存储功率曲线图表的配置选项
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
  console.log('***获取机房主页面环境数据', res)
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
</style>