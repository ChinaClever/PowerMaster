import request from '@/config/axios'

export const CabinetEnergyApi = {
  // 获取机柜有功功率趋势
  getActivePowTrend: async (params: any) => {
    return await request.get({ url: `/cabinet/activePowTrend`, params })
  },
  // 获取机柜用能环比
  getEleChain: async (params: any) => {
    return await request.get({ url: `/cabinet/eleChain`, params })
  },
  // 获取机柜能耗趋势
  getEleTrend: async (params: any) => {
    return await request.get({ url: `/cabinet/eleTrend`, params })
  },
  // 获取机柜能页面
  getEqPage: async (data: any) => {
    return await request.post({ url: `/cabinet/eq/page`, data })
  },
}