<template>
  <div>
    <el-card shadow="never">
      <el-skeleton :loading="loading" animated>
        <el-row :gutter="16" justify="space-between">
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <div class="flex items-center">
              <el-avatar :src="avatar" :size="70" class="mr-16px">
                <img src="@/assets/imgs/avatar.gif" alt="" />
              </el-avatar>
              <div>
                <div class="text-20px">
                  PowerMaster系统安全守护第 {{devInfo.days}} 天
                </div>
                <div class="mt-10px text-14px text-gray-500">
                  <ScrollAlarm :notices="alarmData" v-if="true" />
                  <span v-else>当前一切正常</span>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <div class="h-70px flex items-center justify-end lt-sm:mt-10px">
              <div class="px-8px text-right">
                <div class="mb-16px text-14px text-gray-400">机柜数</div>
                <span class="text-20px">{{devInfo.cabNum}}</span>
              </div>
              <el-divider direction="vertical" />
              <div class="px-8px text-right">
                <div class="mb-16px text-14px text-gray-400">已开通</div>
                <span class="text-20px">{{devInfo.cabUse}}</span>
              </div>
              <el-divider direction="vertical" border-style="dashed" />
              <div class="px-8px text-right">
                <div class="mb-16px text-14px text-gray-400">未启用</div>
                <span class="text-20px">{{devInfo.cabUnused}}</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-skeleton>
    </el-card>
  </div>

  <el-row class="mt-8px" :gutter="8" justify="space-between">
    <el-col :xl="18" :lg="18" :md="24" :sm="24" :xs="24" class="mb-8px">
      <el-card shadow="never">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>机房状态</span>
            <div class="roomPowerBtns">
              <el-button @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''" size="small"><Icon icon="ep:grid" style="margin-right: 4px" />机房功率</el-button>                             
              <el-button @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''" size="small"><Icon icon="ep:grid" style="margin-right: 4px" />机房温度</el-button>            
              <el-button @click="valueMode = 2;" :type="valueMode == 2 ? 'primary' : ''" size="small"><Icon icon="ep:grid" style="margin-right: 4px" />机房对比</el-button>
              <el-button @click="push({path: '/room/roommonitor/roompower'})" size="small">全屏</el-button>
            </div>
          </div>
        </template>
        <RoomPower :isFromHome="true" :valueButton="valueMode" />
      </el-card>

      <el-card shadow="never" class="mt-8px">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>用电分布</span>
            <div class="flex text-12px">
              <template v-for="(btn, index) in powBtns" :key="index">
                <div class="flex mr-8px" style="cursor: pointer;" @click="switchPowBtn(index)">
                  <div class="w-25px h-15px mr-2px" :style="{backgroundColor: prePowBtn == index ? btn.backgroundColor : 'rgb(204,204,204)',borderRadius: '3px'}"></div>
                  <div>{{btn.name}}</div>
                </div>
              </template>
            </div>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row :gutter="20" justify="space-between">
            <el-col :xl="6" :lg="6" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover">
                <el-skeleton :loading="loading" animated>
                  <!-- <div class="w-full h-210px flex" style="flex-direction: column; align-items: center;justify-content: center;">
                    <div>总用电量</div>
                    <div>{{eqInfo[powBtns[prePowBtn].totalParam] ? eqInfo[powBtns[prePowBtn].totalParam].toFixed(2) : '0.00'}}kW·h</div>
                  </div> -->
                  <div class="w-full h-210px flex" style="flex-direction: column; align-items: center;justify-content: center;position: relative;">
                    <el-progress type="dashboard" :percentage="Math.min(eqInfo[powBtns[prePowBtn].totalParam]/eqInfo[oldPowList[prePowBtn]]*100,100)" width="200">
                      <!-- <span v-if="eqInfo[powBtns[prePowBtn].totalParam] > 1000" class="percentage-value">{{eqInfo[powBtns[prePowBtn].totalParam] ? (eqInfo[powBtns[prePowBtn].totalParam]/1000).toFixed(2) : '0.00'}}</span> -->
                      <span class="percentage-value">{{eqInfo[powBtns[prePowBtn].totalParam] ? eqInfo[powBtns[prePowBtn].totalParam].toFixed(2) : '0.00'}}</span>
                      <span class="percentage-label">总用电量</span>
                      <!-- <span class="percentage-unit">kW·h</span> -->
                    </el-progress>
                    <div style="position: absolute;right: 0;top:0;font-size: 12px">单位:kW·h</div>
                  </div>
                </el-skeleton>
              </el-card>
            </el-col>
            <el-col :xl="18" :lg="18" :md="24" :sm="24" :xs="24">
              <div>
                <el-card shadow="hover" :style="computedEnInfo">
                  <el-skeleton :loading="loading" animated>
                    <Echart :options="eqOptionsData" :height="210" :style="computedEnInfoWidth" />
                  </el-skeleton>
                </el-card>
              </div>
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>
    </el-col>
    <el-col :xl="6" :lg="6" :md="24" :sm="24" :xs="24" class="mb-8px">
      <el-card shadow="never" class="mb-8px">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>告警统计</span>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row class="flex justify-between">
            <el-col :span="12" class="flex flex-col justify-evenly">
              <div><span class="bullet" style="background-color:#C8603A;"></span>未处理告警数目：{{alarmInfo.untreated}}</div>
              <div><span class="bullet" style="background-color:#E5B849;"></span>已挂起告警数目：{{alarmInfo.hung}}</div>
              <div><span class="bullet" style="background-color:#AD3762;"></span>已确认告警数目：{{alarmInfo.confirm}}</div>
            </el-col>
            <el-col :span="10" class="flex justify-center">
              <Echart :height="130" :width="130" :options="alarmChartOptions" />
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>
      <el-card shadow="never" class="mb-8px">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>实时功率</span>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row class="flex justify-between">
            <el-col v-if="powInfo.totalPowActive > 1000 || powInfo.totalPowReactive > 1000 || powInfo.totalPowApparent > 1000" :span="12" class="flex flex-col justify-evenly">
              <div><span class="bullet" style="background-color:#E5B849;"></span>总有功功率：{{powInfo.totalPowActive ? (powInfo.totalPowActive/1000).toFixed(1) : '0.0'}}MW</div>
              <div><span class="bullet" style="background-color:#C8603A;"></span>总无功功率：{{powInfo.totalPowReactive ? (powInfo.totalPowReactive/1000).toFixed(1) : '0.0'}}MVar</div>
              <div><span class="bullet" style="background-color:#AD3762;"></span>总视在功率：{{powInfo.totalPowApparent ? (powInfo.totalPowApparent/1000).toFixed(1) : '0.0'}}MVA</div>
            </el-col>
            <el-col v-else :span="12" class="flex flex-col justify-evenly">
              <div><span class="bullet" style="background-color:#E5B849;"></span>总有功功率：{{powInfo.totalPowActive ? powInfo.totalPowActive.toFixed(1) : '0.0'}}kW</div>
              <div><span class="bullet" style="background-color:#C8603A;"></span>总无功功率：{{powInfo.totalPowReactive ? powInfo.totalPowReactive.toFixed(1) : '0.0'}}kVar</div>
              <div><span class="bullet" style="background-color:#AD3762;"></span>总视在功率：{{powInfo.totalPowApparent ? powInfo.totalPowApparent.toFixed(1) : '0.0'}}kVA</div>
            </el-col>
            <el-col :span="10" class="flex justify-center">
              <Echart :height="130" :width="130" :options="powChartOptions" />
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>
      <el-card shadow="never" class="mb-8px" v-if="toggleTable===false">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>设备统计/告警统计</span>
            <el-link @click="toggleTable = !toggleTable" type="primary">切换</el-link>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
            <Echart :height="200" :options="numChartOptions" />
        </el-skeleton>
      </el-card>
      <el-card shadow="never" class="mb-8px" v-else-if="toggleTable===true">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>设备统计/告警统计</span>
            <el-link @click="toggleTable = !toggleTable" type="primary">切换</el-link>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <div ref="scrollableContainerOne" class="scrollable-container-one" @scroll="handleScroll">
            <el-table :data="tableData" style="width: 100%" border class="text-12px">
              <el-table-column prop="error" label="告警内容"  />
              <el-table-column prop="box" label="告警设备" />
              <el-table-column prop="time" label="告警时间" />
            </el-table>
          </div>
        </el-skeleton>
      </el-card>
    </el-col>
  </el-row>
