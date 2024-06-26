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

}
