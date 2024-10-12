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
  getIndexPage: async (data: any) => {
    return await request.post({ url: `/bus/index/page`, data })
  },

  // 查询已删除的始端箱索引分页
  getDeletedIndexPage: async (data: any) => {
    return await request.post({ url: `/bus/index/getDeletedPage`, data })
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

  // 恢复始端箱索引
  restoreIndex: async (id: number) => {
    return await request.put({ url: `/bus/index/restore?id=` + id })
  },  

  // 导出始端箱索引 Excel
  exportIndex: async (params) => {
    return await request.download({ url: `/bus/index/export-excel`, params })
  },

  getBusRedisPage: async (data: any) => {
    return await request.post({ url: `/bus/index/powerpage`, data })
  },

  getEqPage: async (data: any) => {
    return await request.post({ url: `/bus/index/eq/page`, data })
  },

  getEqMax: async (data: any) => {
    return await request.post({ url: `/bus/index/eq/maxEq`, data })
  },

  getBalancePage: async (data: any) => {
    return await request.post({ url: `/bus/index/balance`, data })
  },

  getBusBalanceDetail: async (data: any) => {
    return await request.post({ url: `/bus/index/balance/detail` ,  data})
  },

  getBusBalanceTrend: async (data: any) => {
    return await request.post({ url: `/bus/index/balance/trend` ,  data})
  },

  getBusTemPage: async (data: any) => {
    return await request.post({ url: `/bus/index/bustempage`, data })
  },

  getBusTemDetail: async (data: any) => {
    return await request.post({ url: `/bus/index/tem/detail`, data })
  },

  getBusPFPage: async (data: any) => {
    return await request.post({ url: `/bus/index/buspfpage`, data })
  },

  getBusPFDetail: async (data: any) => {
    return await request.post({ url: `/bus/index/pf/detail`, data })
  },

  getBusHarmonicPage: async (data: any) => {
    return await request.post({ url: `/bus/index/busharmonicpage`, data })
  },

  getHarmonicRedis: async (data: any) => {
    return await request.post({ url: `/bus/index/harmonic/redis`, data })
  },

  getHarmonicLine: async (data: any) => {
    return await request.post({ url: `/bus/index/harmonic/line`, data })
  },

  devKeyList: async () => {
    return await request.download({ url: `/bus/index/devKeyList` })
  },

  getBusLinePage: async (data: any) => {
    return await request.post({ url: `/bus/index/line/page`,data})
  },

  getBusLineCurLine: async (data: any) => {
    return await request.post({ url: `/bus/index/line/cur`,data})
  },

  getBusMenu: async () => {
    return await request.get({ url: `/room/bus/menu` })
  },

  getBoxMenu: async () => {
    return await request.get({ url: `/room/box/menu` })
  },

  getBusIdByDevKey: async (data: any) => {
    return await request.post({ url: `/bus/index/getid` ,  data})
  },

  getBusPowerRedisData: async (data: any) => {
    return await request.post({ url: `/bus/index/power/detail` ,  data})
  },

  getBusLoadRateLine: async (data: any) => {
    return await request.post({ url: `/bus/index/power/loadrate` ,  data})
  },

  getBusPowActiveLine: async (data: any) => {
    return await request.post({ url: `/bus/index/power/powactive` ,  data})
  },

  getBusPowReactiveLine: async (data: any) => {
    return await request.post({ url: `/bus/index/power/powreactive` ,  data})
  },

  getConsumeData: async (data) => {
    return await request.post({ url: `/bus/index/report/ele`,data})
  },

  getBusPFLine : async (data) => {
    return await request.post({ url: `/bus/index/report/pfline`, data })
  },

  getPowData: async (data) => {
    return await request.post({ url: `/bus/index/report/pow`,data})
  },

  getTemData: async (data) => {
    return await request.post({ url: `/bus/index/report/tem`,data})
  },

  getBusRedisByDevKey: async (data) => {
    return await request.post({ url: `/bus/index/redisbydevkey`, data })
  },

  getPeakDemand: async (data: any) => {
    return await request.post({ url: `/bus/index/peakDemand` ,  data})
  },
}