</template>
<script lang="ts" setup>
import { set } from 'lodash-es'
import { EChartsOption } from 'echarts'
import { formatTime } from '@/utils'

import { useUserStore } from '@/store/modules/user'
import { useWatermark } from '@/hooks/web/useWatermark'
import { MachineHomeApi } from '@/api/cabinet/home'

import { defineComponent } from 'vue'

import CabTopology from "@/views/room/topology/index.vue"
import RoomPower from "@/views/room/monitor/index.vue"
import { MachineRoomApi } from '@/api/cabinet/room'
import { AlarmApi } from '@/api/system/notify/alarm'

import ScrollAlarm from "./component/scrollAlarm.vue"


defineOptions({ name: 'Home' })

const { push } = useRouter() // 路由跳转

const valueMode = ref(0)
const switchValue = ref(0) //控制按钮的切换

const { t } = useI18n()
const userStore = useUserStore()
const { setWatermark } = useWatermark()
const loading = ref(true)
const roomShowType = ref(true)
const avatar = userStore.getUser.avatar
const username = userStore.getUser.nickname
const eqOptionsData = reactive<EChartsOption>({}) as EChartsOption
const powOptionsData = reactive<EChartsOption>({}) as EChartsOption
const powOptionsDataOne = reactive<EChartsOption>({}) as EChartsOption
const devInfo = reactive({}) // 设备信息
const powInfo = reactive({}) // 功率数据信息
const eqInfo = reactive<any>({}) // 用能信息
const alarmInfo = reactive({}) // 警告信息
const tableData = ref([]) 
const alarmData = ref([])
const prePowBtn = ref(0) // 当前所选的功率
const toggleTable = ref(false) //设备统计和告警统计的切换
const dialogVisible = ref(false) //全屏弹窗的显示隐藏
const echartOptionsPower = ref<EChartsOption>({}) //用来存储功率曲线图表的配置选项
const radioBtn = ref('pow')
const echartInfo = reactive<any>({}) //配置图表的数据系列
const roomId = ref<number>(0)
const screenValue = ref(0) //控制按钮的切换
const powBtns = [ // 功率 当天/当月等切换
  {
    name:'当天',
    backgroundColor: 'rgb(84, 112, 198)',
    param: 'todayEq',
    totalParam: 'todayEqTotal',
  },
  {
    name:'昨天',
    backgroundColor: 'rgb(145, 204, 117)',
    param: 'yesterdayEq',
    totalParam: 'yesterdayEqTotal',
  },
  {
    name:'上周',
    backgroundColor: 'rgb(250, 200, 88)',
    param: 'lastWeekEq',
    totalParam: 'lastWeekEqTotal',
  },
  {
    name:'上月',
    backgroundColor: 'rgb(238, 102, 102)',
    param: 'lastMonthEq',
    totalParam: 'lastMonthEqTotal',
  },
]
const oldPowList = ['yesterdayEqTotal','oldYesterdayEqTotal','oldLastWeekEqTotal','oldLastMonthEqTotal']
const powChartOptions = ref({
  tooltip: {
    trigger: 'item',
    formatter: function (param) {
      let result = ''
      result += param.name + ':' + param.value;
      if (param.name === '总视在功率') {
        result += 'kVA';
      } else if(param.name === '总有功功率') {
        result += 'kW'
      } else if(param.name === '总无功功率') {
        result += 'kVar'
      }
      
      return result.trimEnd(); // 去除末尾多余的换行符
    }
  },
  series: [
    {
      type: 'pie',
      radius: ['50%', '90%'],
      label: {
        show: false,
      },
      data: [
        { value: 0, name: '总有功功率', itemStyle: { color: '#E5B849' } },
        { value: 0, name: '总无功功率', itemStyle: { color: '#C8603A' } },
        { value: 0, name: '总视在功率', itemStyle: { color: '#AD3762' } },
      ]
    }
  ]
});

