<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="绿色的范围" prop="rangeOne">
        小于<el-input style="width: 11.3%;" type="number" v-model="formData.rangeOne" placeholder="请输入第一个小于的范围" />%
      </el-form-item>
      <el-form-item label="黄色的范围" prop="rangeTwo">
        <el-input style="width: 11.3%;" type="number" v-model="formData.rangeTwo" placeholder="请输入第二个范围的最小值" />%-
        <el-input style="width: 11.3%;" type="number" v-model="formData.rangeThree" placeholder="请输入第二个范围的最大值" />%
      </el-form-item>

      <el-form-item label="红色的范围" prop="rangeFour">
        大于<el-input style="width: 11.3%;" type="number" v-model="formData.rangeFour" placeholder="请输入第三个大于的范围" />%
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { CurbalanceColorApi, CurbalanceColorVO } from '@/api/pdu/curbalancecolor'

/** PDU不平衡度颜色 表单 */
defineOptions({ name: 'CurbalanceColorForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  rangeOne: undefined,
  rangeTwo: undefined,
  rangeThree: undefined,
  rangeFour: undefined,
})
const formRules = reactive({
  rangeOne: [{ required: true, message: '第一个小于的范围不能为空', trigger: 'blur' }],
  rangeTwo: [{ required: true, message: '第二个范围的最小值不能为空', trigger: 'blur' }],
  rangeThree: [{ required: true, message: '第二个范围的最大值不能为空', trigger: 'blur' }],
  rangeFour: [{ required: true, message: '第三个大于的范围不能为空', trigger: 'blur' }],
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据

  formLoading.value = true
  try {
    var data = await CurbalanceColorApi.getCurbalanceColor()
    console.log(data)
    if(data != null){
      formData.value = data;
      formType.value = 'update'
    }
    formData.value;
  } finally {
    formLoading.value = false
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
    const data = formData.value as unknown as CurbalanceColorVO
    if (formType.value === 'create') {
      await CurbalanceColorApi.createCurbalanceColor(data)
      message.success(t('common.createSuccess'))
    } else {
      await CurbalanceColorApi.updateCurbalanceColor(data)
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
    rangeOne: undefined,
    rangeTwo: undefined,
    rangeThree: undefined,
    rangeFour: undefined,
  }
  formRef.value?.resetFields()
}
</script>