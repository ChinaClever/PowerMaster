<template>
  <ContentWrap>
    <div class="flex justify-between">
      <el-form
        class="-mb-15px topForm"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="" prop="jf" >
          机房：<el-select v-model="queryParams.cabinetroomId" placeholder="请选择" class="!w-100px">
            <el-option v-for="item in roomList" :key="item.roomId" :label="item.roomName" :value="item.roomId" />
          </el-select>
        </el-form-item >
        <span class="line"></span>
        <el-form-item label="" prop="jg">
          柜列：<el-select v-model="queryParams.cabinetColumnId" placeholder="请选择" class="!w-100px">
            <el-option v-for="item in machineList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="btns">
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
    </div>
  </ContentWrap>
  <ContentWrap>
  <div class="topologyContainer">
      <div class="loadA">
        <div class="box" v-if="startingBoxA">
          <div class="title">A路始端箱</div>
          <div v-if="chosenBtn==6">
            <div class="info" v-for="(item, index) in startingBoxATEM[btns[chosenBtn].paramBus]" :key="index">
              <div>{{btns[chosenBtn].unitName}}{{letters[index]}}</div>
              <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
            </div>
          </div>
          <div v-else-if="chosenBtn!=6">
            <div class="info" v-for="(item, index) in startingBoxA[btns[chosenBtn].paramBus]" :key="index">
              <div>{{btns[chosenBtn].unitName}}{{letters[index]}}</div>
              <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
            </div>
          </div>
        </div>
        <div class="box-empty" v-else></div>
        <div class="line"></div>
        <div class="boxList">
          <div class="boxItem" v-for="(box, index) in pluginBoxA.filter(item=>item.data !== undefined)" :key="index">
            <div class="layer"></div>
            <div class="stage" v-if="box.data.box_data.box_cfg.box_type==0"></div>
            <div class="box" v-if="box.data.box_data.box_cfg.box_type==0">
              <div class="title">A{{(index + 1)}}#插接箱</div>
              <div v-if="box.data">
              <template v-if="chosenBtn!=6">
                <div class="info" v-for="(item, i) in box.data.box_data.line_item_list[btns[chosenBtn].paramBox]" :key="i">
                  <div>{{btns[chosenBtn].unitName}}{{letters[i]}}</div>
                  <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
                </div>
              </template>
              <template v-else>
                <div class="info" v-for="(item, i) in box.data.env_item_list[btns[chosenBtn].paramBox]" :key="i">
                  <div>{{btns[chosenBtn].unitName}}{{letters[i]}}</div>
                  <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
                </div>
              </template>
            </div>
            </div>
            <div class="boxconnect" v-if="box.data.box_data.box_cfg.box_type==1">
              <div class="title">A{{(index + 1)}}#连接器</div>
              <template v-if="chosenBtn==6">
                <div class="info" v-for="(item, i) in box.data.env_item_list[btns[chosenBtn].paramBox]" :key="i">
                  <div>{{btns[chosenBtn].unitName}}{{letters[i]}}</div>
                  <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
                </div>
              </template>
            </div>
          </div>
          <div class="tip">A路</div>
        </div>
      </div>
      <div class="loadB">
        <div class="box boxB" v-if="startingBoxB">
          <div class="title">B路始端箱</div>
            <div v-if="chosenBtn==6">
              <div class="info" v-for="(item,index) in startingBoxBTEM[btns[chosenBtn].paramBus]" :key="index">
                <div>{{btns[chosenBtn].unitName}}{{letters[index]}}</div>
                <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
              </div>
            </div>
            <div v-else-if="chosenBtn!=6">
              <div class="info" v-for="(item,index) in startingBoxB[btns[chosenBtn].paramBus]" :key="index">
                <div>{{btns[chosenBtn].unitName}}{{letters[index]}}</div>
                <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
              </div>
            </div>
        </div>
        <div class="box-empty boxB" v-else></div>
        <div class="line"></div>
        <div class="boxList">
          <div class="boxItem" v-for="(box, index) in pluginBoxB.filter(item=>item.data !== undefined)" :key="index">
            <div v-if="box && box.data">
            <div class="box" v-if="box.data.box_data.box_cfg.box_type==0">
              <div class="title">B{{(index + 1)}}#插接箱</div>
              
              <template v-if="chosenBtn!=6">
                <div class="info" v-for="(item, i) in box.data.box_data.line_item_list[btns[chosenBtn].paramBox]" :key="i">
                  <div>{{btns[chosenBtn].unitName}}{{letters[i]}}</div>
                  <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
                </div>
              </template>
              <template v-else>
                <div class="info" v-for="(item, i) in box.data.env_item_list[btns[chosenBtn].paramBox]" :key="i">
                  <div>{{btns[chosenBtn].unitName}}{{letters[i]}}</div>
                  <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
                </div>
              </template>
            </div>
          </div>
            <div class="boxconnect" v-if="box.data.box_data.box_cfg.box_type==1">
              <div class="title">B{{(index + 1)}}#连接器</div>
              <template v-if="chosenBtn==6">
                <div class="info" v-for="(item, i) in box.data.env_item_list[btns[chosenBtn].paramBox]" :key="i">
                  <div>{{btns[chosenBtn].unitName}}{{letters[i]}}</div>
                  <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
                </div>
              </template>
            </div>
            <div class="stage" v-if="box.data.box_data.box_cfg.box_type==0"></div>
            <div class="layer"></div>
          </div>
          <div class="tip">B路</div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { BusTopologyApi } from '@/api/bus/topology'