const alarmChartOptions = ref({
  tooltip: {
    trigger: 'item',
    formatter: '{b} : {c}',
    confine: true
  },
  series: [
    {
      type: 'pie',
      radius: '90%',
      label: {
        show: true,
        position: 'inside', // 将标签显示在饼图内部
        formatter: (params) => {
          return `${params.value}`;
        },
        fontSize: 14,
        color: '#fff',
        fontWeight: 'bold'
      },
      data: [
        { value: 0, name: '已确认', itemStyle: { color: '#E5B849' } },
        { value: 0, name: '未处理', itemStyle: { color: '#C8603A' } },
        { value: 0, name: '已挂起', itemStyle: { color: '#AD3762' } },
      ]
    }
  ]
});

const numChartOptions = ref({
  title: { text: ''},
  tooltip: { trigger: 'item',
    formatter: '{b} : {c}',
    confine: true},
  grid: {
    bottom: 20,
    left: 50
  },
  legend: {
    data: ['PDU', '始端箱','插接箱'], // 图例项
    selected: {
      'PDU': true,
      '始端箱': false,
      '插接箱': false
    }
  },
  xAxis: {
    type: 'category',nameLocation: 'end',
    data:['总数','在线','离线']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: 'PDU',
      type: 'bar',
      data: [40,20,30],
    },
    {
      name: '始端箱',
      type: 'bar',
      data: [10,30,20],
    },
    {
      name: '插接箱',
      type: 'bar',
      data: [10,30,20],
    }
  ]
})

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

