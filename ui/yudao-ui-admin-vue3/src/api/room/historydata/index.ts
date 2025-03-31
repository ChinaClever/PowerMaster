import request from '@/config/axios'

// 机房历史数据 API
export const HistoryDataApi = {

  // 查询机房历史数据分页
  getHistoryDataPage: async (data: any) => {
    return await request.post({ url: `/room/history-data/page`, data })
  },

  // 查询机房历史数据详情
  getHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/room/history-data/details`, params })
  },
  // 导出机房历史数据 Excel
  exportHistoryData: async (params, axiosConfig) => {
    return await request.download({ url: `/room/history-data/export-excel`, params, ...axiosConfig })
  },

  // 获得机房电力趋势分析数据详情导出 Excel
  getHistoryDataDetailsExcel: async (params, axiosConfig) => {
    return await request.download({ url: `/room/history-data/detailsExcel`, params, ...axiosConfig })
  },

  // 查询机房电力数据导航的新增记录数据显示
  getNavNewData: async (granularity: string) => {
    return await request.get({ url: `/room/history-data/new-data/`+granularity})
  },
}
