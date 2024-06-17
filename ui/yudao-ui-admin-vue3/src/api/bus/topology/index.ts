import request from '@/config/axios'

export const BusTopologyApi = {
  // 获取机柜有功功率趋势
  getBusTopology: async (data: any) => {
    return await request.post({ url: `/bus/topology`, data })
  },
}