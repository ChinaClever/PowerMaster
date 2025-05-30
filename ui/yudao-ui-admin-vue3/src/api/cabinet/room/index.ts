import request from '@/config/axios'

export const MachineRoomApi = {
  //获取机房详情
  getRoomDetail: async (params: any) => {
    return await request.get({ url: `/room/detail`, params })
  },

  // getRoomDetail: async (params: any) => {
  //   return await request.get({ url: `/room/newDetail`, params })
  // },
  // 获取机房列表
  getRoomList: async (params: any) => {
    return await request.get({ url: `/room/list`, params })
  },
  // 获取机房数据详情
  getRoomDataDetail: async (params: any) => {
    return await request.get({ url: `/room/data/detail`, params })
  },
  getRoomDataNewDetail: async (params: any) => {
    return await request.get({ url: `/room/data/newDetail`, params })
  },
  // 获取机房主页面设备数据
  getRoomDevData: async (params: any) => {
    return await request.get({ url: `/room/main/dev/data`, params })
  },
  // 获取机房主页面功率数据
  getRoomPowData: async (params: any) => {
    return await request.get({ url: `/room/main/pow/data`, params })
  },
  // 获取机房主页面曲线数据
  getRoomEchartData: async (params: any) => {
    return await request.get({ url: `/room/main/curve/data`, params })
  },
  // 获取机房主页面环境数据
  getRoomEnvData: async (params: any) => {
    return await request.get({ url: `/room/main/env/data`, params })
  },
  // 获取机房主页面用能
  getRoomEqData: async (params: any) => {
    return await request.get({ url: `/room/main/eq`, params })
  },
  // 机房删除
  deleteRoom: async (params: any) => {
    return await request.get({ url: `/room/newDelete`, params })
  },
  // 修改机房详情
  saveRoomDetail: async (data: any) => {
    return await request.post({ url: `/room/newSave`, data })
  },

// 机房机柜新增/编辑
saveRoomAisle: async (data: any) => {
  return await request.post({ url: `/room/roomAisleSave`, data })
},

// 机房柜列新增/编辑
saveRoomCabinet: async (data: any) => {
  return await request.post({ url: `/room/roomCabinetSave`, data })
},

//判断柜列的位置是否存在机柜或柜列
findAddAisleVerify: async (data: any) => {
  return await request.post({ url: `/room/findAddAisleVerify`, data })
},

//机房柜列删除
deletedRoomAisleInfo: async (params: any) => {
  return await request.get({ url: `/room/roomAisleDelete`, params })
},

//查找同名机房
selectRoomByName: async (params: any) => {
  return await request.get({ url: `/room/newSelectRoomByName`, params })
},

//判断是否可以减少行数列数
findAreaById: async (params: any) => {
  return await request.get({ url: `/room/findAreaById`, params })
},

//获取机房主页折线图
getLineChartData: async (data: any) => {
  return await request.post({ url: `/room/index/chartDetail`, data })
},



//机房删除
deletedRoomInfo: async (data: any) => {
  return await request.post({ url: `/room/deletedRoomPage`, data })
},

//恢复机房删除
restoreRoomInfo: async (params: any) => {
  return await request.get({ url: `/room/restoreRoomInfo`, params })
},

//获取机房楼层
getRoomAddrList: async (params: any) => {
  return await request.get({ url: `/room/getRoomAddrList`, params })
},

//获取楼层的所有机房
getAddrAllRoomList: async (params: any) => {
  return await request.post({ url: `/room/roomList`, params })
},

//获取所有机房
getRoomAddrListAll: async (params: any) => {
  return await request.get({ url: `/room/getRoomAddrListAll`, params })
},

//获取机房设备数量
getMachineNum: async (params: any) => {
  return await request.get({ url: `/room/index/deviceStatistics`, params })
},

//导出机柜绑定关系表
exportAisleExcel: async ( params: any, axiosConfig) => {
  return await request.downloadPost({ url: `/room/editAisleExport` , params, ...axiosConfig})
},

//导入机柜绑定关系表
importAisleExcel: async ( data: any) => {
  return await request.upload({ url: `/room/editAisleExcel` , data})
},

}