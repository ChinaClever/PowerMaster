<template>
  <ContentWrap>
    <div class="flex justify-between">
      <el-form
        class="-mb-15px"
        :model="queryParams"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="ip：">
          <el-input
            v-model="queryParams.ip"
            placeholder="请输入ip"
            clearable
            class="!w-160px"
            height="35"
          />
        </el-form-item>
        <el-form-item>
          <el-button style="margin-left: 12px" @click="getTableData(true)" ><Icon icon="ep:search" />搜索</el-button>
        </el-form-item>
      </el-form>
    </div>
  </ContentWrap>
  <ContentWrap>
    <el-table style="width: 100%;" height="600" v-loading="loading" :data="tableData">
      <el-table-column label="ip" min-width="110" align="center" prop="devIp" />
      <el-table-column label="位置" min-width="110" align="center" prop="devposition" />
      <el-table-column label="升级文件" min-width="110" align="center" prop="fileNames" />
      <el-table-column label="升级状态" min-width="110" align="center" prop="status" >
        <template #default="scope">
          {{statusList[scope.row.status]}}
        </template>
      </el-table-column>
      <el-table-column label="下载进度" min-width="110" align="center" prop="downloadProgress" >
        <template #default="scope">
          {{scope.row.downloadProgress}}%
        </template>
      </el-table-column>
      <el-table-column label="升级结果" min-width="110" align="center" prop="result" />
      <el-table-column label="结束原因" min-width="110" align="center" prop="finishReason" />
      <el-table-column label="开始时间" min-width="110" align="center" prop="startTime" />
      <el-table-column label="结束时间" min-width="110" align="center" prop="endTime" />
      <el-table-column fixed="right" label="操作" width="120">
        <template #default="scope">
          <el-button link type="primary" size="small" @click.prevent="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="queryParams.pageTotal"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getTableData(false)"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { maintainApi } from '@/api/maintain/upgrade'
import { CabinetApi } from '@/api/cabinet/info'
import IpTable from './componet/ipTable.vue'
import type { UploadProps } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { tr } from 'element-plus/es/locale'

const message = useMessage()
const loading = ref(false) // 
const tableData = ref([])
const roomList = ref([])
const queryParams = reactive({
  ip: '',
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})
const statusList = ['未开始', '已通知', '下载中', '下载完成', '超时未下载', '下载失败', '无需升级', '升级完成']
const options = [
  {
    value: 0,
    label: '全部',
  },
  {
    value: 1,
    label: '机房',
  },
  {
    value: 2,
    label: 'ip',
  },
]
// 获取表格数据
const getTableData = async(reset = false) => {
  console.log('getTableData', queryParams)
  loading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await maintainApi.getUpgradeRecordPage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      ips: queryParams.ip
    })
    console.log('res', res)
    if (res.list) {
      tableData.value = res.list
      queryParams.pageTotal = res.total
    }
  } finally {
    loading.value = false
  }
}
// 获取机房
const getRoomList = async() => {
  const res = await CabinetApi.getRoomList({})
  console.log('获取机房', res)
  roomList.value = res || []
}
const handleDelete = (id) => {
  ElMessageBox.confirm(
    '确定删除这次记录吗?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async() => {
      const res = await maintainApi.deleteUpgradeRecord(id)
      console.log('res', res)
      if (res) message.success('删除成功')
      getTableData(true)
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '取消了',
      })
    })
}
getRoomList()
getTableData()
</script>

<style lang="scss" scoped>

</style>