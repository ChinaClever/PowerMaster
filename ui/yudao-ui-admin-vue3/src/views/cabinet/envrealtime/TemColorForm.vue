<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" style="width:70%;">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="50px"
      v-loading="formLoading"
    >
      <el-button @click="addTemColor">添加温度范围颜色</el-button>
      <el-button @click="deleteTemColor" v-if="formData.colorArr.length > 0">删除最后一个</el-button>
      <div><br/></div>
      <div style="display:blcock;width:50%;margin-left:-30px;">
        <div v-for="item in formData.colorArr" :key="item.id" style="width:100%;">        
        <el-form-item>
          <span style="width:80px;">冷通道温度</span>
          <el-input style="width: 30%;" type="number" v-model="item.min"  />°C~
          <el-input style="width: 30%;" type="number" v-model="item.max"  />°C
          颜色<el-color-picker v-model="item.color" />      
        </el-form-item>    
      </div>
      </div>
      <div style="display:blcock;width:50%;margin-left:50%;margin-top:-152px;">
        <div v-for="item in formData.colorArr" :key="item.id" style="width:100%;">        
        <el-form-item>
          <span style="width:80px;">热通道温度</span>
          <el-input style="width: 30%;" type="number" v-model="item.min"  />°C~
          <el-input style="width: 30%;" type="number" v-model="item.max"  />°C
          颜色<el-color-picker v-model="item.color" />      
        </el-form-item>    
      </div>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { TemColorApi, TemColorVO } from '@/api/cabinet/temcolor'

/** 机柜温度颜色 表单 */
defineOptions({ name: 'TemColorForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
    colorArr : [
      {
        id: undefined,
        min: undefined,
        max: undefined,
        color: undefined,
      },
    ]
})
const formRules = reactive({
  min: [{ required: true, message: '温度范围最小值不能为空', trigger: 'blur' }],
  max: [{ required: true, message: '温度范围最大值不能为空', trigger: 'blur' }],
  color: [{ required: true, message: '温度范围对应的颜色不能为空', trigger: 'blur' }],
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string) => {
  dialogVisible.value = true
  resetForm();
  dialogTitle.value = t('action.' + type)
  formType.value = type
  var data = await TemColorApi.getTemColorAll({});
  formData.value.colorArr = data;
  
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
    const data = formData.value.colorArr as unknown as TemColorVO
    await TemColorApi.createTemColor(data)
    message.success(t('common.createSuccess'))
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
      colorArr : [
        {
          id: undefined,
          min: undefined,
          max: undefined,
          color: undefined,
        },
      ]
  }
  formRef.value?.resetFields()
}

const addTemColor = () =>{

  formData.value.colorArr.push({
          id: undefined,
          min: undefined,
          max: undefined,
          color: undefined,
        });
}

const deleteTemColor = () =>{

  if(formData.value.colorArr.length > 0){
    formData.value.colorArr.pop();
  }

}

</script>