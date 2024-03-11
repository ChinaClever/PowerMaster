import request from '@/config/axios'

// PDU设备 VO
export interface PDUDeviceVO {
  id: number // 编号
  devKey: string // 设备唯一识别码
  ipAddr: string // IP地址
  cascadeNum: number // 级联地址
}

// PDU设备 API
export const PDUDeviceApi = {
  // 查询PDU设备分页
  getPDUDevicePage: async (params: any) => {
    return await request.get({ url: `/pdu/PDU-device/page`, params })
  },

  // 查询PDU设备详情
  getPDUDevice: async (id: number) => {
    return await request.get({ url: `/pdu/PDU-device/get?id=` + id })
  },

  // 新增PDU设备
  createPDUDevice: async (data: PDUDeviceVO) => {
    return await request.post({ url: `/pdu/PDU-device/create`, data })
  },

  // 修改PDU设备
  updatePDUDevice: async (data: PDUDeviceVO) => {
    return await request.put({ url: `/pdu/PDU-device/update`, data })
  },

  // 删除PDU设备
  deletePDUDevice: async (id: number) => {
    return await request.delete({ url: `/pdu/PDU-device/delete?id=` + id })
  },

  // 导出PDU设备 Excel
  exportPDUDevice: async (params) => {
    return await request.download({ url: `/pdu/PDU-device/export-excel`, params })
  },
}