<template>
  <ContentWrap>
    <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
      <el-tab-pane label="消息队列" name="first">
          <el-row :gutter="24">
            <el-col :span="8">
              <ContentWrap>
                <el-row justify="center">
                  <el-form
                    ref="kafkaFormRef"
                    :model="kafkaFormData"
                    :rules="kafkaFormRules"
                    label-width="100px"
                    v-loading="kafkaFormLoading"
                  >
                    <el-form-item label="消息队列" prop="mq">
                      <el-input v-model="kafkaFormData.mq" :disabled="true" />
                    </el-form-item>
                    <el-form-item label="服务器地址" prop="ipAndPorts">
                      <el-input v-model="kafkaFormData.ipAndPorts" placeholder="请输入服务器地址" />
                    </el-form-item>
                    <el-form-item label="用户名" prop="userName">
                      <el-input v-model="kafkaFormData.userName" placeholder="请输入用户名" />
                    </el-form-item>
                    <el-form-item label="密码" prop="passWord">
                      <el-input v-model="kafkaFormData.passWord" placeholder="请输入密码" />
                    </el-form-item>
                    <el-form-item label="主题/分组" prop="topic">
                      <el-input v-model="kafkaFormData.topic" placeholder="请输入主题/分组" />
                    </el-form-item>
                  </el-form>
                </el-row>
                <el-row justify="end">
                  <el-button @click="submitKafkaForm" type="primary" :disabled="kafkaFormLoading">保 存</el-button>
                </el-row>
              </ContentWrap>
            </el-col>
            <el-col :span="8">
              <ContentWrap>
                <el-row justify="center">
                  <el-form
                    ref="rabbitmqFormRef"
                    :model="rabbitmqFormData"
                    :rules="rabbitmqFormRules"
                    label-width="100px"
                    v-loading="rabbitmqFormLoading"
                  >
                    <el-form-item label="消息队列" prop="mq">
                      <el-input v-model="rabbitmqFormData.mq" :disabled="true" />
                    </el-form-item>
                    <el-form-item label="服务器地址" prop="ipAndPorts">
                      <el-input v-model="rabbitmqFormData.ipAndPorts" placeholder="请输入服务器地址" />
                    </el-form-item>
                    <el-form-item label="用户名" prop="userName">
                      <el-input v-model="rabbitmqFormData.userName" placeholder="请输入用户名" />
                    </el-form-item>
                    <el-form-item label="密码" prop="passWord">
                      <el-input v-model="rabbitmqFormData.passWord" placeholder="请输入密码" />
                    </el-form-item>
                    <el-form-item label="主题/分组" prop="topic">
                      <el-input v-model="rabbitmqFormData.topic" placeholder="请输入主题/分组" />
                    </el-form-item>
                    
                  </el-form>
                </el-row>
                <el-row justify="end">
                  <el-button @click="submitRabbitMqForm" type="primary" :disabled="rabbitmqFormLoading">保 存</el-button>
                </el-row>
              </ContentWrap>
            </el-col>
            <el-col :span="8">
              <ContentWrap>
                <el-row justify="center">
                  <el-form
                    ref="rocketmqFormRef"
                    :model="rocketmqFormData"
                    :rules="rocketmqFormRules"
                    label-width="100px"
                    v-loading="rocketmqFormLoading"
                  >
                    <el-form-item label="消息队列" prop="mq">
                      <el-input v-model="rocketmqFormData.mq" :disabled="true" />
                    </el-form-item>
                    <el-form-item label="服务器地址" prop="ipAndPorts">
                      <el-input v-model="rocketmqFormData.ipAndPorts" placeholder="请输入服务器地址" />
                    </el-form-item>
                    <el-form-item label="用户名" prop="userName">
                      <el-input v-model="rocketmqFormData.userName" placeholder="请输入用户名" />
                    </el-form-item>
                    <el-form-item label="密码" prop="passWord">
                      <el-input v-model="rocketmqFormData.passWord" placeholder="请输入密码" />
                    </el-form-item>
                    <el-form-item label="主题/分组" prop="topic">
                      <el-input v-model="rocketmqFormData.topic" placeholder="请输入主题/分组" />
                    </el-form-item>
                  </el-form>
                </el-row>
                <el-row justify="end">
                  <el-button @click="submitRocketMqForm" type="primary" :disabled="rocketmqFormLoading">保 存</el-button>
                </el-row>
              </ContentWrap>
            </el-col>
          </el-row>
      </el-tab-pane>
      <el-tab-pane label="数据采集" name="second">
        <DcConfigForm ref="dcFormRef" @success="getDCList" />
      </el-tab-pane>
      <el-tab-pane label="电费计量方式" name="third">
        <ContentWrap>
          <!-- 搜索工作栏 -->
          <el-form
            class="-mb-15px"
            :model="billQueryParams"
            ref="billQueryFormRef"
            :inline="true"
            label-width="68px"
          >
            <el-form-item label="电费单价" prop="bill" label-width="70px">
              <el-input
                v-model="billQueryParams.bill"
                placeholder="请输入电费单价"
                clearable
                @keyup.enter="handleBillQuery"
                class="!w-120px"
              />
            </el-form-item>
            <el-form-item label="计费方式" prop="billMode" label-width="70px">
              <el-button 
              @click="billQueryParams.billMode = 1;switchValue = 1;" 
              :type="switchValue == 1 ? 'primary' : ''"
            >
              固定计费
            </el-button>
            <el-button 
              @click="billQueryParams.billMode = 2;switchValue = 2;" 
              :type="switchValue == 2 ? 'primary' : ''"
            >
              分段计费
            </el-button>
            </el-form-item>
            <el-form-item label="计费时间段" prop="billPeriod" label-width="83px">
              <el-input
                v-model="billQueryParams.billPeriod"
                placeholder="请输入计费时间段"
                clearable
                @keyup.enter="handleBillQuery"
                class="!w-180px"
              />
            </el-form-item>
            <el-form-item>
              <el-button @click="handleBillQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
              <el-button @click="resetBillQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
              <el-button
                type="primary"
                plain
                @click="openBillForm('create')"
              >
                <Icon icon="ep:plus" class="mr-5px" /> 新增
              </el-button>
              <el-button
                type="success"
                plain
                @click="handleBillExport"
                :loading="billExportLoading"
              >
                <Icon icon="ep:download" class="mr-5px" /> 导出
              </el-button>
            </el-form-item>
          </el-form>
        </ContentWrap>

        <!-- 列表 -->
        <ContentWrap>
          <el-table v-loading="billLoading" :data="billList" :stripe="true" :show-overflow-tooltip="true">
            <el-table-column label="主键id" align="center" prop="id" />
            <el-table-column label="电费单价" align="center" prop="bill" />
            <el-table-column label="计费方式" align="center" prop="billMode" >
              <template #default="scope">
                  <el-tag  v-if="scope.row.billMode == 1">固定计费</el-tag>
                  <el-tag type="success" v-if="scope.row.billMode == 2">分段计费</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="计费时间段" align="center" prop="billPeriod" />
            <el-table-column label="操作" align="center">
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  @click="openBillForm('update', scope.row.id)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  @click="handleBillDelete(scope.row.id)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <!-- 分页 -->
          <Pagination
            :total="billTotal"
            v-model:page="billQueryParams.pageNo"
            v-model:limit="billQueryParams.pageSize"
            @pagination="getBillList"
          />
        </ContentWrap>

        <!-- 表单弹窗：添加/修改 -->
        <EqBillConfigForm ref="billFormRef" @success="getBillList" />
      </el-tab-pane>
      <el-tab-pane label="计算服务" name="fourth">
        <StatisConfigForm ref="staticFormRef" @success="getStaticList" />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

