import request from '@/config/axios'

// 母线数据采集配置 VO
export interface DcConfigVO {
  id: number // 主键ID
  receivePort: number // 数据接收端口
  fixStoreCron: string // 定时存储定时任务配置
  changeStoreCron: string // 变化存储定时任务配置
  eleStoreCron: string // 电能存储定时任务配置
  powLimitRate: number // 总视在功率变化比
  redisExpire: number // rediskey过期时间（秒）
  offLineDuration: number // 离线告警时长（分钟）
  offLineAlarm: number // 离线告警开关（1开启 0关闭）
  statusAlarm: number // 状态告警开关 1开启 0关闭
  timingPushCron: string // 定时推送任务配置
  changePushCron: string // 变化推送任务配置
  alarmPushCron: string // 告警推送任务配置
  timingPush: number // 定时推送开关 1开启 0关闭
  changePush: number // 变化推送开关 1开启 0关闭
  alarmPush: number // 告警推送开关 1开启 0关闭
  pushMqs: string // 配置推送的mq
  fixStore: number // 定时任务开关 默认开 1   关0
  changeStore: number // 变化存储开关 默认开1 关0
  eleStore: number // 电能存储开关 默认开1  关0
  redisSwitch: number // redis保存开关  开1 关0
}

// 母线数据采集配置 API
export const DcConfigApi = {
  // 查询母线数据采集配置分页
  getDcConfigPage: async (params: any) => {
    return await request.get({ url: `/bus/dc-config/page`, params })
  },

  // 查询母线数据采集配置详情
  getDcConfig: async (id: number) => {
    return await request.get({ url: `/bus/dc-config/get?id=` + id })
  },

  // 新增母线数据采集配置
  createDcConfig: async (data: DcConfigVO) => {
    return await request.post({ url: `/bus/dc-config/create`, data })
  },

  // 修改母线数据采集配置
  updateDcConfig: async (data: DcConfigVO) => {
    return await request.put({ url: `/bus/dc-config/update`, data })
  },

  // 删除母线数据采集配置
  deleteDcConfig: async (id: number) => {
    return await request.delete({ url: `/bus/dc-config/delete?id=` + id })
  },

  // 导出母线数据采集配置 Excel
  exportDcConfig: async (params) => {
    return await request.download({ url: `/bus/dc-config/export-excel`, params })
  },
}