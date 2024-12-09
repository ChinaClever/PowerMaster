import request from '@/config/axios'

export const CapacityApi = {
  // 获取机柜有功功率趋势
  getCapacityData: async (data: any) => {
    return await request.post({ url: `/cabinet/capacity/page`, data })
  },
  // // 获取机柜能页面
  // getEqPage: async (data: any) => {
  //   return await request.post({ url: `/cabinet/eq/page`, data })
  // },
}