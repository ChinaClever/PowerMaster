<template>
  <div class="homeContainer">
    <div class="left">
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>当前用能</div>
        </template>
        <div>今日：{{EqInfo.todayEq && EqInfo.todayEq.toFixed(2)}}kW·h</div>
        <div>本周：{{EqInfo.thisWeekEq && EqInfo.thisWeekEq.toFixed(2)}}kW·h</div>
        <div>本月：{{EqInfo.thisMonthEq  && EqInfo.thisMonthEq.toFixed(2)}}kW·h</div>
      </el-card>
      <el-card v-if="pduBar" shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>A路</div>
        </template>
        <div>电流：
          <template v-if="mainInfo.barLineCurA">
            <span v-for="(Eq, i) in mainInfo.barLineCurA" :key="i">I{{String.fromCharCode(97 + i)}}: {{Eq}}、</span>
          </template>
          <span v-else>无</span>
        </div>
        <div>电压：
          <template v-if="mainInfo.barLineVolA">
            <span v-for="(Eq, i) in mainInfo.barLineVolA" :key="i">U{{String.fromCharCode(97 + i)}}: {{Eq}}、</span>
          </template>
          <span v-else>无</span>
        </div>
      </el-card>
      <ContentWrap>
        <div class="progress">
          <el-progress type="dashboard" :percentage="mainInfo.powApparent && mainInfo.powApparent.toFixed()">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}</span>
              <span class="percentage-label">总视在功率</span>
            </template>
          </el-progress>
        </div>
      </ContentWrap>
      <ContentWrap>
        <Echart :options="echartsOptionA" :height="300" />
      </ContentWrap>
    </div>
    <div class="center" id="center">
      <Topology ref="cabCol" :containerInfo="containerInfo" :isFromHome="true" @success="handleCabEchart" >
        <template #define>
          <el-form
            class="-mb-15px topForm"
            :model="queryParams"
            ref="queryFormRef"
            :inline="true"
            label-width="68px"
          >
            <el-form-item label="" prop="jf" >
              <el-select size="small" v-model="queryParams.cabinetroomId" placeholder="请选择" class="!w-100px">
                <el-option v-for="item in roomList" :key="item.roomId" :label="item.roomName" :value="item.roomId" />
              </el-select>
            </el-form-item >
            <span class="line"></span>
            <el-form-item label="" prop="jg">
              <el-select size="small" v-model="queryParams.cabinetColumnId" placeholder="请选择" class="!w-100px">
                <el-option v-for="item in machineList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-form>
        </template>
      </Topology>
      <ContentWrap class="CabEchart">
        <Echart :options="echartsOptionCab" height="100%" width="100%" />
      </ContentWrap>
      <div class="mask" @click.right.prevent=""></div>
    </div>
    <div class="right">
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>历史用能</div>
        </template>
        <div>昨日：{{EqInfo.yesterdayEq && EqInfo.yesterdayEq.toFixed(2)}}kW·h</div>
        <div>上周：{{EqInfo.lastWeekEq && EqInfo.lastWeekEq.toFixed(2)}}kW·h</div>
        <div>上月：{{EqInfo.lastMonthEq && EqInfo.lastMonthEq.toFixed(2)}}kW·h</div>
      </el-card>
      <el-card v-if="pduBar" shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>B路</div>
        </template>
        <div>电流：
          <template v-if="mainInfo.barLineCurB">
            <span v-for="(Eq, i) in mainInfo.barLineCurB" :key="i">I{{String.fromCharCode(97 + i)}}: {{Eq}}、</span>
          </template>
          <span v-else>无</span>
        </div>
        <div>电压：
          <template v-if="mainInfo.barLineVolB">
            <span v-for="(Eq, i) in mainInfo.barLineVolB" :key="i">U{{String.fromCharCode(97 + i)}}: {{Eq}}、</span>
          </template>
          <span v-else>无</span>
        </div>
      </el-card>
      <ContentWrap>
        <div class="progress">
          <el-progress type="dashboard" :percentage="mainInfo.powActive && mainInfo.powActive.toFixed()">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}</span>
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
import Topology from '../topology/index.vue'
import { EChartsOption } from 'echarts'
import { MachineColumnApi } from '@/api/cabinet/column'
import { object } from 'vue-types'

const containerInfo = reactive({
  width: 0
})
const scaleVal = ref(1)
const echartsOptionCab = ref<EChartsOption>({})
const pduBar = ref(1) // 0:pdu 1:母线
const cabCol = ref()
const roomList = ref([]) // 左侧导航栏树结构列表
const machineList = ref<any>([]) // 左侧导航栏树结构列表
const queryParams = reactive({
  cabinetColumnId: history?.state?.id || 6,
  cabinetroomId: history?.state?.roomId || 4
})

