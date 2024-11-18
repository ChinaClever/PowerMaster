import request from '@/config/axios'

export const EnergyConsumptionApi = {

  // 查询始端箱电量数据分页
  getEQDataPage: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/bus/page`, params })
  },

   // 查询始端箱电量费数据分页
   getBillDataPage: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/bus/bill-page`, params })
  },

  // 查询始端箱电量数据详情
  getEQDataDetails: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/bus/details`, params })
  },

  // 查询始端箱实时电量数据分页
  getRealtimeEQDataPage: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/bus/realtime-page`, params })
  },

  // 查询始端箱能耗导航的新增多少条记录数据
  getNavNewData: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/bus/new-data`, params })
  },

  // 查询始端箱电能记录导航的一天数据显示
  getNavOneDayData: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/bus/one-day`, params })
  },

  // 导出始端箱能耗趋势历史数据 Excel
  exportEQPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/bus/eq-data/bus/export-excel`, params, ...axiosConfig })
  },
    // 导出插接箱能耗排名历史数据 Excel
    exportBusDetailsPageData: async (params, axiosConfig) => {
      return await request.download({ url: `/bus/eq-data/bus/details-export-excel`, params, ...axiosConfig })
    },
  // 导出始端箱电费统计历史数据 Excel
  exportBillPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/bus/eq-data/bus/bill-export-excel`, params, ...axiosConfig })
  },

  // 导出始端箱电能记录历史数据 Excel
  exportRealtimeEQPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/bus/eq-data/bus/realtime-export-excel`, params, ...axiosConfig })
  },

  // 获取分段电能电费
  getBusSubBillDetails: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/bus/bill-details`, params})
  },




  //插接箱API
  // 查询插接箱电量数据分页
  getBoxEQDataPage: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/box/page`, params })
  },

  // 查询插接箱电量费数据分页
  getBoxBillDataPage: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/box/bill-page`, params })
  },

  // 查询插接箱电量数据详情
  getBoxEQDataDetails: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/box/details`, params })
  },

  // 查询插接箱实时电量数据分页
  getBoxRealtimeEQDataPage: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/box/realtime-page`, params })
  },

  // 查询插接箱能耗导航的新增多少条记录数据
  getBoxNavNewData: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/box/new-data`, params })
  },

  // 查询插接箱电能记录导航的一天数据显示
  getBoxNavOneDayData: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/box/one-day`, params })
  },

  // 导出插接箱能耗趋势历史数据 Excel
  exportBoxEQPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/bus/eq-data/box/export-excel`, params, ...axiosConfig })
  },
    // 导出插接箱能耗排名历史数据 Excel
    exportBoxDetailsPageData: async (params, axiosConfig) => {
      return await request.download({ url: `/bus/eq-data/box/details-export-excel`, params, ...axiosConfig })
    },

  // 导出插接箱电费统计历史数据 Excel
  exportBoxBillPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/bus/eq-data/box/bill-export-excel`, params, ...axiosConfig })
  },

  // 导出插接箱电能记录历史数据 Excel
  exportBoxRealtimeEQPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/bus/eq-data/box/realtime-export-excel`, params, ...axiosConfig })
  },
  
  // 获取分段电能电费
  getBoxSubBillDetails: async (params: any) => {
    return await request.get({ url: `/bus/eq-data/box/bill-details`, params})
  },

  // 导出实时电能数据 Excel
  getBusEleTotalRealtimeExcel: async (data, axiosConfig) => {
    return await request.downloadPost({ url: `/bus/eq-data/bus/eleTotalRealtimeExcel`, data, ...axiosConfig })
  },
  
  // 获取实时电能
  getBusEleTotalRealtime: async (data: any) => {
    return await request.post({ url: `/bus/eq-data/bus/eleTotalRealtime`, data})
  },

    // 导出实时电能数据 Excel
    getBoxEleTotalRealtimeExcel: async (data, axiosConfig) => {
      return await request.downloadPost({ url: `/bus/eq-data/box/eleTotalRealtimeExcel`, data, ...axiosConfig })
    },
    
    // 获取实时电能
    getBoxEleTotalRealtime: async (data: any) => {
      return await request.post({ url: `/bus/eq-data/box/eleTotalRealtime`, data})
    },
}
