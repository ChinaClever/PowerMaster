import request from '@/config/axios'

// pdu历史数据 VO
export interface HistoryDataVO {
  id: number 
  pduId: number 
  activePow: number 
  apparentPow: number
  powerFactor: number 
  createTime: string 
}

// pdu历史数据 API
export const HistoryDataApi = {
  // 查询pdu历史数据分页
  getHistoryDataPage: async (params: any) => {
    return await request.get({ url: `/pdu/history-data/page`, params })
  },

  // 查询pdu历史数据详情
  getHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/pdu/history-data/details`, params })
  },

  // 导出pdu历史数据 Excel
  exportHistoryData: async (params) => {
    return await request.download({ url: `/pdu/history-data/export-excel`, params })
  },
}