const echartsOptionA = reactive<EChartsOption>({})
const echartsOptionB = reactive<EChartsOption>({})

const mainInfo = reactive({})
const EqInfo = reactive({})

const getMainData = async() => {
  const res =  await MachineColumnApi.getMaindata({id: queryParams.cabinetColumnId})
  Object.assign(mainInfo, res)
  console.log('res', res)
  Object.assign(echartsOptionA, {
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
      indicator: res.cabinetList.map((item, index) => {
        return {
          name: '机柜' + (index+1),
          max: 10
        }
      })
    },
    series: [
      {
        name: 'power',
        type: 'radar',
        data: [
          {
            value: res.cabinetList.map(item => item.powApparentA),
            name: '视在功率'
          },
          {
            value: res.cabinetList.map(item => item.powActiveA),
            name: '有功功率',
            areaStyle: {
              color: 'rgba(206, 255, 171, 0.6)'
            }
          }
        ]
      }
    ]
  })
  Object.assign(echartsOptionB, {
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
      indicator: res.cabinetList.map((item, index) => {
        return {
          name: '机柜' + (index+1),
          max: 10
        }
      })
    },
    series: [
      {
        name: 'power',
        type: 'radar',
        data: [
          {
            value: res.cabinetList.map(item => item.powApparentB),
            name: '视在功率'
          },
          {
            value: res.cabinetList.map(item => item.powActiveB),
            name: '有功功率',
            areaStyle: {
              color: 'rgba(206, 255, 171, 0.6)'
            }
          }
        ]
      }
    ]
  })
}
const getMainEq = async() => {
  const res =  await MachineColumnApi.getMainEq({id: queryParams.cabinetColumnId})
  console.log('getMainEq res', res)
  Object.assign(EqInfo, res)
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await MachineColumnApi.getAisleList({})
  console.log('接口获取机房导航列表*****', res)
  if (res && res.length) {
    roomList.value = res
    machineList.value = handleNavList(queryParams.cabinetroomId)
  }
}

const handleNavList = (cabinetroomId) => {
  const data = roomList.value as any
  const findRoom = data.find(item => item.roomId == cabinetroomId)
  const findMachine = findRoom.aisleList.find(item => item.id == queryParams.cabinetColumnId)
  pduBar.value = findMachine.pduBar
  console.log('roomIndex', findMachine, findRoom)
  return findRoom.aisleList
}

const handleCabEchart = (result, scale) => {
  console.log('handleCabEchart', result, typeof result)
  scaleVal.value = scale
  echartsOptionCab.value = {
    title: {
      text: '机柜列实时统计'
    },
    grid: {
      left: '0',
      right: '25',
      bottom: '5',
      top: '50',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: result.map(item => item.cabinetName || ''),
      axisLabel: {
        interval: 0 // 设置为 0 表示所有标签都显示
      }
    },
    legend:{
      top: '0',
      right: '50',
      selectedMode: 'single'
    },
    yAxis: {
      type: 'value',
      // show: false,
    },
    series: [
      {
        name:'有功功率',
        data: result.map(item => item.powActive == undefined ? null : item.powActive),
        type: 'bar',
        barWidth: '92%',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
      {
        name:'昨日用能',
        data: result.map(item => item.yesterdayEq == undefined ? null : item.yesterdayEq),
        type: 'bar',
        barWidth: '92%',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
    ]
  }
}

getMainData()
getMainEq()
getNavList()

watch(() => queryParams.cabinetroomId, (val) => {
  machineList.value = handleNavList(val)
  if (machineList.value.length == 0) {
    queryParams.cabinetColumnId = null
    return
  }
  const defaultValue = machineList.value[0] as any
  queryParams.cabinetColumnId = defaultValue.id
})

watch(() => queryParams.cabinetColumnId,(val) => {
  console.log('wwwwwwwwwww', val, machineList.value)
  getMainData()
  getMainEq()
})

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
    .mask {
      width: 100%;
      height: calc(100% - 50px);
      position: absolute;
      bottom: 0;
      z-index: 99;
    }
    .CabEchart {
      flex: 1;
      margin-bottom: 0;
      z-index: 999;
    }
  }
  .left {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }
  .right {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
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
.topForm .line {
  display: inline-block;
  width: 8px;
  border-bottom: 1px solid #000;
  margin: 0px 8px 13px 8px;
}
:deep(.el-card__header) {
  padding: 15px;
}
:deep(.el-card__body) {
  padding: 15px;
}
:deep(.CabEchart .el-card__body) {
  box-sizing: border-box;
  height: 100%;
  width: 100%;
  & > div {
    box-sizing: border-box;
    height: 100%;
    width: 100%;
  }
}
:deep(.topForm .el-form-item) {
  margin-right: 0px
}
</style>