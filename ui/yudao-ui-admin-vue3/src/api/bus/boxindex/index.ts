import request from '@/config/axios'

// 始端箱索引 VO
export interface IndexVO {
  id: number // id
  devKey: string // 设备识别码
  ipAddr: string // ip地址
  devAddr: string // 母线地址
  barId: number // 母线编号
  runStatus: number // 运行状态 0：正常 1：预警 2：告警 3: 升级 4：故障 5：离线
  nodeIp: string // 节点IP
  isDeleted: number // 逻辑删除
}

// 始端箱索引 API
export const IndexApi = {
  // 查询始端箱索引分页
  getIndexPage: async (params: any) => {
    return await request.get({ url: `/box/index/page`, params })
  },

  // 查询始端箱索引详情
  getIndex: async (id: number) => {
    return await request.get({ url: `/box/index/get?id=` + id })
  },

  // 新增始端箱索引
  createIndex: async (data: IndexVO) => {
    return await request.post({ url: `/box/index/create`, data })
  },

  // 修改始端箱索引
  updateIndex: async (data: IndexVO) => {
    return await request.put({ url: `/box/index/update`, data })
  },

  // 删除始端箱索引
  deleteIndex: async (id: number) => {
    return await request.delete({ url: `/box/index/delete?id=` + id })
  },

  // 导出始端箱索引 Excel
  exportIndex: async (params) => {
    return await request.download({ url: `/box/index/export-excel`, params })
  },

  getBoxRedisPage: async (params: any) => {
    return await request.get({ url: `/box/index/boxpage`, params })
  },

  getEqPage: async (data: any) => {
    return await request.post({ url: `/box/index/eq/page`, data })
  },

  getBalancePage: async (data: any) => {
    return await request.post({ url: `/box/index/balance`, data })
  },

  getBoxBalanceDetail: async (data: any) => {
    return await request.post({ url: `/box/index/balance/detail` ,  data})
  },

  getBoxBalanceTrend: async (data: any) => {
    return await request.post({ url: `/box/index/balance/trend` ,  data})
  },

  getBoxTemPage: async (data: any) => {
    return await request.post({ url: `/box/index/boxtempage`, data })
  },

  getBoxTemDetail: async (data: any) => {
    return await request.post({ url: `/box/index/tem/detail`, data })
  },

  getBoxPFPage: async (data: any) => {
    return await request.post({ url: `/box/index/boxpfpage`, data })
  },

  getBoxPFDetail: async (data: any) => {
    return await request.post({ url: `/box/index/pf/detail`, data })
  },

  getBoxHarmonicPage: async (params: any) => {
    return await request.get({ url: `/box/index/boxharmonicpage`, params })
  },

  devKeyList: async () => {
    return await request.download({ url: `/box/index/devKeyList` })
  },

  getBoxLinePage: async (data) => {
    return await request.post({ url: `/box/index/line/page`,data})
  },

  getBoxLineCurLine: async (data: any) => {
    return await request.post({ url: `/box/index/line/cur`,data})
  },
}
