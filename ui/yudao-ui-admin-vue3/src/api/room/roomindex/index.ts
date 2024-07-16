import request from '@/config/axios'

// 机房索引 VO
export interface IndexVO {
  id: number // 主键id
  name: string // 机房名
  isDelete: number // 是否删除
  powerCapacity: number // 电力容量
  eleAlarmDay: number // 日用电告警开关 0 关 1开
  eleLimitDay: number // 日用能限制
  eleAlarmMonth: number // 月用电告警开关 0关 1开
  eleLimitMonth: number // 月用能限制
}

// 机房索引 API
export const IndexApi = {
  // 查询机房索引分页
  getIndexPage: async (data: any) => {
    return await request.post({ url: `/room/index/page`, data })
  },

  // 查询机房索引详情
  getIndex: async (id: number) => {
    return await request.get({ url: `/room/index/get?id=` + id })
  },

  // 新增机房索引
  createIndex: async (data: IndexVO) => {
    return await request.post({ url: `/room/index/create`, data })
  },

  // 修改机房索引
  updateIndex: async (data: IndexVO) => {
    return await request.put({ url: `/room/index/update`, data })
  },

  // 删除机房索引
  deleteIndex: async (id: number) => {
    return await request.delete({ url: `/room/index/delete?id=` + id })
  },

  // 导出机房索引 Excel
  exportIndex: async (params) => {
    return await request.download({ url: `/room/index/export-excel`, params })
  },

  getRoomBalancePage: async (data: any) => {
    return await request.post({ url: `/room/index/balancepage`,data})
  },

  getConsumeData: async (data) => {
    return await request.post({ url: `/room/index/report/ele`,data})
  },

  getPowData: async (data) => {
    return await request.post({ url: `/room/index/report/pow`,data})
  },

  getRoomPFLine : async (data) => {
    return await request.post({ url: `/room/index/report/pfline`, data })
  },

  getRoomList: async () => {
    return await request.get({ url: `/room/list` })
  },

  getEqPage: async (data: any) => {
    return await request.post({ url: `/room/index/eq/page`, data })
  },
}
