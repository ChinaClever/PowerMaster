<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="80%" align-center>
    <el-form 
      ref="layoutForm"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="130"
      center
    >
      <div style="display: flex;">
        <div>
          <el-form-item label="柜列方向" prop="direction">
            <el-select v-model="formData.direction" placeholder="请选择活动区域" style="width: 100px">
              <el-option label="横向" :value="1"/>
              <el-option label="纵向" :value="2"/>
            </el-select>
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item label="柜列名称" prop="aisleName" label-width="130">
            <el-input v-model="formData.aisleName" placeholder="请输入柜列名称" style="width: 80%" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item v-if="formData.type == 1" label="机柜数量" prop="amount">
            <el-input-number v-model="formData.amount" :max="26" style="width: 80%" @change="handleChange" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item label="总电力容量">
            <el-input type="number" v-model="formData.powerCapacity" placeholder="请输入总电力容量" style="width: 80%">
              <template #append>kVA</template>
            </el-input>
          </el-form-item>
        </div>
      </div>

      <div style="display: flex;">
        <div>
          <el-form-item label="柜列用能" label-width="130">
            <el-switch v-model="isAutoCreate.aisleAlarm" :active-value="1" :inactive-value="0" style="width: 100px;--el-switch-on-color: #00778c;" />
          </el-form-item>
        </div>
        <div v-if="isAutoCreate.aisleAlarm" style="flex: 1;">
          <el-form-item label="日用能告警" label-width="130">
            <el-switch v-model="formData.eleAlarmDay" :active-value="1" :inactive-value="0" style="width: 15%;--el-switch-on-color: #00778c;" />
            <el-input type="number" v-if="formData.eleAlarmDay" v-model="formData.eleLimitDay" :min="0" controls-position="right" placeholder="请输入柜列日用能限制" style="width: 65%">
              <template #append>kWh</template>
            </el-input>
          </el-form-item>
        </div>
        <div v-if="isAutoCreate.aisleAlarm" style="flex: 1;">
          <el-form-item label="月用能告警" label-width="130">
            <el-switch v-model="formData.eleAlarmMonth" :active-value="1" :inactive-value="0" style="width: 15%;--el-switch-on-color: #00778c;" />
            <el-input type="number" v-if="formData.eleAlarmMonth" v-model="formData.eleLimitMonth" :min="0" controls-position="right" placeholder="请输入柜列月用能限制" style="width: 65%">
              <template #append>kWh</template>
            </el-input>
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <div style="display: flex;">
            <el-form-item label="横坐标" label-width="130">
              <el-input type="number" v-model="formData.xCoordinate" :min="1" :max="60" controls-position="right" placeholder="请输入"/>
            </el-form-item>
            <el-form-item label="纵坐标" label-width="72">
              <el-input type="number" v-model="formData.yCoordinate" :min="1" :max="60" controls-position="right" placeholder="请输入"/>
            </el-form-item>
          </div>
        </div>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { FormRules } from 'element-plus'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const beginAmount = ref(1) // data传进来时的数量 柜列且长度大于一时才需要
const minAmount = ref(1) // 最小机柜数量
const dialogVisible = ref(false) // 弹窗的是否展示
const isAutoCreate = ref({
  air: false,
  cabinet: false,
  pdu: false,
  sensor: false,
  bus: false,
  aisleAlarm: false,
  cabinetAlarm: false
})
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  eleAlarmDay: 0, // 日用能告警
  eleLimitDay: 15000, // 日用能限制
  eleAlarmMonth: 0, // 月用能告警
  eleLimitMonth: 450000, // 月用能限制
  powerCapacity:480, //柜列电力容量
  type: 1,
  aisleName: '',
  cabinetHeight: 42,
  pduBar: false,
  direction: 1,
  amount: 12,
  id: '',
  cabinetList: [] as any,
  xCoordinate: 0,
  yCoordinate: 0
})
const formRules = reactive<FormRules>({
  aisleName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  // id: [{ required: true, message: 'id不能为空', trigger: 'blur' }],
})
const layoutForm = ref() // 表单 Ref

const machineFormData = ref({
  cabinetName: '',
  cabinetType: 'IT机柜',
  cabinetHeight: 42, //U
  powCapacity: 40, // kVA
  pduBox: true,
  pduIp: "",
  addr: 0,
  addrNum: 0,
  busIpa: "",
  busSerialNuma: 1,
  boxAddra: 2,
  outNuma: 3,
  busIpb: "",
  busSerialNumb: 2,
  boxAddrb: 2,
  outNumb: 3,
  frontPath: "",
  frontSensorId: "",
  backPath: "",
  backSensorId: "",
  eleAlarmDay: false,
  eleAlarmMonth: false,
  eleLimitDay: 1000,
  eleLimitMonth: 30000,
  first: false,
  airList: [0,0,0]
})


