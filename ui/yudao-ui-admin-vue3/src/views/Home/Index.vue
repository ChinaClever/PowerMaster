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
                  PowerMater系统安全守护第 0203 天
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
                <span class="text-20px">1200</span>
              </div>
              <el-divider direction="vertical" />
              <div class="px-8px text-right">
                <div class="mb-16px text-14px text-gray-400">已开通</div>
                <span class="text-20px">1199</span>
              </div>
              <el-divider direction="vertical" border-style="dashed" />
              <div class="px-8px text-right">
                <div class="mb-16px text-14px text-gray-400">未启用</div>
                <span class="text-20px">1</span>
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
            <span>机房</span>
            <el-link type="primary" :underline="false">切换</el-link>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row>
            <template v-if="true">
              <el-col
                v-for="(item, index) in projects"
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
                  <div class="mt-14px text-14px text-gray-400">{{ t(item.message) }}</div>
                  <div class="mt-14px flex justify-between text-12px text-gray-400">
                    <span class="text-14px">{{ item.personal }}</span>
                    <span>{{ formatTime(item.time, 'HH:mm:ss') }}</span>
                  </div>
                </el-card>
              </el-col>
            </template>
            <el-col v-else
              :xl="24"
              :lg="24"
              :md="24"
              :sm="24"
              :xs="24">
              <Echart :options="barOptionsData" :height="210" />
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>

      <el-card shadow="never" class="mt-8px">
        <template #header>
          <div class="h-3">用电</div>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row :gutter="20" justify="space-between">
            <el-col :xl="6" :lg="6" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover">
                <el-skeleton :loading="loading" animated>
                  <div class="w-full h-210px flex" style="flex-direction: column; align-items: center;justify-content: center;">
                    <div>总用电量</div>
                    <div>100kW·h</div>
                  </div>
                </el-skeleton>
              </el-card>
            </el-col>
            <el-col :xl="18" :lg="18" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover">
                <el-skeleton :loading="loading" animated>
                  <Echart :options="barOptionsData" :height="210" />
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
                  <div class="percentage-value text-28px mt-12px">{{ 202 }}</div>
                  <div class="percentage-label text-12px mt-12px">告警数量</div>
                </template>
              </el-progress>
            </el-col>
            <el-col :span="12" class="flex flex-col justify-evenly">
              <div>未处理告警数目：5</div>
              <div>已挂起告警数目：6</div>
              <div>已确认告警数目：7</div>
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
                  <div class="percentage-value text-28px mt-12px">8062kW</div>
                  <div class="percentage-label text-12px mt-12px">总功率</div>
                </template>
              </el-progress>
            </el-col>
            <el-col :span="12" class="flex flex-col justify-evenly">
              <div>总有功功率：7880kW</div>
              <div>总无功功率：5680kVar</div>
              <div>总视在功率：2200kVA</div>
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
import type { WorkplaceTotal, Project, Notice, Shortcut } from './types'
import { pieOptions, barOptions } from './echarts-data'

defineOptions({ name: 'Home' })

const { t } = useI18n()
const userStore = useUserStore()
const { setWatermark } = useWatermark()
const loading = ref(true)
const avatar = userStore.getUser.avatar
const username = userStore.getUser.nickname
const pieOptionsData = reactive<EChartsOption>(pieOptions) as EChartsOption
const tableData = [
  {
    name: 'PDU',
    all: 55,
    on: 38,
    off: 17,
  },
  {
    name: '母线',
    all: 55,
    on: 38,
    off: 17,
  },
  {
    name: '插接箱',
    all: 55,
    on: 38,
    off: 17,
  },
]
// 获取统计数
let totalSate = reactive<WorkplaceTotal>({
  project: 0,
  access: 0,
  todo: 0
})

const getCount = async () => {
  const data = {
    project: 40,
    access: 2340,
    todo: 10
  }
  totalSate = Object.assign(totalSate, data)
}

