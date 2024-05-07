import request from '@/config/axios'

export const CabinetApi = {
  // 查询机柜列表分页
  getCabinetInfo: async (data: any) => {
    return await request.post({ url: `/cabinet/page`, data })
  },
  // 获取机柜配置信息
  getCabinetInfoItem: async (params: any) => {
    return await request.get({ url: `/cabinet/detailV2`, params })
  },
  // 获取机房菜单（全部）
  getRoomMenuAll: async (params: any) => {
    return await request.get({ url: `/room/menuAll`, params })
  },
  // 保存机柜
  saveCabinetInfo: async (data: any) => {
    return await request.post({ url: `/cabinet/save`, data })
  },
  // 删除机柜
  deleteCabinetInfo: async (params: any) => {
    return await request.get({ url: `/cabinet/delete`, params })
  },
}