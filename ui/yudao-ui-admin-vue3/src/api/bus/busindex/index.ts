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
  // 获得始端箱报表数据-基础数据
  getReportBasicInformationResVO: async (data: any) => {
    return await request.post({ url: `/bus/index/report/basicInformation`, data })
  },

 // 获得插接箱报表数据-基础数据
 getReportBasicInformationByBusResVO: async (data: any) => {
  return await request.post({ url: `/bus/index/report/basicInformationbybus`, data })
},

  // 查询已删除的始端箱索引分页
  getDeletedIndexPage: async (data: any) => {
    return await request.post({ url: `/bus/index/getDeletedPage`, data })
  },  
  //获得设备负载量状态统计
  getLoadRateStatus: async () => {
    return await request.get({ url: `/bus/index/loadRateStatus`})
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

  getEqMax: async () => {
    return await request.post({ url: `/bus/index/eq/maxEq` })
  },

  getBalancePage: async (data: any) => {
    return await request.post({ url: `/bus/index/balance`, data })
  },
//获得设备平衡统计
getBalanceStatistics: async () => {
  return await request.get({ url: `/bus/index/balance/statistics`})
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

  getBusPFLow: async () => {
    return await request.get({ url: `/bus/index/pf/lowest`})
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

  findKeys: async (params: any) => {
    return await request.get({ url: `/bus/index/findKeys`,params})
  },

  getBusLinePage: async (data: any) => {
    return await request.post({ url: `/bus/index/line/page`,data})
  },

  getBusLineMax: async (data: any) => {
    return await request.post({ url: `/bus/index/line/max` ,data})
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

  //始端箱需量数据图表数据
  getBusLineCurLinePage: async (data) => {
    return await request.post({ url: `/bus/index/line/cur/page` , data})
  },

  //始端箱需量数据图表数据导出
  getBusLineCurLineExcel: async ( data, axiosConfig) => {
    return await request.downloadPost({ url: `/bus/index/line/cur/excel` , data, ...axiosConfig })
  },

  //获得始端箱报表平均电流电压详细信息
  getAvgBusHdaLineForm: async (data: any) => {
    return await request.post({ url: `/bus/index/avg/busHdaLine/form` ,  data})
  },

  //始端箱温度详情导出
  getBusTemDetailExcel: async ( data: any, axiosConfig) => {
    return await request.downloadPost({ url: `/bus/index/tem/detailExcel` , data, ...axiosConfig })
  },
  
  //始端相功率因素详情导出
  getBusPFDetailExcel: async ( data: any, axiosConfig) => {
    return await request.downloadPost({ url: `/bus/index/pf/detailExcel` , data, ...axiosConfig })
  },

  //获得告警记录分页
  getRecordPage: async (data: any) => {
    return await request.post({ url: `/alarm/log-record/page`, data})
  },

  //获得始端箱设备统计
  getBusIndexStatistics: async () => {
    return await request.get({ url: `/bus/index/statistics` })
  },
}
