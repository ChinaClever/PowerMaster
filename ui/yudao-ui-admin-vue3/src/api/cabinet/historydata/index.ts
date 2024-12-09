import request from '@/config/axios'

// 机柜历史数据 API
export const HistoryDataApi = {

  // 查询机柜历史数据分页
  getHistoryDataPage: async (params: any) => {
    return await request.get({ url: `/cabinet/history-data/page`, params })
  },

  // 查询机柜历史数据详情
  getHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/cabinet/history-data/details`, params })
  },

  // 导出机柜历史数据 Excel
  exportHistoryData: async (params, axiosConfig) => {
    return await request.download({ url: `/cabinet/history-data/export-excel`, params, ...axiosConfig })
  },

  // 查询机柜电力数据导航的新增记录数据显示
  getNavNewData: async (granularity: string) => {
    return await request.get({ url: `/cabinet/history-data/new-data/`+granularity})
  },
    // 导出机柜历史数据 Excel
    exportHistorydetailsPageData: async (params, axiosConfig) => {
      return await request.download({ url: `/cabinet/history-data/details-export-excel`, params, ...axiosConfig })
    },
}
