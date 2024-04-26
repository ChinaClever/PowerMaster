<template>
  <ContentWrap>
    <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
      <el-tab-pane label="消息队列" name="first">
        <ContentWrap>
          <!-- 搜索工作栏 -->
          <el-form
            class="-mb-15px"
            :model="mqQueryParams"
            ref="mqQueryFormRef"
            :inline="true"
            label-width="68px"
          >
            <el-form-item label="ip端口" prop="ipAndPorts">
              <el-input
                v-model="mqQueryParams.ipAndPorts"
                placeholder="请输入ip端口"
                clearable
                @keyup.enter="handleMQQuery"
                class="!w-240px"
              />
            </el-form-item>
            <el-form-item label="主题/分组" prop="topic" label-width="90px">
              <el-input
                v-model="mqQueryParams.topic"
                placeholder="请输入主题/分组"
                clearable
                @keyup.enter="handleMQQuery"
                class="!w-240px"
              />
            </el-form-item>
            <el-form-item label="mq名称" prop="mq">
              <el-input
                v-model="mqQueryParams.mq"
                placeholder="请输入mq名称"
                clearable
                @keyup.enter="handleMQQuery"
                class="!w-240px"
              />
            </el-form-item>
            <el-form-item>
              <el-button @click="handleMQQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
              <el-button @click="resetMQQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
              <el-button
                type="primary"
                plain
                @click="openMQForm('create')"
              >
                <Icon icon="ep:plus" class="mr-5px" /> 新增
              </el-button>
              <el-button
                type="success"
                plain
                @click="handleMQExport"
                :mqListLoading="mqExportLoading"
                v-hasPermi="['sys:mq-config:export']"
              >
                <Icon icon="ep:download" class="mr-5px" /> 导出
              </el-button>
            </el-form-item>
          </el-form>
        </ContentWrap>

        <!-- 列表 -->
        <ContentWrap>
          <el-table v-mqListLoading="mqListLoading" :data="mqList" :stripe="true" :show-overflow-tooltip="true">
            <el-table-column label="ip端口" align="center" prop="ipAndPorts" />
            <el-table-column label="用户名" align="center" prop="userName" />
            <el-table-column label="密码" align="center" prop="passWord" />
            <el-table-column label="主题/分组" align="center" prop="topic" />
            <el-table-column label="mq名称" align="center" prop="mq" />
            <el-table-column label="创建时间" align="center" prop="createTime" />
            <el-table-column label="操作" align="center">
              <template #default="scope">
                <el-button
                  link
                  type="primary"
                  @click="openMQForm('update', scope.row.id)"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  @click="handleMQDelete(scope.row.id)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <!-- 分页 -->
          <Pagination
            :mqListTotal="mqListTotal"
            v-model:page="mqQueryParams.pageNo"
            v-model:limit="mqQueryParams.pageSize"
            @pagination="getMQList"
          />
        </ContentWrap>

        <!-- 表单弹窗：添加/修改 -->
        <MqConfigForm ref="mqFormRef" @success="getMQList" />
      </el-tab-pane>
      <el-tab-pane label="数据采集" name="second">
        Config
      </el-tab-pane>
      <el-tab-pane label="电费计量方式" name="third">
        Role
      </el-tab-pane>
      <el-tab-pane label="计算服务" name="fourth">
        Task
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

/** 消息队列系统配置 列表 */
defineOptions({ name: 'MqConfig' })

const mqMessage = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const mqListLoading = ref(true) // 列表的加载中
const mqList = ref<MqConfigVO[]>([]) // 列表的数据
const mqListTotal = ref(0) // 列表的总页数
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
const mqQueryFormRef = ref() // 搜索的表单
const mqExportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getMQList = async () => {
  mqListLoading.value = true
  try {
    const data = await MqConfigApi.getMqConfigPage(mqQueryParams)
    mqList.value = data.mqList
    mqListTotal.value = data.mqListTotal
  } finally {
    mqListLoading.value = false
  }
}

/** 搜索按钮操作 */
const handleMQQuery = () => {
  mqQueryParams.pageNo = 1
  getMQList()
}

/** 重置按钮操作 */
const resetMQQuery = () => {
  mqQueryFormRef.value.resetFields()
  handleMQQuery()
}

/** 添加/修改操作 */
const mqFormRef = ref()
const openMQForm = (type: string, id?: number) => {
  mqFormRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleMQDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await mqMessage.delConfirm()
    // 发起删除
    await MqConfigApi.deleteMqConfig(id)
    mqMessage.success(t('common.delSuccess'))
    // 刷新列表
    await getMQList()
  } catch {}
}

/** 导出按钮操作 */
const handleMQExport = async () => {
  try {
    // 导出的二次确认
    await mqMessage.exportConfirm()
    // 发起导出
    mqExportLoading.value = true
    const data = await MqConfigApi.exportMqConfig(mqQueryParams)
    download.excel(data, '消息队列系统配置.xls')
  } catch {
  } finally {
    mqExportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getMQList()
})

const activeName = ref('first')

const handleClick = (tab: TabsPaneContext, event: Event) => {
  console.log(tab, event)
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