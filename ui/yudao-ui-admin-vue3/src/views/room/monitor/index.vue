<template>
  <div class="demo-collapse">
    <div style="display: flex;align-items: center;justify-content: space-between">
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"                          
      >
        <div>
          <el-form-item label="机房名" prop="devKey">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入机房名"
              clearable
              class="!w-160px"
              height="35"
            />
          </el-form-item>
          <el-form-item style="margin-left: 10px;">
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
            <el-button
              type="primary"
              plain
              @click="openForm('create')"
              v-hasPermi="['pdu:PDU-device:create']"
            >
              <Icon icon="ep:plus" class="mr-5px" /> 新增
            </el-button>
            <el-button
              type="success"
              plain
              @click="handleExport"
              :loading="exportLoading"
              v-hasPermi="['pdu:PDU-device:export']"
            >
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
          </el-form-item>
        </div>
      </el-form>
      <div class="btns">
        <el-button @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />机房功率</el-button>                             
        <el-button @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />机房温度</el-button>            
        <el-button @click="valueMode = 2;" :type="valueMode == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />机房对比</el-button>    
        <el-button @click="handleAdd"><Icon icon="ep:grid" style="margin-right: 4px" />新建机房</el-button>        
        <el-button @click="switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>
        <el-button @click="switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
      </div>
    </div>
    <el-collapse v-model="activeNames" @change="handleChange">
      <el-collapse-item v-for="(e,i) in roomAddrList" :key="i" :title="e" :name="i" style="padding: 10px 0;">
        <el-skeleton :loading="loading" animated v-if="valueMode===0" style="padding: 0 20px">
            <el-row>
              <template v-if="roomShowType">
                <el-col>
                  <div ref="scrollableContainer" class="arrayContainer">
                    <div 
                      class="arrayItem"
                      v-for="(item, index) in addrAllRoomList[i]"
                      :key="`card-${index}`"
                    >
                      <el-card shadow="hover">
                        <div class="flex items-center h-21px mb-2">
                          <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                          <span class="text-15px">{{ item.roomName || '' }}</span>
                        </div>
                        <div style="display: flex;justify-content: space-around;align-items: center;">
                          <div style="">
                            <div><el-text>视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA</el-text></div>
                            <div><el-text>有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW</el-text></div>
                            <div><el-text>无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVAr</el-text></div>
                          </div>
                          <div style="display: flex;flex-direction: column">
                            <div style="text-align: center;font-size: 24px;">1.52</div>
                            <div style="text-align: center;font-size: 12px;">PUE</div>
                          </div>
                        </div>
                      </el-card>
                      <div style="position: absolute;bottom: 0;right: 0">
                        <button class="detail" @click="handleRoomHome(item.id)" >详情</button>
                      </div>
                    </div>
                  </div>
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
        <el-skeleton :loading="loading" animated v-else-if="valueMode===1" style="padding: 0 20px">
            <el-row>
              <template v-if="roomShowType">
                <el-col>
                  <div ref="scrollableContainer" class="arrayContainer" @scroll="handleScroll">
                    <div 
                      class="arrayItem"
                      v-for="(item, index) in powInfo.roomDataList"
                      :key="`card-${index}`"
                    >
                      <el-card shadow="hover" @dblclick="handleRoomHome(item.id)">
                        <div class="flex items-center h-21px">
                          <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                          <span class="text-15px">{{ item.name || '' }}</span>
                        </div>
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <el-text style="margin-left:3vw">前门</el-text>
                          <el-text style="margin-right:2vw">后门</el-text>
                        </div>
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <el-text>最高温度：{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</el-text>
                          <el-text style="margin-right:2vw">{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</el-text>
                        </div>
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <el-text>平均温度：{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</el-text>
                          <el-text style="margin-right:2vw">{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</el-text>
                        </div>
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <el-text>平均湿度：{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</el-text>
                          <el-text style="margin-right:2vw">{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}&deg;C</el-text>
                        </div>
                        <div style="position: absolute;bottom: 0;right: 0">
                          <button class="detail" @click="handleRoomHome(item.id)" >详情</button>
                        </div>
                      </el-card>
                    </div>
                  </div>
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
        <el-skeleton :loading="loading" animated v-else-if="valueMode===2" style="padding: 0 20px">
            <el-row>
              <template v-if="roomShowType">
                <el-col 
                  :xl="24"
                  :lg="24"
                  :md="24"
                  :sm="24"
                  :xs="24">
                  <Echart :options="powOptionsData" :height="400" :width="1702"/>
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
      </el-collapse-item>
  </el-collapse>
    <el-dialog v-model="dialogVisible" title="机房配置" width="30%" :before-close="handleDialogCancel">
      <el-form>
        <div style="margin-bottom: 5px">
          <el-text>机房</el-text>
        </div>
        <div class="double-formitem">
          <el-form-item label="名称" label-width="90">
            <el-input v-model="rowColInfo.roomName" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="楼层" prop="type" label-width="90">
            <el-select v-model="rowColInfo.addr" placeholder="请选择">
              <el-option label="未区分" value="未区分" />
              <el-option label="一楼" value="一楼" />
              <el-option label="二楼" value="二楼" />
              <el-option label="三楼" value="三楼" />
              <el-option label="四楼" value="四楼" />
              <el-option label="五楼" value="五楼" />
              <el-option label="六楼" value="六楼" />
              <el-option label="七楼" value="七楼" />
              <el-option label="八楼" value="八楼" />
              <el-option label="九楼" value="九楼" />
              <el-option label="十楼" value="十楼" />
            </el-select>
          </el-form-item>
        </div>
        <div style="margin-bottom: 5px">
          <el-text>地砖（地砖按60CM*60CM）</el-text>
        </div>
        <div class="double-formitem">
          <el-form-item label="行数" label-width="90">
            <el-input-number v-model="rowColInfo.row" :min="1" :max="100" controls-position="right" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="列数" label-width="90">
            <el-input-number v-model="rowColInfo.col" :min="1" :max="70" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
        
        <div style="margin-bottom: 5px">
          <el-text>容量</el-text>
        </div>
        <el-form-item label="机房总电力容量" label-width="160">
          <el-input v-model="rowColInfo.powerCapacity" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="非IT设备总额定功率" label-width="160">
          <el-input v-model="rowColInfo.airPower" placeholder="包括制冷系统（如空调、冷源设备、新风系统等）" />
        </el-form-item>

        <div class="double-formitem">
          <el-form-item label="显示选择" label-width="90">
            <el-switch v-model="rowColInfo.showLoadOrPue" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-radio-group v-model="rowColInfo.loadOrPue" size="large" style="margin-left: 15x;">
            <el-radio-button label="负载率" value="负载率"/>
            <el-radio-button label="PUE" value="PUE"/>
          </el-radio-group>
        </div>

        <div class="double-formitem">
          <el-form-item label="日用能告警" label-width="90">
            <el-switch v-model="rowColInfo.eleAlarmDay" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="日用能限制" label-width="90">
            <el-input-number v-model="rowColInfo.eleLimitDay" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
        <div class="double-formitem">
          <el-form-item label="月用能告警" label-width="90">
            <el-switch v-model="rowColInfo.eleAlarmMonth" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="月用能限制" label-width="90">
            <el-input-number v-model="rowColInfo.eleLimitMonth" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogCancel">取 消</el-button>
        <el-button type="primary" @click="submitSetting">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import type { CollapseModelValue } from 'element-plus'
import * as echarts from 'echarts';
import { formatTime } from '@/utils'
import { MachineRoomApi } from '@/api/cabinet/room'
import { MachineHomeApi } from '@/api/cabinet/home'

const activeNames = ref(0)
const valueMode = ref(0)
const switchValue = ref(0)
const roomFlag =ref();
const dialogVisible = ref(false);
const isAddRoom = ref(false) // 是否为添加机房模式 
const roomId = ref(0) // 房间id
const radio1 = ref('1')
const rowColInfo = reactive({
  roomName: '', // 机房名
  addr: '未区分', //楼层
  row: 14, // 行
  col: 18, // 列
  powerCapacity:0, //电力容量
  airPower: null, //空调额定功率
  loadOrPue: "负载率",
  showLoadOrPue: 0, // 显示选择
  eleAlarmDay: 0, // 日用能告警
  eleLimitDay: 1000, // 日用能限制
  eleAlarmMonth: 0, // 月用能告警
  eleLimitMonth: 1000, // 月用能限制
})
const queryParams = reactive({
  name: undefined,
})as any

const loading = ref(true)
const roomShowType = ref(true)
const powOptionsData = reactive<EChartsOption>({}) as EChartsOption
const powOptionsDataOne = reactive<EChartsOption>({}) as EChartsOption
const powInfo = reactive({}) // 功率数据信息
const powCopyInfo = reactive({})
const roomAddrList = ref([])
const addrAllRoomList = ref([[]])
const clickIndex = ref(0)

const list = ref([
  { 
    id:null,
    status:null,
    apparentPow:null,
    pow:null,
    ele:null,
    devKey:null,
    location:null,
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null,
    acur : null,
    bcur : null,
    ccur : null,
    curUnbalance : null,
  }
]) as any// 列表的数据
const item = ref(
  { 
    id:null,
    status:null,
    apparentPow:null,
    pow:null,
    ele:null,
    devKey:null,
    location:null,
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null,
    acur : null,
    bcur : null,
    ccur : null,
    curUnbalance : null,
  }
) as any// 列表的数据

const { push } = useRouter() // 路由跳转
const message = useMessage() // 消息弹窗

const handleRoomHome = (id) => {
  push({path: '/room/home', state: { roomId: id }})
}

// 处理弹窗取消事件
const handleDialogCancel = () => {
  dialogVisible.value = false;
  isAddRoom.value = false;
}

// 处理点击添加机房事件
const handleAdd = () => {
  roomFlag.value = 1;
  dialogVisible.value = true;
  resetForm();
}

// 重置表单
const resetForm = () => {
  Object.assign(rowColInfo, {
    roomName: '', // 机房名
    addr: '未区分', //楼层
    row: 14, // 行
    col: 18, // 列,
    powerCapacity:0,
    airPower:null,
    loadOrPue: "负载率",
    showLoadOrPue: 0, // 显示选择
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 1000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 1000, // 月用能限制
  })
}

// 处理设置提交
const submitSetting = async() => {
   let roomFlagId:any = null;
   let messageRoomFlag = "保存成功！";
   console.log("aaaaaaaaaa",rowColInfo)
   try {
    const res = await MachineRoomApi.saveRoomDetail({
      id: roomFlagId,
      roomName: rowColInfo.roomName,
      addr: rowColInfo.addr,
      xLength: rowColInfo.col,
      yLength: rowColInfo.row,
      powerCapacity:rowColInfo.powerCapacity, 
      airPower:rowColInfo.airPower, 
      eleAlarmDay: rowColInfo.eleAlarmDay,
      eleAlarmMonth: rowColInfo.eleAlarmMonth,
      eleLimitDay: rowColInfo.eleLimitDay,
      eleLimitMonth: rowColInfo.eleLimitMonth,
    })
    
    if(res != null || res != "")
    message.success(messageRoomFlag);
    dialogVisible.value = false;
    roomId.value = res;
   } catch (error) {
    console.log(error)
   }
   
  //  getRoomList();
}
// 获取主页面功率数据
const getHomePowData = async() => {
  const res =  await MachineHomeApi.getHomePowData({})
  Object.assign(powInfo, res)
  Object.assign(powCopyInfo, res)

  const modifiedRoomEqList = powInfo.roomDataList.map(item => ({
    ...item, // 复制对象的所有属性
    name: item.name + '1' // 修改name属性，在后面加上'*'号
  }))

  powInfo.roomDataList = [...powInfo.roomDataList,...modifiedRoomEqList] //添加了模拟数据

  console.log('111',powInfo.roomDataList)
  
  Object.assign(powOptionsData, {
    grid: {
      left: 50,
      right: 20,
      bottom: 20
    },
    legend: {
      right: 10,
      selectedMode: 'single'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function (params) {
        console.log('params', params)
        let result = '';
        params.forEach(function (item) {
          // item 是每一个系列的数据
          const seriesName = item.seriesName; // 系列名称
          const value = item.value; // 数据值
          const marker = item.marker; // 标志图形
          let unit = ''
          if (seriesName == '有功功率') {
            unit = 'kW'
          } else if (seriesName == '无功功率') {
            unit = 'kVar'
          } else if (seriesName == '视在功率') {
            unit = 'kVA'
          }
          result += `${marker}${seriesName}: ${value}${unit}<br/>`;
        });
        return result;
      }
    },
    xAxis: {
      type: 'category',
      data: powInfo.roomDataList.map(item => item.name)
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '有功功率',
        data: powInfo.roomDataList.map(item => item.powActive),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kW', // 显示数据值
        },
      },
      {
        name: '无功功率',
        data: powInfo.roomDataList.map(item => item.powReactive),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kVar', // 显示数据值
        },
      },
      {
        name: '视在功率',
        data: powInfo.roomDataList.map(item => item.powApparent),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kVA', // 显示数据值
        },
      },
      {
        name: '功率因素',
        data: powInfo.roomDataList.map(item => item.powerFactor),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
    ]
  })
  Object.assign(powOptionsDataOne, {
    grid: {
      left: 50,
      right: 20,
      bottom: 20
    },
    legend: {
      right: 10,
      selectedMode: 'single'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function (params) {
        console.log('params', params)
        let result = '';
        params.forEach(function (item) {
          // item 是每一个系列的数据
          const seriesName = item.seriesName; // 系列名称
          const value = item.value; // 数据值
          const marker = item.marker; // 标志图形
          let unit = ''
          if (seriesName == '最高温度' || seriesName == '最低温度' || seriesName == '目前温度' || seriesName == '平均温度') {
            unit = '℃'
          }
          result += `${marker}${seriesName}: ${value}${unit}<br/>`;
        });
        return result;
      }
    },
    xAxis: {
      type: 'category',
      data: powInfo.roomDataList.map(item => item.name)
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '最低温度',
        data: powInfo.roomDataList.map(item => item.powActive.toFixed(1)),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}℃', // 显示数据值
        },
      },
      {
        name: '平均温度',
        data: powInfo.roomDataList.map(item => item.powReactive.toFixed(1)),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}℃', // 显示数据值
        },
      },
      {
        name: '最高温度',
        data: powInfo.roomDataList.map(item => item.powApparent.toFixed(1)),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}℃', // 显示数据值
        },
      },
      {
        name: '目前温度',
        data: powInfo.roomDataList.map(item => item.powerFactor.toFixed(1)),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}℃', // 显示数据值
        },
      },
    ]
  })
  console.log('获取主页面功率数据', res)
}

