import request from '@/config/axios'

export const EnvDataApi = {
  // 查询pdu环境数据传感器id最大值
  getSensorIdMaxValue: async () => {
    return await request.get({ url: `/pdu/history-data/sensorId-max-value`})
  },
  
  // 查询pdu环境数据分页
  getEnvDataPage: async (data: any) => {
    return await request.post({ url: `/pdu/history-data/env-page`, data })
  },

  // 查询pdu环境数据分页
  getEnvDataPageByCabinet: async (params: any) => {
    return await request.get({ url: `/pdu/history-data/env-pageByCabinet`, params })
  },

  // 查询pdu环境数据详情
  getEnvDataDetails: async (params: any) => {
    return await request.get({ url: `/pdu/history-data/env-details`, params })
  },

  // 导出pdu环境历史数据 Excel
  exportEnvHistoryData: async (params, axiosConfig) => {
    return await request.download({ url: `/pdu/history-data/env-export-excel`, params, ...axiosConfig })
  },

   // 导出pdu环境历史数据 Excel
   exportEnvHistoryDataByCabinet: async (data, axiosConfig) => {
    return await request.downloadPost({ url: `/pdu/history-data/env-export-excelByCabinet`, data, ...axiosConfig })
  },

  // 查询pdu环境数据导航的新增多少条记录数据
  getEnvNavNewData: async (params: any) => {
    return await request.get({ url: `/pdu/history-data/env-new-data`, params })
  },

  // 查询pdu环境数据导航的新增多少条记录数据
  getEnvNavNewDataByCabinet: async (params: any) => {
    return await request.get({ url: `/pdu/history-data/env-new-dataByCabinet`, params })
  },
}
