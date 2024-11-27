<template>
  <Dialog id="machine-dialog" v-model="dialogVisible" :title="dialogTitle" width="60%">
    <div class="formContainer">
      <el-form
        ref="machineForm"
        v-loading="formLoading"
        :model="machineFormData"
        :rules="machineFormRules"
        label-width="120px"
        center
      >
      <el-collapse v-model="activeNames" @change="handleChange" accordion>
        <el-collapse-item title="机柜参数" name="1">
          <div class="collapseItem">
            <el-form-item label="机房：" prop="roomId">
              <el-select disabled v-model="machineFormData.roomId" placeholder="请选择">
                <el-option v-for="room in roomList" :key="room.id" :label="room.name" :value="room.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="机柜名称：" prop="cabinetName">
              <el-input v-model="machineFormData.cabinetName" placeholder="请输入" />
            </el-form-item>
            <el-form-item label="机柜类型：" prop="type">
              <el-select v-model="machineFormData.type" placeholder="请选择">
                <el-option label="IT机柜" value="IT机柜" />
                <el-option label="网络柜" value="网络柜" />
                <el-option label="配电-电池柜" value="配电-电池柜" />
                <el-option label="水阀占位柜" value="水阀占位柜" />
                <el-option label="适配框" value="适配框" />
                <el-option label="柱子" value="柱子" />
                <el-option label="占位" value="占位" />
              </el-select>
            </el-form-item>
            <el-form-item label="机柜高度：" prop="cabinetHeight">
              <el-input v-model="machineFormData.cabinetHeight" placeholder="请输入" />
            </el-form-item>
            <el-form-item label="电力容量：" prop="powCapacity ">
              <el-input v-model="machineFormData.powCapacity" placeholder="请输入" />
            </el-form-item>
            <el-form-item label="所属公司：" prop="company">
              <el-input v-model="machineFormData.company" placeholder="请输入" />
            </el-form-item>
            <div class="double-formitem">
              <el-form-item label="日用能告警">
                <el-switch v-model="machineFormData.eleAlarmDay" :active-value="1" :inactive-value="0" />
              </el-form-item>
              <el-form-item label="日用能限制">
                <el-input-number v-model="machineFormData.eleLimitDay" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
              </el-form-item>
            </div>
            <div class="double-formitem">
              <el-form-item label="月用能告警">
                <el-switch v-model="machineFormData.eleAlarmMonth" :active-value="1" :inactive-value="0" />
              </el-form-item>
              <el-form-item label="月用能限制">
                <el-input-number v-model="machineFormData.eleLimitMonth" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
              </el-form-item>
            </div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="PDU/母线绑定" name="2">
          <el-tabs type="border-card" class="demo-tabs" v-model="machineFormData.pduBox">
            <el-tab-pane label="PDU" :name="0">
              <div class="pduBus">
                <el-form-item label="A路：">
                  <el-col :span="4" class="text-center">
                    <span class="text-gray-500">IP地址</span>
                  </el-col>
                  <el-col :span="8">
                    <el-input v-model="machineFormData.pduIpA" placeholder="请输入" />
                  </el-col>
                  <el-col :span="4" class="text-center">
                    <span class="text-gray-500">级联地址</span>
                  </el-col>
                  <el-col :span="8">
                    <el-input v-model="machineFormData.casIdA" placeholder="请输入" />
                  </el-col>
                </el-form-item>
                <el-form-item label="B路：">
                  <el-col :span="4" class="text-center">
                    <span class="text-gray-500">IP地址</span>
                  </el-col>
                  <el-col :span="8">
                    <el-input v-model="machineFormData.pduIpB" placeholder="请输入" />
                  </el-col>
                  <el-col :span="4" class="text-center">
                    <span class="text-gray-500">级联地址</span>
                  </el-col>
                  <el-col :span="8">
                    <el-input v-model="machineFormData.casIdB" placeholder="请输入" />
                  </el-col>
                </el-form-item>
              </div>
            </el-tab-pane>
            <el-tab-pane label="母线" :name="1">
              <div class="Bus">
                <div>
                  <div class="title">A路</div>
                  <el-form-item label="母线编号：">
                    <el-input v-model="machineFormData.barIdA" :disabled="isBusBind" placeholder="请输入" />
                  </el-form-item>
                  <el-form-item label="母线地址：">
                    <el-input v-model="machineFormData.busIpA" :disabled="isBusBind" placeholder="请输入" />
                  </el-form-item>
                  <el-form-item label="插接箱名称：">
                    <el-select v-if="isBusBind" v-model="machineFormData.boxIndexA" placeholder="请选择">
                      <el-option v-for="(box, index) in boxListA" :key="index" :disabled="!!box.type" :label="`${box.type ? '连接器':'插接箱'}${index+1}`" :value="index+''" />
                    </el-select>
                    <el-input v-else v-model="machineFormData.boxIndexA" placeholder="请输入" />
                  </el-form-item>
                  <el-form-item label="插接箱输出位：">
                    <el-select v-if="isBusBind" v-model="machineFormData.boxOutletIdA" placeholder="请选择">
                      <el-option v-for="i in 3" :key="i" :label="'输出位' + i" :value="i" />
                    </el-select>
                    <el-input v-else v-model="machineFormData.boxOutletIdA" placeholder="请输入" />
                  </el-form-item>
                </div>
                <div>
                  <div class="title">B路</div>
                  <el-form-item label="母线编号：">
                    <el-input v-model="machineFormData.barIdB" :disabled="isBusBind" placeholder="请输入" />
                  </el-form-item>
                  <el-form-item label="母线地址：">
                    <el-input v-model="machineFormData.busIpB" :disabled="isBusBind" placeholder="请输入" />
                  </el-form-item>
                  <el-form-item label="插接箱名称：">
                    <el-select v-if="isBusBind" v-model="machineFormData.boxIndexB" placeholder="请选择">
                      <el-option v-for="(box, index) in boxListB" :key="index" :disabled="!!box.type" :label="`${box.type ? '连接器':'插接箱'}${index+1}`" :value="index+''" />
                    </el-select>
                    <el-input v-else v-model="machineFormData.boxIndexB" placeholder="请输入" />
                  </el-form-item>
                  <el-form-item label="插接箱输出位：">
                    <el-select v-if="isBusBind" v-model="machineFormData.boxOutletIdB" placeholder="请选择">
                      <el-option v-for="i in 3" :key="i" :label="'输出位' + i" :value="i" />
                    </el-select>
                    <el-input v-else v-model="machineFormData.boxOutletIdB" placeholder="请输入" />
                  </el-form-item>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
          
        </el-collapse-item>
        <el-collapse-item v-if="machineFormData.pduBox == 0" title="机柜与传感器" name="4">
          <div class="sensorContainer">
            <div class="list">
              <template v-for="(item, index) in sensorListLeft" :key="index">
                <div class="minInterval" v-if="index > 0"></div>
                <!-- <div v-if="!item.sensorId" :class="item.sensorId ? 'boxActive' : 'box'" @click.prevent="handleSensorEdit(item, 0, index)">{{sensorType[item.type]}}{{item.position ? sensorPositon[item.position] : ''}}</div> -->
                <el-tooltip placement="right"  effect="light">
                  <template #content>PDU: {{item.pathPdu}}<br />传感器id: {{item.sensorId}}</template>
                  <div :class="item.pathPdu ? 'boxActive' : 'box'" @click.prevent="handleSensorEdit(item, 0, index)">
                    {{sensorType[item.type]}}{{item.position ? sensorPositon[item.position] : ''}}
                    <div v-if="item.pathPdu" @click.stop="handleSensorDelete(0, index)" class="delete"><Icon icon="ep:close" />
                    </div>
                  </div>
                </el-tooltip>
              </template>
              <div class="tip">
                <div>机柜前端</div>
                <div>(冷通道)</div>
              </div>
            </div>
            <div class="list">
              <template v-for="(item, index) in sensorListRight" :key="index">
                <div class="minInterval" v-if="index > 0"></div>
                <el-tooltip placement="right"  effect="light">
                  <template #content>PDU: {{item.pathPdu}}<br />传感器id: {{item.sensorId}}</template>
                  <div :class="item.sensorId ? 'boxActive' : 'box'" @click.prevent="handleSensorEdit(item, 1, index)">
                    {{sensorType[item.type]}}{{item.position ? sensorPositon[item.position] : ''}}
                    <div v-if="item.pathPdu" @click.stop="handleSensorDelete(1, index)" class="delete"><Icon icon="ep:close" /></div>
                  </div>
                </el-tooltip>
              </template>
              <div class="tip">
                <div>机柜后端</div>
                <div>(热通道)</div>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
    </template>
    <Dialog id="sensorDialog" v-model="sensorVisible" width="500px" title="传感器">
      <div class="sensorDialog" style="padding-left: 20px">
        <el-form
          ref="sensorForm"
          v-loading="sensorLoading"
          :model="sensorFormData"
          :rules="sensorFormRules"
          label-width="80px"
          center
        >
          <el-form-item label="类型">
            <el-input disabled :value="sensorType[sensorFormData.type]" />
          </el-form-item>
          <el-form-item label="PDU" prop="pathPdu">
            <el-select v-model="sensorFormData.pathPdu" placeholder="请选择">
              <el-option label="A路" value="A" />
              <el-option label="B路" value="B" />
            </el-select>
          </el-form-item>
          <el-form-item label="传感器id" prop="sensorId">
            <el-select v-model="sensorFormData.sensorId " placeholder="请选择">
              <template v-if="sensorFormData.type == 1 && leftRight[0] == 0">
                <el-option v-for="id in sensorLeftIds" :key="id" :label="id" :value="id" />
              </template>
              <template v-else-if="sensorFormData.type == 1 && leftRight[0] == 1">
                <el-option v-for="id in sensorRightIds" :key="id" :label="id" :value="id" />
              </template>
              <el-option v-else v-for="id in 2" :key="id" :label="id" :value="id" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
      <el-button @click="sensorVisible = false">取 消</el-button>
      <el-button :disabled="sensorLoading" type="primary" @click="submitSensorForm">确 定</el-button>
    </template>
    </Dialog>
  </Dialog>
