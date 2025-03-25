<template>
<!-- <div style="height:calc(100vh - 120px);"> -->
  <el-card shadow="never">
    <div class="toolbar">
      <div style="display: flex;align-items:center" v-if="!isFromHome">
        机房：
        <el-select :size="isFromHome ? 'small' : ''" v-model="roomId" placeholder="请选择" class="!w-200px" @change="handleChangeRoom">
          <el-option v-for="item in roomList" :key="item.id" :label="item.roomName" :value="item.id" />
        </el-select>
      </div>
      <div class="status">
        <template v-for="item in statusInfo" :key="item.value">
          <div class="box" :style="{backgroundColor: item.color}"></div>{{item.name}}
        </template>
      </div>
      <div class="btns" :style="isFromHome ? 'flex: 1;display: flex;justify-content: flex-end;margin-right: 10px' : 'flex: 1;display: flex;justify-content: flex-end;margin-right: 10px'">
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" type="primary" :size="isFromHome ? 'small' : ''" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div style="display: flex;align-items: center">
        <!-- <el-button @click="handleAdd" type="primary">新建机房</el-button> -->
        <el-button v-if="!editEnable" @click="handleEdit" type="primary">编辑</el-button>
        <el-button v-if="editEnable" @click="handleStopDelete" plain type="danger">已删除</el-button>
        <el-button v-if="editEnable" @click="handleCancel" plain type="primary">取消</el-button>
        <el-button v-if="editEnable" @click="openSetting" plain type="primary"><Icon :size="16" icon="ep:setting" style="margin-right: 5px" />配置</el-button>
        <el-button v-if="editEnable" @click="handleSubmit" plain type="primary">保存</el-button>
        <!-- <el-button v-if="editEnable" @click="handleDelete" type="primary">删除机房</el-button> -->
        <div style="display: flex;justify-content: flex-end;margin:3px;width: 100%">
          <el-button size="small" @click="tableScaleValue = 1;tableScaleWidth = 100;tableScaleHeight = 100" circle ><Icon icon="ep:refresh-right" /></el-button>
          <el-button size="small" @click="tableScale(false)" circle ><Icon icon="ep:minus" /></el-button>
          <el-button size="small" @click="tableScale(true)" circle ><Icon icon="ep:plus" /></el-button>
        </div>
      </div>
    </div>
  </el-card>
  <el-card shadow="never">
    <div ref="dragTableViewEle" @mousedown="onMouseDown" @mousemove="onMouseMove" @mouseup="onMouseUp" @mouseleave="onMouseLeave" @selectstart="onSelectStart" :style="{cursor: `${dragCursor}`}">
        <div class="dragContainer" 
          ref="tableContainer"
          v-loading="loading" 
          style=""
          @click.right="handleRightClick"
          :style="isFromHome ? `transform-origin: 0 0;height: 50vh;width:${tableScaleWidth}%;` : `height:calc(100vh - 230px);width:${tableScaleWidth}%;`">
          <!-- <div class="mask" v-if="!editEnable" @click.prevent=""></div> -->
          <el-table ref="dragTable" class="dragTable" v-if="tableData.length > 0" :style="{width: '100%',height: `${tableScaleHeight}%`,transform: `translateZ(0) scale(${tableScaleValue})`, transformOrigin: '0 0',transition: 'none'}" :data="tableData" border :row-style="{background: 'revert'}" :span-method="arraySpanMethod" row-class-name="dragRow">
            <el-table-column fixed type="index" width="60" align="center" :resizable="false" />
            <template v-for="(formItem, index) in formParam" :key="index">
              <el-table-column :label="formItem" min-width="60" align="center" :resizable="false">
                <template #default="scope">
                  <draggable
                    :id="`${scope.$index}-${index}`"
                    class="dragTd"
                    :list="scope.row[formItem]"
                    :itemKey="item => item.id"
                    tag="div"
                    :group="scope.row[formItem].length > 0 ? groupMachineFill : groupMachineBlank"
                    animation="100"
                    @start="onStart"
                    @end.prevent="onEnd"
                  >
                    <template #item="{ element }">
                      <div v-if="element && element.type == 2" class="normalDrag" @dblclick="handleJump(element)" >
                        <template v-if="element.name">
                          <el-tooltip effect="light">
                            <template #content>
                              名称：{{element.cabinetName}} <br/>
                              负载率：{{element.loadRate ? element.loadRate.toFixed(1) : '0.0'}}%<br/>
                              昨日用能：{{element.yesterdayEq || 0}}kW·h<br/>
                              总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                              总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                              总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                              总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}<br/>
                              A路供电占比：{{element.outletA}} <br/>
                              B路供电占比：{{element.outletB}} <br/>
                              最高温度：{{element.tem}}°C<br/>
                              已用空间：{{element.usedSpace}}U<br/>
                              未用空间：{{element.freeSpace}}U<br/>
                            </template>
                            <div v-if="chosenBtn == 0">{{element.loadRate ? element.loadRate.toFixed(1) : '0.0'}}%</div>
                            <div v-if="chosenBtn == 1">{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW</div>
                            <div v-if="chosenBtn == 2">{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.000'}}</div>
                            <div v-if="chosenBtn == 3">{{element.tem}}°C</div>
                            <div v-if="chosenBtn == 4">{{element.cabinetHeight}}U</div>
                          </el-tooltip>
                        </template>
                      </div>
                      <div v-else-if="element.type == 1" :class="element.direction == '1' ? 'dragChild' : 'dragChildCol'"  @dblclick="handleJump(element)">
                        <template v-if="element.cabinetList.length > 0">
                          <div :class="item.cabinetName ? 'dragSon fill' : 'dragSon'" :style="{backgroundColor: statusInfo[item.runStatus].color}" v-for="(item, i) in element.cabinetList" :key="i">
                            <template v-if="item.id > 0">
                              <el-tooltip effect="light">
                                <template #content>
                                  名称：{{item.cabinetName}} <br/>
                                  负载率：{{item.loadRate ? item.loadRate.toFixed(1) : '0.0'}}%<br/>
                                  昨日用能：{{item.yesterdayEq || 0}}kW·h<br/>
                                  总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                  总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                  总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                  总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}<br/>
                                  A路供电占比：{{item.outletA}} <br/>
                                  B路供电占比：{{item.outletB}} <br/>
                                  最高温度：{{item.tem}}°C<br/>
                                  已用空间：{{item.usedSpace}}U<br/>
                                  未用空间：{{item.freeSpace}}U<br/>
                                </template>
                                <div v-if="chosenBtn == 0">{{item.loadRate ? formItem : formItem}}%</div>
                                <div v-if="chosenBtn == 1">{{item.powActive ? item.powActive.toFixed(0) : '0'}}kW</div>
                                <div v-if="chosenBtn == 2">{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.000'}}</div>
                                <div v-if="chosenBtn == 3">{{item.tem}}°C</div>
                                <div v-if="chosenBtn == 4">{{item.cabinetHeight}}U</div>
                              </el-tooltip>
                            </template>
                          </div>
                        </template>
                        <template v-else>
                          <div class="dragSon" v-for="item in element.amount" :key="item">{{item}}</div>
                        </template>
                      </div>
                    </template>
                  </draggable>
                </template>
              </el-table-column>
            </template>
          </el-table>
        <!--  <el-empty v-if="loading == false && tableData.length == 0" style="height: calc(100vh - 220px)" description="机房暂未配置，请先编辑配置" /> -->
          <div class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
            <div class="menu_item" v-if="!editEnable" @click="dragTableView">拖拽</div>
            <div class="menu_item" v-if="showMenuAdd && editEnable" @click="addMachine">新增</div>
            <div class="menu_item" v-if="!showMenuAdd && editEnable" @click="editMachine">编辑</div>
            <div class="menu_item" v-if="!showMenuAdd && editEnable" @click="handleJump(false)">查看</div>
            <div class="menu_item" v-if="!showMenuAdd && editEnable" @click="deleteMachine">删除</div>
          </div>
        </div>
    </div>
  </el-card>
  <layoutForm ref="machineForm" @success="handleChange" />
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
              <el-option v-for="(addr_item,addr_index) in addrList" :key="addr_index" :label="addr_item" :value="addr_item" />
            </el-select>
          </el-form-item>
        </div>
        <div style="margin-bottom: 10px">
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
          <el-input v-model="rowColInfo.powerCapacity" placeholder="请输入">
            <template #append>kVA</template>
          </el-input>
        </el-form-item>
        <el-form-item label="非IT设备总额定功率" label-width="160">
          <el-input v-model="rowColInfo.airPower" placeholder="包括制冷系统（如空调、冷源设备、新风系统等）">
            <template #append>kVA</template>
          </el-input>
        </el-form-item>

        <div class="double-formitem">
          <el-form-item label="显示选择" label-width="90" style="padding-top: 15px">
            <el-switch v-model="rowColInfo.displayFlag" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-radio-group v-model="radio" size="large" style="margin-left: 15px;">
            <el-radio-button label="负载率" value="负载率"/>
            <el-radio-button label="PUE" value="PUE"/>
          </el-radio-group>
        </div>

        <!-- <div class="double-formitem">
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
        </div> -->
      </el-form>
      <template #footer>
        <el-button @click="handleDialogCancel">取 消</el-button>
        <el-button type="primary" @click="submitSetting">确 定</el-button>
      </template>
    </el-dialog>

  
  <el-dialog v-model="dialogStopDelete" title="恢复机房"  :before-close="handleDialogStopDelete">
       <el-table v-loading="loading" :data="deletedList" :stripe="true" :show-overflow-tooltip="true"  :border=true>
         <el-table-column label="编号" min-width="110" align="center">
            <template #default="scope">
               <div>{{scope.row.id}}</div>
            </template>
        </el-table-column>
        <el-table-column label="机房名称" min-width="110" align="center">
            <template #default="scope">
               <div>{{scope.row.roomName}}</div>
            </template>
        </el-table-column>
        
       <el-table-column label="删除日期" min-width="110" align="center">
           <template #default="scope">
               {{ (new Date(scope.row.updateTime)).toISOString().slice(0, 10) }}
            </template>
        </el-table-column>
        
        
       <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="danger"
              @click="handleRestore(scope.row.id)"
            >
              恢复机房
            </el-button>
          </template>
        </el-table-column>
     </el-table>
     <Pagination
        :total="queryParams.pageTotal"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="handleStopDelete"
      />
      <div style="height:30px"></div>
  </el-dialog>
<!-- </div> -->
</template>

<script lang="ts" setup>
import draggable from "vuedraggable";
import layoutForm from './component/layoutForm.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MachineRoomApi } from '@/api/cabinet/room'
import { Console } from "console";

const { push } = useRouter() // 路由跳转
const message = useMessage() // 消息弹窗

const queryParams = reactive({
  company: undefined,
  showCol: [1, 2, 12, 13, 15, 16] as number[],
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})

const {containerInfo, isFromHome} = defineProps({
  containerInfo: {
    type: Object,
  },
  isFromHome: {
    type: Boolean,
    default: false,
  }
})
let timer = null as any // 定时器
const isAddRoom = ref(false) // 是否为添加机房模式 
const roomId = ref(0) // 房间id
const roomList = ref<any[]>([]) // 左侧导航栏树结构列表
const dragTable = ref() // 可移动编辑表格
const dragTableViewEle = ref()
const tableContainer = ref()
const scaleValue = ref(1) // 缩放比例
const tableScaleValue = ref(1)
const tableScaleWidth = ref(100)
const tableScaleHeight = ref(100)
const deletedList = ref<any>([]) //已删除的
const chosenBtn = ref(0)
const ContainerHeight = ref(100)
const loading = ref(false)
const movingInfo = ref<any>({})
const roomFlag =ref();
const aisleFlag = ref();
const roomDownValId = ref();
const updateCfgInfo = ref();
const roomsId = reactive({
  roomDownValIds: history?.state?.id,
})

const addrList = ref([
  '未区分',
  '一楼',
  '二楼',
  '三楼',
  '四楼',
  '五楼',
  '六楼',
  '七楼',
  '八楼',
  '九楼',
  '十楼',
]) // 楼层

const radio = ref("负载率")
const rowColInfo = reactive({
  roomName: '', // 机房名
  addr: '未区分', //楼层
  row: 14, // 行
  col: 18, // 列
  powerCapacity:0, //电力容量
  airPower: null, //空调额定功率
  displayType: 0, //0负载率 1PUE
  displayFlag: 0, // 显示选择
  eleAlarmDay: 0, // 日用能告警
  eleLimitDay: 1000, // 日用能限制
  eleAlarmMonth: 0, // 月用能告警
  eleLimitMonth: 1000, // 月用能限制
})
const emit = defineEmits(['backData', 'getroomid']) // 定义 backData 事件，用于操作成功后的回调
const tableData = ref<Record<string, any[]>[]>([]);
const statusInfo = ref([
  {
    name: '空机柜',
    color: '#effaff',
    value: 0,
  },
  {
    name: '正常',
    color: '#3bbb00',
    value: 1,
  },
  {
    name: '预警',
    color: '#ffc402',
    value: 2,
  },
  {
    name: '告警',
    color: '#fa3333',
    value: 3,
  },
  {
    name: '未绑定',
    color: '#05ebfc',
    value: 4,
  },
  {
    name: '离线',
    color: '#7700ff',
    value: 5,
  },
])
const btns = [
  {
    value: 0,
    name: '机柜负荷',
  },
  {
    value: 1,
    name: '有功功率',
  },
  {
    value: 2,
    name: '功率因素',
  },
  {
    value: 5,
    name: '昨日用能',
  },
  {
    value: 3,
    name: '温度',
  },
  // {
  //   value: 4,
  //   name: '容量',
  // },
]

const emptyObject = {
    "id": 0,
    "cabinetName": null,
    "roomId": 0,
    "roomName": null,
    "aisleId": 0,
    "powCapacity": null,
    "cabinetType": null,
    "pduBox": null,
    "isDisabled": null,
    "isDeleted": null,
    "cabinetHeight": 0,
    "cabinetUseHeight": 0,
    "type": null,
    "company": null,
    "pduIpA": null,
    "casIdA": 0,
    "pduIpB": null,
    "casIdB": 0,
    "runStatus": 0,
    "sensorList": null,
    "rackIndexList": null,
    "outletA": null,
    "outletB": null,
    "usedSpace": 0,
    "rackNum": 0,
    "freeSpace": 0,
    "busIpA": null,
    "busNameA": null,
    "boxNameA": null,
    "barIdA": null,
    "addrA": null,
    "barIdB": null,
    "addrB": null,
    "boxOutletIdA": null,
    "busIpB": null,
    "busNameB": null,
    "boxNameB": null,
    "boxOutletIdB": null,
    "index": 4,
    "yesterdayEq": null,
    "boxIndexA": null,
    "boxIndexB": null,
    "eleAlarmDay": null,
    "eleAlarmMonth": null,
    "eleLimitDay": null,
    "eleLimitMonth": null,
    "loadRate": 0,
    "lineCurA": null,
    "lineCurB": null,
    "lineVolA": null,
    "lineVolB": null,
    "powerFactor": 0,
    "powerFactorA": 0,
    "powerFactorB": 0,
    "powActive": 0,
    "powActiveA": 0,
    "powActiveB": 0,
    "powReactive": 0,
    "powReactiveA": 0,
    "powReactiveB": 0,
    "powApparent": 0,
    "powApparentA": 0,
    "powApparentB": 0,
    "temData": null,
    "temDataHot": null,
    "cabinetBoxes": null,
    "cabinetPdus": null,
    "rackIndices": null,
    "xCoordinate": 0,
    "yCoordinate": 0
}

const dialogVisible = ref(false);
const dialogStopDelete = ref(false);
const editEnable = ref(false);
const dragCursor = ref();
const tableHeight = ref(0);
const machineForm = ref();
const startX = ref(0);
const startY = ref(0);
const scrollLeft = ref(0);
const scrollTop = ref(0);

const groupMachineFill = {
  name: 'MachineFill',
  pull: () => {
    return editEnable.value
  }, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: false // 拖入
}
const groupMachineBlank = {
  name: 'MachineBlank',
  pull: false, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: (event) => {
   // console.log('event', event.el.id)
    const moveBox = movingInfo.value
    if (editEnable.value && moveBox.type == 1 && moveBox.amount > 1) {
      if (moveBox.direction == 1) {
        const X = +event.el.id.split('-')[1]
        const Y = +event.el.id.split('-')[0]
        if (X + moveBox.amount > rowColInfo.col) return false
        for(let i = 0;i < moveBox.amount;i++) {
          if(tableData.value[Y][formParam.value[X+i]].length || tableData.value[Y+1][formParam.value[X+i]].length || (Y > 0 && tableData.value[Y-1][formParam.value[X+i]].length)) {
            return false
          }
        }
      } else {
        const X = +event.el.id.split('-')[1]
        const Y = +event.el.id.split('-')[0]
        if (Y + moveBox.amount > rowColInfo.row) return false
        for(let i = 0;i < moveBox.amount;i++) {
          if(tableData.value[Y+i][formParam.value[X]].length || tableData.value[Y+i][formParam.value[X+1]].length || (X > 0 && tableData.value[Y+i][formParam.value[X-1]].length)) {
            return false
          }
        }
      }
    }
    return editEnable.value
  }, // 拖入
}

const operateMenu = ref({
  left: '0px',
  top: '0px',
  show: false,
  lndexX: 0,
  lndexY: 0,
  maxlndexX: 0,
  maxlndexY: 0,
})

// 接口获取机房导航列表
const getRoomList = async() => {
  const res = await MachineRoomApi.getRoomList({})
  //console.log('接口获取机房导航列表*****', res, roomId.value)
  if (res && res.length) {
    roomList.value = res
    const find = res.find(item => item.id == roomId.value)
    if (!find) {
      if(roomsId.roomDownValIds == null){
          roomId.value = res[0].id
      }else{
          roomId.value = roomsId.roomDownValIds;
      }
    }
    emit('getroomid', roomId.value);
    getRoomInfo();
  }
}

const getRoomInfo = async() => {
  resetForm();
  loading.value = true;
  try {
    const result = await MachineRoomApi.getRoomDataNewDetail({id: roomId.value});
    const res = result;
    // const result1 = MachineRoomApi.getRoomDetail({id: roomId.value});
    // const result2 = MachineRoomApi.getRoomDataDetail({id: roomId.value})
    // const results = await Promise.all([result1, result2])
    // const res = results[0];
    console.log("res",res)
    res.aisleList = res.aisleList==null ? [] : res.aisleList
    res.cabinetList = res.cabinetList==null ? [] : res.cabinetList
    updateCfgInfo.value=res;
    roomDownValId.value = res.id;
    const data: Record<string, any[]>[] = [];
    Object.assign(rowColInfo, {
      roomName: res.roomName,
      row: res.y_length,
      col: res.x_length,
      powerCapacity:res.powerCapacity,
      airPower:res.airPower,
      addr: res.addr,
      displayType: res.displayType, //0负载率 1PUE
      displayFlag: res.displayFlag, // 显示选择
      eleAlarmDay: res.eleAlarmDay,
      eleLimitDay: res.eleLimitDay,
      eleAlarmMonth: res.eleAlarmMonth,
      eleLimitMonth: res.eleLimitMonth,
    })
    emit('backData', res)
    
    for (let row = 0; row < res.y_length; row++) {
      const rowData: Record<string, any[]> = {};
      for (let col = 0; col < res.x_length; col++) {
        const colKey = getTableColCharCode(col);
        rowData[colKey] = [];
      }
      data.push(rowData);
    }
    res.aisleList.forEach(item => {
      if(item.cabinetList == null) {
        item.cabinetList = []
      }
      let emptyLength = item.length-item.cabinetList.length
      for(let j=0;j < emptyLength;j++) {
        item.cabinetList.push(emptyObject)
      }
      for(let i=0; i < item.length; i++) {
        const dataItem =  {
          id: item.id,
          name: item.aisleName,
          direction: item.direction == 'x' ? 1 : 2,
          type: 1,
          amount: item.cabinetList.length,
          cabinetList: item.cabinetList,
          first: false,
          originAmount: item.cabinetList.length,
          originDirection: item.direction == 'x' ? 1 : 2,
          powerCapacity:item.powerCapacity,
          eleAlarmDay: item.eleAlarmDay,
          eleLimitDay: item.eleLimitDay,
          eleAlarmMonth: item.eleAlarmMonth,
          eleLimitMonth: item.eleLimitMonth,
        }
        if (i == 0) dataItem.first = true
        if (dataItem.direction == 1) {
          console.log('----dataItem1', dataItem )
          data[item.yCoordinate - 1][getTableColCharCode(item.xCoordinate - 1 + i)].splice(0, 1, dataItem)
          data[item.yCoordinate][getTableColCharCode(item.xCoordinate - 1 + i)].splice(0, 1, {...dataItem,first: false})
        } else {
          data[item.yCoordinate - 1 + i][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, dataItem)
          data[item.yCoordinate - 1 + i][getTableColCharCode(item.xCoordinate)].splice(0, 1, {...dataItem,first: false})
        }
      }
    })
    res.cabinetList.forEach(item => {
      if (item.xCoordinate > 0 && item.yCoordinate > 0)
      data[item.yCoordinate - 1][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, {...item, name: item.cabinetName, type: 2})
    })
    tableData.value = data;
    console.log("tableData.value",tableData.value)
    getRoomStatus(result)
    handleCssScale()
  } finally {
    loading.value = false
  }
}

const getRoomStatus = async(res) => {
  if (!res) res = await MachineRoomApi.getRoomDataDetail({id: roomId.value})
  console.log('getRoomStatus', res)
  if (res.cabinetList && res.cabinetList.length) {
    res.cabinetList.forEach(cab => {
      if (cab.yCoordinate > 0 && cab.xCoordinate > 0)
      tableData.value[cab.yCoordinate - 1][formParam.value[cab.xCoordinate - 1]][0] = {
        ...cab,
        ...tableData.value[cab.yCoordinate - 1][formParam.value[cab.xCoordinate - 1]][0],
      }
    })
  }
  if (res.aisleList && res.aisleList.length) {
    res.aisleList.forEach(aisle => {
      aisle.cabinetList.forEach((cab, index) => {
       console.log('111', aisle,tableData.value)
       console.log('tableData.value[aisle.yCoordinate][formParam.value[aisle.xCoordinate - 1]]', tableData.value[aisle.yCoordinate - 1][formParam.value[aisle.xCoordinate - 1]], aisle.yCoordinate, formParam.value[aisle.xCoordinate - 1])
        const targetIndex = tableData.value[aisle.yCoordinate - 1][formParam.value[aisle.xCoordinate - 1]][0].cabinetList.findIndex(item => item.id == cab.id)
        tableData.value[aisle.yCoordinate - 1][formParam.value[aisle.xCoordinate - 1]][0].cabinetList[targetIndex] = {
          ...cab,
          ...tableData.value[aisle.yCoordinate - 1][formParam.value[aisle.xCoordinate - 1]][0].cabinetList[targetIndex]
        }
      })
    })
  }
 // console.log('//////////', tableData.value)
}

let lastTime = 0;
const throttleDelay = 100;
const tableScale = (flag) => {
  if(tableScaleValue.value == 0.4 && !flag) {
    return
  }
  const now = Date.now();

  // 如果距离上次执行的时间小于 throttleDelay，则跳过
  if (now - lastTime < throttleDelay) {
    return
  } 
  console.log(now)
  lastTime = now;
  if(flag) {
    tableScaleValue.value += 0.2
  } else {
    tableScaleValue.value -= 0.2
  }
  tableScaleValue.value = Math.max(0.4, tableScaleValue.value);
  tableScaleWidth.value = 1/tableScaleValue.value*100
  tableScaleHeight.value = 1/tableScaleValue.value*100
}

// 计算出要缩放的比例
const handleCssScale = () => {
  isFromHome && nextTick(() => {
    const timer = setTimeout(() => {
      const targetBody = dragTable.value.$el.querySelector('.el-table__body ') as HTMLElement
      // const targetContainer = document.querySelector('.dragContainer ') as HTMLElement
      const tableWidth = +targetBody.getBoundingClientRect().width.toFixed() + 30 // Container元素的宽
      const tableHeight = +targetBody.getBoundingClientRect().height.toFixed() // Container元素的高
      scaleValue.value = +((containerInfo?.width/tableWidth).toFixed(2))
      ContainerHeight.value = scaleValue.value * tableHeight
      clearTimeout(timer)
    //  console.log('containerInfotargetBody',scaleValue.value, containerInfo?.width, targetBody, targetBody.getBoundingClientRect(), tableWidth, tableHeight)
    }, 10)
  })
}

// 处理修改机房的事件
const handleChangeRoom = (val) => {
  roomId.value = val
  tableScaleValue.value = 1
  tableScaleWidth.value = 100
  tableScaleHeight.value = 100
  getRoomInfo()
}
//取消
const handleCancel = () => {
  editEnable.value = false;
  getRoomInfo();
}
//已删除
const handleStopDelete = async() =>{
  dialogStopDelete.value =true;
  const res = await MachineRoomApi.deletedRoomInfo({
    pageNo: queryParams.pageNo,
    pageSize: queryParams.pageSize,
  })
  deletedList.value = res.list;
  queryParams.pageTotal = res.total;
}

//恢复机房
const handleRestore = async (flagRoomid) => {
  ElMessageBox.confirm('确认恢复机房吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    const res = await MachineRoomApi.restoreRoomInfo({id: flagRoomid});
    if(res != null || res != "")
    dialogStopDelete.value =false;
    message.success('恢复成功')
    getRoomList()
  })
}



const handleDialogStopDelete =() =>{
   dialogStopDelete.value =false;
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
    displayType: 0, //0负载率 1PUE
    displayFlag: 0, // 显示选择
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 1000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 1000, // 月用能限制
  })
  radio.value = "负载率"
}
// 处理点击删除机房事件
const handleDelete = () => {
  ElMessageBox.confirm('确认删除机房吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    const res = await MachineRoomApi.deleteRoom({id: roomId.value})
    console.log('handleDelete', res)
    editEnable.value = false
    message.success('删除成功')
    getRoomList()
  })
}
// 处理点击编辑事件
const handleEdit = () => {
  if (isFromHome) {
    push({path: '/room/topology', state: { id:roomDownValId.value}})
    return
  }
  editEnable.value = true
}

