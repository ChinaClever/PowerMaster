<template>
  <Dialog id="col-dialog" v-model="dialogVisible" :title="dialogTitle" width="60%">
    <div class="formContainer">
      <el-form
        ref="colForm"
        v-loading="formLoading"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        center
      >
        <el-collapse v-model="activeNames" @change="handleChange">
          <el-collapse-item title="始端箱" name="1">
            <div class="collapseItem">
              <el-form-item label="A路名称：" prop="nameA">
                <el-input v-model="formData.nameA" placeholder="请输入" />
              </el-form-item>
              <el-form-item label="B路名称：" prop="nameB">
                <el-input v-model="formData.nameB" placeholder="请输入" />
              </el-form-item>
              <el-form-item label="A路ip：" prop="ipA">
                <el-input v-model="formData.ipA" placeholder="请输入" />
              </el-form-item>
              <el-form-item label="B路ip：" prop="ipB">
                <el-input v-model="formData.ipB" placeholder="请输入" />
              </el-form-item>
              <el-form-item label="位置：" prop="direction">
                <el-select v-model="formData.direction" placeholder="请选择">
                  <el-option label="同侧" :value="0" />
                  <el-option label="异侧" :value="1" />
                </el-select>
              </el-form-item>
            </div>
          </el-collapse-item>
          <el-collapse-item title="插接箱和连接器" name="2">
            <div class="collapseItem">
              <el-form-item label="插接箱数量：" prop="cjxAmount">
                <el-input-number v-model="formData.cjxAmount" :min="0" :max="20" />
              </el-form-item>
              <el-form-item label="连接器数量：" prop="ljqAmount">
                <el-input-number v-model="formData.ljqAmount" :min="0" :max="20" />
              </el-form-item>
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

const colForm = ref()
const activeNames = ref(['1', '2'])
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref({
  nameA: '',
  ipA: '',
  nameB: '',
  direction: 0,
  ipB: '',
  cjxAmount: 0,
  ljqAmount: 0,
})

const formRules = reactive<FormRules>({
  nameA: [{ required: true, message: 'A路始端箱名称不能为空', trigger: 'blur' }],
  ipA: [{ required: true, message: 'A路ip不能为空', trigger: 'blur' }],
  nameB: [{ required: true, message: 'B路始端箱名称不能为空', trigger: 'blur' }],
  ipB: [{ required: true, message: 'B路ip不能为空', trigger: 'blur' }],
})

const handleChange = () => {
  console.log('handleChange', activeNames.value)
}

/** 打开弹窗 */
const open = async (type: string, data) => {
  dialogVisible.value = true
  dialogTitle.value = type == 'edit' ? '编辑': '添加'
  resetForm()
  console.log('data', data)
  if (data) formData.value = data
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
    nameA: '',
    ipA: '',
    nameB: '',
    direction: 0,
    ipB: '',
    cjxAmount: 0,
    ljqAmount: 0,
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