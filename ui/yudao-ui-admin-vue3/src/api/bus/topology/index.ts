import request from '@/config/axios'

export const BusTopologyApi = {
  // 获取母线拓扑页面
  getBusTopology: async (data: any) => {
    return await request.post({ url: `/bus/topology`, data })
  },
}