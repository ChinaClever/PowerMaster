import request from '@/config/axios'

// 柜列历史数据 API
export const HistoryDataApi = {

  // 查询柜列历史数据分页
  getHistoryDataPage: async (params: any) => {
    return await request.get({ url: `/aisle/history-data/page`, params })
  },

  // 查询柜列历史数据详情
  getHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/aisle/history-data/details`, params })
  },

  // 导出柜列历史数据 Excel
  exportHistoryData: async (params, axiosConfig) => {
    return await request.download({ url: `/aisle/history-data/export-excel`, params, ...axiosConfig })
  },

  // 查询柜列电力数据导航的新增记录数据显示
  getNavNewData: async (granularity: string) => {
    return await request.get({ url: `/aisle/history-data/new-data/`+granularity})
  },
}
