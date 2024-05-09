<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="计费方式 1固定计费 2分段计费" prop="billMode">
        <el-input v-model="formData.billMode" placeholder="请输入计费方式 1固定计费 2分段计费" />
      </el-form-item>
      <el-form-item label="按天统计历史数据任务" prop="dayCron">
        <el-input v-model="formData.dayCron" placeholder="请输入按天统计历史数据任务" />
      </el-form-item>
      <el-form-item label="按小时统计历史数据任务" prop="hourCron">
        <el-input v-model="formData.hourCron" placeholder="请输入按小时统计历史数据任务" />
      </el-form-item>
      <el-form-item label="电量按天统计任务" prop="eqDayCron">
        <el-input v-model="formData.eqDayCron" placeholder="请输入电量按天统计任务" />
      </el-form-item>
      <el-form-item label="电量按周执行任务" prop="eqWeekCron">
        <el-input v-model="formData.eqWeekCron" placeholder="请输入电量按周执行任务" />
      </el-form-item>
      <el-form-item label="按月统计电量任务" prop="eqMonthCron">
        <el-input v-model="formData.eqMonthCron" placeholder="请输入按月统计电量任务" />
      </el-form-item>
      <el-form-item label="负载限制" prop="loadLimit">
        <el-input v-model="formData.loadLimit" placeholder="请输入负载限制" />
      </el-form-item>
      <el-form-item label="状态告警开关 0关 1开" prop="statusAlarm">
        <el-input v-model="formData.statusAlarm" placeholder="请输入状态告警开关 0关 1开" />
      </el-form-item>
      <el-form-item label="存储任务" prop="storeCron">
        <el-input v-model="formData.storeCron" placeholder="请输入存储任务" />
      </el-form-item>
      <el-form-item label="告警任务" prop="alarmCron">
        <el-input v-model="formData.alarmCron" placeholder="请输入告警任务" />
      </el-form-item>
      <el-form-item label="告警推送开关 0 关 1开" prop="alarmPush">
        <el-input v-model="formData.alarmPush" placeholder="请输入告警推送开关 0 关 1开" />
      </el-form-item>
      <el-form-item label="告警推送任务" prop="alarmPushCron">
        <el-input v-model="formData.alarmPushCron" placeholder="请输入告警推送任务" />
      </el-form-item>
      <el-form-item label="推送mq配置" prop="pushMqs">
        <el-input v-model="formData.pushMqs" placeholder="请输入推送mq配置" />
      </el-form-item>
      <el-form-item label="redis key过期时间" prop="redisExpire">
        <el-input v-model="formData.redisExpire" placeholder="请输入redis key过期时间" />
      </el-form-item>
      <el-form-item label="电能存储任务" prop="eleStoreCron">
        <el-input v-model="formData.eleStoreCron" placeholder="请输入电能存储任务" />
      </el-form-item>
      <el-form-item label="定时推送任务" prop="timingPushCron">
        <el-input v-model="formData.timingPushCron" placeholder="请输入定时推送任务" />
      </el-form-item>
      <el-form-item label="定时推送开关 1开启 0关闭" prop="timingPush">
        <el-input v-model="formData.timingPush" placeholder="请输入定时推送开关 1开启 0关闭" />
      </el-form-item>
      <el-form-item label="redis缓存任务" prop="redisCron">
        <el-input v-model="formData.redisCron" placeholder="请输入redis缓存任务" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { StatisConfigApi, StatisConfigVO } from '@/api/cabinet/statisconfig'

/** 机柜计算服务配置 表单 */
defineOptions({ name: 'StatisConfigForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(true) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  billMode: undefined,
  dayCron: undefined,
  hourCron: undefined,
  eqDayCron: undefined,
  eqWeekCron: undefined,
  eqMonthCron: undefined,
  loadLimit: undefined,
  statusAlarm: undefined,
  storeCron: undefined,
  alarmCron: undefined,
  alarmPush: undefined,
  alarmPushCron: undefined,
  pushMqs: undefined,
  redisExpire: undefined,
  eleStoreCron: undefined,
  timingPushCron: undefined,
  timingPush: undefined,
  redisCron: undefined,
})
const formRules = reactive({
  billMode: [{ required: true, message: '计费方式 1固定计费 2分段计费不能为空', trigger: 'blur' }],
  loadLimit: [{ required: true, message: '负载限制不能为空', trigger: 'blur' }],
  statusAlarm: [{ required: true, message: '状态告警开关 0关 1开不能为空', trigger: 'blur' }],
  alarmPush: [{ required: true, message: '告警推送开关 0 关 1开不能为空', trigger: 'blur' }],
  timingPush: [{ required: true, message: '定时推送开关 1开启 0关闭不能为空', trigger: 'blur' }],
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
      formData.value = await StatisConfigApi.getStatisConfig(id)
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
    loadLimit: undefined,
    statusAlarm: undefined,
    storeCron: undefined,
    alarmCron: undefined,
    alarmPush: undefined,
    alarmPushCron: undefined,
    pushMqs: undefined,
    redisExpire: undefined,
    eleStoreCron: undefined,
    timingPushCron: undefined,
    timingPush: undefined,
    redisCron: undefined,
  }
  formRef.value?.resetFields()
}
</script>