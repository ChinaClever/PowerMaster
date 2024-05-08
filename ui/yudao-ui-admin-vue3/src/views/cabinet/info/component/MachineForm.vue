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
            <el-form-item label="机房：" prop="roomName">
              <el-select v-model="machineFormData.roomName" placeholder="请选择">
                <el-option label="机房1" value="机房1" />
                <el-option label="机房2" value="机房2" />
                <el-option label="机房3" value="机房3" />
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
          </div>
        </el-collapse-item>
        <el-collapse-item title="PDU绑定" name="2">
          <div class="collapseItem">
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
        </el-collapse-item>
        <el-collapse-item title="U位绑定" name="3">
          <div class="collapseItem">
            <TopologyEdit />
          </div>
        </el-collapse-item>
        <el-collapse-item title="机柜与传感器" name="4">
          <div class="sensorContainer">
            <div class="list">
              <div class="box">温湿度传感器上</div>
              <div class="box">水浸</div>
              <div class="box">烟雾传感器</div>
              <div class="minInterval"></div>
              <div class="box">温湿度传感器中</div>
              <div class="defaultInterval"></div>
              <div class="box">门禁</div>
              <div class="defaultInterval"></div>
              <div class="box">温湿度传感器下</div>
              <div class="tip">
                <div>机柜前端</div>
                <div>(冷通道)</div>
              </div>
            </div>
            <div class="list">
              <div class="box">温湿度传感器上</div>
              <div class="box">水浸</div>
              <div class="box">烟雾传感器</div>
              <div class="minInterval"></div>
              <div class="box">温湿度传感器中</div>
              <div class="defaultInterval"></div>
              <div class="box">门禁</div>
              <div class="defaultInterval"></div>
              <div class="box">温湿度传感器下</div>
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
  </Dialog>
</template>
<script lang="ts" setup>
import { FormRules } from 'element-plus'
import { CabinetApi } from '@/api/cabinet/info'
import TopologyEdit from './TopologyEdit.vue'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const isFullscreen = ref(false)
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const machineFormData = ref({
  roomName: '',
  cabinetName: '',
  type: '',
  cabinetHeight: 42, //U
  powCapacity: 8, // kAV
  company: '',
  pduIpA: '',
  casIdA: '',
  pduIpB: '',
  casIdB: '',
})
const PDUFormData = ref({
  ipdzA: '',
  jldzA: '',
  ipdzB: '',
  jldzB: '',
})
const sensorFormData = ref()
const machineFormRules = reactive<FormRules>({
  roomName: [{ required: true, message: '所属机房不能为空', trigger: 'blur' }],
  cabinetName: [{ required: true, message: '机柜名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '机柜类型不能为空', trigger: 'blur' }],
  cabinetHeight: [{ required: true, message: '机柜高度不能为空', trigger: 'blur' }],
  powCapacity: [{ required: true, message: '电力容量不能为空', trigger: 'blur' }],
})
const PDUFormRules = reactive<FormRules>({
  ipdzA: [{ required: true, message: 'A路PDU不能为空', trigger: 'blur' }],
  jldzA: [{ required: true, message: 'B路PDU不能为空', trigger: 'blur' }],
})
const activeNames = ref(['1'])
const handleChange = (val: string[]) => {
  console.log(val)
}
const toggleFull = () => {
  isFullscreen.value = !isFullscreen.value
}
const machineForm = ref() // 机柜表单 Ref
// const deptList = ref<Tree[]>([]) // 树形结构
// const postList = ref([] as PostApi.PostVO[]) // 岗位列表

/** 打开弹窗 */
const open = async (type: string, data) => {
  dialogVisible.value = true
  dialogTitle.value = type == 'edit' ? '编辑': '添加'
  formType.value = type
  resetForm()
  console.log('data', data)
  machineFormData.value = data || {
    cabinetName: '',
    roomName: '',
    type: '',
    cabinetHeight: 42,
    powCapacity: 8,
    company: '',
    pduIpA: '',
    casIdA: '',
    pduIpB: '',
    casIdB: '',
  }
  // 修改时，设置数据
  // if (id) {
  //   formLoading.value = true
  //   try {
  //     machineFormData.value = await UserApi.getUser(id)
  //   } finally {
  //     formLoading.value = false
  //   }
  // }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!machineForm) return
  const valid = await machineForm.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    console.log('roomName', machineFormData.value)
    const res = await CabinetApi.saveCabinetInfo({
      ...machineFormData.value,
      roomId: 1,
    })
    console.log('res', res, machineFormData.value)
    // const data = machineFormData.value as unknown as UserApi.UserVO
    // if (formType.value === 'create') {
    //   await UserApi.createUser(data)
    //   message.success(t('common.createSuccess'))
    // } else {
    //   await UserApi.updateUser(data)
    //   message.success(t('common.updateSuccess'))
    // }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  machineFormData.value = {
    cabinetName: '',
    roomName: '',
    type: '',
    cabinetHeight: 42,
    powCapacity: 8,
    company: '',
    pduIpA: '',
    casIdA: '',
    pduIpB: '',
    casIdB: '',
  }
  machineForm.value?.resetFields()
}
</script>
<style lang="scss" scoped>
.sensorContainer {
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
:deep(.el-collapse-item__content) {
  padding: 0 20px 20px;
}
.formContainer {
  padding: 20px;
}
</style>