</template>
<script lang="ts" setup>
import { ref } from 'vue'
import type { TabsPaneContext } from 'element-plus'
import download from '@/utils/download'
import { MqConfigApi, MqConfigVO } from '@/api/pdu/mqconfig'
import MqConfigForm from './MqConfigForm.vue'
import { DcConfigApi, DcConfigVO } from '@/api/pdu/dcconfig'
import DcConfigForm from './DcConfigForm.vue'
import { EqBillConfigApi, EqBillConfigVO } from '@/api/pdu/eqbillconfig'
import EqBillConfigForm from './EqBillConfigForm.vue'
import { StatisConfigApi, StatisConfigVO } from '@/api/pdu/statisconfig'
import StatisConfigForm from './StatisConfigForm.vue'

/** 消息队列系统配置 列表 */
defineOptions({ name: 'MqConfig' })

const switchValue = ref(0);
const mqMessage = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const mqQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  ipAndPorts: undefined,
  userName: undefined,
  passWord: undefined,
  topic: undefined,
  mq: undefined,
  createTime: [],
})

const kafkaFormLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const kafkaFormData = ref({
  id: undefined,
  ipAndPorts: undefined,
  userName: undefined,
  passWord: undefined,
  topic: undefined,
  mq: 'KAFKA',
})
const kafkaFormRules = reactive({
  ipAndPorts: [{ required: true, message: '服务器地址不能为空', trigger: 'blur' }]
})
const kafkaFormRef = ref() // 表单 Ref

