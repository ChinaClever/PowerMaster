<template>
  <Dialog id="col-dialog" v-model="dialogVisible" title="配置" width="50%">
    <div class="formContainer">
      <el-form
        ref="colForm"
        v-loading="formLoading"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        center
      >
        <el-form-item label="类型：" prop="type">
          <el-select v-model="formData.type" placeholder="请选择">
            <el-option label="插接箱" :value="0" />
            <el-option label="连接器" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="级联地址：" prop="casAddr">
          <el-input v-model="formData.casAddr" placeholder="请输入" />
        </el-form-item>
        <el-form-item v-if="formData.type == 0" label="输出位：" prop="outletNum">
          <el-select v-model="formData.outletNum" placeholder="请选择">
            <el-option label="1个" :value="1" />
            <el-option label="2个" :value="2" />
            <el-option label="3个" :value="3" />
          </el-select>
        </el-form-item>
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

const colForm = ref()
const activeNames = ref(['1', '2', '3'])
const dialogVisible = ref(false) // 弹窗的是否展示
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref({
  id: '',
  type: 0,
  outletNum: 3,
  casAddr: '',
  boxIndex: 0
})

const tsead = () => {
  console.log('formData.value', formData.value)
}

const formRules = reactive<FormRules>({
  // barIdA: [{ required: true, message: 'A路母线编号不能为空', trigger: 'blur' }],
  // ipA: [{ required: true, message: 'A路ip不能为空', trigger: 'blur' }],
  // barIdB: [{ required: true, message: 'B路母线编号不能为空', trigger: 'blur' }],
  // ipB: [{ required: true, message: 'B路ip不能为空', trigger: 'blur' }],
})

const handleChange = () => {
  console.log('handleChange', activeNames.value)
}

/** 打开弹窗 */
const open = async (data) => {
  dialogVisible.value = true
  resetForm()
  console.log('data', data)
  if (data) formData.value = {...data}
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  try {
    if (!colForm) return
    formLoading.value = true
    const valid = await colForm.value.validate()
    if (!valid) return
    // 提交请求
    console.log('提交请求', formData.value)
    dialogVisible.value = false
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
    id: '',
    type: 0,
    outletNum: 3,
    casAddr: '',
    boxIndex: 0
  }
  colForm.value?.resetFields()
}
</script>

<style lang="scss" scoped>
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