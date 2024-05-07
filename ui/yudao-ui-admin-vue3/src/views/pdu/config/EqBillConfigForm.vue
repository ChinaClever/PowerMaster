<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="电费单价" prop="bill">
        <el-input v-model="formData.bill" placeholder="请输入电费单价" />
      </el-form-item>
      <el-form-item label="计费方式" prop="billMode" >
        <el-radio-group v-model="formData.billMode">
          <el-radio :label="1">固定计费</el-radio>
          <el-radio :label="2">分段计费</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="计费时间段" prop="timePicker" >
        <el-time-picker
          v-model="formData.timePicker"
          is-range
          arrow-control
          value-format="HH:mm:ss"
          range-separator="到"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { EqBillConfigApi, EqBillConfigVO } from '@/api/pdu/eqbillconfig'

/** pdu电量计费方式配置 表单 */
defineOptions({ name: 'EqBillConfigForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  bill: undefined,
  billMode: undefined,
  billPeriod: undefined,
  timePicker:[],
})
const formRules = reactive({
  bill: [{ required: true, message: '电费单价不能为空', trigger: 'blur' }],
  billMode: [{ required: true, message: '计费方式 1固定计费 2 分段计费不能为空', trigger: 'blur' }],
  billPeriod: [{ required: true, message: '计费时间段不能为空', trigger: 'blur' }],
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await EqBillConfigApi.getEqBillConfig(id)
      if(formData.value.billPeriod){
        formData.value.timePicker = formData.value.billPeriod.split("-")
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as EqBillConfigVO
    data.billPeriod = formData.value.timePicker[0] + "-" + formData.value.timePicker[1];
    console.log(data.billPeriod)
    if (formType.value === 'create') {
      await EqBillConfigApi.createEqBillConfig(data)
      message.success(t('common.createSuccess'))
    } else {
      await EqBillConfigApi.updateEqBillConfig(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    bill: undefined,
    billMode: undefined,
    billPeriod: undefined,
  }
  formRef.value?.resetFields()
}
</script>