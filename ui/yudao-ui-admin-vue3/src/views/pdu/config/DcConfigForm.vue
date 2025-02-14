<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="200px"
    v-loading="formLoading"
  >
    <!-- 1. 数据接收与存储相关 -->
    <div style="border: 1px solid #ccc; padding: 5px; margin-bottom: 15px;">
      <b style="font-size: 20px;margin-bottom: 10px;">1. 数据接收与变化存储相关</b>
      <el-row :gutter="24" style="margin-top: 10px;">
        <el-col :span="24">
          <el-form-item label="数据接收端口" prop="receivePort">
            <el-input type="number" v-model="formData.receivePort" placeholder="请输入数据接收端口" />
          </el-form-item>
          <el-form-item label="总视在功率变化比（%）" prop="powLimitRate">
            <el-input type="number" v-model="formData.powLimitRate" placeholder="请输入总视在功率变化比" />
          </el-form-item>
          <el-form-item label="redis保存开关" prop="redisSwitch">
            <el-radio-group v-model="formData.redisSwitch">
              <el-radio :label="1">开启</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="rediskey过期时间（秒）" prop="redisExpire">
            <el-input type="number" v-model="formData.redisExpire" placeholder="请输入rediskey过期时间（分钟）" />
          </el-form-item>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="定时记录" prop="fixStore">
                <el-radio-group v-model="formData.fixStore">
                  <el-radio :label="1">开启</el-radio>
                  <el-radio :label="0">关闭</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="定时存储时间间隔" prop="fixStoreCronValue">
                每
                <el-input :disabled="formData.fixStore == 0"  size="small" style="width: 70px" type="number" :min="1" :max="formData.fixStoreCronType == 3 ? 24 : 60" v-model="formData.fixStoreCronValue" />
                <el-select :disabled="formData.fixStore == 0" size="small" v-model="formData.fixStoreCronType" placeholder="时间" clearable style="width: 80px">
                  <el-option
                    label="秒钟"
                    :value="1"
                  />
                  <el-option
                    label="分钟"
                    :value="2"
                  />
                  <el-option
                    label="小时"
                    :value="3"
                  />
                </el-select>
                执行一次
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
          <el-form-item label="变化记录" prop="changeStore">
            <el-radio-group v-model="formData.changeStore">
              <el-radio :label="1">开启</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="变化存储时间间隔" prop="changeStoreCronValue">
            每
            <el-input prop="changeStoreCronValue" :disabled="formData.changeStore == 0"  size="small" style="width: 70px" type="number" :min="1" :max="formData.changeStoreCronType == 3 ? 24 : 60" v-model="formData.changeStoreCronValue" />
            <el-select :disabled="formData.changeStore == 0" size="small" v-model="formData.changeStoreCronType" placeholder="时间" clearable style="width: 80px">
              <el-option
                label="秒钟"
                :value="1"
              />
              <el-option
                label="分钟"
                :value="2"
              />
              <el-option
                label="小时"
                :value="3"
              />
            </el-select>
            执行一次
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="电能记录" prop="eleStore">
            <el-radio-group v-model="formData.eleStore">
              <el-radio :label="1">开启</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电能存储时间间隔" prop="eleStoreCronValue">
            每
            <el-input prop="eleStoreCronValue" :disabled="formData.eleStore == 0" size="small" style="width: 70px" type="number" :min="1" :max="formData.eleStoreCronType == 3 ? 24 : 60" v-model="formData.eleStoreCronValue" />
            <el-select :disabled="formData.eleStore == 0" size="small" v-model="formData.eleStoreCronType" placeholder="时间" clearable style="width: 80px">
              <el-option
                  label="秒钟"
                  :value="1"
              />
              <el-option
                label="分钟"
                :value="2"
              />
              <el-option
                label="小时"
                :value="3"
              />
            </el-select>
            执行一次
          </el-form-item>
        </el-col>
      </el-row>
        </el-col>
      </el-row>
    </div>

    <!-- 2. 告警相关 -->
    <div style="border: 1px solid #ccc; padding: 10px; margin-bottom: 15px;">
      <b style="font-size: 20px;margin-bottom: 10px;">2. 告警相关</b>
      <el-row :gutter="24" style="margin-top: 10px;">
        <el-col :span="24">
          <el-form-item label="离线告警开关" prop="offLineAlarm">
            <el-radio-group v-model="formData.offLineAlarm">
              <el-radio :label="1">开启</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="离线告警时长（秒）" prop="offLineDuration">
            <el-input type="number" v-model="formData.offLineDuration" placeholder="请输入离线告警时长（分钟）" />
          </el-form-item>
          <el-form-item label="状态告警开关" prop="statusAlarm">
            <el-radio-group v-model="formData.statusAlarm">
              <el-radio :label="1">开启</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-row :gutter="24">
            <el-col :span="12">
          <el-form-item label="告警推送开关" prop="alarmPush">
            <el-radio-group v-model="formData.alarmPush" >
              <el-radio :label="1">开启</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="告警推送任务配置" prop="alarmPushCronValue">
              每
              <el-input prop="alarmPushCronValue" :disabled="formData.alarmPush == null ||formData.alarmPush == 0" size="small" style="width: 70px" type="number" :min="1" :max="formData.alarmPushCronType == 3 ? 24 : 60" v-model="formData.alarmPushCronValue" />
              <el-select :disabled="formData.alarmPush == null ||formData.alarmPush == 0" size="small" v-model="formData.alarmPushCronType" placeholder="时间" clearable style="width: 80px">
                <el-option
                    label="秒钟"
                    :value="1"
                />
                <el-option
                  label="分钟"
                  :value="2"
                />
                <el-option
                  label="小时"
                  :value="3"
                />
              </el-select>
              执行一次
          </el-form-item>
        </el-col>
      </el-row>
        </el-col>
      </el-row>
    </div>

    <!-- 3. 定时任务与推送相关 -->
    <!-- <div style="border: 1px solid #ccc; padding: 10px; margin-bottom: 15px;">
      <b style="font-size: 20px;margin-bottom: 10px;">3. 定时任务与推送相关</b>
      <el-row :gutter="24" style="margin-top: 10px;">
        <el-col :span="24">
          <el-row :gutter="24">
            <el-col :span="12">
          <el-form-item label="定时推送开关" prop="timingPush">
            <el-radio-group v-model="formData.timingPush" >
              <el-radio :label="1">开启</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="定时推送任务配置" prop="timingPushCronValue">
              每
              <el-input prop="timingPushCronValue" :disabled="formData.timingPush == null ||formData.timingPush == 0" size="small" style="width: 70px" type="number" :min="1" :max="formData.timingPushCronType == 3 ? 24 : 60" v-model="formData.timingPushCronValue" />
              <el-select :disabled="formData.timingPush == null ||formData.timingPush == 0" size="small" v-model="formData.timingPushCronType" placeholder="时间" clearable style="width: 80px">
                <el-option
                    label="秒钟"
                    :value="1"
                />
                <el-option
                  label="分钟"
                  :value="2"
                />
                <el-option
                  label="小时"
                  :value="3"
                />
              </el-select>
              执行一次
          </el-form-item>
        </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
          <el-form-item label="变化推送开关" prop="changePush">
            <el-radio-group v-model="formData.changePush" >
              <el-radio :label="1">开启</el-radio>
              <el-radio :label="0">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
          </el-col>
          <el-col :span="12">
          <el-form-item label="变化推送任务配置" prop="changePushCronValue">
            每
              <el-input prop="changePushCronValue" :disabled="formData.changePush == null || formData.changePush == 0" size="small" style="width: 70px" type="number" :min="1" :max="formData.changePushCronType == 3 ? 24 : 60" v-model="formData.changePushCronValue" />
              <el-select :disabled="formData.changePush == null || formData.changePush == 0" size="small" v-model="formData.changePushCronType" placeholder="时间" clearable style="width: 80px">
                <el-option
                    label="秒钟"
                    :value="1"
                />
                <el-option
                  label="分钟"
                  :value="2"
                />
                <el-option
                  label="小时"
                  :value="3"
                />
              </el-select>
              执行一次
          </el-form-item>
        </el-col>
          </el-row>
          <el-form-item label="配置推送的mq" prop="pushMqs">
            <el-checkbox-group :disabled="formData.timingPush == null || formData.alarmPush == null ||formData.changePush == null ||(formData.timingPush === 0 && formData.alarmPush === 0 && formData.changePush === 0)" v-model="formData.pushMqs">
              <el-checkbox label="1" value="1">kafka</el-checkbox>
              <el-checkbox label="2" value="2">RocketMQ</el-checkbox>
              <el-checkbox label="3" value="3">RabbitMQ</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-col>
      </el-row>
    </div> -->

    <el-row justify="end">
      <el-button @click="submitForm" type="primary" :disabled="formLoading">保 存</el-button>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { DcConfigApi, DcConfigVO } from '@/api/pdu/dcconfig'

