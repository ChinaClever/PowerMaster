<template>
  <CommonMenu :showNavTree="false" navTitle="告警" >
    <template #NavInfo>
      <div class="filterList">
        <!-- <div class="title">告警</div> -->
        <el-menu default-active="0" @select="handleSelect" >
          <el-menu-item index="0">
            <Icon :size="18" icon="ep:bell" />
            <span>未处理警告</span>
          </el-menu-item>
          <el-menu-item index="1">
            <Icon :size="18" icon="ep:bell" />
            <span>已挂起警告</span>
          </el-menu-item>
          <el-menu-item index="2">
            <Icon :size="18" icon="ep:bell" />
            <span>已确认警告</span>
          </el-menu-item>
          <el-menu-item index="3">
            <Icon :size="18" icon="ep:bell" />
            <span>历史警告</span>
          </el-menu-item>
        </el-menu>
      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        :inline="true"
        label-width="68px"
        v-loading="queryLoading"
        @submit.prevent=""
        
      >
        <div>
          <el-form-item label="" label-width="100">
            <el-input
              v-model="queryParams.search"
              placeholder="搜索告警"
              clearable
              class="!w-170px"
              height="35"
            />
          </el-form-item>
          <el-form-item>
            <el-button style="margin-left: 12px" v-model.trim="queryParams.search" type="primary" @click="getTableData(true)" ><Icon icon="ep:search" />搜索</el-button>
          </el-form-item>
          <el-form-item label="发送邮件" label-width="90">
            <el-switch v-model="queryParams.openEmail" @change="saveAlarmConfig" />
          </el-form-item>
          <el-form-item label="声音报警" label-width="90">
            <el-switch v-model="queryParams.openVoice" @change="saveAlarmConfig" />
          </el-form-item>
          <el-form-item label="短信报警" label-width="90">
            <el-switch v-model="queryParams.openMessage" @change="saveAlarmConfig" />
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <el-button  type="primary" @click="handleFormOpen('phone')">手机配置</el-button>
          <el-button  type="primary" @click="handleFormOpen('mail')">邮箱配置</el-button>
          <el-button  type="primary" :disabled="!targetId || preStatus.includes(3)" @click="toHandle(1)">挂起</el-button>
          <el-button  type="primary" :disabled="!targetId || preStatus.includes(3)" @click="toHandle(2)">确认</el-button>
          <el-button  type="primary" :disabled="!targetId || preStatus.includes(3)" @click="toHandle(3)">结束</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <el-table
        ref="multipleTableRef"
        :data="tableData"
        highlight-current-row
        style="width: 100%"
        :stripe="true" 
        :border="true"
        @current-change="handleCurrentChange"
      >
          <!-- <el-table-column type="selection" width="55" /> -->
          <el-table-column type="index" width="80" label="序号" align="center" />
          <el-table-column property="devPosition" label="区域" min-width="100" align="center" />
          <el-table-column property="devName" label="设备" min-width="100" align="center" />
          <el-table-column property="alarmLevelDesc" label="告警等级" min-width="100" align="center" />
          <el-table-column property="alarmTypeDesc" label="告警类型" min-width="100" align="center" />
          <el-table-column property="alarmDesc" label="描述" min-width="120" align="center">
            <template #default="scope">
              <el-tooltip  placement="right">
                <div class="table-desc">{{scope.row.alarmDesc}}</div>
                <template #content>
                  <div class="tooltip-width">{{scope.row.alarmDesc}}</div>
                </template>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column property="startTime" label="开始时间" min-width="100" align="center" />
          <el-table-column property="endTime" label="结束时间" min-width="100" align="center" />
          <el-table-column property="finishReason" label="结束原因" min-width="100" align="center" />
          <el-table-column property="confirmReason" label="确认原因" min-width="100" align="center" />
      </el-table>
      <Pagination
        :total="queryParams.pageTotal"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getTableData(false)"
      />
    </template>
  </CommonMenu>
  <AlarmForm ref="alarmForm" @success="handleChange" />
</template>

<script lang="ts" setup>
import { AlarmApi } from '@/api/system/notify/alarm'
import { ElMessageBox, ElMessage } from 'element-plus'
import AlarmForm from './AlarmForm.vue'

const message = useMessage() // 消息弹窗