//获取数据
const handleGetRoomId = (data) => {
  roomId.value = data
  getRoomEchartData()
  console.log('handleGetRoomId', data)
}

// 获取主页面设备数据
const getHomeDevData = async() => {
  const res =  await MachineHomeApi.getHomeDevData({})
  console.log('获取主页面设备数据', res)

  numChartOptions.value.series = [
    {
      name: 'PDU',
      type: 'bar',
      data: [res.pduNum,res.pduOnLine,res.pduOffLine],
    },
    {
      name: '始端箱',
      type: 'bar',
      data: [res.busNum,res.busOnLine,res.busOffLine],
    },
    {
      name: '插接箱',
      type: 'bar',
      data: [res.boxNum,res.boxOnLine,res.boxOffLine],
    }
  ]


  const arr = [] as any
  if (res.pduNum > 0) {
    arr.push({
      name: 'PDU',
      all: res.pduNum,
      on: res.pduOnLine,
      off: res.pduOffLine,
      error:'温度超阀值上限',
      box:'温湿度01',
      time:'2022-11-07 12:15:46'
    })
  }
  if (res.pduNum > 0) {
    arr.push({
      name: '始端箱',
      all: res.busNum,
      on: res.busOnLine,
      off: res.busOffLine,
      error:'制冷压力超阀值上限',
      box:'精密空调',
      time:'2022-11-07 12:15:46'
    })
  }
  if (res.pduNum > 0) {
    arr.push({
      name: '插接箱',
      all: res.boxNum,
      on: res.boxOnLine,
      off: res.boxOffLine,
      error:'检测到有水',
      box:'水浸',
      time:'2022-11-07 12:15:46'
    })
  }
    if (res.pduNum > 0) {
    arr.push({
      name: 'PDU-1',
      all: res.pduNum,
      on: res.pduOnLine,
      off: res.pduOffLine,
      error:'开门时间异常',
      box:'东门门禁',
      time:'2022-11-07 12:15:46'
    })
  }
  if (res.pduNum > 0) {
    arr.push({
      name: '始端箱-1',
      all: res.busNum,
      on: res.busOnLine,
      off: res.busOffLine,
      error:'设备离线',
      box:'摄像机1',
      time:'2022-11-07 12:15:46'
    })
  }
  if (res.pduNum > 0) {
    arr.push({
      name: '插接箱-1',
      all: res.boxNum,
      on: res.boxOnLine,
      off: res.boxOffLine,
      error:'检测到有人',
      box:'红外1',
      time:'2022-11-07 12:15:46'
    })
  }
    if (res.pduNum > 0) {
    arr.push({
      name: 'PDU-2',
      all: res.pduNum,
      on: res.pduOnLine,
      off: res.pduOffLine,
      error:'温度超阀值上限',
      box:'温湿度02',
      time:'2022-11-07 12:15:46'
    })
  }
  if (res.pduNum > 0) {
    arr.push({
      name: '始端箱-2',
      all: res.busNum,
      on: res.busOnLine,
      off: res.busOffLine,
      error:'温度超阀值上限',
      box:'温湿度03',
      time:'2022-11-07 12:15:46'
    })
  }
  if (res.pduNum > 0) {
    arr.push({
      name: '插接箱-2',
      all: res.boxNum,
      on: res.boxOnLine,
      off: res.boxOffLine,
      error:'温度超阀值上限',
      box:'温湿度04',
      time:'2022-11-07 12:15:46'
    })
  }
  tableData.value = arr
  if(tableData.value.length) {
    toggleTable.value = true
  }
  Object.assign(devInfo, res)
}
// 获取主页面功率数据
const getHomePowData = async() => {
  const res =  await MachineHomeApi.getHomePowData({})
  Object.assign(powInfo, res)
  powChartOptions.value.series = [
    {
      type: 'pie',
      radius: ['50%', '90%'],
      label: {
        show: false,
      },
      data: [
        { value: powInfo.totalPowActive, name: '总有功功率', itemStyle: { color: '#E5B849' } },
        { value: powInfo.totalPowReactive, name: '总无功功率', itemStyle: { color: '#C8603A' } },
        { value: powInfo.totalPowApparent, name: '总视在功率', itemStyle: { color: '#AD3762' } },
      ]
    }
  ]
  
  console.log('获取主页面功率数据', res)
}
// 获取主页面用能
const getHomeEqData = async() => {
  const res =  await MachineHomeApi.getHomeEqData({})

  console.log('222',res)
// const modifiedRoomEqList = (res.roomEqList && Array.isArray(res.roomEqList)) ? res.roomEqList.map(item => {
//   if (item && typeof item.name === 'string') {
//     return {
//       ...item,
//       name: item.name + '1'
//     };
//   }
//   return item; // 如果item不存在或name不是字符串，保持原样
// }) : [];

  res.roomEqList = [...res.roomEqList] //添加了模拟数据
  //.slice(0, 12)
  
  Object.assign(eqInfo, res)

  // let unitFlag = false

  // for(let i = 0;i < res.roomEqList.length;i++) {
  //   if(res.roomEqList[i].todayEq > 1000) {
  //     unitFlag = true
  //     break
  //   }
  // }

  // if(unitFlag) {
  //   Object.assign(eqOptionsData, {
  //     grid: {
  //       left: 30,
  //       right: 20,
  //       bottom: 20,
  //       containLabel: true
  //     },
  //     tooltip: {
  //       trigger: 'axis',
  //       axisPointer: {
  //         type: 'shadow'
  //       },
  //       formatter: function (params) {
  //         console.log('params', params)
  //         let result = '';
  //         params.forEach(function (item) {
  //           // item 是每一个系列的数据
  //           const seriesName = item.seriesName; // 系列名称
  //           const value = item.value; // 数据值
  //           const marker = item.marker; // 标志图形
  //           result += `${marker}${seriesName}: ${value}MW·h<br/>`;
  //         });
  //         return result;
  //       }
  //     },
  //     xAxis: {
  //       type: 'category',
  //       data: res.roomEqList ? res.roomEqList.map(item => item.name) : [],
  //       axisLabel: {
  //         interval: 0, // 显示所有标签，不设间隔
  //       },
  //     },
  //     yAxis: {
  //       type: 'value',
  //       name: "MW·h",
  //       nameTextStyle: {
  //         color: "#000",
  //         nameLocation: "start",
  //       },
  //     },
  //     series: [
  //       {
  //         name: '当天',
  //         data: res.roomEqList ? res.roomEqList.map(item => item.todayEq ? (item.todayEq/1000).toFixed(2) : '0.00') : [],
  //         type: 'bar',
  //         barWidth: 30, // 固定柱宽为 30 像素
  //         label: {
  //           show: true,
  //           position: 'top', // 顶部显示
  //           formatter: '{c}MW·h', // 显示数据值
  //         },
  //       },
  //     ]
  //   })
  // }
    Object.assign(eqOptionsData, {
      grid: {
        left: 30,
        right: 20,
        bottom: 20,
        containLabel: true
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: function (params) {
          console.log('params', params)
          let result = '';
          params.forEach(function (item) {
            // item 是每一个系列的数据
            const seriesName = item.seriesName; // 系列名称
            const value = item.value; // 数据值
            const marker = item.marker; // 标志图形
            result += `${marker}${seriesName}: ${value}kW·h<br/>`;
          });
          return result;
        }
      },
      xAxis: {
        type: 'category',
        data: res.roomEqList ? res.roomEqList.map(item => item.name) : [],
        axisLabel: {
          interval: 0, // 显示所有标签，不设间隔
        },
      },
      yAxis: {
        type: 'value',
        name: "kW·h",
        nameTextStyle: {
          color: "#000",
          nameLocation: "start",
        },
      },
      series: [
        {
          name: '当天',
          data: res.roomEqList ? res.roomEqList.map(item => item.todayEq ? item.todayEq.toFixed(2) : '0.00') : [],
          type: 'bar',
          barWidth: 30, // 固定柱宽为 30 像素
          label: {
            show: true,
            position: 'top', // 顶部显示
          },
        },
      ]
    })
  
  console.log('获取主页面用能', res)
}
// 获取未处理告警数量
const getHomeAlarmData = async() => {
  const res =  await MachineHomeApi.getHomeAlarmData({})
  Object.assign(alarmInfo, res)
  alarmChartOptions.value.series = [
    {
      type: 'pie',
      radius: '90%',
      label: {
        show: true,
        position: 'inside', // 将标签显示在饼图内部
        formatter: (params) => {
          return `${params.value}`;
        },
        fontSize: 14,
        color: '#fff',
        fontWeight: 'bold'
      },
      data: [
        { value: alarmInfo.confirm, name: '已确认', itemStyle: { color: '#E5B849' } },
        { value: alarmInfo.untreated, name: '未处理', itemStyle: { color: '#C8603A' } },
        { value: alarmInfo.hung, name: '已挂起', itemStyle: { color: '#AD3762' } },
      ]
    }
  ]
  console.log('获取未处理告警数量', res)

  //获取告警轮播信息
  const res2 = await AlarmApi.getAlarmRecord({
    pageNo: 1,
    pageSize: 30,
    a: 0,
    status: [0]
  })
  if (res2.list) {
    alarmData.value = res2.list
  }
}

