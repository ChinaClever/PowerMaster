import request from '@/config/axios'

// 机柜计算服务配置 VO
export interface StatisConfigVO {
  id: number // 主键id
  billMode: number // 计费方式 1固定计费 2分段计费
  dayCron: string // 按天统计历史数据任务
  hourCron: string // 按小时统计历史数据任务
  eqDayCron: string // 电量按天统计任务
  eqWeekCron: string // 电量按周执行任务
  eqMonthCron: string // 按月统计电量任务
  loadLimit: number // 负载限制
  statusAlarm: number // 状态告警开关 0关 1开
  storeCron: string // 存储任务
  alarmCron: string // 告警任务
  alarmPush: number // 告警推送开关 0 关 1开
  alarmPushCron: string // 告警推送任务
  pushMqs: string // 推送mq配置
  redisExpire: number // redis key过期时间
  eleStoreCron: string // 电能存储任务
  timingPushCron: string // 定时推送任务
  timingPush: number // 定时推送开关 1开启 0关闭
  redisCron: string // redis缓存任务
}

// 机柜计算服务配置 API
export const StatisConfigApi = {
  // 查询机柜计算服务配置分页
  getStatisConfigPage: async (params: any) => {
    return await request.get({ url: `/cabinet/statis-config/page`, params })
  },

  // 查询机柜计算服务配置详情
  getStatisConfig: async (id: number) => {
    return await request.get({ url: `/cabinet/statis-config/get?id=` + id })
  },

  // 新增机柜计算服务配置
  createStatisConfig: async (data: StatisConfigVO) => {
    return await request.post({ url: `/cabinet/statis-config/create`, data })
  },

  // 修改机柜计算服务配置
  updateStatisConfig: async (data: StatisConfigVO) => {
    return await request.put({ url: `/cabinet/statis-config/update`, data })
  },

  // 删除机柜计算服务配置
  deleteStatisConfig: async (id: number) => {
    return await request.delete({ url: `/cabinet/statis-config/delete?id=` + id })
  },

  // 导出机柜计算服务配置 Excel
  exportStatisConfig: async (params) => {
    return await request.download({ url: `/cabinet/statis-config/export-excel`, params })
  },
}