import request from '@/config/axios'
import exp from 'constants'
import { get } from 'http'

// 机柜历史数据 API
export const HistoryDataApi = {

  // 查询机柜历史数据分页
  getHistoryDataPage: async (params: any) => {
    return await request.get({ url: `/cabinet/history-data/page`, params })
  },

  // 查询机柜历史数据详情
  getHistoryDataDetails: async (params: any) => {
    return await request.get({ url: `/cabinet/history-data/details`, params })
  },

  //查询机柜历史环境数据
  getHistoryEnvData: async (data: any) => {
    return await request.post({ url: `/cabinet/history-data/pageEnv`, data })
  },

  //查询机柜历史环境详细数据
  getHistoryEnvDetailData: async (params: any) => {
    return await request.get({ url: `/cabinet/history-data/env-details`, params })
  },

  //获得机柜环境数据插入数据量
  getHistoryEnvDataCount: async () => {
    return await request.get({ url: `/cabinet/history-data/new-env-data`})
  },

  // 导出机柜历史数据 Excel
  exportHistoryData: async (params, axiosConfig) => {
    return await request.download({ url: `/cabinet/history-data/export-excel`, params, ...axiosConfig })
  },

  // 查询机柜电力数据导航的新增记录数据显示
  getNavNewData: async (granularity: string) => {
    return await request.get({ url: `/cabinet/history-data/new-data/`+granularity})
  },
  // 导出机柜历史数据 Excel
  exportHistorydetailsPageData: async (params, axiosConfig) => {
    return await request.download({ url: `/cabinet/history-data/details-export-excel`, params, ...axiosConfig })
  },
  
  //导出机柜历史环境数据
  exportHistoryEnvData: async (params, axiosConfig) => {
    return await request.downloadPost({ url: `/cabinet/history-data/env-export`, params, ...axiosConfig })
  },
  exportHistoryEnvDetailData: async (params, axiosConfig) => {
    return await request.downloadPost({ url: `/cabinet/history-data/env-detail-export`, params, ...axiosConfig })
  },
}