const openSetting = () => {
  roomFlag.value = 2;
  Object.assign(rowColInfo, {
    roomName: updateCfgInfo.value.roomName,
    row: updateCfgInfo.value.y_length,
    col: updateCfgInfo.value.x_length,
    powerCapacity:updateCfgInfo.value.powerCapacity,
    addr: updateCfgInfo.value.addr,
    airPower:updateCfgInfo.value.airPower,
    displayType: updateCfgInfo.value.displayType ? 1 : 0, //0负载率 1PUE
    displayFlag: updateCfgInfo.value.displayFlag ? 1 : 0,
    eleAlarmDay: updateCfgInfo.value.eleAlarmDay,
    eleLimitDay: updateCfgInfo.value.eleLimitDay,
    eleAlarmMonth: updateCfgInfo.value.eleAlarmMonth,
    eleLimitMonth: updateCfgInfo.value.eleLimitMonth,
  })
  radio.value = updateCfgInfo.value.displayType ? "PUE" : "负载率"
  dialogVisible.value = true;
}

const switchBtn = (value) => {
  chosenBtn.value = value
}

const arraySpanMethod = ({
  row,
  columnIndex,
}) => {
  if (columnIndex > 0) {
    const num = 1
    const td = row[getTableColCharCode(columnIndex - num)]
    const tdData = td[0]
    if (tdData && tdData.type && tdData.type == 1) { // 如果是柜列
      if (tdData.first) { // 如果是柜列中开头第一个  合并单元格
        if (tdData.direction == 1) { // 横向
          return [2, tdData.amount]
        } else { // 纵向
          return [tdData.amount, 2]
        }
      } else { // 如果不是柜列中开头第一个 该单元格不显示
        return [0, 0]
      }
    } 
    if (tdData && tdData.type && tdData.type == 2) { // 如果是机柜
      return [2, 1]
    } 
  }
  return [1, 1]
}

