import request from '@/config/axios'

export const CabinetApi = {
  // 查询机柜列表分页
  getCabinetInfo: async (data: any) => {
    return await request.post({ url: `/cabinet/page`, data })
  },

  getCabinetBalance: async (data: any) => {
    return await request.post({ url: `/cabinet/balance/page`, data })
  },
  //获得机柜负荷分页
  getIndexLoadPage: async (data: any) => {
    return await request.post({ url: `/cabinet/loadPage`, data })
  },
  //机柜配电状态统计
  getCabinetInfoStatus: async () => {
    return await request.post({ url: `/cabinet/runStatus` })
  },

  //获得已删除机柜分页
  getDeletedCabinetPage: async (data: any) => {
    return await request.post({ url: `/cabinet/deletedCabinetPage`, data })
  },

  //恢复设备
  getrestorerCabinet: async (params: any) => {
    return await request.get({ url: `/cabinet/restorerCabinet`,params})
  },


  // 获取机柜配置信息
  getCabinetInfoItem: async (params: any) => {
    return await request.get({ url: `/cabinet/detailV2`, params })
  },
  // 获取机房菜单（全部）
  getRoomMenuAll: async (params: any) => {
    return await request.get({ url: `/room/menuAll`, params })
  },
  // 获取机架菜单（全部）
  getRackMenuAll: async (params: any) => {
    return await request.get({ url: `room/rack/menu`, params })
  },
  // 保存机柜
  saveCabinetInfo: async (data: any) => {
    return await request.post({ url: `/cabinet/save`, data })
  },
  // 删除机柜
  deleteCabinetInfo: async (params: any) => {
    return await request.get({ url: `/cabinet/delete`, params })
  },
  // 获取机房菜单
  getRoomList: async (params: any) => {
    return await request.get({ url: `/room/list`, params })
  },
  // 机柜负载状态统计
  getLoadStatus: async (params: any) => {
    return await request.get({ url: `/cabinet/load/count`, params })
  },
  // 获取机房菜单
  getRoomPDUList: async (params: any) => {
    return await request.get({ url: `/room/pdu/menu`, params })
  },
  // 获取机柜详情
  getCabinetDetail: async (params: any) => {
    return await request.get({ url: `/cabinet/detail`, params })
  },
  // 机架保存
  saveRackBatch: async (data: any) => {
    return await request.post({ url: `/rack/batch/save`, data })
  },
  // 机架主页面
  getRackPage: async (data: any) => {
    return await request.post({ url: `/rack/data/page`, data })
  },
  // 机架数据详情
  getRackDetail: async (params: any) => {
    return await request.get({ url: `/rack/data/detail`, params })
  },
  // 机架功率曲线
  getPowTrend: async (params: any) => {
    return await request.get({ url: `/rack/pow/trend`, params })
  },
  // 机架用能趋势
  getEleTrend: async (params: any) => {
    return await request.get({ url: `/rack/ele/trend`, params })
  },
  //机架统计
  getRackStatistics: async () => {
    return await request.get({ url: `/rack/pageStatistics` })
  },
}

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