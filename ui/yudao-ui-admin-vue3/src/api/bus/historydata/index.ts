import request from '@/config/axios'

// 母线历史数据 API
export const HistoryDataApi = {

  // 查询母线插接箱数据参数类型各id最大值
  getTypeMaxValue: async (params: any) => {
    return await request.get({ url: `/bus/history-data/type-max-value`, params})
  },

  // 查询母线始端箱历史数据分页
  getBusHistoryDataPage: async (data: any) => {
    return await request.post({ url: `/bus/history-data/bus-page`, data })
  },

  // 查询母线插接箱历史数据分页
  getBoxHistoryDataPage: async (data: any) => {
    return await request.post({ url: `/bus/history-data/box-page`, data })
  },
  
  // 查询母线始端箱历史数据详情
  getBusHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/bus/history-data/bus-details`, params })
  },
  
  // 查询母线插接箱历史数据详情
  getBoxHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/bus/history-data/box-details`, params })
  },

  // 导出母线插接箱历史数据 Excel
  exportBoxHistoryData: async (data, axiosConfig) => {
    return await request.downloadPost({ url: `/bus/history-data/box-export-excel`, data, ...axiosConfig })
  },

  // 导出母线始端箱历史数据 Excel
  exportBusHistoryData: async (data, axiosConfig) => {
    return await request.downloadPost({ url: `/bus/history-data/bus-export-excel`, data, ...axiosConfig })
  },

  // 查询母线始端箱电力数据导航的新增记录数据显示
  getBusNavNewData: async (granularity: string) => {
    return await request.get({ url: `/bus/history-data/bus-new-data/`+granularity})
  },

  // 查询母线插接箱电力数据导航的新增记录数据显示
  getBoxNavNewData: async (granularity: string) => {
    return await request.get({ url: `/bus/history-data/box-new-data/`+granularity})
  },
    // 导出母线始端箱历史数据 Excel
    exportBusHistorydetailsPageData: async (params, axiosConfig) => {
      return await request.download({ url: `/bus/history-data/bus-export-detail-excel`, params, ...axiosConfig })
    },
    // 导出母线插接箱历史数据 Excel
    exportBoxHistorydetailsPageData: async (params, axiosConfig) => {
      return await request.download({ url: `/bus/history-data/box-export-detail-excel`, params, ...axiosConfig })
    },
}