// 右击弹出菜单
const handleRightClick = (e) => {
  e.preventDefault()
  const container = e.currentTarget;
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  const currentId = e.target.id ? e.target.id : (e.target.parentNode.id ? e.target.parentNode.id :  e.target.parentNode.parentNode.id)
  //console.log('handleRightClick', e.target, currentId, offsetX, offsetY)
  const lndexX = currentId.split('-')[1]
  const lndexY = currentId.split('-')[0]
  if (!currentId) return
  operateMenu.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    lndexX, // 当前列
    lndexY, // 当前行
    maxlndexX: rowColInfo.col - lndexX,
    maxlndexY: rowColInfo.row - lndexY,
  }
  console.log('editEnable.value', editEnable.value)
}
// 判断是否展示添加菜单项
const showMenuAdd = computed(() => {
  const lndexX = operateMenu.value.lndexX
  const lndexY = operateMenu.value.lndexY
  console.log('tableData.value[lndexY][formParam.value[lndexX]]', tableData.value[lndexY][formParam.value[lndexX]], lndexX, lndexY)
  return !(tableData.value[lndexY][formParam.value[lndexX]].length > 0)
})
// 拖拽开始的事件
const onStart = ({from}) => {
  const X = from.id.split('-')[1]
  const Y = from.id.split('-')[0]
  const target = tableData.value[Y][formParam.value[X]][0]
  if (target.type == 1) movingInfo.value = target
  //console.log('onStssasrt', target)
}
// 拖拽结束的事件
const onEnd = ({from, to}) => {
 console.log('onsEnd',tableData.value, from, to, from.id, to.id)
  if (from.id != to.id) { // 发生移动才处理
    const X = +to.id.split('-')[1]
    const Y = +to.id.split('-')[0]
    const targetTo = tableData.value[Y][formParam.value[X]][0]
   console.log('value*******', targetTo)
    
    if (targetTo.type == 1 && targetTo.amount > 1) {
      const X1 = +from.id.split('-')[1]
      const Y1 = +from.id.split('-')[0]
      if (targetTo.direction == 1) {
        tableData.value[Y1+1][formParam.value[X1]] = []
        tableData.value[Y+1][formParam.value[X]] = [{...targetTo,first: false}]
      } else {
        tableData.value[Y1][formParam.value[X1+1]] = []
        tableData.value[Y][formParam.value[X+1]] = [{...targetTo,first: false}]
      }
      for (let i=  1; i < targetTo.amount; i++) {
        if (targetTo.direction == 1) {
          tableData.value[Y1][formParam.value[X1+i]] = []
          tableData.value[Y1+1][formParam.value[X1+i]] = []
          tableData.value[Y][formParam.value[X+i]] = [{...targetTo,first: false}]
          tableData.value[Y+1][formParam.value[X+i]] = [{...targetTo,first: false}]
        } else {
          tableData.value[Y1+i][formParam.value[X1]] = []
          tableData.value[Y1+i][formParam.value[X1+1]] = []
          tableData.value[Y+i][formParam.value[X]] = [{...targetTo,first: false}]
          tableData.value[Y+i][formParam.value[X+1]] = [{...targetTo,first: false}]
        }
      }
    }
    console.log('tableData.value*******', tableData.value)
  }
}
//移动表格视图
const dragTableView = () => {
  dragCursor.value = 'grab';
  operateMenu.value.show = false
}
const onMouseDown = (e) => {
  if (dragCursor.value == 'grab') {
    dragCursor.value = 'grabbing';
    startX.value = e.pageX;
    startY.value = e.pageY;
    // 获取表格滚动区域元素
    const tableScrollbarWrap = dragTable.value.$el.querySelector('.el-scrollbar__wrap');
    // 获取滚动位置
    scrollTop.value = tableScrollbarWrap.scrollTop;
    scrollLeft.value = tableScrollbarWrap.scrollLeft;
    return false
  }
}

