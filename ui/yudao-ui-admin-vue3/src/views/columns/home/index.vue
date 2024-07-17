<template>
  <div class="homeContainer">
    <div class="left">
      <el-form
        class="-mb-15px topForm"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="" prop="jf" >
          <el-select v-model="queryParams.cabinetroomId" placeholder="请选择" class="!w-100px">
            <el-option v-for="item in roomList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item >
        <span class="line"></span>
        <el-form-item label="" prop="jg">
          <el-select v-model="queryParams.cabinetColumnId" placeholder="请选择" class="!w-100px">
            <el-option v-for="item in machineList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>用能情况</div>
        </template>
        <div>今日：{{EqInfo.todayEq && EqInfo.todayEq.toFixed(2)}}kW·h</div>
        <div>本周：{{EqInfo.thisWeekEq && EqInfo.thisWeekEq.toFixed(2)}}kW·h</div>
        <div>本月：{{EqInfo.thisMonthEq  && EqInfo.thisMonthEq.toFixed(2)}}kW·h</div>
      </el-card>
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>A路</div>
        </template>
        <div>电流：
          <template v-if="EqInfo.barLineCurA">
            <span v-for="(Eq, i) in EqInfo.barLineCurA" :key="i">I{{i}}: {{Eq}}、</span>
          </template>
          <span v-else>无</span>
        </div>
        <div>电压：Ua: 9.20、Ub: 8.31、Uc: 1.33</div>
      </el-card>
      <ContentWrap>
        <div class="progress">
          <el-progress type="dashboard" :percentage="mainInfo.powApparent && mainInfo.powApparent.toFixed()">
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
      <Topology :cabinetColumnId="queryParams.cabinetColumnId" />
    </div>
    <div class="right">
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>用能情况</div>
        </template>
        <div>昨日：{{EqInfo.yesterdayEq && EqInfo.yesterdayEq.toFixed(2)}}kW·h</div>
        <div>上周：{{EqInfo.lastWeekEq && EqInfo.lastWeekEq.toFixed(2)}}kW·h</div>
        <div>上月：{{EqInfo.lastMonthEq && EqInfo.lastMonthEq.toFixed(2)}}kW·h</div>
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
          <el-progress type="dashboard" :percentage="mainInfo.powActive && mainInfo.powActive.toFixed()">
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
import { MachineColumnApi } from '@/api/cabinet/column'
import { CabinetApi } from '@/api/cabinet/info'
import { object } from 'vue-types'

const roomList = ref([]) // 左侧导航栏树结构列表
const machineList = ref([]) // 左侧导航栏树结构列表
const queryParams = reactive({
  cabinetColumnId: history?.state?.id || 6,
  cabinetroomId: history?.state?.roomId || 4
})

const echartsOptionA = reactive<EChartsOption>({
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
      // [
      //   { name: 'Sales', max: 6500 },
      //   { name: 'Administration', max: 16000 },
      //   { name: 'Information Technology', max: 30000 },
      //   { name: 'Customer Support', max: 38000 },
      //   { name: 'Development', max: 52000 },
      //   { name: 'Marketing', max: 25000 }
      // ]
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
}
const getMainEq = async() => {
  const res =  await MachineColumnApi.getMainEq({id: queryParams.cabinetColumnId})
  console.log('getMainEq res', res)
  Object.assign(EqInfo, res)
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  if (res.length > 0) {
    roomList.value = res
    machineList.value = handleNavList(queryParams.cabinetroomId)
    console.log('接口获取机房导航列表', res)
  }
}

const handleNavList = (cabinetroomId) => {
  const data = roomList.value as any
  const roomIndex = data.findIndex(item => item.id == cabinetroomId)
  let list = [] as any
  // if (data[roomIndex].children.length > 0 && data[roomIndex].children[0].type == 2) {
  //   data[roomIndex].children.forEach(child => {
  //     if (child.children.length > 0) {
  //       child.children.forEach(grandChild => {
  //         list.push(grandChild)
  //       })
  //     }
  //   })
  // } else 
  if(data[roomIndex].children.length > 0 && data[roomIndex].children[0].type == 2) {
    list = data[roomIndex].children.map(item => item)
  }
  console.log('list', list)
  return list
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
  console.log('wwwwwwwwwww', val)
  getMainData()
  getMainEq()
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
:deep(.topForm .el-form-item) {
  margin-right: 0px
}
</style>