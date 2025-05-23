import request from '@/config/axios'

// 机柜索引 VO
export interface IndexVO {
  id: number // 主键id
  roomId: number // 机房id
  name: string // 机柜名称
  aisleId: number // 通道编号
  powCapacity: number // 电力容量
  pduBox: number // 数据来源 0：PDU 1：母线
  isDisabled: number // 禁用 0：启用 1：禁用
  isDeleted: number // 是否删除 0未删除 1已删除
  runStatus: number // 运行状态
}

// 机柜索引 API
export const IndexApi = {
  // 查询机柜索引分页
  getIndexPage: async (params: any) => {
    return await request.get({ url: `/cabinet/index/page`, params })
  },

  // 查询机柜索引详情
  getIndex: async (id: number) => {
    return await request.get({ url: `/cabinet/index/get?id=` + id })
  },

  // 新增机柜索引
  createIndex: async (data: IndexVO) => {
    return await request.post({ url: `/cabinet/index/create`, data })
  },

  // 修改机柜索引
  updateIndex: async (data: IndexVO) => {
    return await request.put({ url: `/cabinet/index/update`, data })
  },

  // 删除机柜索引
  deleteIndex: async (id: number) => {
    return await request.delete({ url: `/cabinet/index/delete?id=` + id })
  },

  // 导出机柜索引 Excel
  exportIndex: async (params) => {
    return await request.download({ url: `/cabinet/index/export-excel`, params })
  },

  // 获取机架信息
  getRackByCabinet: async (params: any) => {
    return await request.get({ url: `/cabinet/index/getRackByCabinet`, params })
  },

  getCabinetPFDetail: async (data: any) => {
    return await request.post({ url: `/pf/detail`, data })
  },

  //始端相功率因素详情导出
  getCabinetPFDetailExcel: async ( data: any, axiosConfig) => {
    return await request.downloadPost({ url: `/pf/detailExcel` , data, ...axiosConfig })
  },

  getConsumeData: async (params) => {
    return await request.get({ url: `/cabinet/index/report/ele`,params})
  },

    getPduBasicInformation: async (data) => {
      return await request.post({ url: `/pdu/PDU-device/pduBasicInformation`,data})
    },

  getPowData: async (params) => {
    return await request.get({ url: `/cabinet/index/report/pow`,params})
  },

    getPowDataByType: async (params) => {
    return await request.get({ url: `/cabinet/index/report/powByType`,params})
  },


  getTemData: async (params) => {
    return await request.get({ url: `/cabinet/index/report/tem`,params})
  },
  getCabinetEnvPage: async (data : any) => {
    return await request.post({ url: `/cabinet/env/page`,data})
  },
  getCabinetIceTemAndHumById: async (params) => {
    return await request.get({ url: `/cabinet/index/env/ice`,params})
  },

    getCabinetIceTemAndHumByTypeById: async (params) => {
    return await request.get({ url: `/cabinet/index/env/iceByType`,params})
  },

   getCabinetIceAndHotTemAndHumByTypeById: async (params) => {
    return await request.get({ url: `/cabinet/index/env/getIceAndHot`,params})
  },
  

  getCabinetHotTemAndHumById: async (params) => {
    return await request.get({ url: `/cabinet/index/env/hot`,params})
  },
  getCabinetPFLine : async (params) => {
    return await request.get({ url: `/cabinet/index/report/pfline`, params })
  },
    getCabinetPFLineByType : async (params) => {
    return await request.get({ url: `/cabinet/index/report/pfLineByType`, params })
  },
  getEleByCabinet : async (params) => {
    return await request.get({ url: `/cabinet/index/getEleByCabinet`, params })
  },
  // 查询所有颜色
  getCabinetColorAll : async () => {
    return await request.get({ url: `/cabinet/tem-color/all` })
  },

  idList: async () => {
    return await request.download({ url: `/cabinet/index/idList`})
  },
}
