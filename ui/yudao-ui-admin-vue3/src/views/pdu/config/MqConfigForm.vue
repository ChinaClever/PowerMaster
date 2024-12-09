<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="ip端口" prop="ipAndPorts">
        <el-input v-model="formData.ipAndPorts" placeholder="请输入ip端口" />
      </el-form-item>
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="formData.userName" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="passWord">
        <el-input v-model="formData.passWord" placeholder="请输入密码" />
      </el-form-item>
      <el-form-item label="主题/分组" prop="topic">
        <el-input v-model="formData.topic" placeholder="请输入主题/分组" />
      </el-form-item>
      <el-form-item label="mq名称" prop="mq">
        <el-select v-model="formData.mq" placeholder="请选择一个消息队列">
          <el-option label="kafka" value="kafka" />
          <el-option label="RocketMQ" value="RocketMQ" />
          <el-option label="RabbitMQ" value="RabbitMQ" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { MqConfigApi, MqConfigVO } from '@/api/pdu/mqconfig'

/** 消息队列系统配置 表单 */
defineOptions({ name: 'MqConfigForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  ipAndPorts: undefined,
  userName: undefined,
  passWord: undefined,
  topic: undefined,
  mq: undefined,
})
const formRules = reactive({
  ipAndPorts: [{ required: true, message: 'ip端口不能为空', trigger: 'blur' }],
  mq: [{ required: true, message: 'mq名称不能为空', trigger: 'blur' }],
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
      formData.value = await MqConfigApi.getMqConfig(id)
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
    const data = formData.value as unknown as MqConfigVO
    if (formType.value === 'create') {
      await MqConfigApi.createMqConfig(data)
      message.success(t('common.createSuccess'))
    } else {
      await MqConfigApi.updateMqConfig(data)
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
    ipAndPorts: undefined,
    userName: undefined,
    passWord: undefined,
    topic: undefined,
    mq: undefined,
  }
  formRef.value?.resetFields()
}
</script>