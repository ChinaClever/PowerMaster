import request from '@/config/axios'

export const MachineColumnApi = {
  // 获取柜列详情
  getAisleDetail: async (params: any) => {
    return await request.get({ url: `/aisle/detail`, params })
  },
  // 获取机房下柜列列表
  getAisleList: async (params: any) => {
    return await request.get({ url: `/aisle/list`, params })
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
  // 机柜主界面用能
  getMainEq: async (params: any) => {
    return await request.get({ url: `aisle/main/eq`, params })
  },

  
  // 柜列始端箱单个删除
  getDeleteAisleSingleBox: async (params: any) => {
    return await request.get({ url: `aisle/box/singleDelete`, params })
  },


}