const submitKafkaForm = async () => {
  // 校验表单
  await kafkaFormRef.value.validate()
  // 提交请求
  kafkaFormLoading.value = true
  try {
    const data = kafkaFormData.value as unknown as MqConfigVO
    if (!kafkaFormData.value.id) {
      await MqConfigApi.createMqConfig(data)
      mqMessage.success(t('common.createSuccess'))
    } else {
      await MqConfigApi.updateMqConfig(data)
      mqMessage.success(t('common.updateSuccess'))
    }
    mqQueryParams.mq = "KAFKA";
    const kafkaData = await MqConfigApi.getMqConfigPage(mqQueryParams)
    if(kafkaData.list?.length > 0){
      kafkaFormData.value =  kafkaData.list[0];
    }
  } finally {
    kafkaFormLoading.value = false
  }
}

const rabbitmqFormLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const rabbitmqFormData = ref({
  id: undefined,
  ipAndPorts: undefined,
  userName: undefined,
  passWord: undefined,
  topic: undefined,
  mq: 'RABBITMQ',
})
const rabbitmqFormRules = reactive({
  ipAndPorts: [{ required: true, message: '服务器地址不能为空', trigger: 'blur' }]
})
const rabbitmqFormRef = ref() // 表单 Ref

const submitRabbitMqForm = async () => {
  // 校验表单
  await rabbitmqFormRef.value.validate()
  // 提交请求
  rabbitmqFormLoading.value = true
  try {
    const data = rabbitmqFormData.value as unknown as MqConfigVO
    if (!rabbitmqFormData.value.id) {
      await MqConfigApi.createMqConfig(data)
      mqMessage.success(t('common.createSuccess'))
    } else {
      await MqConfigApi.updateMqConfig(data)
      mqMessage.success(t('common.updateSuccess'))
    }
    mqQueryParams.mq = "RABBITMQ";
    const rabbitmqData = await MqConfigApi.getMqConfigPage(mqQueryParams)
    if(rabbitmqData.list?.length > 0){
      rabbitmqFormData.value =  rabbitmqData.list[0];
    }
  } finally {
    rabbitmqFormLoading.value = false
  }
}

const rocketmqFormLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const rocketmqFormData = ref({
  id: undefined,
  ipAndPorts: undefined,
  userName: undefined,
  passWord: undefined,
  topic: undefined,
  mq: 'ROCKETMQ',
})
const rocketmqFormRules = reactive({
  ipAndPorts: [{ required: true, message: '服务器地址不能为空', trigger: 'blur' }]
})
const rocketmqFormRef = ref() // 表单 Ref

const submitRocketMqForm = async () => {
  // 校验表单
  await rocketmqFormRef.value.validate()
  // 提交请求
  rocketmqFormLoading.value = true
  try {
    const data = rocketmqFormData.value as unknown as MqConfigVO
    if (!rocketmqFormData.value.id) {
      await MqConfigApi.createMqConfig(data)
      mqMessage.success(t('common.createSuccess'))
    } else {
      await MqConfigApi.updateMqConfig(data)
      mqMessage.success(t('common.updateSuccess'))
    }
    mqQueryParams.mq = "ROCKETMQ";
    const rocketmqData = await MqConfigApi.getMqConfigPage(mqQueryParams)
    if(rocketmqData.list?.length > 0){
      rocketmqFormData.value =  rocketmqData.list[0];
    }
  } finally {
    rocketmqFormLoading.value = false
  }
}

const dcList = ref<DcConfigVO[]>([]) as any // 列表的数据
const dcListTotal = ref(0) // 列表的总页数
const dcQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  alarmPush: undefined,
})
const dcQueryFormRef = ref() // 搜索的表单

const billLoading = ref(true) // 列表的加载中
const billList = ref<EqBillConfigVO[]>([]) // 列表的数据
const billTotal = ref(0) // 列表的总页数
const billQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  bill: undefined,
  billMode: undefined,
  billPeriod: '',
  createTime: [],
})
const billQueryFormRef = ref() // 搜索的表单
const billExportLoading = ref(false) // 导出的加载中


