<template>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="200px"
      v-loading="formLoading"
    >
      <el-row :gutter="24">
        <el-col :span="12" >
          <el-form-item label="计费方式" prop="billMode">      
            <el-radio-group v-model="formData.billMode">
              <el-radio :label="1">固定计费</el-radio>
              <el-radio :label="2">分段计费</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="按天统计历史数据任务" prop="dayCron">

            每天的
            <el-form-item prop="dayHour">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="24" v-model="formData.dayHour" />
            </el-form-item>
            时
            <el-form-item prop="dayMin">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.dayMin" /> 
            </el-form-item>
            分
            <el-form-item  prop="daySec">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.daySec" /> 
            </el-form-item>
            秒执行一次
          </el-form-item>
          <el-form-item label="按小时统计历史数据任务" prop="hourCron">

            每时的
            <el-form-item prop="hourMin">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.hourMin" /> 
            </el-form-item>
            分
            <el-form-item prop="hourSec">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.hourSec" /> 
            </el-form-item>
            秒执行一次
          </el-form-item>
        </el-col>
        <el-col :span="12" >
          <el-form-item label="电量按天统计任务" prop="eqDayCron">

            每天的
            <el-form-item prop="eqDayHour">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="24" v-model="formData.eqDayHour" />
            </el-form-item>
            时
            <el-form-item prop="eqDayMin">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.eqDayMin" /> 
            </el-form-item>            
            分
            <el-form-item prop="eqDaySec">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.eqDaySec" /> 
            </el-form-item>        
            秒执行一次
          </el-form-item>
          <el-form-item label="电量按周执行任务" prop="eqWeekCron">

            每周星期
            <el-form-item prop="eqWeekDay">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="7" v-model="formData.eqWeekDay" />
            </el-form-item>            
            的
            <el-form-item prop="eqWeekHour">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="24" v-model="formData.eqWeekHour" />
            </el-form-item>            
            时
            <el-form-item prop="eqWeekMin">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.eqWeekMin" /> 
            </el-form-item>            
            分
            <el-form-item prop="eqWeekSec">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.eqWeekSec" /> 
            </el-form-item>          
            秒执行一次
          </el-form-item>
          <el-form-item label="按月统计电量任务" prop="eqMonthCron">

            每月的
            <el-form-item prop="eqMonthDay">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="31" v-model="formData.eqMonthDay" />
            </el-form-item>            
            日
            <el-form-item prop="eqMonthHour">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="24" v-model="formData.eqMonthHour" />
            </el-form-item>            
            时
            <el-form-item prop="eqMonthMin">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.eqMonthMin" /> 
            </el-form-item>
            分
            <el-form-item prop="eqMonthSec">
              <el-input size="small" style="width: 70px" type="number" :min="1" :max="60" v-model="formData.eqMonthSec" /> 
            </el-form-item>
            秒执行一次
          </el-form-item>

        </el-col>
    
      </el-row>
      <el-row justify="end">
        <el-button @click="submitForm" type="primary" :disabled="formLoading">保 存</el-button>
      </el-row>
    </el-form>
</template>
<script setup lang="ts">
import { StatisConfigApi, StatisConfigVO } from '@/api/bus/busstatisconfig'