/** 打开弹窗 */
const open = async (type: string, data, info) => {
  dialogVisible.value = true
  dialogTitle.value = type == 'edit' ? '编辑柜列': '新增柜列'
  formType.value = type
  resetForm()
  console.log('data', data,info)
  if (data && data.type == 1 && data.amount > 1 && data.cabinetList.length > 0) { // 如果是柜列且长度大于一，则该柜列的最小长度应为其中最后那个绑定id机柜的下标值，如果最小值比这个下标值还小的话就会删掉该机柜
    const length = data.amount
    beginAmount.value = length
    for (let i =1; i <= length; i++) {
      console.log('data.cabinetList[length - i].index', data.cabinetList[length - i].index)
      if (data.cabinetList[length - i].id > 0) {
        minAmount.value = data.cabinetList[length - i].index
        break
      }
    }
  }
  if (data) formData.value = data
  formData.value.direction = data.direction == 'x' ? 1 : 2
  console.log('formData.value', formData.value)
  if(type == 'edit' && (formData.value.eleAlarmDay || formData.value.eleAlarmMonth)) {
    isAutoCreate.value.aisleAlarm = true
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  console.log('校验表单',)
  if (!layoutForm) return
  const valid = await layoutForm.value.validate()
  if (!valid) return
  // 提交请求
  if (formData.value.type == 1 && formData.value.cabinetList.length > 0) {
    const diff = formData.value.amount - beginAmount.value
    console.log('diff', diff)
    if (diff > 0) { // 增加
      for (let i = 0; i < diff; i++) {
        formData.value.cabinetList.push({
          id: 0,
          cabinetName: '',
          runStatus: 0
        })
      }
    } else if(diff < 0) { // 减少
      formData.value.cabinetList.splice(formData.value.amount - 1, -diff)
    }
  }

  if(!isAutoCreate.value.aisleAlarm) {
    formData.value.eleAlarmDay = 0
    formData.value.eleAlarmMonth = 0
  }
  
  formLoading.value = true
  try {
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success', {...formData.value});
  } catch (error) {
    console.log('error', error)
  } finally {
    formLoading.value = false
  }
}

const handleChange = (currentValue: number | undefined, oldValue: number | undefined) => {
  if(currentValue < minAmount.value) {
    formData.value.amount = minAmount.value
    message.warning("当前柜列中机柜数量为" + minAmount.value + ",要减少的位置不能为空")
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 15000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 450000, // 月用能限制
    powerCapacity:480,
    type: 1,
    aisleName: '',
    cabinetHeight: 42,
    pduBar: false,
    direction: 1,
    amount: 12,
    id: '',
    cabinetList: [],
    xCoordinate: 0,
    yCoordinate: 0
  }

  machineFormData.value = {
    cabinetName: '',
    cabinetType: 'IT机柜',
    cabinetHeight: 42, //U
    powCapacity: 40, // kVA
    pduBox: true,
    pduIp: "",
    addr: 0,
    addrNum: 0,
    busIpa: "",
    busSerialNuma: 1,
    boxAddra: 2,
    outNuma: 3,
    busIpb: "",
    busSerialNumb: 2,
    boxAddrb: 2,
    outNumb: 3,
    frontPath: "",
    frontSensorId: "",
    backPath: "",
    backSensorId: "",
    eleAlarmDay: false,
    eleAlarmMonth: false,
    eleLimitDay: 1000,
    eleLimitMonth: 30000,
    first: false,
    airList: [0,0,0]
  }
  isAutoCreate.value = {
    air: false,
    cabinet: false,
    pdu: false,
    sensor: false,
    bus: false,
    aisleAlarm: false,
    cabinetAlarm: false
  }
  minAmount.value = 1
  layoutForm.value?.resetFields()
}
</script>
<style lang="scss" scoped>
.double-formitem {
  display: flex;
  & > div {
    flex: 1;
  }
}
.box {
  background-color: #f5f7fa;
  color: #a8abb2;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding-left: 10px;
  border-radius: 5px;
}
.boxActive {
  background-color: #78d560;
  color: #fff;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  padding-left: 10px;
  border-radius: 5px;
  .delete {
    display: none;
    position: absolute;
    top: -5px;
    right: 0px;
  }
  &:hover {
    .delete {
      display: block;
      position: absolute;
      top: -5px;
      right: 0px;
    }
  }
  
}
</style>