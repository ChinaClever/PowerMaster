import request from '@/config/axios'

export const MachineRoomApi = {
  // 获取机房详情
  getRoomDetail: async (params: any) => {
    return await request.get({ url: `/room/detail`, params })
  },
  // 获取机房列表
  getRoomList: async (params: any) => {
    return await request.get({ url: `/room/list`, params })
  },
  // 获取机房数据详情
  getRoomDataDetail: async (params: any) => {
    return await request.get({ url: `/room/data/detail`, params })
  },
  // 获取机房主页面设备数据
  getRoomDevData: async (params: any) => {
    return await request.get({ url: `/room/main/dev/data`, params })
  },
  // 获取机房主页面功率数据
  getRoomPowData: async (params: any) => {
    return await request.get({ url: `/room/main/pow/data`, params })
  },
  // 获取机房主页面曲线数据
  getRoomEchartData: async (params: any) => {
    return await request.get({ url: `/room/main/curve/data`, params })
  },
  // 获取机房主页面环境数据
  getRoomEnvData: async (params: any) => {
    return await request.get({ url: `/room/main/env/data`, params })
  },
  // 获取机房主页面用能
  getRoomEqData: async (params: any) => {
    return await request.get({ url: `/room/main/eq`, params })
  },
  // 机房删除
  deleteRoom: async (params: any) => {
    return await request.get({ url: `/room/delete`, params })
  },
  // 修改机房详情
  saveRoomDetail: async (data: any) => {
    return await request.post({ url: `/room/save`, data })
  },
}