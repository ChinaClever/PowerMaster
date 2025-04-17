import request from '@/config/axios'

export const MachineHomeApi = {
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
  // 获取未处理告警数量
  getHomeAlarmData: async (params: any) => {
    return await request.get({ url: `/alarm/log-record/level/count`, params })
  }, 

}