const onMouseMove = (e) => {
  const now = Date.now();

  // 如果距离上次执行的时间小于 throttleDelay，则跳过
  if (now - lastTime < throttleDelay) {
    return
  } 

  lastTime = now;
  if (dragCursor.value == 'grabbing') {
    const dx = e.pageX - startX.value;
    const dy = e.pageY - startY.value;
    dragTable.value.setScrollLeft(scrollLeft.value-dx)
    dragTable.value.setScrollTop(scrollTop.value-dy)
  }
};
const onMouseUp = () => {
  if (dragCursor.value == 'grabbing') {
    dragCursor.value = 'grab'
  }
}

const onMouseLeave = () => {
  dragCursor.value = ''
}

const onSelectStart = (e) => {
  if(dragCursor.value == 'grabbing') {
    e.preventDefault();
  }
}

// 增加机柜弹框
const addMachine = () => {
  aisleFlag.value = 1;
  machineForm.value.open('add', null, operateMenu.value)
  operateMenu.value.show = false
}
// 编辑机柜弹框
const editMachine = () => {
  aisleFlag.value = 2;
  const Y = operateMenu.value.lndexY;
  const X = formParam.value[operateMenu.value.lndexX];
  console.log("machineForm",machineForm)
  machineForm.value.open('edit', {...tableData.value[Y][X][0]}, operateMenu.value);
  operateMenu.value.show = false;
}
// 跳转机柜/柜列
const handleJump = (data) => {
  let target = {} as any;
  if (data) {
    target = data;
  } else {
    const Y = operateMenu.value.lndexY;
    const X = formParam.value[operateMenu.value.lndexX];
    target = tableData.value[Y][X][0];
  }
 // console.log('target', target)
  if (!target.id) {
    message.error(`该${target.type == 1 ? '柜列' : '机柜'}暂未保存绑定，无法跳转`)
    return
  }
  if (target.type == 1) {
    push({path: '/aisle/columnHome', state: { id: target.id, roomId: roomId.value }})
  } else {
    push({path: '/cabinet/cab/detail', state: {id: target.id}})
  }
}
// 删除机柜
const deleteMachine = () => {
  const Y = operateMenu.value.lndexY
  const X = formParam.value[operateMenu.value.lndexX]
  const target = JSON.parse(JSON.stringify(tableData.value[Y][X][0])) // 要删除的目标
 // console.log('删除机柜',tableData.value[Y][X], target)
  if (target.type && target.type == 1) {
    for (let i = 0; i < target.originAmount; i++) {
      if (target.direction == 1) {
        // const charCode = X.charCodeAt(0) + i
    //    console.log('String.fromCharCode(charCode)', operateMenu.value.lndexX, operateMenu.value.lndexX+i)
        tableData.value[Y][formParam.value[+operateMenu.value.lndexX + i]].splice(0, 1)
      } else {
        tableData.value[+Y + i][X].splice(0, 1)
      }
    }
  } else {
    tableData.value[Y][X].splice(0, 1)
  }
  ElMessageBox.confirm('确认要删除吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    await MachineRoomApi.deletedRoomAisleInfo({id: roomId.value})
    message.success('删除成功')
  })
  operateMenu.value.show = false
}
// 处理增加/编辑机柜
const handleChange = async(data) => {
  tableData.value[operateMenu.value.lndexY][formParam.value[operateMenu.value.lndexX]].splice(0, 1, {...data, first: true, originAmount: data.amount, originDirection: data.direction});
  const X =getColumnCharCodeToNumber(formParam.value[operateMenu.value.lndexX]);
  const Y = (operateMenu.value.lndexY).toString(); // 当前机柜/机柜列所处行
  let aisleFlagId:any = null;
  let messageAisleFlag = "保存成功！";
  if(aisleFlag.value == 2){
      aisleFlagId = data.id; 
      messageAisleFlag = "修改成功！";
  }
  if(data.type == 1){
      const aisleRes = await MachineRoomApi.saveRoomAisle({
          id:aisleFlagId,
          roomId: roomId.value,
          aisleName:data.name,
          aisleLength:data.amount,
          xCoordinate:X+1,
          yCoordinate:parseInt(Y)+1,
          direction:data.direction == 1 ? 'x' : 'y',
          powerCapacity:data.powerCapacity,
          eleAlarmDay:data.eleAlarmDay,
          eleAlarmMonth:data.eleAlarmMonth,
          eleLimitDay:data.eleLimitDay,
          eleLimitMonth:data.eleLimitMonth
      }) 
      if(aisleRes != null || aisleRes != "")
      message.success(messageAisleFlag);
  }else{
      const cabinetRes = await MachineRoomApi.saveRoomCabinet({
          id:aisleFlagId,
          roomId: roomId.value,
          cabinetName: data.name,
          cabinetHeight: data.cabinetHeight,
          xCoordinate:X+1,
          yCoordinate:parseInt(Y)+1,
          powerCapacity:data.powerCapacity,
          eleAlarmDay: data.eleAlarmDay,
          eleLimitDay: data.eleLimitDay,
          eleAlarmMonth: data.eleAlarmMonth,
          eleLimitMonth: data.eleLimitMonth
      })
      if(cabinetRes != null || cabinetRes != "")
      message.success(messageAisleFlag);
  }
}

