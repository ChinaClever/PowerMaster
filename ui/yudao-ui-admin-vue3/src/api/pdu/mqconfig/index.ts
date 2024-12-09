import request from '@/config/axios'

// 消息队列系统配置 VO
export interface MqConfigVO {
  id: number // 主键ID
  ipAndPorts: string // ip端口
  userName: string // 用户名
  passWord: string // 密码
  topic: string // 主题/分组
  mq: string // mq名称
}

// 消息队列系统配置 API
export const MqConfigApi = {
  // 查询消息队列系统配置分页
  getMqConfigPage: async (params: any) => {
    return await request.get({ url: `/sys/mq-config/page`, params })
  },

  // 查询消息队列系统配置详情
  getMqConfig: async (id: number) => {
    return await request.get({ url: `/sys/mq-config/get?id=` + id })
  },

  // 新增消息队列系统配置
  createMqConfig: async (data: MqConfigVO) => {
    return await request.post({ url: `/sys/mq-config/create`, data })
  },

  // 修改消息队列系统配置
  updateMqConfig: async (data: MqConfigVO) => {
    return await request.put({ url: `/sys/mq-config/update`, data })
  },

  // 删除消息队列系统配置
  deleteMqConfig: async (id: number) => {
    return await request.delete({ url: `/sys/mq-config/delete?id=` + id })
  },

  // 导出消息队列系统配置 Excel
  exportMqConfig: async (params) => {
    return await request.download({ url: `/sys/mq-config/export-excel`, params })
  },
}