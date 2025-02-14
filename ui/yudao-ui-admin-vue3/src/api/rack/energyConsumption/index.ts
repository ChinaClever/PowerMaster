import request from '@/config/axios'

// 机架历史数据 API
export const EnergyConsumptionApi = {

  // 查询机架电量数据分页
  getEQDataPage: async (data: any) => {
    return await request.post({ url: `/rack/eq-data/page`, data })
  },

   // 查询机架电量费数据分页
   getBillDataPage: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/bill-page`, params })
  },

  // 查询机架电量数据详情
  getEQDataDetails: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/details`, params })
  },

  // 查询机架各输出位电量数据详情
  getOutletsEQData: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/outlets-details`, params })
  },

  // 查询机架实时电量数据分页
  getRealtimeEQDataPage: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/realtime-page`, params })
  },

  // 查询机架能耗导航的新增多少条记录数据
  getNavNewData: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/new-data`, params })
  },

  // 查询机架导航的一天数据显示
  getNavOneDayData: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/one-day`, params })
  },

  // 导出机架能耗趋势历史数据 Excel
  exportEQPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/rack/eq-data/export-excel`, params, ...axiosConfig })
  },
    // 导出机架能耗排名历史数据 Excel
    exportOutletsPageData: async (params, axiosConfig) => {
      return await request.download({ url: `/rack/eq-data/outlets-export-excel`, params, ...axiosConfig })
    },
  
  // 导出机架电费统计历史数据 Excel
  exportBillPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/rack/eq-data/bill-export-excel`, params, ...axiosConfig })
  },

  // 导出机架电能记录历史数据 Excel
  exportRealtimeEQPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/rack/eq-data/realtime-export-excel`, params, ...axiosConfig })
  },

  // 获取分段电能电费
  getSubBillDetails: async (params: any) => {
    return await request.get({ url: `/rack/eq-data/bill-details`, params})
  },


    // 导出机架实时电能记录历史数据 Excel
    getRackTotalRealtimeExcel: async (data, axiosConfig) => {
      return await request.downloadPost({ url: `/rack/eq-data/rack_total_realtimeExcel`, data, ...axiosConfig })
    },
  
    // 获取实时电量
    getRackTotalRealtime: async (data: any) => {
      return await request.post({ url: `/rack/eq-data/rack_total_realtime`, data})
    },
}
