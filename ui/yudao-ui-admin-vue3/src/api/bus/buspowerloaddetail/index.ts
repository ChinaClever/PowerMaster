import request from '@/config/axios'

export const BusPowerLoadDetailApi = {
 
  // 查询电力负荷详情
  getDetailData: async (params: any) => {
    return await request.post({ url: `/bus/PowerLoad/detail`, params })
  },

  // 查询电力负荷详情 折线图数据
  getLineChartData: async (params: any) => {
    return await request.post({ url: `/bus/PowerLoad/chart-detail`, params })
  },

  getBoxDetailData: async (params: any) => {
    return await request.post({ url: `/bus/PowerLoad/box/detail`, params })
  },

  // 查询电力负荷详情 折线图数据
  getBoxLineChartData: async (params: any) => {
    return await request.post({ url: `/bus/PowerLoad/box/chart-detail`, params })
  },
  //查询量数据
  getBoxEqChartData: async (params: any) => {
    return await request.post({ url: `/bus/PowerLoad/box/eqData`, params })
  },
  
  //查询量数据
  getBusEqChartData: async (params: any) => {
    return await request.post({ url: `/bus/PowerLoad/bus/eqData`, params })
  },

  //查询id
  getBusId: async (params: any) => {
    return await request.post({ url: `/bus/PowerLoad/bus/BusId`, params })
  },

  //查询id
  getBoxId: async (params: any) => {
    return await request.post({ url: `/bus/PowerLoad/bus/BoxId`, params })
  },

  getBusdevKeyList: async () => {
    return await request.download({ url: `/bus/PowerLoad/bus/devKeyList` })
  },

  getBoxdevKeyList: async () => {
    return await request.download({ url: `/bus/PowerLoad/box/devKeyList` })
  },

}
