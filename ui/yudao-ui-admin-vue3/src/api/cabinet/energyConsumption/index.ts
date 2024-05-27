import request from '@/config/axios'

export const EnergyConsumptionApi = {

  // 查询电量数据分页
  getEQDataPage: async (params: any) => {
    return await request.get({ url: `/cabinet/eq-data/page`, params })
  },

   // 查询电量费数据分页
   getBillDataPage: async (params: any) => {
    return await request.get({ url: `/cabinet/eq-data/bill-page`, params })
  },

  // 查询电量数据详情
  getEQDataDetails: async (params: any) => {
    return await request.get({ url: `/cabinet/eq-data/details`, params })
  },

  // 查询实时电量数据分页
  getRealtimeEQDataPage: async (params: any) => {
    return await request.get({ url: `/cabinet/eq-data/realtime-page`, params })
  },

  // 查询pdu导航的一周数据显示
  getNavOneWeekData: async (params: any) => {
    return await request.get({ url: `/cabinet/eq-data/one-week`, params })
  },

  // 查询pdu导航的一天数据显示
  getNavOneDayData: async (params: any) => {
    return await request.get({ url: `/cabinet/eq-data/one-day`, params })
  },

}
