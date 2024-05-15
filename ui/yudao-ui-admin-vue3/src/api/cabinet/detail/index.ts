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
}