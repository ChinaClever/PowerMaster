import request from '@/config/axios'

// 通道列 VO
export interface IndexVO {
  id: number // 主键id
  roomId: number // 机房id
  name: string // 通道名称
  pduBar: number // 数据来源
  isDelete: number // 是否删除
  length: number // 长度
  type: string // 柜列类型
}

// 通道列 API
export const IndexApi = {
  // 查询通道列分页
  getIndexPage: async (data: any) => {
    return await request.post({ url: `/aisle/index/page`, data })
  },

  // 查询通道列详情
  getIndex: async (id: number) => {
    return await request.get({ url: `/aisle/index/get?id=` + id })
  },

  // 新增通道列
  createIndex: async (data: IndexVO) => {
    return await request.post({ url: `/aisle/index/create`, data })
  },

  // 修改通道列
  updateIndex: async (data: IndexVO) => {
    return await request.put({ url: `/aisle/index/update`, data })
  },

  // 删除通道列
  deleteIndex: async (id: number) => {
    return await request.delete({ url: `/aisle/index/delete?id=` + id })
  },

  // 恢复通道列
  restoreAisleInfo: async (params: any) => {
    return await request.get({ url: `/aisle/index/restore`, params })
  },

  // 删除通道列分页
  deletedAisleInfo: async (data: any) => {
    return await request.post({ url: `/aisle/index/delPage`, data })
  },

  // 导出通道列 Excel
  exportIndex: async (params) => {
    return await request.download({ url: `/aisle/index/export-excel`, params })
  },

  devKeyList: async () => {
    return await request.download({ url: `/aisle/index/devKeyList` })
  },

  getBusMenu: async () => {
    return await request.get({ url: `/room/bus/menu` })
  },

  getAisleMenu: async () => {
    return await request.get({ url: `/room/aisle/menu` })
  },

  getAisleRedisPage: async (data: any) => {
    return await request.post({ url: `/aisle/index/powerpage`, data })
  },

  getLineChartData: async (data: any) => {
    return await request.post({ url: `/aisle/index/chartDetail`, data })
  },

  getEqPage: async (data: any) => {
    return await request.post({ url: `/aisle/index/eq/page`, data })
  },

  getAislePFPage: async (data: any) => {
    return await request.post({ url: `/aisle/index/buspfpage`, data })
  },

  getAislePFDetail: async (data: any) => {
    return await request.post({ url: `/aisle/index/pf/detail`, data })
  },

  getAisleLinePage: async (data: any) => {
    return await request.post({ url: `/aisle/index/line/page`,data})
  },

  getAisleLineCurLine: async (data: any) => {
    return await request.post({ url: `/aisle/index/line/cur`,data})
  },

  getAisleBalancePage: async (data: any) => {
    return await request.post({ url: `/aisle/index/balancepage`,data})
  },

 getAisBasicInformation: async (data: any) => {
    return await request.post({ url: `/aisle/index/aisBasicInformation`,data})
  },


  getAisleEleChain: async (params: any) => {
    return await request.get({ url: `/aisle/index/eleChain`, params })
  },

  getConsumeData: async (data) => {
    return await request.post({ url: `/aisle/index/report/ele`,data})
  },

    getConsumeDataById: async (data) => {
    return await request.post({ url: `/aisle/index/report/eleUse`,data})
  },

  getPowData: async (data) => {
    return await request.post({ url: `/aisle/index/report/pow`,data})
  },

    getPowDataByType: async (data) => {
    return await request.post({ url: `/aisle/index/report/powByType`,data})
  },

    getCabinetEleByAis: async (params) => {
      return await request.get({ url: `/cabinet/index/getEleByAisle`,params})
    },

     getAisleHdaLineData: async (params) => {
      return await request.get({ url: `/aisle/index/aisleHdaLineHisdataReport`,params})
    },
    // 获取机柜详情
    getCabinetDetail: async (params: any) => {
      return await request.get({ url: `/cabinetBasicInformation`, params })
    },

  getAislePFLine : async (data) => {
    return await request.post({ url: `/aisle/index/report/pfline`, data })
  },

    getAislePFLineByType : async (data) => {
    return await request.post({ url: `/aisle/index/report/pflineByType`, data })
  },


  idList: async () => {
    return await request.download({ url: `/aisle/index/idList`})
  },

  getAislePFDetailExcel: async (data) => {
    return await request.downloadPost({ url: `/aisle/index/pf/detail/excel`, data })
  },

  getBalanceDetail: async (data) => {
    return await request.post({ url: `/aisle/index/balance/chart?id=${data}`, })
  },

  getMaxEq: async () => {
    return await request.post({ url: `/aisle/index/eq/maxEq`})
  },
  getMaxAndMinPowFac: async () => {
    return await request.get({ url: `/aisle/index/findAisleFactor`})
  },
  getMaxApparentPower: async (data) => {
    return await request.post({ url: `/aisle/index/line/max`,data})
  },
  getRecordPage: async (data: any) => {
    return await request.post({ url: `/alarm/log-record/page`, data})
  },
    getRecordPageByType: async (data: any) => {
    return await request.post({ url: `/alarm/log-record/pdu`, data})
  },
}
