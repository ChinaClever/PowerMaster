import request from '@/config/axios'

// 机架历史数据 API
export const HistoryDataApi = {
  
  // 查询机架历史数据分页
  getHistoryDataPage: async (params: any) => {
    return await request.get({ url: `/rack/history-data/page`, params })
  },

  // 查询机架历史数据详情
  getHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/rack/history-data/details`, params })
  },

    // 查询机架历史数据详情 Excel
    getHistoryDataDetailsExcel: async (params, axiosConfig)=> {
      return await request.download({ url: `/rack/history-data/detailsExcel`, params , ...axiosConfig })
    },

  // 查询机架电力数据导航的新增记录数据显示
  getNavNewData: async (granularity: string) => {
    return await request.get({ url: `/rack/history-data/new-data/`+granularity})
  },

  // 导出机架历史数据 Excel
  exportHistoryData: async (params, axiosConfig) => {
    return await request.download({ url: `/rack/history-data/export-excel`, params, ...axiosConfig })
  },
}