import { MachineColumnApi } from '@/api/cabinet/column'

const queryParams = reactive({
  cabinetColumnId: history?.state?.id,
  cabinetroomId: history?.state?.roomId
})
const roomList = ref<any>([]) // 机房列表
const machineList = ref<any>([]) // 机柜列列表

const btns = [
  {
    name: '电流',
    value: 0,
    unit: 'A',
    unitName: 'I',
    paramBox: 'cur_value',
    paramBus: 'cur_value',
    fixNum: 2,
  },
  {
    name: '电压',
    value: 1,
    unit: 'V',
    unitName: 'U',
    paramBox: 'vol_value',
    paramBus: 'vol_value',
    fixNum: 1,
  },
  {
    name: '有功功率',
    value: 2,
    unit: 'kW',
    unitName: 'P',
    paramBox: 'pow_active',
    paramBus: 'pow_value',
    fixNum: 3,
  },
  {
    name: '无功功率',
    value: 3,
    unit: 'kVar',
    unitName: 'P',
    paramBox: 'pow_reactive',
    paramBus: 'pow_reactive',
    fixNum: 3,
  },
  {
    name: '视在功率',
    value: 4,
    unit: 'kVA',
    unitName: 'P',
    paramBox: 'pow_apparent',
    paramBus: 'pow_apparent',
    fixNum: 3,
  },
  {
    name: '功率因素',
    value: 5,
    unit: '',
    unitName: 'P',
    paramBox: 'power_factor',
    paramBus: 'power_factor',
    fixNum: 2,
  },
  {
    name: '温度',
    value: 6,
    unit: '°C',
    unitName: 'T',
    paramBox: 'tem_value',
    paramBus: 'tem_value',
    fixNum: 2,
  },
]
const letters = ['a', 'b', 'c', 'd']
const chosenBtn = ref(0)
const pluginBoxA = ref([])
const pluginBoxB = ref([])
const startingBoxA = ref([])
const startingBoxB = ref({})

const startingBoxATEM = ref([])
const startingBoxBTEM = ref({})

