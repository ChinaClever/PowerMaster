import request from '@/config/axios'

// 插接箱不平衡度颜色 VO
export interface BoxCurbalanceColorVO {
  id: number // 自增id
  rangeOne: number // 第一个小于的范围
  rangeTwo: number // 第二个范围的最小值
  rangeThree: number // 第二个范围的最大值
  rangeFour: number // 第三个大于的范围
}

// 插接箱不平衡度颜色 API
export const BoxCurbalanceColorApi = {
  // 查询插接箱不平衡度颜色分页
  getBoxCurbalanceColorPage: async (params: any) => {
    return await request.get({ url: `/bus/box-curbalance-color/page`, params })
  },

  // 查询插接箱不平衡度颜色详情
  getBoxCurbalanceColor: async (id: number) => {
    return await request.get({ url: `/bus/box-curbalance-color/get?id=` + id })
  },

  // 新增插接箱不平衡度颜色
  createBoxCurbalanceColor: async (data: BoxCurbalanceColorVO) => {
    return await request.post({ url: `/bus/box-curbalance-color/create`, data })
  },

  // 修改插接箱不平衡度颜色
  updateBoxCurbalanceColor: async (data: BoxCurbalanceColorVO) => {
    return await request.put({ url: `/bus/box-curbalance-color/update`, data })
  },

  // 删除插接箱不平衡度颜色
  deleteBoxCurbalanceColor: async (id: number) => {
    return await request.delete({ url: `/bus/box-curbalance-color/delete?id=` + id })
  },

  // 导出插接箱不平衡度颜色 Excel
  exportBoxCurbalanceColor: async (params) => {
    return await request.download({ url: `/bus/box-curbalance-color/export-excel`, params })
  },
}