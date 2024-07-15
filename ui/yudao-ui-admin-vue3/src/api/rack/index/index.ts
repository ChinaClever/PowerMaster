import request from '@/config/axios'

// 机架索引 VO
export interface IndexVO {
  id: number // 机架id
  cabinetId: number // 机柜id
  roomId: number // 机房id
  rackName: string // U位名称
  isDelete: number // 是否删除 0未删除 1已删除
  outletIdA: string // A路PDU输出位
  outletIdB: string // B路PDU输出位
  company: string // 所属公司
  uAddress: number // U位位置
  uHeight: number // U位高度
  type: string // 设备类型
}

// 机架索引 API
export const IndexApi = {
  // 查询机架索引分页
  getIndexPage: async (params: any) => {
    return await request.get({ url: `/rack/index/page`, params })
  },

  // 查询机架索引详情
  getIndex: async (id: number) => {
    return await request.get({ url: `/rack/index/get?id=` + id })
  },

  // 新增机架索引
  createIndex: async (data: IndexVO) => {
    return await request.post({ url: `/rack/index/create`, data })
  },

  // 修改机架索引
  updateIndex: async (data: IndexVO) => {
    return await request.put({ url: `/rack/index/update`, data })
  },

  // 删除机架索引
  deleteIndex: async (id: number) => {
    return await request.delete({ url: `/rack/index/delete?id=` + id })
  },

  // 导出机架索引 Excel
  exportIndex: async (params) => {
    return await request.download({ url: `/rack/index/export-excel`, params })
  },

  getConsumeData: async (params) => {
    return await request.get({ url: `/rack/index/report/ele`,params})
  },

  getPowData: async (params) => {
    return await request.get({ url: `/rack/index/report/pow`,params})
  },

  getRackPFLine : async (params) => {
    return await request.get({ url: `/rack/index/report/pfline`, params })
  },

  getRackAll: async (params: any) => {
    return await request.get({ url: `/room/rack/menu`, params })
  },

  getRackRedis: async (params: any) => {
    return await request.download({ url: `/rack/index/redisData`, params })
  },
}
