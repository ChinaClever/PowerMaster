import request from '@/config/axios'

export const AisleEnergyApi = {
  
  // 获取机柜有功功率趋势
  getActivePowTrend: async (params: any) => {
    return await request.get({ url: `/aisle/index/activePowTrend`, params })
  },
  // 获取机柜用能环比
  getEleChain: async (params: any) => {
    return await request.get({ url: `/aisle/index/eleChain`, params })
  },
  // 获取机柜能耗趋势
  getEleTrend: async (params: any) => {
    return await request.get({ url: `/aisle/index/eleTrend`, params })
  },
  //删除柜列
  deleteAisle: async (params: any) => {
    return await request.get({ url: `/aisle/delete`, params })
  },

}