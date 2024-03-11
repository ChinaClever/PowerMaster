import request from '@/config/axios'

// pdu历史数据 VO
export interface HistoryDataVO {
  id: number // 编号
  pduId: number // pdu编号
  type: string // 数据类型	
  topic: string // topic
  indexes: number // indexes
  value: number // 值
}

// pdu历史数据 API
export const HistoryDataApi = {
  // 查询pdu历史数据分页
  getHistoryDataPage: async (params: any) => {
    return await request.get({ url: `/pdu/history-data/page`, params })
  },

  // 查询pdu历史数据详情
  getHistoryData: async (id: number) => {
    return await request.get({ url: `/pdu/history-data/get?id=` + id })
  },

  // 新增pdu历史数据
  createHistoryData: async (data: HistoryDataVO) => {
    return await request.post({ url: `/pdu/history-data/create`, data })
  },

  // 修改pdu历史数据
  updateHistoryData: async (data: HistoryDataVO) => {
    return await request.put({ url: `/pdu/history-data/update`, data })
  },

  // 删除pdu历史数据
  deleteHistoryData: async (id: number) => {
    return await request.delete({ url: `/pdu/history-data/delete?id=` + id })
  },

  // 导出pdu历史数据 Excel
  exportHistoryData: async (params) => {
    return await request.download({ url: `/pdu/history-data/export-excel`, params })
  },
}