//获取机房楼层
const getRoomAddrList = async() => {
  const res =  await MachineRoomApi.getRoomAddrList({})
  roomAddrList.value = res
  roomAddrList.value.forEach(async (item,index) => {
    const res2 = await MachineRoomApi.getAddrAllRoomList({addr: item})
    console.log(res2)
    addrAllRoomList.value[index] = res2
  })
}
const getAllApi = async () => {
  await getHomePowData()
  await getRoomAddrList()
  loading.value = false
}

getAllApi()

const handleChange = async (val: CollapseModelValue) => {
}
</script>

<style scoped lang="scss">
.arrayContainer {
  display: flex;
  flex-wrap: wrap;
  padding: 0 20px;
  .arrayItem {
    width: 25%;
    font-size: 13px;
    box-sizing: border-box;
    border: 5px solid #fff;
    position: relative;
    .content {
      display: flex;
      align-items: center;
      .icon {
        width: 74px;
        height: 30px;
        margin: 0 28px;
        font-size: large;
        text-align: center;
      }
    }
    .devKey{
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .room {
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .status {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      color: #fff;
      position: absolute;
      right: 38px;
      top: 8px;
    }
    .detail {
      width: 40px;
      height: 25px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 8px;
      bottom: 8px;
      cursor: pointer;
    }
  }
}
.btns {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 5px;
}
.double-formitem {
  display: flex;
  & > div {
    flex: 1;
  }
}

:deep(.el-collapse-item__header) {
  padding: 0 20px;
}
:deep(.el-card) {
  background-color: #eef4fc;
}
</style>