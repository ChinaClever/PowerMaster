import request from '@/config/axios'

export const AlarmApi = {
  // 告警记录列表分页
  getAlarmRecord: async (data: any) => {
    return await request.post({ url: `/alarm/log-record/page`, data })
  },
  // 告警记录修改
  saveAlarmRecord: async (data: any) => {
    return await request.post({ url: `/alarm/log-record/update`, data })
  },
  // 获取未处理告警数量
  getUnhandleAlarm: async (params: any) => {
    return await request.get({ url: `/alarm/log-record/level/count`, params })
  },
  // 获取告警配置
  getAlarmConfig: async (params: any) => {
    return await request.get({ url: `/alarm/cfg-prompt/list`, params })
  },
  // 保存告警配置
  saveAlarmConfig: async (data: any) => {
    return await request.post({ url: `/alarm/cfg-prompt/update`, data })
  },
  // 播放告警提示音
  playAudio: async (params: any) => {
    return await request.get({ url: `/alarm/cfg-prompt/play`, params })
  },
  // 关闭告警提示音
  stopAudio: async (params: any) => {
    return await request.get({ url: `/alarm/cfg-prompt/stop`, params })
  },
  // 获取邮件列表
  getMailList: async (params: any) => {
    return await request.get({ url: `/alarm/cfg-mail/list`, params })
  },
  // 获取手机列表
  getPhoneList: async (params: any) => {
    return await request.get({ url: `/alarm/cfg-sms/list`, params })
  },
  // 保存告警邮件配置
  saveMailConfig: async (data: any) => {
    return await request.post({ url: `/alarm/cfg-mail/batchSave`, data })
  },
  // 保存告警邮件配置
  savePhoneConfig: async (data: any) => {
    return await request.post({ url: `/alarm/cfg-sms/batchSave`, data })
  },
}