const switchPowBtn = (index) => {
  prePowBtn.value = index

  let unitFlag = false

  // for(let i = 0;i < eqInfo.roomEqList.length;i++) {
  //   if(eqInfo.roomEqList[i][powBtns[index].param] > 1000) {
  //     unitFlag = true
  //     break
  //   }
  // }

  // if(unitFlag) {
  //   eqOptionsData.series = [{
  //     name: powBtns[index].name,
  //     data: eqInfo.roomEqList ? eqInfo.roomEqList.map(item => item[powBtns[index].param] ? (item[powBtns[index].param]/1000).toFixed(2) : '0.00') : [],
  //     type: 'bar',
  //     barWidth: 30, // 固定柱宽为 30 像素
  //     label: {
  //       show: true,
  //       position: 'top', // 顶部显示
  //       formatter: '{c}MW·h', // 显示数据值
  //     },
  //     itemStyle: {
  //       color: powBtns[index].backgroundColor
  //     }
  //   }]
  //   eqOptionsData.yAxis = {
  //     type: 'value',
  //     name: "MW·h",
  //     nameTextStyle: {
  //       color: "#000"
  //     }
  //   }
  // }
    eqOptionsData.series = [{
      name: powBtns[index].name,
      data: eqInfo.roomEqList ? eqInfo.roomEqList.map(item => item[powBtns[index].param] ? item[powBtns[index].param].toFixed(2) : '0.00') : [],
      type: 'bar',
      barWidth: 30, // 固定柱宽为 30 像素
      label: {
        show: true,
        position: 'top', // 顶部显示
      },
      itemStyle: {
        color: powBtns[index].backgroundColor
      }
    }]
    eqOptionsData.yAxis = {
      type: 'value',
      name: "kW·h",
      nameTextStyle: {
        color: "#000"
      }
    }
}

