<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item prop="rangeOne" >
        <div style="background-color: #3bbb00; margin-right:20px;width:100px;text-align:center;border-radius:5px;">绿色的范围</div>
        小于<el-input style="width: 11.3%;" type="number" v-model="formData.rangeOne" placeholder="请输入第一个小于的范围" />%
      </el-form-item>
      <el-form-item  prop="rangeTwo" >
        <div style="background-color: #ffc402;margin-right:20px;width:100px;text-align:center;border-radius:5px;">黄色的范围</div>
        <el-input style="width: 11.3%;" type="number" v-model="formData.rangeTwo" placeholder="请输入第二个范围的最小值" />%-
        <el-input style="width: 11.3%;" type="number" v-model="formData.rangeThree" placeholder="请输入第二个范围的最大值" />%
      </el-form-item>

      <el-form-item  prop="rangeFour" >
        <div style="background-color: #fa3333;margin-right:20px;width:100px;text-align:center;border-radius:5px;">红色的范围</div>
        大于<el-input style="width: 11.3%;" type="number" v-model="formData.rangeFour" placeholder="请输入第三个大于的范围" />%
      </el-form-item>

      <el-form-item  prop="lowCurrent" >
        <div style="background-color: #aaa;margin-right:20px;width:100px;text-align:center;border-radius:5px;">小电流的范围</div>
        小于<el-input style="width: 11.3%;" type="number" v-model="formData.lowCurrent" placeholder="请输入第四个小于的范围" />%
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
import { inject } from 'vue';
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
  lowCurrent: undefined,
})
const validateRangeOrder = (rule, value, callback) => {
  // 检查范围是否为空
  if (!formData.value.rangeOne || !formData.value.rangeTwo || !formData.value.rangeThree || !formData.value.rangeFour) {
    callback(new Error('请正确填写所有范围'));
  } else {
    const ranges = [parseFloat(formData.value.rangeOne), parseFloat(formData.value.rangeTwo), parseFloat(formData.value.rangeThree), parseFloat(formData.value.rangeFour)];
    const rangeNames = ['第一空', '第二空', '第三空', '第四空'];
    for (let i = 0; i < ranges.length; i++) {
      if(ranges[i] < 0){
        return callback(new Error(`第${i+1}空不能为负数`));
      }
    }
    // 检查范围顺序
    for (let i = 0; i < ranges.length - 1; i++) {
      if (ranges[i] > ranges[i + 1]) {
        return callback(new Error(`${rangeNames[i]} 必须小于等于 ${rangeNames[i + 1]}`));
      }
    }

    // 如果所有条件都满足，则回调无错误
    callback();
  }
}

const formRules = reactive({
  rangeOne: [{ validator: validateRangeOrder,required: true,  trigger: 'blur' }],
  rangeTwo: [{ validator: validateRangeOrder,required: true,  trigger: 'blur' }],
  rangeThree: [{ validator: validateRangeOrder,required: true,  trigger: 'blur' }],
  rangeFour: [{ validator: validateRangeOrder,required: true,  trigger: 'blur' }],
  lowCurrent: [{ validator: validateRangeOrder,required: true,  trigger: 'blur' }],
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string) => {
  dialogVisible.value = true
  if (type === 'create') {
    dialogTitle.value = t('电流不平衡度范围设定')
  } else {
    dialogTitle.value = t('action.' + type)
  }
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
const emit = defineEmits(['success', 'custom-event']); // 定义 success 事件，用于操作成功后的回调
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
    lowCurrent: undefined,
  }
  formRef.value?.resetFields()
}
</script>