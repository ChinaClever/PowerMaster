import request from '@/config/axios'

// 机架计算服务配置 VO
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
}

// 机架计算服务配置 API
export const StatisConfigApi = {
  // 查询机架计算服务配置分页
  getStatisConfigPage: async (params: any) => {
    return await request.get({ url: `/rack/statis-config/page`, params })
  },

  // 查询机架计算服务配置详情
  getStatisConfig: async (id: number) => {
    return await request.get({ url: `/rack/statis-config/get?id=` + id })
  },

  // 新增机架计算服务配置
  createStatisConfig: async (data: StatisConfigVO) => {
    return await request.post({ url: `/rack/statis-config/create`, data })
  },

  // 修改机架计算服务配置
  updateStatisConfig: async (data: StatisConfigVO) => {
    return await request.put({ url: `/rack/statis-config/update`, data })
  },

  // 删除机架计算服务配置
  deleteStatisConfig: async (id: number) => {
    return await request.delete({ url: `/rack/statis-config/delete?id=` + id })
  },

  // 导出机架计算服务配置 Excel
  exportStatisConfig: async (params) => {
    return await request.download({ url: `/rack/statis-config/export-excel`, params })
  },
}