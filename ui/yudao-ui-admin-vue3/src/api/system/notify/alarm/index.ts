import request from '@/config/axios'

export const AlarmApi = {
  // 告警记录列表分页
  getAlarmRecord: async (data: any) => {
    return await request.post({ url: `/system/alarm/record/page`, data })
  },
  // 告警记录修改
  saveAlarmRecord: async (data: any) => {
    return await request.post({ url: `/system/alarm/record/save`, data })
  },
  // 获取未处理告警数量
  getUnhandleAlarm: async (params: any) => {
    return await request.get({ url: `/system/alarm/record/level/count`, params })
  },
  // 获取告警配置
  getAlarmConfig:async (params: any) => {
    return await request.get({ url: `/system/alarm/config/get`, params })
  },
  // 保存告警配置
  saveAlarmConfig:async (data: any) => {
    return await request.post({ url: `/system/alarm/config/save`, data })
  },
  // 获取邮件列表
  getMailList:async (params: any) => {
    return await request.get({ url: `/system/alarm/mail/list`, params })
  },
  // 获取手机列表
  getPhoneList:async (params: any) => {
    return await request.get({ url: `/system/alarm/sms/list`, params })
  },
}