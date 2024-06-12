import request from '@/config/axios'

// pdu历史数据 API
export const EnergyConsumptionApi = {
  // 查询pdu数据参数类型各id最大值
  getTypeMaxValue: async () => {
    return await request.get({ url: `/pdu/eq-data/type-max-value`})
  },

  // 查询pdu电量数据分页
  getEQDataPage: async (params: any) => {
    return await request.get({ url: `/pdu/eq-data/page`, params })
  },

   // 查询pdu电量费数据分页
   getBillDataPage: async (params: any) => {
    return await request.get({ url: `/pdu/eq-data/bill-page`, params })
  },

  // 查询pdu电量数据详情
  getEQDataDetails: async (params: any) => {
    return await request.get({ url: `/pdu/eq-data/details`, params })
  },

  // 查询pdu各输出位电量数据详情
  getOutletsEQData: async (params: any) => {
    return await request.get({ url: `/pdu/eq-data/outlets-details`, params })
  },

  // 查询pdu实时电量数据分页
  getRealtimeEQDataPage: async (params: any) => {
    return await request.get({ url: `/pdu/eq-data/realtime-page`, params })
  },

  // 查询pdu能耗导航的新增多少条记录数据
  getNavNewData: async (params: any) => {
    return await request.get({ url: `/pdu/eq-data/new-data`, params })
  },

  // 查询pdu电能记录导航的一天数据显示
  getNavOneDayData: async (params: any) => {
    return await request.get({ url: `/pdu/eq-data/one-day`, params })
  },
  

}