const getAllApi = async () => {
  await Promise.all([
    getHomeDevData(),
    getHomePowData(),
    getHomeEqData(),
    getHomeAlarmData()
  ])
  loading.value = false
}

getAllApi()

const computedEnInfo = computed(() => {
  if(eqInfo.roomEqList?.length > 12){
    return {
      overflowX:'auto',
    }
  }
})

const computedEnInfoWidth = computed(() => {
  let num = Math.floor(eqInfo.roomEqList.length / 10) + 1
  num = num * 20 + 45
  if(eqInfo.roomEqList.length > 10){
    return {
      width:num+'vw',
    }
  }
})

const handleClose = () => {
  // 弹窗关闭时执行的逻辑
  console.log('弹窗已关闭');
}

const handleCancel = () =>  {
  // 取消按钮点击时执行的逻辑
  dialogVisible.value = false;
  console.log('已取消');
}

const handleConfirm = () => {
  // 确定按钮点击时执行的逻辑
  dialogVisible.value = false;
  console.log('已确定');
}

const scrollableContainer = ref(null); // 挂载到机房状态
const scrollableContainerOne = ref(null); // 挂载到设备模块
 
let scrollInterval; // 机房状态的定时器
let scrollIntervalOne; // 设备模块的定时器
let scrollTimeout; // 用于检测滚动是否停止的延迟定时器
let isScrollingManually = false; // 标记是否正在手动滚动
 
