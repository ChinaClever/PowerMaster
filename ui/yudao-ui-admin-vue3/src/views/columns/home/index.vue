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
      <ContentWrap v-if="mainInfo.cabinetList && mainInfo.cabinetList.length">
        <Echart :options="echartsOptionA" :height="300" />
      </ContentWrap>
    </div>
    <div class="center" id="center">
      <Topology :containerInfo="containerInfo" :isFromHome="true" @back-data="handleCabEchart" @id-change="handleIdChange" @getpdubar="handlePduBar">
        <template #btn>
          <el-button @click="handleJump" type="primary" plain><Icon icon="ep:edit" class="mr-5px" />编辑</el-button>
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
      <ContentWrap v-if="mainInfo.cabinetList && mainInfo.cabinetList.length">
        <Echart :options="echartsOptionB" :height="300" />
      </ContentWrap>
    </div>
  </div>
</template>

<script lang="ts" setup>
import Topology from '../topology/index.vue'
import { EChartsOption } from 'echarts'
import { MachineColumnApi } from '@/api/cabinet/column'

const {push} = useRouter()
const containerInfo = reactive({
  width: 0,
  cabinetColumnId: history?.state?.id,
  cabinetroomId: history?.state?.roomId
})
console.log('containerInfo', containerInfo)
const scaleVal = ref(1)
const echartsOptionCab = ref<EChartsOption>({})
const pduBar = ref(1) // 0:pdu 1:母线

const echartsOptionA = reactive<EChartsOption>({})
const echartsOptionB = reactive<EChartsOption>({})

const mainInfo = reactive({})
const EqInfo = reactive({})

const getMainData = async() => {
  debugger
  const res =  await MachineColumnApi.getMaindata({id: containerInfo.cabinetColumnId})
  Object.assign(mainInfo, res)
  console.log('res', res)
  if (!res.cabinetList) return
  
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
  const res =  await MachineColumnApi.getMainEq({id: containerInfo.cabinetColumnId})
  console.log('getMainEq res', res)
  Object.assign(EqInfo, res)
}
// 处理跳转
const handleJump = () => {
  push({path: '/aisle/topology', state: { id: containerInfo.cabinetColumnId, roomId: containerInfo.cabinetroomId }})
}
// 处理时pdu还是母线的事件
const handlePduBar = (type) => {
  pduBar.value = type
}
// 处理柜列id/机柜id切换事件
const handleIdChange = (id) => {
  containerInfo.cabinetColumnId = id
  getMainData()
  getMainEq()
}
// 处理柜列实时统计图表
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

// getMainData()
// getMainEq()

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
  height: calc(100vh - 120px);
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
  }
  .right {
    width: 300px;
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

</style>