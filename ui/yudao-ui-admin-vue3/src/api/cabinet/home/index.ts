import request from '@/config/axios'

export const CabinetApi = {
  // 获取主页面设备数据
  getHomeDevData: async (params: any) => {
    return await request.get({ url: `/main/dev/data`, params })
  },
  // 获取主页面功率数据
  getHomePowData: async (params: any) => {
    return await request.get({ url: `/main/pow/data`, params })
  },
  // 获取主页面用能
  getHomeEqData: async (params: any) => {
    return await request.get({ url: `/main/eq/data`, params })
  },
  // 获取机架设备详情
  getHomeAlarmData: async (params: any) => {
    return await request.get({ url: `/system/alarm/record/level/count`, params })
  }, 

}