const startScrolling = () => {
  // 检查是否已经有一个定时器在运行
  if (scrollInterval || scrollIntervalOne || isScrollingManually) return;
 
  scrollInterval = setInterval(() => {
    // 机房状态的滚动逻辑
    scrollContainer('scrollableContainer');
  }, 1000);
 
  scrollIntervalOne = setInterval(() => {
    // 设备模块的滚动逻辑
    scrollContainer('scrollableContainerOne');
  }, 1000);
};
 
const scrollContainer = (containerName) => {
  let containerRef, interval;
  if (containerName === 'scrollableContainer') {
    containerRef = scrollableContainer;
    interval = scrollInterval;
  } else if (containerName === 'scrollableContainerOne') {
    containerRef = scrollableContainerOne;
    interval = scrollIntervalOne;
  }

  // 检查 containerRef.value 是否存在，用来解决控制台报错
  if (!containerRef?.value) {
    return; // 可以返回一个特定的值或对象来表示错误
  }
 
  const { scrollTop, clientHeight, scrollHeight } = containerRef.value;
  const scrollStep = 10; // 滚动步长
  const scrollTolerance = -10; // 停止前的容忍范围
 
  if (scrollTop + clientHeight >= scrollHeight) {
    // 滚动到顶部
    containerRef.value.scrollTop = 0;
  } else if (scrollTop + scrollStep + scrollTolerance >= scrollHeight - clientHeight) {
    // 接近底部时停止定时器
    clearInterval(interval);
    if (containerName === 'scrollableContainer') {
      scrollInterval = null;
    } else {
      scrollIntervalOne = null;
    }
  } else {
    // 继续滚动
    containerRef.value.scrollTop += scrollStep;
  }
};
 
const stopScrolling = () => {
  clearInterval(scrollInterval);
  scrollInterval = null;
  clearInterval(scrollIntervalOne);
  scrollIntervalOne = null;
};
 
const handleScroll = (event, containerName) => {
  // 停止自动滚动
  isScrollingManually = true;
  stopScrolling();
 
  // 设置延迟来判断滚动是否停止
  clearTimeout(scrollTimeout);
  scrollTimeout = setTimeout(() => {
    // 如果在延迟期间没有新的滚动事件，则恢复自动滚动
    isScrollingManually = false;
    startScrolling();
  }, 1000); // 延迟时间，单位毫秒，可以根据需要调整
};

