import request from '@/config/axios'

export const maintainApi = {
  // 重发
  getreNotice: async (params: any) => {
    return await request.get({ url: `/system/upgrade/file/reNotice`, params })
  },
  // 文件上传记录列表分页
  getUpgradeFilePage: async (data: any) => {
    return await request.post({ url: `/system/upgrade/file/page`, data })
  },
  // 文件升级记录列表分页
  getUpgradeRecordPage: async (data: any) => {
    return await request.post({ url: `/system/upgrade/record/page`, data })
  },
  // 升級文件上傳
  getUpgradeFillUpload: async (params:any, data: any) => {
    return await request.post({ url: `/system/upgrade/file/upload`, data, params, headersType: 'multipart/form-data' })
  },
  // 删除升级记录
  deleteUpgradeRecord: async (ids: any) => {
    return await request.delete({ url: `system/upgrade/record/delete?ids=` + ids })
  },
  // 删除文件上传记录
  deleteFillRecord: async (ids: any) => {
    return await request.delete({ url: `system/upgrade/file/delete?ids=` + ids })
  },
}