const alarmForm = ref()
const multipleTableRef = ref()
const targetId = ref('')
const tableLoading = ref(false)
const tableData = ref([])
const preStatus = ref([0])
const queryLoading = ref(false)
const queryParams = reactive({
  search: '',
  pageNo: 1,
  pageSize: 10,
  a: 0,
  pageTotal: 8,
  id: '',
  openEmail: false,
  openVoice: true,
  openMessage: false,
})
// const tableData = [
//   {
//     qy: '2房-3柜',
//     level: '二级警告',
//     lx: 'ATS警告',
//     ms: '*****',
//     sb: '服务器12',
//     kssj: '2024-06-13 22:25:11',
//     jssj: '2024-06-13 22:25:11',
//     jsyy: '断网',
//     qrry: '确认断网',
//   },
//   {
//     qy: '2房-3柜',
//     level: '二级警告',
//     lx: 'ATS警告',
//     ms: '*****',
//     sb: '服务器12',
//     kssj: '2024-06-13 22:25:11',
//     jssj: '2024-06-13 22:25:11',
//     jsyy: '断网',
//     qrry: '确认断网',
//   },
//   {
//     qy: '2房-3柜',
//     level: '二级警告',
//     lx: 'ATS警告',
//     ms: '*****',
//     sb: '服务器12',
//     kssj: '2024-06-13 22:25:11',
//     jssj: '2024-06-13 22:25:11',
//     jsyy: '断网',
//     qrry: '确认断网',
//   },
//   {
//     qy: '2房-3柜',
//     level: '二级警告',
//     lx: 'ATS警告',
//     ms: '*****',
//     sb: '服务器12',
//     kssj: '2024-06-13 22:25:11',
//     jssj: '2024-06-13 22:25:11',
//     jsyy: '断网',
//     qrry: '确认断网',
//   },
//   {
//     qy: '2房-3柜',
//     level: '二级警告',
//     lx: 'ATS警告',
//     ms: '*****',
//     sb: '服务器12',
//     kssj: '2024-06-13 22:25:11',
//     jssj: '2024-06-13 22:25:11',
//     jsyy: '断网',
//     qrry: '确认断网',
//   },
//   {
//     qy: '2房-3柜',
//     level: '二级警告',
//     lx: 'ATS警告',
//     ms: '*****',
//     sb: '服务器12',
//     kssj: '2024-06-13 22:25:11',
//     jssj: '2024-06-13 22:25:11',
//     jsyy: '断网',
//     qrry: '确认断网',
//   },
// ]

// 获取警告配置
const getAlarmConfig = async() => {
  queryLoading.value = true
  try {
    const res = await AlarmApi.getAlarmConfig({})
    console.log('res', res)
    if (res) {
      queryParams.id = res.id
      queryParams.openEmail = !!res.mailAlarm
      queryParams.openVoice = !!res.voiceAlarm
      queryParams.openMessage = !!res.smsAlarm
    }
  } finally  {
    queryLoading.value = false
  }
}
// 保存警告配置
const saveAlarmConfig = async() => {
  queryLoading.value = true
  try {
    const res = await AlarmApi.saveAlarmConfig({
      id: queryParams.id,
      mailAlarm: queryParams.openEmail ? 1 : 0,
      voiceAlarm: queryParams.openVoice ? 1 : 0,
      smsAlarm: queryParams.openMessage ? 1 : 0,
    })
    console.log('保存警告配置', res)
    if (res) message.success('配置修改成功')
  } finally {
    queryLoading.value = false
  }
  console.log('saveAlarmConfig')
}
// 获取数据
const getTableData = async(reset = false) => {
  // debugger
  tableLoading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await AlarmApi.getAlarmRecord({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      a: 0,
      status: preStatus.value,
      likeName: queryParams.search
    })
    console.log('res', res)
    if (res.list) {
      tableData.value = res.list
      queryParams.pageTotal = res.total
    }
  } finally {
    tableLoading.value = false
  }
}
// 警告处理
const toHandle = (type) => {
  const statusList = ['挂起', '确认', '结束']
  ElMessageBox.prompt('请输入原因：', `${statusList[type-1]}告警`, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputErrorMessage: '请输入',
  })
    .then(({ value }) => {
      console.log(value)
      AlarmApi.saveAlarmRecord({
        id: targetId.value,
        status: type,
        confirmReason: value
      }).then(() => {
        ElMessage({
          type: 'success', // 0 未处理 1 挂起 2确认 3结束
          message: '成功',
        })
      })
    })
}
// 表格行选择处理
const handleCurrentChange = (val) => {
  if (!val) return
  targetId.value = val.id
}
// 
const handleSelect = (key) => {
  console.log('handleSelect', key)
  preStatus.value.splice(0, 1, +key)
  targetId.value = ''
  getTableData(true)
}
// 处理表单弹窗
const handleFormOpen = (type) => {
  alarmForm.value.open(type)
}
// 表单确认完成处理
const handleChange = () => {
  console.log('表单确认完成处理')
}

getTableData()
getAlarmConfig()
</script>

<style lang="scss" scoped>
.filterList {
  background-color: #fff;
  margin-top: 20px;
  .title {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 58px;
    font-weight: bold;
    background-color: #ddd;
  }
  // width: auto;
}
.Alarm-right {
  flex: 1;
  .top {
    padding: 15px;
    background-color: #fff;
  }
}
.table-desc {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  /* 设置宽度和高度 */
  width: 100%;
  height: 100%;
}
.tooltip-width {
  max-width: 500px ;
}
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
:deep(.el-menu) {
  border-right: none;
}
::v-deep .el-table .el-table__header th {
    background-color: #F5F7FA;
    color: #909399;
}
</style>