</template>
<script lang="ts" setup>
import { FormRules } from 'element-plus'
import { CabinetApi } from '@/api/cabinet/info'
import TopologyEdit from './TopologyEdit.vue'

// const {roomList} = defineProps({
//   roomList: {
//     type: Array
//   },
// })
const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const roomList = ref([])
const isBusBind = ref(false) // 柜列中是否已经绑定母线了  绑定了则有些数据不能修改
const boxAmount = ref(0) // 插接箱数量, 柜列中如果绑定母线，则回显对应的插接箱数量
const boxListA = ref([])
const boxListB = ref([])
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const isFullscreen = ref(false)
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const sensorVisible = ref(false) // 传感器弹窗的是否展示
const sensorLoading = ref(false) // 传感器表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const sensorFormData = reactive({
  type: null,
  sensorId: null,
  pathPdu: '',
  channel: null
},)
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const sensorType = ['', '温湿度传感器', '门禁传感器', '水浸传感器', '烟雾传感器',]
const sensorPositon = {
  1: '(上)', 
  2: '(中)', 
  3: '(下)', 
}
const sensorLeftIds = ref([1, 2, 3, 4])
const sensorRightIds = ref([1, 2, 3, 4])
const leftRight = ref([0, 0])
const sensorListLeft = reactive([
  {
    type: 1,
    sensorId: null,
    position: 1,
    pathPdu: '',
    channel: 1
  },
  {
    type: 3,
    sensorId: null,
    pathPdu: '',
    channel: 1
  },
  {
    type: 4,
    sensorId: null,
    pathPdu: '',
    channel: 1
  },
  {
    type: 1,
    sensorId: null,
    position: 2,
    pathPdu: '',
    channel: 1
  },
  {
    type: 2,
    sensorId: null,
    pathPdu: '',
    channel: 1
  },
  {
    type: 1,
    sensorId: null,
    position: 3,
    pathPdu: '',
    channel: 1
  }
])
const sensorListRight = reactive([
  {
    type: 1,
    sensorId: null,
    position: 1,
    pathPdu: '',
    channel: 2
  },
  {
    type: 3,
    sensorId: null,
    pathPdu: '',
    channel: 2
  },
  {
    type: 4,
    sensorId: null,
    pathPdu: '',
    channel: 2
  },
  {
    type: 1,
    sensorId: null,
    position: 1,
    pathPdu: '',
    channel: 2
  },
  {
    type: 2,
    sensorId: null,
    pathPdu: '',
    channel: 2
  },
  {
    type: 1,
    sensorId: null,
    position: 3,
    pathPdu: '',
    channel: 2
  },
])
const machineFormData = ref({
  roomId: '',
  cabinetName: '',
  type: '',
  cabinetHeight: 42, //U
  powCapacity: 8, // kAV
  company: '',
  pduIpA: '',
  casIdA: '',
  pduIpB: '',
  casIdB: '',
  sensorList: [] as any,
  busIpA: '',
  barIdA: '',
  boxIndexA: '',
  boxOutletIdA: '',
  busIpB: '',
  barIdB: '',
  boxIndexB: '',
  boxOutletIdB: '',
  pduBox: 0, // 0 pdu 1母线
  eleAlarmDay: 0, // 日用能告警
  eleLimitDay: 1000, // 日用能限制
  eleAlarmMonth: 0, // 月用能告警
  eleLimitMonth: 1000, // 月用能限制
})
const PDUFormData = ref({
  ipdzA: '',
  jldzA: '',
  ipdzB: '',
  jldzB: '',
})

