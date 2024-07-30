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
                  PowerMater系统安全守护第 {{devInfo.days}} 天
                </div>
                <div class="mt-10px text-14px text-gray-500">
                  今日一切正常
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
            <el-link type="primary" :underline="false" @click="roomShowType = !roomShowType">切换</el-link>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row>
            <template v-if="roomShowType">
              <el-col
                v-for="(item, index) in powInfo.roomDataList"
                :key="`card-${index}`"
                :xl="6"
                :lg="6"
                :md="6"
                :sm="24"
                :xs="24"
              >
                <el-card shadow="hover">
                  <div class="flex items-center">
                    <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                    <span class="text-16px">{{ item.name }}</span>
                  </div>
                  <div class="mt-14px text-14px text-gray-400">实时总功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW</div>
                  <div class="mt-14px flex justify-between text-12px text-gray-400">
                    <span class="text-14px">实时视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA</span>
                    <span>{{ formatTime(new Date(), 'HH:mm:ss') }}</span>
                  </div>
                </el-card>
              </el-col>
            </template>
            <template v-else>
              <el-col 
                :xl="24"
                :lg="24"
                :md="24"
                :sm="24"
                :xs="24">
                <Echart :options="powOptionsData" :height="258" />
              </el-col>
            </template>
          </el-row>
        </el-skeleton>
      </el-card>

      <el-card shadow="never" class="mt-8px">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>用电分布</span>
            <!-- <el-link type="primary" :underline="false" @click="roomShowType = !roomShowType">切换</el-link> -->
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
                  <div class="w-full h-210px flex" style="flex-direction: column; align-items: center;justify-content: center;">
                    <div>总用电量</div>
                    <div>{{eqInfo[powBtns[prePowBtn].totalParam]}}kW·h</div>
                  </div>
                </el-skeleton>
              </el-card>
            </el-col>
            <el-col :xl="18" :lg="18" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover">
                <el-skeleton :loading="loading" animated>
                  <Echart :options="eqOptionsData" :height="210" />
                </el-skeleton>
              </el-card>
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
            <el-col :span="10" class="flex justify-center">
              <el-progress type="circle" :percentage="100">
                <template #default>
                  <div class="percentage-value text-28px mt-12px">{{alarmInfo.total}}</div>
                  <div class="percentage-label text-12px mt-12px">告警数量</div>
                </template>
              </el-progress>
            </el-col>
            <el-col :span="12" class="flex flex-col justify-evenly">
              <div>未处理告警数目：{{alarmInfo.untreated}}</div>
              <div>已挂起告警数目：{{alarmInfo.hung}}</div>
              <div>已确认告警数目：{{alarmInfo.confirm}}</div>
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
            <el-col :span="10" class="flex justify-center">
              <el-progress type="circle" :percentage="100">
                <template #default>
                  <div class="percentage-value text-28px mt-12px">{{powInfo.totalPowActive}}kW</div>
                  <div class="percentage-label text-12px mt-12px">总功率</div>
                </template>
              </el-progress>
            </el-col>
            <el-col :span="12" class="flex flex-col justify-evenly">
              <div>总有功功率：{{powInfo.totalPowActive}}kW</div>
              <div>总无功功率：{{powInfo.totalPowReactive}}kVar</div>
              <div>总视在功率：{{powInfo.totalPowApparent}}kVA</div>
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>
      <el-card shadow="never" class="mb-8px">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>设备统计</span>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-table :data="tableData" style="width: 100%" border class="text-12px">
            <el-table-column prop="name" label=""  />
            <el-table-column prop="all" label="总数"  />
            <el-table-column prop="on" label="在线" />
            <el-table-column prop="off" label="离线" />
          </el-table>
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

defineOptions({ name: 'Home' })