const getColumnCharCodeToNumber = (columnId: string): number => {
  let result = 0;
  const base = 26; // 因为英文字母有 26 个
  // 从字符串末尾开始遍历，因为 Excel 列标识符是从左到右递增的 26 进制数
  for (let i = 0; i < columnId.length; i++) {
    const charCode = columnId.charCodeAt(i) - 'A'.charCodeAt(0) + 1; // 将字符转换为 1-26 的数字
    const power = Math.pow(base, columnId.length - 1 - i); // 计算当前位的权重（26 的幂）
    result += charCode * power; // 将当前位的值加到结果中
  }
  return result - 1; // 因为我们的计算是从 1 开始的（A=1），而通常我们希望索引从 0 开始
};



// 处理设置提交
const submitSetting = async() => {
   let roomFlagId:any = null;
   let messageRoomFlag = "保存成功！";
   if(roomFlag.value == 2){
      roomFlagId = roomId.value; 
      messageRoomFlag = "修改成功！";
   }
   if(radio.value === "PUE") {
    rowColInfo.displayType = 1
   }
   else {
    rowColInfo.displayType = 0
   }
   try {
    const res = await MachineRoomApi.saveRoomDetail({
      id: roomFlagId,
      roomName: rowColInfo.roomName,
      addr: rowColInfo.addr,
      xLength: rowColInfo.col,
      yLength: rowColInfo.row,
      powerCapacity:rowColInfo.powerCapacity, 
      airPower:rowColInfo.airPower, 
      displayType: rowColInfo.displayType, 
      displayFlag: rowColInfo.displayFlag, 
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
   
   getRoomList();
}


// 获取表格列label字符
const getTableColCharCode = (num: number): string => {
  const baseCharCode = 65; // A 的 ASCII 码
  if (num < 26) {
    return String.fromCharCode(baseCharCode + num);
  } else if (num < 52) {
    return `A ${String.fromCharCode(baseCharCode + (num - 26))}`;
  } else {
    return `B ${String.fromCharCode(baseCharCode + (num - 52))}`;
  }
};
// 处理提交保存事件
const handleSubmit = async() => {
  const aisleList = [] as any
  const cabinetList = [] as any
  if (!isAddRoom.value)
  console.log(rowColInfo.row)
  console.log(rowColInfo.row)
  for(let i = 0; i < rowColInfo.row; i++) {
    for(let j = 0; j < rowColInfo.col; j++) {
    //  console.log('处理提交保存事件', tableData.value, i, getTableColCharCode(j))
      const target = tableData.value[i][getTableColCharCode(j)][0]
      if (target && target.type == 1 && target.first) {
        console.log('target.......', target)
        aisleList.push({
          id: target.id,
          aisleName: target.name,
          xCoordinate: j + 1,
          yCoordinate: i + 1,
          direction: target.direction == 1 ? 'x' : 'y',
          length: target.amount,
          eleAlarmDay: target.eleAlarmDay,
          eleLimitDay: target.eleLimitDay,
          eleAlarmMonth: target.eleAlarmMonth,
          eleLimitMonth: target.eleLimitMonth,
        })
      } else if (target && target.type == 2) {
        cabinetList.push({
          id: target.id,
          cabinetName: target.name,
          cabinetHeight: target.cabinetHeight,
          xCoordinate: j + 1,
          yCoordinate: i + 1,
          eleAlarmDay: target.eleAlarmDay,
          eleLimitDay: target.eleLimitDay,
          eleAlarmMonth: target.eleAlarmMonth,
          eleLimitMonth: target.eleLimitMonth,
        })
      }
    }
  }
  try {
    console.log('aisleListend.......', aisleList)
    console.log('cabinetList.......', cabinetList)
    loading.value = true
    const res = await MachineRoomApi.saveRoomDetail({
        id: isAddRoom.value ? '' : roomId.value,
        roomName: rowColInfo.roomName,
        addr: rowColInfo.addr,
        xLength: rowColInfo.col,
        yLength: rowColInfo.row,
        powerCapacity:rowColInfo.powerCapacity, 
        airPower:rowColInfo.airPower, 
        displayType: rowColInfo.displayType, 
        displayFlag: rowColInfo.displayFlag, 
        eleAlarmDay: rowColInfo.eleAlarmDay,
        eleAlarmMonth: rowColInfo.eleAlarmMonth,
        eleLimitDay: rowColInfo.eleLimitDay,
        eleLimitMonth: rowColInfo.eleLimitMonth,
        aisleList,
        cabinetList
    })
    if (isAddRoom.value) {
          roomId.value = res;
          getRoomList();
          message.success('新建成功！');
          dialogVisible.value = false;
          editEnable.value = false;
          isAddRoom.value = false;
          return;
    }
    editEnable.value = false;
    message.success('保存成功！');
  } finally {
    loading.value = false;
  }
}

// const formParam = Object.keys(tableData[0])
const formParam = computed(() => {
  return Object.keys(tableData.value[0] || {});
})

getRoomList()

watch(() => containerInfo, (val) => {
  if (val) {
    roomId.value = containerInfo?.roomId
  }
},{immediate: true})

onMounted(() => {
  document.addEventListener('mousedown', (event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item') {
      operateMenu.value.show = false
    }
  })
  // timer = setInterval(() => {
  //   getRoomStatus(false)
  // }, 5000)
})

onUnmounted(() => {
  window.onresize = null
  clearInterval(timer)
  timer = null
})
</script>

<style lang="scss" scoped>
.btns {
  margin-left: 25px;
}
.status {
  font-size: 14px;
  display: flex;
  align-items: center;
  .box {
    width: 10px;
    height: 10px;
    border-radius: 2px;
    margin-left: 10px;
    margin-right: 5px;
  }
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items:center
}
.dragContainer {
  // transform-origin: left right;
  position: relative;
  .dragTable {
    transition: transform 0.3s ease; /* 添加平滑过渡效果 */
  }
  .mask {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 50%;
    z-index: 999;
  }
  .menu {
    box-sizing: border-box;
    position: absolute;
    padding: 10px 0;
    background-color: #fff;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
    z-index: 999;
    .menu_item {
      width: 70px;
      height: 30px;
      padding: 0 10px;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .menu_item:hover {
      background-color: rgb(231, 245, 255);
      color: rgb(82, 177, 255);
    }
  }
  .dragTd {
    width: 100%;
    height: 100%;
    min-height: 40px;
    .dragChild {
      width: 100%;
      height: 100%;
      min-height: 40px;
      box-sizing: border-box;
      display: flex;
      border: 1px solid #000;
      // align-items: center;
      .dragSon {
        min-height: 40px;
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
        background-color: #effaff;
        border-right: 1px solid #bed1ff;
        &>div {
          min-height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      .fill {
        background-color: #0cff2c;
      }
    }
    .dragChildCol {
      width: 100%;
      height: 100%;
      min-height: 40px;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      border: 1px solid #000;
      .dragSon {
        flex: 1;
        min-height: 40px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #effaff;
        border-bottom: 1px solid #bed1ff;
      }
      .fill {
        background-color: #0cff2c;
      }
    }
  }
  .warnDrag {
    min-height: 40px;
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    // height: 40px;
    background-color: rgb(255, 219, 12);
  }
  .normalDrag {
    min-height: 40px;
    height: 100%;
    width: 100%;
    // height: 40px;
    background-color: #effaff;
    box-sizing: border-box;
    border-right: 1px solid #ebeef5;
    &>div {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      min-height: 40px;
    }
  }
}
.double-formitem {
  display: flex;
  & > div {
    flex: 1;
  }
}

:deep(.el-card__body) {
  padding: 15px;
}

:deep(.el-card) {
  margin-top: -5px;
  margin-bottom: 10px;
}
:deep(.el-input-number) {
  width: 100%;
}
:deep(.dragTable .hover-row .el-table__cell td) {
  background-color:unset!important;
}
:deep(.dragTable .el-table__cell) {
  padding: 0;
}
:deep(.dragTable .el-table__cell .cell) {
  width: 100%;
  height: 100%;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  & > div {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

:deep(.dragTable .el-table__header .el-table__cell) {
  // background-color: #ddd;
  // box-shadow: 0 1px 0px #ddd;
}

:deep(.dragTable .el-table__body) {
  height: 100%;
  transform-origin: let top;
}

:deep(.dragTable .el-table__body .el-table__row .el-table__cell:nth-of-type(1)) {
  // background-color: #ddd;
  // box-shadow: 0 1px 0px #ddd;
}

.crosshair {
  cursor: crosshair; /* 当正在拖动时显示十字图标 */
}
</style>