// 获取项目数
let projects = reactive<Project[]>([])
const getProject = async () => {
  const data = [
    {
      name: '机房1',
      icon: 'akar-icons:github-fill',
      message: '实时总功率：88kW',
      personal: '视在功率：55kVA',
      time: new Date()
    },
    {
      name: '机房2',
      icon: 'logos:vue',
      message: '实时总功率：88kW',
      personal: '视在功率：55kVA',
      time: new Date()
    },
    {
      name: '机房3',
      icon: 'logos:angular-icon',
      message: '实时总功率：88kW',
      personal: '视在功率：55kVA',
      time: new Date()
    },
    {
      name: '机房4',
      icon: 'logos:react',
      message: '实时总功率：88kW',
      personal: '视在功率：55kVA',
      time: new Date()
    },
    {
      name: '机房5',
      icon: 'logos:webpack',
      message: '实时总功率：88kW',
      personal: '视在功率：55kVA',
      time: new Date()
    },
    {
      name: '机房6',
      icon: 'vscode-icons:file-type-vite',
      message: '实时总功率：88kW',
      personal: '视在功率：55kVA',
      time: new Date()
    },
    {
      name: '机房7',
      icon: 'vscode-icons:file-type-vite',
      message: '实时总功率：88kW',
      personal: '视在功率：55kVA',
      time: new Date()
    },
    {
      name: '机房8',
      icon: 'vscode-icons:file-type-vite',
      message: '实时总功率：88kW',
      personal: '视在功率：55kVA',
      time: new Date()
    },
  ]
  projects = Object.assign(projects, data)
}

// 获取通知公告
let notice = reactive<Notice[]>([])
const getNotice = async () => {
  const data = [
    {
      title: '系统升级版本',
      type: '通知',
      keys: ['通知', '升级'],
      date: new Date()
    },
    {
      title: '系统凌晨维护',
      type: '公告',
      keys: ['公告', '维护'],
      date: new Date()
    },
    {
      title: '系统升级版本',
      type: '通知',
      keys: ['通知', '升级'],
      date: new Date()
    },
    {
      title: '系统凌晨维护',
      type: '公告',
      keys: ['公告', '维护'],
      date: new Date()
    }
  ]
  notice = Object.assign(notice, data)
}

// 获取快捷入口
let shortcut = reactive<Shortcut[]>([])

const getShortcut = async () => {
  const data = [
    {
      name: 'Github',
      icon: 'akar-icons:github-fill',
      url: 'github.io'
    },
    {
      name: 'Vue',
      icon: 'logos:vue',
      url: 'vuejs.org'
    },
    {
      name: 'Vite',
      icon: 'vscode-icons:file-type-vite',
      url: 'https://vitejs.dev/'
    },
    {
      name: 'Angular',
      icon: 'logos:angular-icon',
      url: 'github.io'
    },
    {
      name: 'React',
      icon: 'logos:react',
      url: 'github.io'
    },
    {
      name: 'Webpack',
      icon: 'logos:webpack',
      url: 'github.io'
    }
  ]
  shortcut = Object.assign(shortcut, data)
}

// 用户来源
const getUserAccessSource = async () => {
  const data = [
    { value: 335, name: 'analysis.directAccess' },
    { value: 310, name: 'analysis.mailMarketing' },
    { value: 234, name: 'analysis.allianceAdvertising' },
    { value: 135, name: 'analysis.videoAdvertising' },
    { value: 1548, name: 'analysis.searchEngines' }
  ]
  set(
    pieOptionsData,
    'legend.data',
    data.map((v) => t(v.name))
  )
  pieOptionsData!.series![0].data = data.map((v) => {
    return {
      name: t(v.name),
      value: v.value
    }
  })
}
const barOptionsData = reactive<EChartsOption>(barOptions) as EChartsOption

// 周活跃量
const getWeeklyUserActivity = async () => {
  const data = [
    { value: 13253, name: '机房1' },
    { value: 34235, name: '机房2' },
    { value: 26321, name: '机房3' },
    { value: 12340, name: '机房4' },
    { value: 24643, name: '机房5' },
    { value: 12322, name: '机房6' },
    { value: 23151, name: '机房7' },
    { value: 23141, name: '机房8' },
    { value: 15256, name: '机房9' },
    { value: 17264, name: '机房10' },
  ]
  Object.assign(barOptionsData, {
      title: {
        text: '各机房用电量',
        left: 'center'
      },
      grid: {
        left: 50,
        right: 20,
        bottom: 20
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        right: 20,
        selectedMode: 'single'
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.name)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '当天',
          data: data.map(item => item.value),
          type: 'bar',
        },
        {
          name: '昨天',
          data: data.map(item => item.value),
          type: 'bar',
        },
        {
          name: '上周',
          data: data.map(item => item.value),
          type: 'bar',
        },
        {
          name: '上月',
          data: data.map(item => item.value),
          type: 'bar',
        },
      ]
  })
}

const getAllApi = async () => {
  await Promise.all([
    getCount(),
    getProject(),
    getNotice(),
    getShortcut(),
    getUserAccessSource(),
    getWeeklyUserActivity()
  ])
  loading.value = false
}

getAllApi()
</script>
