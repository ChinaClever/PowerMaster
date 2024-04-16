<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form 
      ref="layoutForm"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      center
    >
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="id" prop="id">
        <el-input v-model="formData.id" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status" prop="status">
          <el-radio :label="1">正常</el-radio>
          <el-radio :label="0">异常</el-radio>
        </el-radio-group>
      </el-form-item>
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

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  name: '',
  status: 1,
  id: '',
})
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  id: [{ required: true, message: 'id不能为空', trigger: 'blur' }],
})
const layoutForm = ref() // 表单 Ref
// const deptList = ref<Tree[]>([]) // 树形结构
// const postList = ref([] as PostApi.PostVO[]) // 岗位列表

/** 打开弹窗 */
const open = async (type: string, data) => {
  dialogVisible.value = true
  dialogTitle.value = type == 'edit' ? '编辑': '添加'
  formType.value = type
  resetForm()
  console.log('data', data)
  // const d = toRef(data)
  // console.log('d', d)
  formData.value = data || {
    name: '',
    status: 1,
    id: '',
  }
  console.log('formData', formData.value)
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
  console.log('提交请求')
  formLoading.value = true
  try {
    // const data = formData.value as unknown as UserApi.UserVO
    // if (formType.value === 'create') {
    //   await UserApi.createUser(data)
    //   message.success(t('common.createSuccess'))
    // } else {
    //   await UserApi.updateUser(data)
    //   message.success(t('common.updateSuccess'))
    // }
    dialogVisible.value = false
    console.log('发送操作成功的事件')
    // 发送操作成功的事件
    emit('success', formData.value)
  } catch (error) {
    console.log('error', error)
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    name: '',
    status: 1,
    id: '',
  }
  layoutForm.value?.resetFields()
}
</script>