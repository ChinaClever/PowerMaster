import request from '@/config/axios'

export const MachineColumnApi = {
  // 获取柜列详情
  getAisleDetail: async (params: any) => {
    return await request.get({ url: `/aisle/detail`, params })
  },
  // 柜列详情保存
  saveAisleDetail: async (data: any) => {
    return await request.post({ url: `/aisle/save`, data })
  },
  // 柜列母线保存
  saveAisleBus: async (data: any) => {
    return await request.post({ url: `/aisle/bus/save`, data })
  },
  // 柜列状态数据详情
  getDataDetail: async (params: any) => {
    return await request.get({ url: `/aisle/data/detail`, params })
  },
  // 柜列主界面数据
  getMaindata: async (params: any) => {
    return await request.get({ url: `/aisle/main/data`, params })
  },
}