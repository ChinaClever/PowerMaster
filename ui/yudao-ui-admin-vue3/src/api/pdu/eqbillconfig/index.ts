import request from '@/config/axios'

// pdu电量计费方式配置 VO
export interface EqBillConfigVO {
  id: number // 主键id
  bill: number // 电费单价
  billMode: number // 计费方式 1固定计费 2 分段计费
  billPeriod: string // 计费时间段
  createTime: Date
}

// pdu电量计费方式配置 API
export const EqBillConfigApi = {
  // 查询pdu电量计费方式配置分页
  getEqBillConfigPage: async (params: any) => {
    return await request.get({ url: `/pdu/eq-bill-config/page`, params })
  },

  // 查询pdu电量计费方式配置详情
  getEqBillConfig: async (id: number) => {
    return await request.get({ url: `/pdu/eq-bill-config/get?id=` + id })
  },

  // 新增pdu电量计费方式配置
  createEqBillConfig: async (data: EqBillConfigVO) => {
    return await request.post({ url: `/pdu/eq-bill-config/create`, data })
  },

  // 修改pdu电量计费方式配置
  updateEqBillConfig: async (data: EqBillConfigVO) => {
    return await request.put({ url: `/pdu/eq-bill-config/update`, data })
  },

  // 删除pdu电量计费方式配置
  deleteEqBillConfig: async (id: number) => {
    return await request.delete({ url: `/pdu/eq-bill-config/delete?id=` + id })
  },

  // 导出pdu电量计费方式配置 Excel
  exportEqBillConfig: async (params) => {
    return await request.download({ url: `/pdu/eq-bill-config/export-excel`, params })
  },
}
