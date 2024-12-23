import request from '@/config/axios'

// 插接箱谐波颜色 VO
export interface BoxHarmonicColorVO {
  id: number // 自增id
  rangeOne: number // 第一个小于的范围
  rangeTwo: number // 第二个范围的最小值
  rangeThree: number // 第二个范围的最大值
  rangeFour: number // 第三个大于的范围
}

// 插接箱谐波颜色 API
export const BoxHarmonicColorApi = {
  // 查询插接箱不平衡度颜色分页
  getBoxHarmonicColorPage: async (params: any) => {
    return await request.get({ url: `/bus/box-harmonic-color/page`, params })
  },

  // 查询插接箱谐波颜色详情
  getBoxHarmonicColor: async () => {
    return await request.get({ url: `/bus/box-harmonic-color/get`  })
  },

  // 新增插接箱谐波度颜色
  createBoxHarmonicColor: async (data: BoxHarmonicColorVO) => {
    return await request.post({ url: `/bus/box-harmonic-color/create`, data })
  },

  // 修改插接箱谐波度颜色
  updateBoxHarmonicColor: async (data: BoxHarmonicColorVO) => {
    return await request.put({ url: `/bus/box-harmonic-color/update`, data })
  },

  // 删除插接箱谐波颜色
  deleteBoxHarmonicColor: async (id: number) => {
    return await request.delete({ url: `/bus/box-harmonic-color/delete?id=` + id })
  },

  // 导出插接箱谐波颜色 Excel
  exportBoxHarmonicColor: async (params) => {
    return await request.download({ url: `/bus/box-harmonic-color/export-excel`, params })
  },
}
