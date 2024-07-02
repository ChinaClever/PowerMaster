import request from '@/config/axios'

export const MachineRoomApi = {
  // 获取机房详情
  getRoomDetail: async (params: any) => {
    return await request.get({ url: `/room/detail`, params })
  },
  // 修改机房详情
  saveRoomDetail: async (data: any) => {
    return await request.post({ url: `/room/save`, data })
  },
}