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
    return await request.post({ url: `/box/index/page`, data })
  },
  // 获得插接箱报表数据-基础数据
  getReportBasicInformationResVO: async (data: any) => {
    return await request.post({ url: `/box/index/report/basicInformation`, data })
  },

  // 查询已删除的始端箱索引分页
  getDeletedIndexPage: async (data: any) => {
    return await request.post({ url: `/box/index/getDeletedPage`, data })
  },  

  // 查询始端箱索引详情
  getIndex: async (id: number) => {
    return await request.get({ url: `/box/index/get?id=` + id })
  },
  //获得设备负载量状态统计
  getLoadRateStatus: async () => {
    return await request.get({ url: `/box/index/loadRateStatus`})
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

  // 恢复始端箱索引
  restoreIndex: async (id: number) => {
    return await request.put({ url: `/box/index/restore?id=` + id })
  },  

  // 导出始端箱索引 Excel
  exportIndex: async (params) => {
    return await request.download({ url: `/box/index/export-excel`, params })
  },

  getBoxRedisPage: async (data: any) => {
    return await request.post({ url: `/box/index/boxpage`, data })
  },

  getEqPage: async (data: any) => {
    return await request.post({ url: `/box/index/eq/page`, data })
  },

  getEqMax: async () => {
    return await request.post({ url: `/box/index/eq/maxEq`})
  },

  getBalancePage: async (data: any) => {
    return await request.post({ url: `/box/index/balance`, data })
  },
//获得设备平衡统计
  getBalanceStatistics: async () => {
    return await request.get({ url: `/box/index/balance/statistics`})
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

  getBoxHarmonicPage: async (data: any) => {
    return await request.post({ url: `/box/index/boxharmonicpage`, data })
  },

  getHarmonicRedis: async (data: any) => {
    return await request.post({ url: `/box/index/harmonic/redis`, data })
  },

  getHarmonicLine: async (data: any) => {
    return await request.post({ url: `/box/index/harmonic/line`, data })
  },

  devKeyList: async () => {
    return await request.download({ url: `/box/index/devKeyList` })
  },

  getBoxLinePage: async (data) => {
    return await request.post({ url: `/box/index/line/page`,data})
  },

  getBoxLineMax: async (data: any) => {
    return await request.post({ url: `/box/index/line/max`,data})
  },

  getBoxLineCurLine: async (data: any) => {
    return await request.post({ url: `/box/index/line/cur`,data})
  },

  getBoxIdByDevKey: async (data: any) => {
    return await request.post({ url: `/box/index/getid` ,  data})
  },

  getBoxMenu: async () => {
    return await request.get({ url: `/room/box/menu` })
  },

  getBoxPowerRedisData: async (data: any) => {
    return await request.post({ url: `/box/index/power/detail` ,  data})
  },

  getBoxLoadRateLine: async (data: any) => {
    return await request.post({ url: `/box/index/power/loadrate` ,  data})
  },

  getBoxPowActiveLine: async (data: any) => {
    return await request.post({ url: `/box/index/power/powactive` ,  data})
  },

  getBoxPowReactiveLine: async (data: any) => {
    return await request.post({ url: `/box/index/power/powreactive` ,  data})
  },

  getConsumeData: async (data) => {
    return await request.post({ url: `/box/index/report/ele`,data})
  },

  getBoxPFLine : async (data) => {
    return await request.post({ url: `/box/index/report/pfline`, data })
  },

  getPowData: async (data) => {
    return await request.post({ url: `/box/index/report/pow`,data})
  },

  getTemData: async (data) => {
    return await request.post({ url: `/box/index/report/tem`,data})
  },

  getBoxRedisByDevKey: async (data) => {
    return await request.post({ url: `/box/index/redisbydevkey`, data })
  },

  //插接箱需量数据图表数据
  getBoxLineCurLinePage: async (data) => {
    return await request.post({ url: `/box/index/line/cur/page` , data})
  },

  //插接箱需量数据图表数据导出
  getBoxLineCurLineExcel: async ( data, axiosConfig) => {
    return await request.downloadPost({ url: `/box/index/line/cur/excel` , data, ...axiosConfig })
  },

  //获得始端箱报表相平均电流电压详细信息
  getAvgBoxHdaLineForm: async (data: any) => {
    return await request.post({ url: `/box/index/avg/boxHdaLine/form` ,  data})
  },
    //获得始端箱报表回路平均电流电压详细信息
  getAvgBoxHdaLoopForm: async (data: any) => {
    return await request.post({ url: `/box/index/avg/boxHdaLoop/form` ,  data})
  },
  //插接箱温度详情导出
  getBoxTemDetailExcel: async ( data: any, axiosConfig) => {
    return await request.downloadPost({ url: `/box/index/tem/detailExcel` , data, ...axiosConfig })
  },
  
  //插接箱功率因素详情导出
  getBoxPFDetailExcel: async ( data: any, axiosConfig) => {
    return await request.downloadPost({ url: `/box/index/pf/detailExcel` , data, ...axiosConfig })
  },
  //获得告警记录分页
  getRecordPage: async (data: any) => {
    return await request.post({ url: `/system/alarm/record/page`, data})
  },
   //获得插接箱设备统计-去除连接器
    getBoxIndexStatistics: async () => {
      return await request.get({ url: `/box/index/statistics` })
    },
       //获得插接箱设备统计-所有
       getBoxIndexStatisticsAll: async () => {
        return await request.get({ url: `/box/index/statisticsAll` })
      },
}
