import request from '@/config/axios'

// 机柜温度颜色 VO
export interface TemColorVO {
  id: number // 自增id
  min: number // 温度范围最小值
  max: number // 温度范围最大值
  color: string // 温度范围对应的颜色
}

// 机柜温度颜色 API
export const TemColorApi = {
  // 查询机柜温度颜色分页
  getTemColorPage: async (params: any) => {
    return await request.get({ url: `/cabinet/tem-color/page`, params })
  },

  // 查询机柜温度颜色详情
  getTemColor: async (id: number) => {
    return await request.get({ url: `/cabinet/tem-color/get?id=` + id })
  },

  // 新增机柜温度颜色
  createTemColor: async (data: TemColorVO) => {
    return await request.post({ url: `/cabinet/tem-color/create`, data })
  },

  // 修改机柜温度颜色
  updateTemColor: async (data: TemColorVO) => {
    return await request.put({ url: `/cabinet/tem-color/update`, data })
  },

  // 删除机柜温度颜色
  deleteTemColor: async (id: number) => {
    return await request.delete({ url: `/cabinet/tem-color/delete?id=` + id })
  },

  // 导出机柜温度颜色 Excel
  exportTemColor: async (params) => {
    return await request.download({ url: `/cabinet/tem-color/export-excel`, params })
  },

  // 查询所有机柜温度颜色
  getTemColorAll: async (params: any) => {
    return await request.get({ url: `/cabinet/tem-color/all`, params })
  },
}
