import request from '@/config/axios'

export const CabinetApi = {
  // 获取机柜详情
  getDetail: async (params: any) => {
    return await request.get({ url: `/cabinet/detail`, params })
  },
  // 获取机柜趋势曲线
  getPowTrend: async (params: any) => {
    return await request.get({ url: `/cabinet/powTrend`, params })
  },
  // 平衡详情电流趋势
  getBalanceTrend: async (params: any) => {
    return await request.get({ url: `/cabinet/curTrend`, params })
  },
  // 获取机架设备详情
  getframeDeviceDetail: async (params: any) => {
    return await request.get({ url: `/cabinet/frameDeviceDetail`, params })
  },

  getCabinetdistributionDetails: async (params: any) => {
    return await request.get({ url: `/cabinet/distributionDetails`, params })
  }
}