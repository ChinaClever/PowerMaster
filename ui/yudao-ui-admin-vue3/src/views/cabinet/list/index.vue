<template>
  <el-row :gutter="20">
    <el-col :span="treeWidth" :xs="24">
      
      <el-input
        v-model="filterText"
        style="width: 190px"
        placeholder="Filter keyword"
      />

      <el-tree
        ref="treeRef"
        style="max-width: 600px"
        class="filter-tree"
        :data="serverRoomArr"
        :props="defaultProps"
        default-expand-all
        show-checkbox
        :filter-node-method="filterNode"
      />
    </el-col>
    <el-col :span="24 - treeWidth" :xs="24">
      <ContentWrap>
        
        <!-- 搜索工作栏 -->
        <el-form
          class="-mb-15px"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="68px"                          
        >
          <el-form-item label="" prop="collaspe">
            <el-switch 
            v-model="isCollapsed"  
            active-color="#409EFF" 
            inactive-color="#909399"
            active-text="折叠"  
            active-value="100"
            inactive-value="0" 
            @change="toggleCollapse" />
          </el-form-item>
         
          <div class="split-container">
              <div class="blue-square" :style="{ width: AWidth + '%'}"></div>
              <div class="orange-square" :style="{ width: BWidth + '%' }"></div>
            </div>

          <el-form-item>
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
            <el-button
              type="primary"
              plain
              @click="openForm('create')"
            >
              <Icon icon="ep:plus" class="mr-5px" /> 新增
            </el-button>
            <el-button
              type="success"
              plain
              @click="handleExport"
              :loading="exportLoading"
              v-hasPermi="['pdu:PDU-device:export']"
            >
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>

      <!-- 列表 -->
      <ContentWrap>
        <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  >
          <el-table-column label="名称" align="center" prop="id" />
          <el-table-column label="总AB视在功率" align="center" prop="apparent_pow" width="130px" />
          <el-table-column label="总AB有功功率" align="center" prop="pow" width="130px"/>
          <el-table-column label="总AB电能" align="center" prop="ele" />
          <el-table-column label="所属公司" align="center" prop="owner" />
          <el-table-column label="负载比" align="center" prop="load_ratio"/> 
          <el-table-column label="AB占比" align="center" prop="percent"  >
            <template #default="scope">
              <div class="split-container">
                <div class="blue-square" :style="{ width: scope.row.percent + '%'}"></div>
                <div class="orange-square" :style="{ width: (100 - scope.row.percent) + '%' }"></div>
              </div>
            </template> 
          </el-table-column>
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button
                link
                type="danger"
                @click="handleDelete(scope.row.id)"
                v-if="scope.row.status == 2"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页 -->
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </ContentWrap>
    </el-col>
  </el-row>
  <!-- 表单弹窗：添加/修改 -->
  <cabinetForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { cabinetApi } from '@/api/cabinet/list'
import cabinetForm from './cabinetForm.vue'
import { ElTree } from 'element-plus'

/** PDU设备 列表 */
defineOptions({ name: 'cabinet' })

const serverRoomArr =  [
  {
    value: '1',
    label: '机房1',
    children: [
      {
        value: '1-1',
        label: '柜列1',
      },
    ],
  },
  {
    value: '2',
    label: '机房2',
    children: [
      {
        value: '2-1',
        label: '柜列1',
      },
    ],
  },
  {
    value: '3',
    label: '机房3',
    children: [
      {
        value: '3-1',
        label: '柜列1',
      },
    ],
  },
]

//折叠功能
let treeWidth = ref(3)
let isCollapsed = ref(0);

const AWidth = ref(30);
const BWidth = ref(70);

const toggleCollapse = () => {
  treeWidth.value = isCollapsed.value == 0 ? 3 : 0;
};

//树型控件
interface Tree {
  [key: string]: any
}

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.label.includes(value)
}

const defaultProps = {
  children: 'children',
  label: 'label',
}

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

// 下拉框选项数组
const deviceStatus = ref([])

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中
const list = ref([
// <el-table-column label="名称" align="center" prop="id" />
//           <el-table-column label="总AB视在功率" align="center" prop="apparent_pow" width="130px" />
//           <el-table-column label="总AB有功功率" align="center" prop="pow" width="130px"/>
//           <el-table-column label="总AB电能" align="center" prop="ele" />
//           <el-table-column label="所属公司" align="center" prop="owner" />
//           <el-table-column label="负载比" align="center" prop="dev_key"/> 
//           <el-table-column label="AB占比" align="center" prop="percent"  >
  {
    id:"机柜1",
    apparent_pow:"225kW",
    pow:"220kW",
    ele:"200kwh",
    owner:"克莱沃",
    load_ratio:"10%",
    percent:50
  },

]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  devKey: undefined,
  dev_key: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:undefined,
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
// const getList = async () => {
//   loading.value = true
//   try {
//     const data = await cabinetApi.getcabinetPage(queryParams)
//     list.value = data.list
//     total.value = data.total
//   } finally {
//     loading.value = false
//   }
// }


/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  // getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await cabinetApi.deletecabinet(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    // await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await cabinetApi.exportcabinet(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  // getList()
})
</script>

<style scoped>
.split-container {
  display: flex;
  height: 200px;
}

.blue-square, .orange-square {
  height: 100%;
}

.blue-square {
  background-color: blue;
}

.orange-square {
  background-color: orange;
}
</style>
