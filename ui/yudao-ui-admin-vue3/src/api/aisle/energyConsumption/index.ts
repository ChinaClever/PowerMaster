import request from '@/config/axios'

export const EnergyConsumptionApi = {

  // 查询电量数据分页
  getEQDataPage: async (params: any) => {
    return await request.get({ url: `/aisle/eq-data/page`, params })
  },

   // 查询电量费数据分页
   getBillDataPage: async (params: any) => {
    return await request.get({ url: `/aisle/eq-data/bill-page`, params })
  },

  // 查询电量数据详情
  getEQDataDetails: async (params: any) => {
    return await request.get({ url: `/aisle/eq-data/details`, params })
  },

  // 查询实时电量数据分页
  getRealtimeEQDataPage: async (params: any) => {
    return await request.get({ url: `/aisle/eq-data/realtime-page`, params })
  },

  // 查询机柜能耗导航的新增多少条记录数据
  getNavNewData: async (params: any) => {
    return await request.get({ url: `/aisle/eq-data/new-data`, params })
  },

  // 查询机柜电能记录导航的一天数据显示
  getNavOneDayData: async (params: any) => {
    return await request.get({ url: `/aisle/eq-data/one-day`, params })
  },

  // 导出机柜能耗趋势历史数据 Excel
  exportEQPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/aisle/eq-data/export-excel`, params, ...axiosConfig })
  },

  // 导出机柜电费统计历史数据 Excel
  exportBillPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/aisle/eq-data/bill-export-excel`, params, ...axiosConfig })
  },

  // 导出机柜电能记录历史数据 Excel
  exportRealtimeEQPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/aisle/eq-data/realtime-export-excel`, params, ...axiosConfig })
  },
  

}
