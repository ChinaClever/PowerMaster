import request from '@/config/axios'

// 母线历史数据 API
export const HistoryDataApi = {

  // 查询母线插接箱数据参数类型各id最大值
  getTypeMaxValue: async (params: string[]) => {
    return await request.get({ url: `/bus/history-data/type-max-value`, params})
  },

  // 查询母线始端箱历史数据分页
  getBusHistoryDataPage: async (params: any) => {
    return await request.get({ url: `/bus/history-data/bus-page`, params })
  },

  // 查询母线插接箱历史数据分页
  getBoxHistoryDataPage: async (params: any) => {
    return await request.get({ url: `/bus/history-data/box-page`, params })
  },
  
  // 查询母线始端箱历史数据详情
  getBusHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/bus/history-data/bus-details`, params })
  },
  
  // 查询母线插接箱历史数据详情
  getBoxHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/bus/history-data/box-details`, params })
  },

  // 导出母线历史数据 Excel
  exportHistoryData: async (params) => {
    return await request.download({ url: `/bus/history-data/export-excel`, params })
  },

  // 查询母线导航始端箱的一小时数据显示
  getNavBusOneHourData: async (params: any) => {
    return await request.get({ url: `/bus/history-data/bus-one-hour`, params })
  },

  // 查询母线导航始端箱的一小时数据显示
  getNavBoxOneHourData: async (params: any) => {
    return await request.get({ url: `/bus/history-data/box-one-hour`, params })
  },
}
