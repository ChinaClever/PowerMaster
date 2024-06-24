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
    return await request.get({ url: `/bus/index/page`, params })
  },

  // 查询始端箱索引详情
  getIndex: async (id: number) => {
    return await request.get({ url: `/bus/index/get?id=` + id })
  },

  // 新增始端箱索引
  createIndex: async (data: IndexVO) => {
    return await request.post({ url: `/bus/index/create`, data })
  },

  // 修改始端箱索引
  updateIndex: async (data: IndexVO) => {
    return await request.put({ url: `/bus/index/update`, data })
  },

  // 删除始端箱索引
  deleteIndex: async (id: number) => {
    return await request.delete({ url: `/bus/index/delete?id=` + id })
  },

  // 导出始端箱索引 Excel
  exportIndex: async (params) => {
    return await request.download({ url: `/bus/index/export-excel`, params })
  },

  getBusRedisPage: async (params: any) => {
    return await request.get({ url: `/bus/index/buspage`, params })
  },

  getEqPage: async (data: any) => {
    return await request.post({ url: `/bus/index/eq/page`, data })
  },

  getBalancePage: async (data: any) => {
    return await request.post({ url: `/bus/index/balance`, data })
  },

  getBusTemPage: async (data: any) => {
    return await request.post({ url: `/bus/index/bustempage`, data })
  },

  getBusPFPage: async (data: any) => {
    return await request.post({ url: `/bus/index/buspfpage`, data })
  },

  getBusHarmonicPage: async (params: any) => {
    return await request.get({ url: `/bus/index/busharmonicpage`, params })
  },

  devKeyList: async () => {
    return await request.download({ url: `/bus/index/devKeyList` })
  },

  getBusLinePage: async (data: any) => {
    return await request.post({ url: `/bus/index/line/page`,data})
  },

  getBusTemDetail: async (data: any) => {
    return await request.post({ url: `/bus/index/tem/detail`, data })
  },

  getHarmonicRedis: async (data: any) => {
    return await request.post({ url: `/bus/index/harmonic/redis`, data })
  },

  getHarmonicLine: async (data: any) => {
    return await request.post({ url: `/bus/index/harmonic/line`, data })
  },
}