/** pdu数据采集配置 表单 */
defineOptions({ name: 'DcConfigForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formId = ref() // 表单的id
const formData = ref({
  receivePort: undefined,
  fixStoreCron: undefined,
  changeStoreCron: undefined,
  eleStoreCron: undefined,
  powLimitRate: undefined,
  redisExpire: undefined,
  offLineDuration: undefined,
  offLineAlarm: undefined,
  statusAlarm: undefined,
  timingPushCron: undefined,
  changePushCron: undefined,
  alarmPushCron: undefined,
  timingPush: undefined,
  changePush: undefined,
  alarmPush: undefined,
  pushMqs: [] as any,
  fixStore: undefined,
  changeStore: undefined,
  eleStore: undefined,
  redisSwitch: undefined,
  fixStoreCronType : undefined,
  fixStoreCronValue : undefined,
  changeStoreCronType : undefined,
  changeStoreCronValue : undefined,
  eleStoreCronType : undefined,
  eleStoreCronValue : undefined,
  timingPushCronType : undefined,
  timingPushCronValue : undefined,
  changePushCronType : undefined,
  changePushCronValue : undefined,
  alarmPushCronType : undefined,
  alarmPushCronValue : undefined,
})

const checkCronValue = (rule: any, value: any, callback: any) => {

  if(value){
    if(formData.value[rule.field.replace("Value", "Type")]){
      let max = formData.value[rule.field.replace("Value", "Type")] == 3 ? 24 : 60;
      if(value > max){
        return callback(new Error('请不要超过最大值(小时是24，秒和分是60)'))
      } else if (value < 1){
        return callback(new Error('请输入大于1的数'))
      } else {
        return callback();
      }
    }else{
      return callback();
    }
  }else{
    return callback();
  }

}
const formRules = reactive({
  receivePort: [{ required: true, message: '数据接收端口不能为空', trigger: 'blur' }],
  powLimitRate: [{ required: true, message: '总视在功率变化比不能为空', trigger: 'blur' }],
  redisExpire: [{ required: true, message: 'rediskey过期时间（分钟）不能为空', trigger: 'blur' }],
  offLineAlarm: [{ required: true, message: '离线告警开关不能为空', trigger: 'blur' }],
  statusAlarm: [{ required: true, message: '状态告警开关不能为空', trigger: 'blur' }],
  timingPush: [{ required: true, message: '定时推送开关不能为空', trigger: 'blur' }],
  changePush: [{ required: true, message: '变化推送开关不能为空', trigger: 'blur' }],
  alarmPush: [{ required: true, message: '告警推送开关不能为空', trigger: 'blur' }],
  fixStore: [{ required: true, message: '定时任务开关 默认开 1   关0不能为空', trigger: 'blur' }],
  changeStore: [{ required: true, message: '变化存储开关 默认开1 关0不能为空', trigger: 'blur' }],
  eleStore: [{ required: true, message: '电能存储开关 默认开1  关0不能为空', trigger: 'blur' }],
  redisSwitch: [{ required: true, message: 'redis保存开关  开1 关0不能为空', trigger: 'blur' }],
  fixStoreCronValue:[{validator: checkCronValue, trigger: 'blur'}],
  changeStoreCronValue:[{validator: checkCronValue, trigger: 'blur'}],
  eleStoreCronValue:[{validator: checkCronValue, trigger: 'blur'}],
  timingPushCronValue:[{validator: checkCronValue, trigger: 'blur'}],
  changePushCronValue:[{validator: checkCronValue, trigger: 'blur'}],
  alarmPushCronValue:[{validator: checkCronValue, trigger: 'blur'}],
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
    formId.value = id;
    try {
      formData.value = await DcConfigApi.getDcConfig(id)
      if(!formData.value?.pushMqs){
        formData.value.pushMqs = [];
      }else{
        formData.value.pushMqs = formData.value.pushMqs?.split(",");
      }
      getTypeAndValue(formData.value.fixStoreCron,formData.value,"fixStoreCronType","fixStoreCronValue");
      getTypeAndValue(formData.value.changeStoreCron,formData.value,"changeStoreCronType","changeStoreCronValue");
      getTypeAndValue(formData.value.eleStoreCron,formData.value,"eleStoreCronType","eleStoreCronValue");
      getTypeAndValue(formData.value.timingPushCron,formData.value,"timingPushCronType","timingPushCronValue");
      getTypeAndValue(formData.value.changePushCron,formData.value,"changePushCronType","changePushCronValue");
      getTypeAndValue(formData.value.alarmPushCron,formData.value,"alarmPushCronType","alarmPushCronValue");
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

function getTypeAndValue(cronExpression, obj, typeKey, valueKey) {
  if (cronExpression) {
    const lastZeroIndex = cronExpression.lastIndexOf("/");
    if (lastZeroIndex === 1) {
      obj[typeKey] = 1;
    } else if (lastZeroIndex === 3) {
      obj[typeKey] = 2;
    } else if (lastZeroIndex === 5) {
      obj[typeKey] = 3;
    }
    obj[valueKey] = cronExpression[lastZeroIndex + 1];
    if(cronExpression[lastZeroIndex + 2] != ' '){
      obj[valueKey] += cronExpression[lastZeroIndex + 2];
    }
  }
}

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    if(formData.value.fixStoreCronType && formData.value.fixStoreCronValue){
      setCronExpression(formData.value.fixStoreCronType,formData.value.fixStoreCronValue,formData.value,"fixStoreCron")
    }
    if(formData.value.changeStoreCronType && formData.value.changeStoreCronValue){
      setCronExpression(formData.value.changeStoreCronType,formData.value.changeStoreCronValue,formData.value,"changeStoreCron")
    }
    if(formData.value.eleStoreCronType && formData.value.eleStoreCronValue){
      setCronExpression(formData.value.eleStoreCronType,formData.value.eleStoreCronValue,formData.value,"eleStoreCron")
    }
    if(formData.value.timingPush == 1 && formData.value.timingPushCronType && formData.value.timingPushCronValue){
      setCronExpression(formData.value.timingPushCronType,formData.value.timingPushCronValue,formData.value,"timingPushCron")
    }
    if(formData.value.alarmPush == 1 && formData.value.alarmPushCronType && formData.value.alarmPushCronValue){
      setCronExpression(formData.value.alarmPushCronType,formData.value.alarmPushCronValue,formData.value,"alarmPushCron")
    }
    if(formData.value.changePush == 1 && formData.value.changePushCronType && formData.value.changePushCronValue){
      setCronExpression(formData.value.changePushCronType,formData.value.changePushCronValue,formData.value,"changePushCron")
    }
    const data = formData.value as unknown as DcConfigVO
    var temp = "";
    formData.value.pushMqs?.forEach((element,index) => {
      if(element){
        if(index != formData.value.pushMqs?.length - 1){
          temp = temp + element + ",";
        } else {
          temp = temp + element;
        }
      }
    });
    data.pushMqs = temp;
    if (formType.value === 'create') {
      await DcConfigApi.createDcConfig(data)
      message.success(t('common.createSuccess'))
    } else {
      await DcConfigApi.updateDcConfig(data)
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



const setCronExpression = (type, value, obj, valueKey) => {
  let cronExpression;
  if (type === 1) {
    cronExpression = `0/${value} * * * * ?`;
  } else if (type === 2) {
    cronExpression = `0 0/${value} * * * ?`;
  } else if (type === 3) {
    cronExpression = `0 0 0/${value} * * ?`;
  }

  if (cronExpression) {
    Object.defineProperty(obj, valueKey, { value: cronExpression });

  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    receivePort: undefined,
    fixStoreCron: undefined,
    changeStoreCron: undefined,
    eleStoreCron: undefined,
    powLimitRate: undefined,
    redisExpire: undefined,
    offLineDuration: undefined,
    offLineAlarm: undefined,
    statusAlarm: undefined,
    timingPushCron: undefined,
    changePushCron: undefined,
    alarmPushCron: undefined,
    timingPush: undefined,
    changePush: undefined,
    alarmPush: undefined,
    fixStore: undefined,
    changeStore: undefined,
    eleStore: undefined,
    redisSwitch: undefined,
    pushMqs: []
  }
  formRef.value?.resetFields()
}
</script>