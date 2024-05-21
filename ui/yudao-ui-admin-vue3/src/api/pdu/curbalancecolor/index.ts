import request from '@/config/axios'

// PDU不平衡度颜色 VO
export interface CurbalanceColorVO {
  id: number // 自增id
  rangeOne: number // 第一个小于的范围
  rangeTwo: number // 第二个范围的最小值
  rangeThree: number // 第二个范围的最大值
  rangeFour: number // 第三个大于的范围
}

// PDU不平衡度颜色 API
export const CurbalanceColorApi = {
  // 查询PDU不平衡度颜色分页
  getCurbalanceColorPage: async (params: any) => {
    return await request.get({ url: `/pdu/curbalance-color/page`, params })
  },

  // 查询PDU不平衡度颜色详情
  getCurbalanceColor: async () => {
    return await request.get({ url: `/pdu/curbalance-color/get` })
  },

  // 新增PDU不平衡度颜色
  createCurbalanceColor: async (data: CurbalanceColorVO) => {
    return await request.post({ url: `/pdu/curbalance-color/create`, data })
  },

  // 修改PDU不平衡度颜色
  updateCurbalanceColor: async (data: CurbalanceColorVO) => {
    return await request.put({ url: `/pdu/curbalance-color/update`, data })
  },

  // 删除PDU不平衡度颜色
  deleteCurbalanceColor: async (id: number) => {
    return await request.delete({ url: `/pdu/curbalance-color/delete?id=` + id })
  },

  // 导出PDU不平衡度颜色 Excel
  exportCurbalanceColor: async (params) => {
    return await request.download({ url: `/pdu/curbalance-color/export-excel`, params })
  },
}