const switchBtn = (value) => {
  chosenBtn.value = value
}
// 接口获取机房导航列表
const getNavList = async() => {
  const res = await MachineColumnApi.getAisleList({})
  console.log('接口获取机房导航列表*****', res)
  if (res && res.length) {
    roomList.value = res
    handleNavList()
  }
}
const handleNavList = () => {
  let targetRoom = null as any
  if (!queryParams.cabinetroomId) {
    queryParams.cabinetroomId = roomList.value[0].roomId
    targetRoom = roomList.value[0]
  } else {
    targetRoom = roomList.value.find(item => item.roomId == queryParams.cabinetroomId)
  }
  machineList.value = targetRoom.aisleList || []
  if (!queryParams.cabinetColumnId && machineList.value) {
    queryParams.cabinetColumnId = machineList.value[0].id
  }
}
const getBusData = async() => {
  const res = await BusTopologyApi.getBusTopology({id: queryParams.cabinetColumnId})
  console.log('res', res)
  if (res.length > 0) {
    startingBoxA.value = res[0].busData ? res[0].busData.bus_data.line_item_list : null
    startingBoxB.value = res[1].busData ? res[1].busData.bus_data.line_item_list : null

    startingBoxATEM.value = res[0].busData ? res[0].busData.env_item_list : null
    startingBoxBTEM.value = res[1].busData ? res[1].busData.env_item_list : null
    // pluginBoxA.value = res[0].boxDataList.filter(item=>item.data.box_data.box_cfg.box_type==0)

    pluginBoxA.value = res[0].boxDataList
    pluginBoxB.value = res[1].boxDataList
    
    // console.log('btns[chosenBtn.value]', btns[chosenBtn.value], startingBoxA.value)
    // console.log('startingBoxA.value', startingBoxA.value, startingBoxA.value[(btns[chosenBtn.value].paramBox)], pluginBoxA.value[0])
  } else {
    startingBoxA.value = []
    startingBoxB.value = []
    startingBoxATEM.value = []
    startingBoxBTEM.value = []
    pluginBoxA.value = []
    pluginBoxB.value = []
  }
}
watch(() => queryParams.cabinetroomId, (val) => {
  if (val) {
    const targetRoom = roomList.value.find(item => item.roomId == val)
    machineList.value = targetRoom.aisleList || []
    if (!machineList.value.find(item => item.id == queryParams.cabinetColumnId)) {
      queryParams.cabinetColumnId = machineList.value[0].id
    }
  }
})

watch(() => queryParams.cabinetColumnId,(val) => {
  console.log('wwwwwwwwwww', val, machineList.value)
  getBusData()
})
getNavList()
</script>

<style scoped lang="scss">
.btns {
  display: flex;
  justify-content: flex-end;
}

.topologyContainer {
  box-sizing: border-box;
  height: calc(100vh - 250px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  // height: calc(100% - var(--app-content-padding) - var(--app-content-padding) - var(--app-footer-height)) !important;
  overflow: auto;
  padding: 20px 0;
  .loadA,
  .loadB {
    display: flex;
    .box {
      width: 100px;
      height: 150px;
      // height: fit-content;
      border: 1px solid #ddd;
      background-color: #fff;
      flex-shrink: 0;
      .title {
        padding: 15px 13px;
        font-size: 13px;
        font-weight: bold;
        color: #0051ff;
      }
      .info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
        padding: 0 10px;
        font-size: 12px;
      }
    }
    .box-empty {
      height: 150px;
    }
    .line {
      flex-shrink: 0;
      width: 12px;
      height: 30px;
      background-color: red;
      margin-top: 48px;
    }
    .boxList {
      position: relative;
      flex: 1;
      box-sizing: border-box;
      display: flex;
      justify-content: space-between;
      padding: 0 10px 20px 10px;
      border-top: 4px solid red;
      margin-top: 60px;
      .boxItem {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 15px;
        .layer {
          width: 30px;
          height: 8px;
          background-color: red;
        }
        .stage {
          width: 4px;
          height: 10px;
          background-color: red;
        }
      }
      .tip {
        position: absolute;
        left: 25px;
        top: -35px;
        color: red;
        font-size: 20px;
        font-weight: bold;
      }
    }
  }
  .loadB {
    .boxB {
      margin-top: 100px;
    }
    .line {
      margin-top: 175px;
      background-color: green;
    }
    .boxList {
      margin-top: 0px;
      margin-bottom: 60px;
      border-top: none;
      border-bottom: 4px solid green;
      padding: 0 10px;
      .boxItem {
        justify-content: flex-end;
        .layer {
          background-color: green;
        }
        .stage {
          background-color: green;
        }
      }
      .tip {
        top: 195px;
        color: green;
      }
    }
  }
}
.topForm .line {
  display: inline-block;
  width: 8px;
  border-bottom: 1px solid #000;
  margin: 0px 8px 13px 8px;
}
:deep(.topForm .el-form-item) {
  margin-right: 0px
}
.boxconnect {
      width: 100px;
      height: 150px;
      // height: fit-content;
      // border: 1px solid #ddd;
      background-color: #fff;
      flex-shrink: 0;
      .title {
        padding: 15px 13px;
        font-size: 13px;
        font-weight: bold;
        color: #0051ff;
      }
      .info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
        padding: 0 10px;
        font-size: 12px;
      }
    }
</style>