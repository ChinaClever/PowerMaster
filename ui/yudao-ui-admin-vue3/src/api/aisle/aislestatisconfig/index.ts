import request from '@/config/axios'

// 柜列计算服务配置 VO
export interface StatisConfigVO {
  id: number // 主键id
  billMode: number // 计费方式 1固定计费 2分段计费
  dayCron: string // 按天统计历史数据任务
  hourCron: string // 按小时统计历史数据任务
  eqDayCron: string // 电量按天统计任务
  eqWeekCron: string // 电量按周执行任务
  eqMonthCron: string // 按月统计电量任务
  storeCron: string // 存储任务
  redisExpire: number // redis key过期时间
  eleStoreCron: string // 电能存储任务
  redisCron: string // redis保存定时
  eleAlarmDay: number // 日用电告警开关 0 关 1开
  eleLimitDay: number // 日用能限制
  eleAlarmMonth: number // 月用电告警开关 0关 1开
  eleLimitMonth: number // 月用能限制
}

// 柜列计算服务配置 API
export const StatisConfigApi = {
  // 查询柜列计算服务配置分页
  getStatisConfigPage: async (params: any) => {
    return await request.get({ url: `/aisle/statis-config/page`, params })
  },

  // 查询柜列计算服务配置详情
  getStatisConfig: async (id: number) => {
    return await request.get({ url: `/aisle/statis-config/get?id=` + id })
  },

  // 新增柜列计算服务配置
  createStatisConfig: async (data: StatisConfigVO) => {
    return await request.post({ url: `/aisle/statis-config/create`, data })
  },

  // 修改柜列计算服务配置
  updateStatisConfig: async (data: StatisConfigVO) => {
    return await request.put({ url: `/aisle/statis-config/update`, data })
  },

  // 删除柜列计算服务配置
  deleteStatisConfig: async (id: number) => {
    return await request.delete({ url: `/aisle/statis-config/delete?id=` + id })
  },

  // 导出柜列计算服务配置 Excel
  exportStatisConfig: async (params) => {
    return await request.download({ url: `/aisle/statis-config/export-excel`, params })
  },
}