const staticList = ref<StatisConfigVO[]>([]) as any// 列表的数据
const staticTotal = ref(0) // 列表的总页数
const staticQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  id: undefined,
  billMode: undefined,
  dayCron: undefined,
  hourCron: undefined,
  eqDayCron: undefined,
  eqWeekCron: undefined,
  eqMonthCron: undefined,
})
const staticQueryFormRef = ref() // 搜索的表单




/** 查询列表 */
const getMQList = async () => {
  try {
    mqQueryParams.mq = "KAFKA";
    const kafkaData = await MqConfigApi.getMqConfigPage(mqQueryParams)
    if(kafkaData.list?.length > 0){
      kafkaFormData.value =  kafkaData.list[0];
    }
    mqQueryParams.mq = "RABBITMQ";
    const rabbitmqData = await MqConfigApi.getMqConfigPage(mqQueryParams)
    if(rabbitmqData.list?.length > 0){
      rabbitmqFormData.value =  rabbitmqData.list[0];
    }
    mqQueryParams.mq = "ROCKETMQ";
    const rocketmqData = await MqConfigApi.getMqConfigPage(mqQueryParams)
    if(rocketmqData.list?.length > 0){
      rocketmqFormData.value =  rocketmqData.list[0];
    }

  } finally {
  }
}
const getDCList = async () => {
  try {
    const data = await DcConfigApi.getDcConfigPage(dcQueryParams)
    dcList.value = data.list
    dcListTotal.value = data.total
  } finally {
  }
}

const getBillList = async () => {
  billLoading.value = true
  try {
    const data = await EqBillConfigApi.getEqBillConfigPage(billQueryParams)
    billList.value = data.list
    billTotal.value = data.total
  } finally {
    billLoading.value = false
  }
}

const getStaticList = async () => {
  try {
    const data = await StatisConfigApi.getStatisConfigPage(staticQueryParams)
    staticList.value = data.list
    staticTotal.value = data.total
  }finally {
  }
}

/** 搜索按钮操作 */
const handleMQQuery = () => {
  mqQueryParams.pageNo = 1
  getMQList()
}

const handleBillQuery = () => {
  billQueryParams.pageNo = 1
  getBillList()
}



const resetBillQuery = () => {
  billQueryFormRef.value.resetFields()
  switchValue.value = 0
  handleBillQuery()
}



const dcFormRef = ref()
const openDCForm = (type: string, id?: number) => {
  dcFormRef.value.open(type, id)
}

const billFormRef = ref()
const openBillForm = (type: string, id?: number) => {
  billFormRef.value.open(type, id)
}

const staticFormRef = ref()
const openStaticForm = (type: string, id?: number) => {
  staticFormRef.value.open(type, id)
}



const handleBillDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await mqMessage.delConfirm()
    // 发起删除
    await EqBillConfigApi.deleteEqBillConfig(id)
    mqMessage.success(t('common.delSuccess'))
    // 刷新列表
    await getBillList()
  } catch {}
}



const handleBillExport = async () => {
  try {
    // 导出的二次确认
    await mqMessage.exportConfirm()
    // 发起导出
    billExportLoading.value = true
    const data = await EqBillConfigApi.exportEqBillConfig(billQueryParams)
    download.excel(data, 'pdu电量计费方式配置.xls')
  } catch {
  } finally {
    billExportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getMQList();
  getDCList();
  getBillList();
  getStaticList();
})

const activeName = ref('first')

const handleClick = (tab: TabsPaneContext, event: Event) => {
  if(tab.props.label === "数据采集"){
    if(dcList.value && dcList.value.length > 0 && dcList.value[0].id){
      openDCForm('update', dcList.value[0].id);
    }else{
      openDCForm('create');
    }
    
  }
  if(tab.props.label === "消息队列"){
    getMQList();
  }
  if(tab.props.label === "电费计量方式"){
    getBillList();
  }
  if(tab.props.label === "计算服务"){
    if(staticList.value && staticList.value?.length > 0 && staticList.value[0].id){
      openStaticForm('update', staticList.value[0].id);
    } else {'create'
      openStaticForm('create');
    }
    
  }

}
</script>

<style>
.demo-tabs > .el-tabs__content {
  padding: 32px;
  color: #6b778c;
  font-size: 32px;
  font-weight: 600;
}
</style>