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
      <el-form-item label="类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择活动区域">
          <el-option label="机柜列" :value="1" />
          <el-option label="机柜" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入" />
      </el-form-item>

      <el-form-item v-if="formData.type == 2" label="高度" prop="cabinetHeight">
         <el-input v-model.number="formData.cabinetHeight" :suffixIcon="() => 'U'" />
      </el-form-item> 
      <el-form-item v-if="formData.type == 1" label="方向" prop="direction">
        <el-select v-model="formData.direction" placeholder="请选择活动区域">
          <el-option label="横向" :value="1" />
          <el-option label="纵向" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="formData.type == 1" label="数量" prop="amount">
         <el-input-number v-model="formData.amount" :min="minAmount" :max="formData.direction == 1 ? operateInfo.maxlndexX : operateInfo.maxlndexY" />
      </el-form-item>

      <el-form-item label="电力容量">
        <el-input v-model="formData.powerCapacity" placeholder="请输入" />
      </el-form-item>

      <div style="display: flex;">
        <div style="flex: 1;">
          <el-form-item label="日用能告警" label-width="100">
            <el-switch v-model="formData.eleAlarmDay" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item label="日用能限制" label-width="100">
            <el-input-number v-model="formData.eleLimitDay" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
      </div>
      <div style="display: flex;justify-content: space-evenly;">
        <div style="flex: 1;">
          <el-form-item label="月用能告警" label-width="100">
            <el-switch v-model="formData.eleAlarmMonth" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item label="月用能限制" label-width="100">
            <el-input-number v-model="formData.eleLimitMonth" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
          </el-form-item>
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
const dialogTitle = ref('') // 弹窗的标题
const operateInfo = ref<any>({}) // 机柜列中机柜最大数量  默认26
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  eleAlarmDay: 0, // 日用能告警
  eleLimitDay: 1000, // 日用能限制
  eleAlarmMonth: 0, // 月用能告警
  eleLimitMonth: 1000, // 月用能限制
  powerCapacity:0, //柜列电力容量
  type: 1,
  name: '',
  cabinetHeight: 42,
  direction: 1,
  amount: 12,
  id: '',
  cabinetList: [] as any
})
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  // id: [{ required: true, message: 'id不能为空', trigger: 'blur' }],
})
const layoutForm = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, data, info) => {
  dialogVisible.value = true
  dialogTitle.value = type == 'edit' ? '编辑': '添加'
  formType.value = type
  operateInfo.value = info
  resetForm()
  console.log('data', data)
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
  console.log('formData.value', formData.value)
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
  formLoading.value = true
  try {
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success', {...formData.value });
  } catch (error) {
    console.log('error', error)
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 1000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 1000, // 月用能限制
    powerCapacity:0,
    type: 1,
    name: '',
    cabinetHeight: 42,
    direction: 1,
    amount: 12,
    id: '',
    cabinetList: []
  }
  minAmount.value = 1
  layoutForm.value?.resetFields()
}

watch(() => formData.value.direction, (val) => {
  if (val == 1 && formData.value.amount > operateInfo.value.maxlndexX) {
    formData.value.amount = operateInfo.value.maxlndexX
  } else if(val == 2 && formData.value.amount > operateInfo.value.maxlndexY) {
    formData.value.amount = operateInfo.value.maxlndexY
  }
})
</script>