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
  getPDUDevicePage: async (data: any) => {
    return await request.post({ url: `/pdu/PDU-device/page`, data })
  },
    
  // 获取已删除的PDU设备
  getDeletedPDUDevice: async (data: any) => {
    return await request.post({ url: `/pdu/PDU-device/getDeletedPage`,data})
  },

  getPDUDeviceMaxCur: async (data: any) => {
    return await request.post({ url: `/pdu/PDU-device/line/getMaxCur`,data})
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
  deletePDUDevice: async (params: any) => {
    return await request.get({ url: `/pdu/PDU-device/delete`, params })
  },

  //恢复PDU设备
  restorePDUDevice: async (params: any) => {
    return await request.get({ url: `/pdu/PDU-device/restore`, params })
  },

  //获取PDU相历史数据
  getPDUHdaLineHisdata: async (params: any) => {
    return await request.get({ url: `pdu/PDU-device/pduHdaLineHisdata`, params})
  },

  //获取PDU需量详细数据
  getMaxLineHisdata: async (params: any) => {
    return await request.get({ url: `pdu/PDU-device/line/getMaxLine`, params})
  },

  // 导出PDU设备 Excel
  exportPDUDevice: async (params) => {
    return await request.download({ url: `/pdu/PDU-device/export-excel`, params })
  },

  PDUDisplay: async (params) => {
    return await request.get({ url: `/pdu/PDU-device/displayscreen`, params })
  },
  
  getLocation: async (params) => {
    return await request.get({ url: `/pdu/PDU-device/displayscreen/location`, params })
  },
  
  PDUHis: async (params) => {
    return await request.get({ url: `/pdu/PDU-device/hisdata/`, params})
  },

  ChartNewData: async (params) => {
    return await request.get({ url: `/pdu/PDU-device/chartNewData`,params})
  },

  getConsumeData: async (data) => {
    return await request.post({ url: `/pdu/PDU-device/report/ele`,data})
  },

  getPDUPFLine : async (data) => {
    return await request.post({ url: `/pdu/PDU-device/report/pfline`, data })
  },

  getPowData: async (data) => {
    return await request.post({ url: `/pdu/PDU-device/report/pow`,data})
  },

  getOutLetData: async (data) => {
    return await request.post({ url: `/pdu/PDU-device/report/outlet`,data})
  },

  getTemData: async (data) => {
    return await request.post({ url: `/pdu/PDU-device/report/tem`,data})
  },

  getPDULinePage: async (data) => {
    return await request.post({ url: `/pdu/PDU-device/line/page`,data})
  },

  getPDUMaxLineId: async (data) => {
    return await request.post({ url: `/pdu/PDU-device/line/getMaxLineId`,data})
  },  

  devKeyList: async () => {
    return await request.download({ url: `/pdu/PDU-device/devKeyList` })
  },

  ipList: async () => {
    return await request.download({ url: `/pdu/PDU-device/ipList` })
  },
  
}
