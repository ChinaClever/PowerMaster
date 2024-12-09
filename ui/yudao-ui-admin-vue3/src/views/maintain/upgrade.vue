<template>
  <ContentWrap>
    <div class="flex justify-between">
      <el-form
        class="-mb-15px"
        :model="queryParams"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="搜索方式：" label-width="90" prop="type">
          <el-select v-model="queryParams.type" clearable style="width: 140px">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="queryParams.type == 1" label="机房：">
          <el-select multiple collapse-tags v-model="queryParams.rooms" placeholder="请选择" style="width: 140px">
            <el-option
              v-for="item in roomList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="queryParams.type == 2" label="ip：">
          <el-input
            v-model="queryParams.ip"
            placeholder="请输入ip"
            clearable
            class="!w-160px"
            height="35"
          />
        </el-form-item>
        <el-form-item>
          <el-button v-if="queryParams.type == 2 || queryParams.type == 1" style="margin-left: 12px" @click="getTableData(true)" ><Icon icon="ep:search" />搜索</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="dialogChooseVisible = true" plain>上传</el-button>
    </div>
  </ContentWrap>
  <ContentWrap>
    <el-table style="width: 100%;" height="600" v-loading="loading" :data="tableData">
        <el-table-column label="升级设备" min-width="110" align="center" prop="upgradeDevMsg" />
        <el-table-column label="升级文件" min-width="110" align="center" prop="fileNames" />
        <el-table-column label="文件路径" min-width="110" align="center" prop="filePath" />
        <el-table-column label="上传结果" min-width="110" align="center" prop="cabinetName" />
        <el-table-column label="创建时间" min-width="110" align="center" prop="createTime" />
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
  <Dialog v-model="dialogChooseVisible" title="上传对象">
    <el-form label-width="100">
      <el-form-item label="类型：">
        <el-radio-group v-model="dialogFrom.type">
          <el-radio :label="0">全部</el-radio>
          <el-radio :label="1">机房</el-radio>
          <el-radio :label="2">ip</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="dialogFrom.type == 1" label="机房：">
        <el-select multiple v-model="dialogFrom.rooms" collapse-tags  placeholder="请选择" style="width: 140px">
          <el-option
            v-for="item in roomList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="dialogFrom.type == 2" label="ip：">
        <IpTable @update="handleUpdateIp" />
      </el-form-item>
      <el-form-item label="文件：">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-remove="handleRemove"
          :on-change="handleChange"
          :auto-upload="false"
          multiple
          :limit="10"
          :on-exceed="handleExceed"
          :file-list="fileList">
          <el-button size="small" type="primary">点击上传</el-button>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogChooseVisible = false">取 消</el-button>
      <el-button :loading="dialogLoading" @click="handleSubmit" type="primary" >确 定</el-button>
    </template>
  </Dialog>
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
const dialogLoading = ref(false) // 
const dialogChooseVisible = ref(false) // 
const dialogFrom = ref({
  type: 0,
  rooms: [],
  ips: [] as any
})
const serchType = ref(0)
const tableData = ref([])
const roomList = ref([])
const fileList = ref<any>([])
const queryParams = reactive({
  type: 0,
  rooms: [],
  ip: '',
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})
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
    const res = await maintainApi.getUpgradeFilePage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      upgradeDev: queryParams.type,
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
      const res = await maintainApi.deleteFillRecord(id)
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
const handleUpdateIp = (data) => {
  dialogFrom.value.ips = data
  console.log('handleUpdateIp', data, dialogFrom.value.ips)
}
const handleRemove: UploadProps['onRemove'] = (file, uploadFiles) => {
  console.log('handleRemove', file, uploadFiles)
  fileList.value = uploadFiles
}
const handleChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  fileList.value = uploadFiles
}
const handleExceed = (files, fileList) => {
  message.warning(`当前限制选择 3 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`)
}
const handleSubmit = async() => {
  if (fileList.value.length == 0) {
    message.error('请上传文件')
    return
  }
  dialogLoading.value = true
  console.log('handleSubmit', fileList.value)
  try {
    let formData = new FormData()
    fileList.value.forEach(file => {
      formData.append('files', file.raw)
    })
    const params = {
      upgradeDev: dialogFrom.value.type
    } as any
    //debugger
    if (params.upgradeDev == 2) params.ipList = dialogFrom.value.ips.map(item=>item.ip)
    if (params.upgradeDev == 1) params.roomIds = dialogFrom.value.rooms
    console.log('params', params, dialogFrom.value.ips)
    const res = await maintainApi.getUpgradeFillUpload(params, formData)
    if (res) {
      message.success('上传成功！')
      dialogChooseVisible.value = false
      getTableData(true)
    }
  } catch (error) {
    console.log(error)
  } finally {
    dialogLoading.value = false
  }
}
getRoomList()
getTableData()
</script>

<style lang="scss" scoped>

</style>