import request from '@/config/axios'

// pdu历史数据 VO
export interface energyConsumptionVO {
  id: number 
  pduId: number 
  activePow: number 
  apparentPow: number
  powerFactor: number 
  createTime: string 
}

// pdu历史数据 API
export const EnergyConsumptionApi = {
  // 查询pdu数据参数类型各id最大值
  getTypeMaxValue: async () => {
    return await request.get({ url: `/pdu/eq-data/type-max-value`})
  },

  // 查询pdu电量数据分页
  getEQDataPage: async (params: any) => {
    return await request.get({ url: `/pdu/eq-data/page`, params })
  },

  // 查询pdu历史数据详情
  getEQDataDetails: async (params: any) => {
    return await request.get({ url: `/pdu/eq-data/details`, params })
  },

}
