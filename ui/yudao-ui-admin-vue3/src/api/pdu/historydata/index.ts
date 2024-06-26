import request from '@/config/axios'
// pdu历史数据 API
export const HistoryDataApi = {
  // 查询pdu数据参数类型各id最大值
  getTypeMaxValue: async () => {
    return await request.get({ url: `/pdu/history-data/type-max-value`})
  },

  // 查询pdu历史数据分页
  getHistoryDataPage: async (params: any) => {
    return await request.get({ url: `/pdu/history-data/page`, params })
  },

  // 查询pdu历史数据详情
  getHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/pdu/history-data/details`, params })
  },

  // 导出pdu能耗趋势数据 Excel
  exportHistoryData: async (params, axiosConfig) => {
    return await request.download({ url: `/pdu/history-data/export-excel`, params, ...axiosConfig })
  },
  // 查询pdu电力数据导航的新增记录数据显示
  getNavNewData: async (granularity: string) => {
    return await request.get({ url: `/pdu/history-data/new-data/`+granularity})
  },
}
