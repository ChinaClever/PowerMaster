import request from '@/config/axios'

export const EnvDataApi = {
  
  // 查询始端箱环境数据分页
  getBusEnvDataPage: async (params: any) => {
    return await request.post({ url: `/bus/history-data/bus-env-page`, params })
  },

  // 查询始端箱环境数据详情
  getBusEnvDataDetails: async (params: any) => {
    return await request.get({ url: `/bus/history-data/bus-env-details`, params })
  },

  // 导出始端箱环境历史数据 Excel
  exportBusEnvHistoryData: async (params, axiosConfig) => {
    return await request.downloadPost({ url: `/bus/history-data/bus-env-export-excel`, params, ...axiosConfig })
  },

  // 查询始端箱环境数据导航的新增多少条记录数据
  getBusEnvNavNewData: async () => {
    return await request.get({ url: `/bus/history-data/bus-env-new-data`})
  },
  // 导出插接箱环境历史数据 Excel
  exportBusTemHistoryData: async (params, axiosConfig) => {
    return await request.download({ url: `/bus/history-data/bus-tem-export-excel`, params, ...axiosConfig })
  },

    // 查询插接箱环境数据分页
    getBoxEnvDataPage: async (params: any) => {
      return await request.post({ url: `/bus/history-data/box-env-page`, params })
    },
  
    // 查询插接箱环境数据详情
    getBoxEnvDataDetails: async (params: any) => {
      return await request.get({ url: `/bus/history-data/box-env-details`, params })
    },
  
    // 导出插接箱环境历史数据 Excel
    exportBoxEnvHistoryData: async (params, axiosConfig) => {
      return await request.downloadPost({ url: `/bus/history-data/box-env-export-excel`, params, ...axiosConfig })
    },
  
    // 查询插接箱环境数据导航的新增多少条记录数据
    getBoxEnvNavNewData: async () => {
      return await request.get({ url: `/bus/history-data/box-env-new-data`})
    },
        // 导出插接箱环境历史数据 Excel
    exportBoxTemHistoryData: async (params, axiosConfig) => {
      return await request.download({ url: `/bus/history-data/box-tem-export-excel`, params, ...axiosConfig })
    },
}