const { t } = useI18n()
const userStore = useUserStore()
const { setWatermark } = useWatermark()
const loading = ref(true)
const roomShowType = ref(true)
const avatar = userStore.getUser.avatar
const username = userStore.getUser.nickname
const eqOptionsData = reactive<EChartsOption>({}) as EChartsOption
const powOptionsData = reactive<EChartsOption>({}) as EChartsOption
const devInfo = reactive({}) // 设备信息
const powInfo = reactive({}) // 功率数据信息
const eqInfo = reactive<any>({}) // 用能信息
const alarmInfo = reactive({}) // 警告信息
const tableData = ref([]) // 
const prePowBtn = ref(0) // 当前所选的功率
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

// 获取主页面设备数据
const getHomeDevData = async() => {
  const res =  await MachineHomeApi.getHomeDevData({})
  console.log('获取主页面设备数据', res)
  const arr = [] as any
  if (res.pduNum > 0) {
    arr.push({
      name: 'PDU',
      all: res.pduNum,
      on: res.pduOnLine,
      off: res.pduOffLine,
    })
  }
  if (res.pduNum > 0) {
    arr.push({
      name: '始端箱',
      all: res.busNum,
      on: res.busOnLine,
      off: res.busOffLine,
    })
  }
  if (res.pduNum > 0) {
    arr.push({
      name: '插接箱',
      all: res.boxNum,
      on: res.boxOnLine,
      off: res.boxOffLine,
    })
  }
  tableData.value = arr
  Object.assign(devInfo, res)
}
// 获取主页面功率数据
const getHomePowData = async() => {
  const res =  await MachineHomeApi.getHomePowData({})
  Object.assign(powInfo, res)
  Object.assign(powOptionsData, {
    grid: {
      left: 50,
      right: 20,
      bottom: 20
    },
    legend: {
      right: 10,
      selectedMode: 'single'
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
          let unit = ''
          if (seriesName == '有功功率') {
            unit = 'kW'
          } else if (seriesName == '无功功率') {
            unit = 'kVar'
          } else if (seriesName == '视在功率') {
            unit = 'kVA'
          }
          result += `${marker}${seriesName}: ${value}${unit}<br/>`;
        });
        return result;
      }
    },
    xAxis: {
      type: 'category',
      data: res.roomDataList.map(item => item.name)
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '有功功率',
        data: res.roomDataList.map(item => item.powActive),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kW', // 显示数据值
        },
      },
      {
        name: '无功功率',
        data: res.roomDataList.map(item => item.powReactive),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kVar', // 显示数据值
        },
      },
      {
        name: '视在功率',
        data: res.roomDataList.map(item => item.powApparent),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kVA', // 显示数据值
        },
      },
      {
        name: '功率因素',
        data: res.roomDataList.map(item => item.powerFactor),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
    ]
  })
  console.log('获取主页面功率数据', res)
}
// 获取主页面用能
const getHomeEqData = async() => {
  const res =  await MachineHomeApi.getHomeEqData({})
  Object.assign(eqInfo, res)
  Object.assign(eqOptionsData, {
    grid: {
      left: 50,
      right: 20,
      bottom: 20
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
      data: res.roomEqList.map(item => item.name)
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
        data: res.roomEqList.map(item => item.yesterdayEq),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kW·h', // 显示数据值
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
  console.log('获取未处理告警数量', res)
}
//
const switchPowBtn = (index) => {
  prePowBtn.value = index
  eqOptionsData.series = [{
    name: powBtns[index].name,
    data: eqInfo.roomEqList.map(item => item[powBtns[index].param]),
    type: 'bar',
    label: {
      show: true,
      position: 'top', // 顶部显示
      formatter: '{c}kW·h', // 显示数据值
    },
    itemStyle: {
      color: powBtns[index].backgroundColor
    }
  }]
}

const getAllApi = async () => {
  await Promise.all([
    getHomeDevData(),
    getHomePowData(),
    getHomeEqData(),
    getHomeAlarmData(),
  ])
  loading.value = false
}

getAllApi()
</script>
