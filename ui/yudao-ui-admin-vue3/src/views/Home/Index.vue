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
            <el-button @click="switchValue = 0;" :type="switchValue === 0 ? 'primary' : ''" style="margin-left:0px;margin-right:0vw;" size="small">机房功率</el-button>
            <el-button @click="switchValue = 1;" :type="switchValue === 1 ? 'primary' : ''" style="margin-left:-39vw;margin-right:0vw;" size="small">机房温度</el-button>
            <el-button @click="switchValue = 2;" :type="switchValue === 2 ? 'primary' : ''" style="margin-left:-39vw;margin-right:-1vw;" size="small">机房对比</el-button>
            <el-button @click="switchValue = 3;" :type="switchValue === 3 ? 'primary' : ''" style="margin-left:-38vw;margin-right:0vw;" size="small">实时曲线</el-button>
            <!--<el-button @click="switchValue = 2;" :type="switchValue === 2 ? 'primary' : ''" style="margin-left:0px;margin-right:-45vw;" size="small">图表</el-button>-->
            <el-button @click="dialogVisible = true" :type="toggleButton ? 'primary' : ''" style="margin-left:-39vw;margin-right:0vw;" size="small">全屏</el-button>
            <el-dialog
              v-model="dialogVisible"
              @close="handleClose"
            >       
              <!-- 自定义的头部内容（可选） -->
              <template #header>
                <div class="custom-header">
                  <span style="margin-right:70vw;">机房状态全屏显示</span>
                  <el-button @click="switchValue = 0;" :type="switchValue === 0 ? 'primary' : ''" size="small">机房功率</el-button>
                  <el-button @click="switchValue = 1;" :type="switchValue === 1 ? 'primary' : ''" size="small">机房温度</el-button>
                  <el-button @click="switchValue = 2;" :type="switchValue === 2 ? 'primary' : ''" size="small">机房对比</el-button>
                  <el-button @click="switchValue = 3;" :type="switchValue === 3 ? 'primary' : ''" size="small">实时曲线</el-button>
                </div>
              </template>

              <!-- 自定义的主要内容 -->
              <div class="custom-content">
                <el-row>
                  <template v-if="switchValue === 0">
                    <el-col>
                      <template>
                        <div 
                          class="full-screen-item"
                          v-for="(item, index) in powInfo.roomDataList"
                          :key="`card-${index}`"
                        >
                          <el-card shadow="hover">
                            <div class="flex items-center h-21px">
                              <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                              <span class="text-15px">{{ item.name || '' }}</span>
                              <span class="text-15px" style="margin-left:5vw;">PUE：1.5</span>
                            </div>
                            <div class="mt-14px text-14px text-gray-400">实时总功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW</div>
                            <div class="mt-14px flex justify-between text-12px text-gray-400">
                              <span class="text-14px">实时视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA</span>
                              <span>{{ formatTime(new Date(), 'HH:mm:ss') }}</span>
                            </div>
                          </el-card>
                        </div>
                      </template>
                    </el-col>
                  </template>
                  <template v-else-if="switchValue === 1">
                    <el-col>
                      <div 
                        class="full-screen-item"
                        v-for="(item, index) in powInfo.roomDataList"
                        :key="`card-${index}`"
                      >
                        <el-card shadow="hover">
                          <div class="flex items-center h-21px">
                            <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                            <span class="text-16px">{{ item.name || '' }}</span>
                            <span class="text-15px" style="margin-left:5vw;">PUE：1.5</span>
                          </div>
                          <div class="mt-14px flex justify-between text-12px text-gray-400">
                            <span style="margin-left:3vw">冷通道</span>
                            <span style="margin-right:2vw">热通道</span>
                          </div>
                          <div class="mt-14px flex justify-between text-12px text-gray-400">
                            <span class="text-12px">最高温度：{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</span>
                            <span class="text-12px" style="margin-right:2vw">{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</span>
                          </div>
                          <div class="mt-14px flex justify-between text-12px text-gray-400">
                            <span class="text-12px">平均温度：{{item.powActive ? item.powActive.toFixed(1) : '0.0'}}&deg;C</span>
                            <span class="text-12px" style="margin-right:2vw">{{item.powActive ? item.powActive.toFixed(1) : '0.0'}}&deg;C</span>
                          </div>
                          <div class="mt-14px flex justify-between text-12px text-gray-400">
                            <span>时间：{{ formatTime(new Date(), 'HH:mm:ss') }}</span>
                          </div>
                        </el-card>
                      </div>
                    </el-col>
                  </template>
                  <template v-else-if="switchValue === 2">
                    <el-col>
                      <Echart :options="powOptionsData" :height="400" />
                    </el-col>
                  </template>
                  <template v-if="switchValue === 3">
                    <el-col>
                      <div 
                        class="centerCope"
                      >
                        <CabTopology v-show="false" :containerInfo="containerInfo" :isFromHome="true" @back-data="handleBackData" @getroomid="handleGetRoomId" />
                        <ContentWrap class="CabEchart">
                          <Echart :options="echartOptionsPower" height="60vh" width="100%" />
                          <div class="btns">
                            <el-button class="btn" size="small" :plain="!(radioBtn == 'pow')" type="primary" @click="switchTrend('pow')">功率</el-button>
                            <el-button class="btn" size="small" :plain="!(radioBtn == 'ele')" type="primary" @click="switchTrend('ele')">用能</el-button>
                          </div>
                        </ContentWrap>
                      </div>
                    </el-col>
                  </template>
                </el-row>
              </div>
            </el-dialog>
          </div>
        </template>
        <el-skeleton :loading="loading" animated v-if="switchValue===0">
            <el-row>
              <template v-if="roomShowType">
                <el-col>
                  <div ref="scrollableContainer" class="scrollable-container" @scroll="handleScroll">
                    <div 
                      class="scroll-item"
                      v-for="(item, index) in powInfo.roomDataList"
                      :key="`card-${index}`"
                    >
                      <el-card shadow="hover">
                        <div class="flex items-center h-21px">
                          <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                          <span class="text-15px">{{ item.name || '' }}</span>
                          <span class="text-15px" style="margin-left:5vw;">PUE：1.5</span>
                        </div>
                        <div class="mt-14px text-14px text-gray-400">实时总功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW</div>
                        <div class="mt-14px flex justify-between text-12px text-gray-400">
                          <span class="text-14px">实时视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA</span>
                          <span>{{ formatTime(new Date(), 'HH:mm:ss') }}</span>
                        </div>
                      </el-card>
                    </div>
                  </div>
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
        <el-skeleton :loading="loading" animated v-else-if="switchValue===1">
            <el-row>
              <template v-if="roomShowType">
                <el-col>
                  <div ref="scrollableContainer" class="temperature-container" @scroll="handleScroll">
                    <div 
                      class="scroll-item"
                      v-for="(item, index) in powInfo.roomDataList"
                      :key="`card-${index}`"
                    >
                      <el-card shadow="hover">
                        <div class="flex items-center h-21px">
                          <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                          <span class="text-16px">{{ item.name || '' }}</span>
                          <span class="text-15px" style="margin-left:5vw;">PUE：1.5</span>
                        </div>
                        <div class="mt-14px flex justify-between text-12px text-gray-400">
                          <span style="margin-left:3vw">冷通道</span>
                          <span style="margin-right:2vw">热通道</span>
                        </div>
                        <div class="mt-14px flex justify-between text-12px text-gray-400">
                          <span class="text-12px">最高温度：{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</span>
                          <span class="text-12px" style="margin-right:2vw">{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</span>
                        </div>
                        <div class="mt-14px flex justify-between text-12px text-gray-400">
                          <span class="text-12px">平均温度：{{item.powActive ? item.powActive.toFixed(1) : '0.0'}}&deg;C</span>
                          <span class="text-12px" style="margin-right:2vw">{{item.powActive ? item.powActive.toFixed(1) : '0.0'}}&deg;C</span>
                        </div>
                        <div class="mt-14px flex justify-between text-12px text-gray-400">
                          <span>时间：{{ formatTime(new Date(), 'HH:mm:ss') }}</span>
                        </div>
                      </el-card>
                    </div>
                  </div>
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
        <el-skeleton :loading="loading" animated v-else-if="switchValue===2">
            <el-row>
              <template v-if="roomShowType">
                <el-col 
                  :xl="24"
                  :lg="24"
                  :md="24"
                  :sm="24"
                  :xs="24">
                  <Echart :options="powOptionsData" :height="400" />
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
        <el-skeleton :loading="loading" animated v-else-if="switchValue===3">
            <el-row>
              <template v-if="roomShowType">
                <el-col>
                  <div 
                    class="center"
                  >
                    <CabTopology v-show="false" :containerInfo="containerInfo" :isFromHome="true" @back-data="handleBackData" @getroomid="handleGetRoomId" />
                    <ContentWrap class="CabEchart">
                      <Echart :options="echartOptionsPower" height="30vh" width="100%" />
                      <div class="btns">
                        <el-button class="btn" size="small" :plain="!(radioBtn == 'pow')" type="primary" @click="switchTrend('pow')">功率</el-button>
                        <el-button class="btn" size="small" :plain="!(radioBtn == 'ele')" type="primary" @click="switchTrend('ele')">用能</el-button>
                      </div>
                    </ContentWrap>
                  </div>
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
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
                  <div class="w-full h-210px flex" style="flex-direction: column; align-items: center;justify-content: center;">
                    <div>总用电量</div>
                    <div>{{eqInfo[powBtns[prePowBtn].totalParam] ? eqInfo[powBtns[prePowBtn].totalParam].toFixed(2) : '0.00'}}kW·h</div>
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
            <span>实时功率</span>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row class="flex justify-between">
            <el-col :span="10" class="flex justify-center">
              <el-progress type="circle" :percentage="100">
                <template #default>
                  <div class="percentage-value text-28px mt-12px">1.5</div>
                  <div class="percentage-label text-12px mt-12px">PUE</div>
                </template>
              </el-progress>
            </el-col>
            <el-col :span="12" class="flex flex-col justify-evenly">
              <div>总有功功率：{{powInfo.totalPowActive ? powInfo.totalPowActive.toFixed(3) : '0.000'}}kW</div>
              <div>总无功功率：{{powInfo.totalPowReactive ? powInfo.totalPowReactive.toFixed(3) : '0.000'}}kVar</div>
              <div>总视在功率：{{powInfo.totalPowApparent ? powInfo.totalPowApparent.toFixed(3) : '0.000'}}kVA</div>
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>
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
      <el-card shadow="never" class="mb-8px" v-if="toggleTable===false">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>设备统计/告警统计</span>
            <el-link @click="toggleTable = !toggleTable" type="primary">切换</el-link>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <div ref="scrollableContainerOne" class="scrollable-container-one" @scroll="handleScroll">
            <el-table :data="tableData" style="width: 100%" border class="text-12px">
              <el-table-column prop="name" label=""  />
              <el-table-column prop="all" label="总数"  />
              <el-table-column prop="on" label="在线" />
              <el-table-column prop="off" label="离线" />
            </el-table>
          </div>
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
import { MachineRoomApi } from '@/api/cabinet/room'


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
const powOptionsDataOne = reactive<EChartsOption>({}) as EChartsOption
const devInfo = reactive({}) // 设备信息
const powInfo = reactive({}) // 功率数据信息
const powCopyInfo = reactive({})
const eqInfo = reactive<any>({}) // 用能信息
const alarmInfo = reactive({}) // 警告信息
const tableData = ref([]) // 
const prePowBtn = ref(0) // 当前所选的功率
const switchValue = ref(0) //控制按钮的切换
const toggleTable = ref(false) //设备统计和告警统计的切换
const toggleButton = ref(false) //全屏按钮的样式切换
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
  Object.assign(devInfo, res)
}
// 获取主页面功率数据
const getHomePowData = async() => {
  const res =  await MachineHomeApi.getHomePowData({})
  Object.assign(powInfo, res)
  Object.assign(powCopyInfo, res)

  const modifiedRoomEqList = powInfo.roomDataList.map(item => ({
    ...item, // 复制对象的所有属性
    name: item.name + '1' // 修改name属性，在后面加上'*'号
  }))

  powInfo.roomDataList = [...powInfo.roomDataList,...modifiedRoomEqList] //添加了模拟数据

  console.log('111',powInfo.roomDataList)
  
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
      data: powInfo.roomDataList.map(item => item.name)
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '有功功率',
        data: powInfo.roomDataList.map(item => item.powActive),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kW', // 显示数据值
        },
      },
      {
        name: '无功功率',
        data: powInfo.roomDataList.map(item => item.powReactive),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kVar', // 显示数据值
        },
      },
      {
        name: '视在功率',
        data: powInfo.roomDataList.map(item => item.powApparent),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kVA', // 显示数据值
        },
      },
      {
        name: '功率因素',
        data: powInfo.roomDataList.map(item => item.powerFactor),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
    ]
  })
  Object.assign(powOptionsDataOne, {
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
          if (seriesName == '最高温度' || seriesName == '最低温度' || seriesName == '目前温度' || seriesName == '平均温度') {
            unit = '℃'
          }
          result += `${marker}${seriesName}: ${value}${unit}<br/>`;
        });
        return result;
      }
    },
    xAxis: {
      type: 'category',
      data: powInfo.roomDataList.map(item => item.name)
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '最低温度',
        data: powInfo.roomDataList.map(item => item.powActive.toFixed(1)),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}℃', // 显示数据值
        },
      },
      {
        name: '平均温度',
        data: powInfo.roomDataList.map(item => item.powReactive.toFixed(1)),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}℃', // 显示数据值
        },
      },
      {
        name: '最高温度',
        data: powInfo.roomDataList.map(item => item.powApparent.toFixed(1)),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}℃', // 显示数据值
        },
      },
      {
        name: '目前温度',
        data: powInfo.roomDataList.map(item => item.powerFactor.toFixed(1)),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}℃', // 显示数据值
        },
      },
    ]
  })
  console.log('获取主页面功率数据', res)
}
// 获取主页面用能
const getHomeEqData = async() => {
  const res =  await MachineHomeApi.getHomeEqData({})

  const modifiedRoomEqList = res.roomEqList.map(item => ({
    ...item, // 复制对象的所有属性
    name: item.name + '1' // 修改name属性，在后面加上'*'号
  }))

  res.roomEqList = [...res.roomEqList, ...modifiedRoomEqList] //添加了模拟数据
  //.slice(0, 12)
  
  Object.assign(eqInfo, res)
  Object.assign(eqOptionsData, {
    grid: {
      left: 30,
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

const switchPowBtn = (index) => {
  prePowBtn.value = index
  eqOptionsData.series = [{
    name: powBtns[index].name,
    data: eqInfo.roomEqList ? eqInfo.roomEqList.map(item => item[powBtns[index].param] ? item[powBtns[index].param].toFixed(2) : '0.00') : [],
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
    getHomeAlarmData()
  ])
  loading.value = false
}

getAllApi()

const computedEnInfo = computed(() => {
  if(eqInfo.roomEqList.length > 12){
    return {
      overflowX:'auto',
    }
  }
})

const computedEnInfoWidth = computed(() => {
  let num = Math.floor(eqInfo.roomEqList.length / 12) + 1
  num = num * 20 + 45
  if(eqInfo.roomEqList.length > 12){
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
  }, 100);
 
  scrollIntervalOne = setInterval(() => {
    // 设备模块的滚动逻辑
    scrollContainer('scrollableContainerOne');
  }, 100);
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
    height: 52vh;
    overflow-y:auto;
  }

  .scrollable-container {
    height: 36vh;
    overflow-y:auto;
  }
 
  .scroll-item {
    display: inline-block;
    height: 14vh;
    width: 15vw;
    margin-left: 1vh;
    margin-bottom: -2vw;
  }
}

@media screen and (max-width:2048px) and (min-width:1600px){
  .temperature-container{
    height: 38vh;
    overflow-y:auto;
  }

  .scrollable-container {
    height: 28vh;
    overflow-y:auto;
  }
 
  .scroll-item {
    display: inline-block;
    height: 14vh;
    width: 15vw;
  }

  .scrollable-container-one{
    height: 20vh;
    width: 20vw;
    overflow-y:auto;
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
    height: 14vh;
    width: 20vw;
    margin-left: 2vh;
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
</style>