onMounted(() => {
  // 添加滚动事件监听器
  if (scrollableContainer.value) {
    scrollableContainer.value.addEventListener('scroll', (event) => handleScroll(event, 'scrollableContainer'));
  }
  if (scrollableContainerOne.value) {
    scrollableContainerOne.value.addEventListener('scroll', (event) => handleScroll(event, 'scrollableContainerOne'));
  }
 
  // 初始启动自动滚动
  startScrolling();
});
 
onUnmounted(() => {
  // 移除滚动事件监听器
  if (scrollableContainer.value) {
    scrollableContainer.value.removeEventListener('scroll', (event) => handleScroll(event, 'scrollableContainer'));
  }
  if (scrollableContainerOne.value) {
    scrollableContainerOne.value.removeEventListener('scroll', (event) => handleScroll(event, 'scrollableContainerOne'));
  }
 
  // 确保在组件卸载时清除定时器
  stopScrolling();
});

</script>

<style lang="scss" scoped>
.scroll-contentX{
  width: 50vw;
  overflow-x: auto; /* 允许横向滚动 */
  white-space: nowrap; /* 防止内容换行 */
}

.full-screen-item{
  display: inline-block;
  height: 16vh;
  width: 19vw;
}

:deep(.el-dialog) {
  margin-top: 0;
  height: 100%;
  width: 100%;
}


@media screen and (min-width:2048px){
  .temperature-container{
    height: 400px;
    overflow-y:auto;
  }

  .scrollable-container {
    height: 390px;
    overflow-y:auto;
  }
 
  .scroll-item {
    display: inline-block;
    height: 130px;
    width: 320px;
    margin-left: 10px;
  }

  .name-column {
    width: 150px;
  }
  
  .numeric-column {
    width: 80px;
  }
}

@media screen and (max-width:2048px) and (min-width:1600px){
  .temperature-container{
    height: 260px;
    overflow-y:auto;
  }

  .scrollable-container {
    height: 260px;
    overflow-y:auto;
  }
 
  .scroll-item {
    display: inline-block;
    height: 130px;
    width: 290px;
    margin-left: 10px;
  }

  .scrollable-container-one{
    height: 20.5vh;
    width: 20vw;
    overflow-y:auto;
  }

  .name-column {
    width: 127px;
  }
  
  .numeric-column {
    width: 80px;
  }
}

@media screen and (max-width:1600px){
  .temperature-container{
    height: 47vh;
    overflow-y:auto;
  }

  .scrollable-container {
    height: 33vh;
    overflow-y:auto;
  }
 
  .scroll-item {
    display: inline-block;
    height: 130px;
    width: 310px;
    margin-left: 10px;
  }

  .scrollable-container-one{
    height: 30vh;
    width: 20vw;
    overflow-y: auto;
  }
}

.center {
  height: 40vh;
  width: 100%;
  .CabEchart{
    position: relative;
    .btns {
      position: absolute;
      z-index: 9;
      right: 30px;
      top: 20px;
    }
  }
}

.bullet {
  display: inline-block;
  margin-right: 5px;
  border-radius: 50%;
  width: 10px;
  height: 10px;
}

.centerCope {
  height: 70vh;
  width: 100%;
  .CabEchart{
    position: relative;
    .btns {
      position: absolute;
      z-index: 9;
      right: 30px;
      top: 20px;
    }
  }
}

.roomPowerBtns {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 5px;
}

.percentage-value {
  display: block;
  margin-top: 10px;
  font-size: 25px;
}
.percentage-label {
  display: block;
  margin-top: 10px;
  font-size: 12px;
}
.percentage-unit {
  display: block;
  font-size: 10px;
}

:deep(.CabEchart) {
  width: 100%;
  height: 100%;
  & > div {
    width: 100%;
    height: 100%;
    box-sizing: border-box;
    padding: 5 20px;
  }
}

:deep(.el-card__body) {
  padding: 15px;
}

</style>
