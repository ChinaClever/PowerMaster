<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="pdu编号" prop="pduId">
        <el-input v-model="formData.pduId" placeholder="请输入pdu编号" />
      </el-form-item>
      <el-form-item label="数据类型	" prop="type">
        <el-select v-model="formData.type" placeholder="请选择数据类型	">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="topic" prop="topic">
        <el-select v-model="formData.topic" placeholder="请选择topic">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="indexes" prop="indexes">
        <el-input v-model="formData.indexes" placeholder="请输入indexes" />
      </el-form-item>
      <el-form-item label="值" prop="value">
        <el-input v-model="formData.value" placeholder="请输入值" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { HistoryDataApi, HistoryDataVO } from '@/api/pdu/historydata'

/** pdu历史数据 表单 */
defineOptions({ name: 'HistoryDataForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  pduId: undefined,
  type: undefined,
  topic: undefined,
  indexes: undefined,
  value: undefined,
})
const formRules = reactive({
  pduId: [{ required: true, message: 'pdu编号不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '数据类型	不能为空', trigger: 'change' }],
  topic: [{ required: true, message: 'topic不能为空', trigger: 'change' }],
  indexes: [{ required: true, message: 'indexes不能为空', trigger: 'blur' }],
  value: [{ required: true, message: '值不能为空', trigger: 'blur' }],
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
      formData.value = await HistoryDataApi.getHistoryData(id)
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
    const data = formData.value as unknown as HistoryDataVO
    if (formType.value === 'create') {
      await HistoryDataApi.createHistoryData(data)
      message.success(t('common.createSuccess'))
    } else {
      await HistoryDataApi.updateHistoryData(data)
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
    pduId: undefined,
    type: undefined,
    topic: undefined,
    indexes: undefined,
    value: undefined,
  }
  formRef.value?.resetFields()
}
</script>