/** pdu计算服务配置 表单 */
defineOptions({ name: 'StatisConfigForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const checkHour = (rule: any, value: any, callback: any) => {

if(value){
  if(value > 23){
    return callback(new Error('不超过23'));
  } else if (value < 0){
    return callback(new Error('应大于0'));
  } else {
    return callback();
  }
}else{
  return callback();
}

}

const checkMinAndSec = (rule: any, value: any, callback: any) => {

if(value){
  if(value > 59){
    return callback(new Error('不超过59'));
  } else if (value < 0){
    return callback(new Error('应大于0'));
  } else {
    return callback();
  }
}else{
  return callback();
}

}

const checkDay = (rule: any, value: any, callback: any) => {

if(value){
  if(value > 31){
    return callback(new Error('不超过31'));
  } else if (value < 0){
    return callback(new Error('应大于0'));
  } else {
    return callback();
  }
}else{
  return callback();
}

}

const checkWeek = (rule: any, value: any, callback: any) => {
if(value){
  if(value > 7){
    return callback(new Error('不超过7'));
  } else if (value < 0){
    return callback(new Error('应大于0'));
  } else {
    return callback();
  }
}else{
  return callback();
}
}

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formId = ref()
const formData = ref({
  id: undefined,
  billMode: undefined,
  dayCron: undefined,
  hourCron: undefined,
  eqDayCron: undefined,
  eqWeekCron: undefined,
  eqMonthCron: undefined,
  dayHour : null,
  dayMin : null,
  daySec : null,
  eqDayHour : null,
  eqDayMin : null,
  eqDaySec : null,
  hourMin: null,
  hourSec : null,
  eqMonthDay : null,
  eqMonthHour : null,
  eqMonthMin : null,
  eqMonthSec : null,
  eqWeekDay : null as any,
  eqWeekHour : null,
  eqWeekMin : null,
  eqWeekSec : null,
}) as any
const formRules = reactive({
  billMode: [{ required: true, message: '计费方式 1固定计费 2分段计费不能为空', trigger: 'blur' }],
  dayHour:[{validator: checkHour, trigger: 'blur'}],
  dayMin : [{validator: checkMinAndSec, trigger: 'blur'}],
  daySec : [{validator: checkMinAndSec, trigger: 'blur'}],
  eqDayHour : [{validator: checkHour, trigger: 'blur'}],
  eqDayMin : [{validator: checkMinAndSec, trigger: 'blur'}],
  eqDaySec : [{validator: checkMinAndSec, trigger: 'blur'}],
  hourMin: [{validator: checkMinAndSec, trigger: 'blur'}],
  hourSec : [{validator: checkMinAndSec, trigger: 'blur'}],
  eqMonthDay : [{validator: checkDay, trigger: 'blur'}],
  eqMonthHour : [{validator: checkHour, trigger: 'blur'}],
  eqMonthMin : [{validator: checkMinAndSec, trigger: 'blur'}],
  eqMonthSec : [{validator: checkMinAndSec, trigger: 'blur'}],
  eqWeekDay : [{validator: checkWeek, trigger: 'blur'}],
  eqWeekHour : [{validator: checkHour, trigger: 'blur'}],
  eqWeekMin : [{validator: checkMinAndSec, trigger: 'blur'}],
  eqWeekSec : [{validator: checkMinAndSec, trigger: 'blur'}],
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
    formLoading.value = true;
    formId.value = id;
    try {
      formData.value = await StatisConfigApi.getStatisConfig(id)
      if(formData.value.dayCron){
        var temp = formData.value.dayCron?.split(" ");
        formData.value.dayHour = temp[2];
        formData.value.dayMin = temp[1];
        formData.value.daySec = temp[0];
      }
      if(formData.value.eqDayCron){
        var temp = formData.value.eqDayCron?.split(" ");
        formData.value.eqDayHour = temp[2];
        formData.value.eqDayMin = temp[1];
        formData.value.eqDaySec = temp[0];
      }
      if(formData.value.hourCron){
        var temp = formData.value.hourCron?.split(" ");
        formData.value.hourMin = temp[1];
        formData.value.hourSec = temp[0];
      }
      if(formData.value.eqMonthCron){
        var temp = formData.value.eqMonthCron?.split(" ");
        formData.value.eqMonthDay = temp[3];
        formData.value.eqMonthHour = temp[2];
        formData.value.eqMonthMin = temp[1];
        formData.value.eqMonthSec = temp[0];
      }
      if(formData.value.eqWeekCron){
        var temp = formData.value.eqWeekCron?.split(" ");
        if(temp[5] == 'MON'){
          formData.value.eqWeekDay = 1;
        } else if(temp[5] == 'TUE'){
          formData.value.eqWeekDay = 2;
        } else if(temp[5] == 'WED'){
          formData.value.eqWeekDay = 3;
        } else if(temp[5] == 'THU'){
          formData.value.eqWeekDay = 4;
        } else if(temp[5] == 'FRI'){
          formData.value.eqWeekDay = 5;
        } else if(temp[5] == 'SAT'){
          formData.value.eqWeekDay = 6;
        } else if(temp[5] == 'SUN'){
          formData.value.eqWeekDay = 7;
        }
        formData.value.eqWeekHour = temp[2];
        formData.value.eqWeekMin = temp[1];
        formData.value.eqWeekSec = temp[0];

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
    if(formData.value.eqDayHour != null && formData.value.eqDayMin != null && formData.value.eqDaySec != null ){
      formData.value.eqDayCron = `${formData.value.eqDaySec} ${formData.value.eqDayMin} ${formData.value.eqDayHour} * * ?`;
    }
    if(formData.value.dayHour != null && formData.value.dayMin != null && formData.value.daySec != null ){
      formData.value.dayCron = `${formData.value.daySec} ${formData.value.dayMin} ${formData.value.dayHour} * * ?`;
    }
    if(formData.value.hourMin != null && formData.value.hourSec != null  ){
      formData.value.hourCron = `${formData.value.hourSec} ${formData.value.hourMin} * * * ?`;
    }
    if(formData.value.eqMonthDay != null && formData.value.eqMonthHour != null && formData.value.eqMonthMin != null && formData.value.eqMonthSec != null){
      formData.value.eqMonthCron = `${formData.value.eqMonthSec} ${formData.value.eqMonthMin} ${formData.value.eqMonthHour} ${formData.value.eqMonthDay} * ?`;
    }
    if(formData.value.eqWeekDay != null && formData.value.eqWeekHour != null && formData.value.eqWeekMin != null && formData.value.eqWeekSec != null){
      var temp = null as any;
      if(formData.value.eqWeekDay == 1){ temp = 'MON' } 
      else if (formData.value.eqWeekDay == 2){temp = 'TUE'}
      else if (formData.value.eqWeekDay == 3){temp = 'WED'}
      else if (formData.value.eqWeekDay == 4){temp = 'THU'}
      else if (formData.value.eqWeekDay == 5){temp = 'FRI'}
      else if (formData.value.eqWeekDay == 6){temp = 'SAT'}
      else if (formData.value.eqWeekDay == 7){temp = 'SUN'}
      formData.value.eqWeekCron = `${formData.value.eqWeekSec} ${formData.value.eqWeekMin} ${formData.value.eqWeekHour} ? * ${temp}`;
    }
    const data = formData.value as unknown as StatisConfigVO
    if (formType.value === 'create') {
      await StatisConfigApi.createStatisConfig(data)
      message.success(t('common.createSuccess'))
    } else {
      await StatisConfigApi.updateStatisConfig(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
    if(formId.value != null || formId.value != undefined){
      open(formType.value,formId.value);
    }
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    billMode: undefined,
    dayCron: undefined,
    hourCron: undefined,
    eqDayCron: undefined,
    eqWeekCron: undefined,
    eqMonthCron: undefined,
  }
  formRef.value?.resetFields()
}
</script>