const machineFormRules = reactive<FormRules>({
  roomId: [{ required: true, message: '所属机房不能为空', trigger: 'blur' }],
  cabinetName: [{ required: true, message: '机柜名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '机柜类型不能为空', trigger: 'blur' }],
  cabinetHeight: [{ required: true, message: '机柜高度不能为空', trigger: 'blur' }],
  powCapacity: [{ required: true, message: '电力容量不能为空', trigger: 'blur' }],
})
const PDUFormRules = reactive<FormRules>({
  ipdzA: [{ required: true, message: 'A路PDU不能为空', trigger: 'blur' }],
  jldzA: [{ required: true, message: 'B路PDU不能为空', trigger: 'blur' }],
})
const sensorFormRules = reactive<FormRules>({
  pathPdu: [{ required: true, message: 'PDU不能为空', trigger: 'blur' }],
  sensorId: [{ required: true, message: '传感器id不能为空', trigger: 'blur' }],
})
const activeNames = ref(['1'])

watch(sensorListLeft, (val) => {
  const usedFilter = val.filter(item => item.type == 1 && item.sensorId)
  const list = usedFilter.map(item => item.sensorId)
  if (list.length == 0) return
  sensorLeftIds.value = sensorLeftIds.value.filter(item => !list.includes(item))
})

watch(sensorListRight, (val) => {
  const usedFilter = val.filter(item => item.type == 1 && item.sensorId)
  const list = usedFilter.map(item => item.sensorId)
  if (list.length == 0) return
  sensorRightIds.value = sensorRightIds.value.filter(item => !list.includes(item))
})

const handleSensorEdit = (data, i, index) => {
  console.log('handleSensorEdit', data)
  leftRight.value = [i, index]
  Object.assign(sensorFormData, data)
  sensorVisible.value = true
}
const handleSensorDelete = (i, index) => {
  console.log('handleSensorDelete',i, index)
  if (i == 0) {
    Object.assign(sensorListLeft[index], {
      ...sensorListLeft[index],
      sensorId: null,
      pathPdu: null,
    })
  } else {
    Object.assign(sensorListRight[index], {
      ...sensorListLeft[index],
      sensorId: null,
      pathPdu: null,
    })
  }
}
const submitSensorForm = async(data) => {
  try {
    // 校验表单
    if (!machineForm) return
    const valid = await machineForm.value.validate()
    if (!valid) return
    const index = leftRight.value[1]
    console.log('submitSensorForm', sensorFormData, leftRight.value[0], index)
    if (leftRight.value[0] == 0) {
      Object.assign(sensorListLeft[index], sensorFormData)
    } else {
      Object.assign(sensorListRight[index], sensorFormData)
    }
    sensorVisible.value = false
    console.log('sensorListLeft', sensorListLeft, sensorListRight)
  } catch (error) {
    message.error(String(error))
  }
}
const handleChange = (val: string[]) => {
  console.log(val)
}
const toggleFull = () => {
  isFullscreen.value = !isFullscreen.value
}
const machineForm = ref() // 机柜表单 Ref
const sensorForm = ref() // 传感器表单 Ref

/** 打开弹窗 */
const open = async (type: string, data, machineColInfo) => {
  dialogVisible.value = true
  dialogTitle.value = type == 'edit' ? '编辑': '添加'
  formType.value = type
  resetForm()
  sensorListLeft.forEach(item => {
    item.sensorId = null
    item.pathPdu = ''
  })
  console.log('data', data)
  if (data && data.sensorList && data.sensorList.length > 0) {
    data.sensorList.forEach(item => {
      if (item.channel == 1) {
        const index = sensorListLeft.findIndex(sensor => item.position ? (item.position == sensor.position) : (sensor.type == item.sensorType))
        sensorListLeft[index] = {
          ...sensorListLeft[index],
          sensorId: item.sensorId,
          pathPdu: item.pathPdu,
        }
      } else if (item.channel == 2) {
        const index = sensorListRight.findIndex(sensor => item.position ? (item.position == sensor.position) : (sensor.type == item.sensorType))
        sensorListRight[index] = {
          ...sensorListRight[index],
          sensorId: item.sensorId,
          pathPdu: item.pathPdu,
        }
      }
    })
  }
  machineFormData.value = data || {
    cabinetName: '',
    roomId: '',
    type: '',
    cabinetHeight: 42,
    powCapacity: 8,
    company: '',
    pduIpA: '',
    casIdA: '',
    pduIpB: '',
    casIdB: '',
    sensorList: [],
    busIpA: '',
    barIdA: '',
    boxIndexA: '',
    boxOutletIdA: '',
    busIpB: '',
    barIdB: '',
    boxIndexB: '',
    boxOutletIdB: '',
    pduBox: 0, // 0 pdu 1母线
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 1000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 1000, // 月用能限制
  }
  machineFormData.value.roomId = machineColInfo.roomId
  console.log('machineColInfo', machineColInfo)
  if (machineColInfo.barA) {
    isBusBind.value = true
    boxListA.value = machineColInfo.barA
    boxListB.value = machineColInfo.barB
    machineFormData.value = {
      ...machineFormData.value,
      barIdA: machineColInfo.barIdA,
      busIpA: machineColInfo.busIpA,
      barIdB: machineColInfo.barIdB,
      busIpB: machineColInfo.busIpB,
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  try {
    // 校验表单
    if (!machineForm) return
    const valid = await machineForm.value.validate()
    if (!valid) return
    // 提交请求
    formLoading.value = true
    const sensorList = [...sensorListLeft, ...sensorListRight]
    const sensorListFilter = sensorList.filter(item => item.sensorId)
    console.log('sensorListFilter', sensorListFilter)
    machineFormData.value.sensorList = sensorListFilter
    console.log('roomName', machineFormData.value)
    // const res = await CabinetApi.saveCabinetInfo({
    //   ...machineFormData.value,
    // })
    console.log('res', {...machineFormData.value}, machineFormData.value)
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success', {...machineFormData.value, addrA:null, addrB: null})
    resetForm()
  } catch(error:any) {
    console.log('error', error)
    message.error(String(error.cabinetName ? error.cabinetName[0].message : error.type[0].message))
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  machineFormData.value = {
    cabinetName: '',
    roomId: '',
    type: '',
    cabinetHeight: 42,
    powCapacity: 8,
    company: '',
    pduIpA: '',
    casIdA: '',
    pduIpB: '',
    casIdB: '',
    sensorList: [],
    busIpA: '',
    barIdA: '',
    boxIndexA: '',
    boxOutletIdA: '',
    busIpB: '',
    barIdB: '',
    boxIndexB: '',
    boxOutletIdB: '',
    pduBox: 0, // 0 pdu 1母线
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 1000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 1000, // 月用能限制
  }
  machineForm.value?.resetFields();
  sensorListRight.forEach(item => {
    item.sensorId = null
    item.pathPdu = ''
  })
  sensorListLeft.forEach(item => {
    item.sensorId = null
    item.pathPdu = ''
  })
  
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  console.log('接口获取机房导航列表', res)
  roomList.value = res
}
getNavList()
</script>
<style lang="scss" scoped>
.sensorContainer {
  border: 1px solid #efefef;
  padding: 30px 50px 20px 0;
  display: flex;
  align-items: center;
  justify-content: space-around;
  .list {
    width: 130px;
    border-top: 1px solid #ccc;
    & > div {
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #ccc;
      border-top: none;
    }
    .box {
      height: 45px;
      background-color: #f5f7fa;
      color: #a8abb2;
      cursor: pointer;
    }
    .boxActive {
      height: 45px;
      background-color: #78d560;
      color: #fff;
      cursor: pointer;
      position: relative;
      .delete {
        display: none;
        position: absolute;
        top: 0px;
        right: 0px;
      }
      &:hover {
        .delete {
          display: block;
          position: absolute;
          top: 0px;
          right: 0px;
        }
      }
      
    }
    .minInterval {
      height: 8px;
    }
    .defaultInterval {
      height: 15px;
    }
    .tip {
      flex-direction: column;
      border: none;
      margin-top: 10px;
    }
  }
}
.collapseItem {
  border: 1px solid #efefef;
  padding: 30px 50px 10px 0;
}
.pduBus {
  padding: 30px 50px 10px 0;
}
.double-formitem {
  display: flex;
  & > div {
    flex: 1;
  }
}
.Bus {
  display: flex;
  &>div {
    width: 50%;
    box-sizing: border-box;
    padding-right: 20px;
  }
  .title {
    font-size: 14px;
    margin-bottom: 20px;
    text-align: center;
  }
}
:deep(.el-collapse-item__content) {
  padding: 0 20px 20px;
}
.formContainer {
  padding: 20px;
}
</style>