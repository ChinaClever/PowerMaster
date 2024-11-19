import request from '@/config/axios'

export const RoomEnergyApi = {
  
  // 获取机柜有功功率趋势
  getActivePowTrend: async (params: any) => {
    return await request.get({ url: `/room/index/activePowTrend`, params })
  },
  // 获取机柜用能环比
  getEleChain: async (params: any) => {
    return await request.get({ url: `/room/index/eleChain`, params })
  },
  // 获取机柜能耗趋势
  getEleTrend: async (params: any) => {
    return await request.get({ url: `/room/index/eleTrend`, params })
  },
    // 获取机柜实时能耗趋势
  getRoomEleTotalRealtime: async (data: any) => {
    return await request.post({ url: `/room/index/eleTotalRealtime`, data })
  },
        // 获取实时电量 Excel
  getRoomEleTotalRealtimeExcel: async (data: any, axiosConfig) => {
    return await request.downloadPost({ url: `/room/index/eleTotalRealtimeExcel`, data, ...axiosConfig})
  },
  
}