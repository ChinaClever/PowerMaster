import request from '@/config/axios'

export interface EnvDataVO {
  id: number 
  pduId: number 
  sensorId: number 
  temValue: number
  humValue: number 
  createTime: string 
}

export const EnvDataApi = {
  // 查询pdu环境数据传感器id最大值
  getSensorIdMaxValue: async () => {
    return await request.get({ url: `/cabinet/history-data/sensorId-max-value`})
  },
  
  // 查询pdu环境数据分页
  getEnvDataPage: async (params: any) => {
    return await request.get({ url: `/cabinet/history-data/env-page`, params })
  },

  // 查询pdu环境数据详情
  getEnvDataDetails: async (params: any) => {
    return await request.get({ url: `/cabinet/history-data/env-details`, params })
  },

  // 导出pdu历史数据 Excel
  exportEnvData: async (params) => {
    return await request.download({ url: `/cabinet/history-data/env-export-excel`, params })
  },

  // 查询pdu导航的一小时数据显示
  getNavOneHourData: async (params: any) => {
    return await request.get({ url: `/cabinet/history-data/env-one-hour`, params })
  },
}
