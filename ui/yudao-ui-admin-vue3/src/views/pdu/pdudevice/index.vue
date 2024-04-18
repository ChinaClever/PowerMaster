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
          <el-form-item label="状态"  prop="status">
            <el-checkbox-group v-model="queryParams.status" @change="handleCheckboxChange">
              <el-checkbox :label="0" >
                <el-tag>空载设备</el-tag>
              </el-checkbox>
              <el-checkbox :label="1" >
                <el-tag type="warning" >预警设备</el-tag>
              </el-checkbox>
              <el-checkbox :label="2" >
                <el-tag type="danger">告警设备</el-tag>
              </el-checkbox>
              <el-checkbox :label="3" >
                <el-tag type="success">升级设备</el-tag>
              </el-checkbox>
              <el-checkbox :label="4" >
                <el-tag type="info" >故障设备</el-tag>
              </el-checkbox>
              <el-checkbox :label="5" >
                <el-tag type="info" >离线设备</el-tag>
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="网络地址" prop="devKey">
            <el-input
              v-model="queryParams.devKey"
              placeholder="请输入网络地址"
              clearable
              @keyup.enter="handleQuery"
              class="!w-240px"
            />
          </el-form-item>
        

          <el-form-item>
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
            <el-button @click="pageSizeArr=[24,36,48];queryParams.pageSize = 25;getList();switchValue = 0;" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />阵列模式</el-button>
            <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 1;" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />表格模式</el-button>
            <el-button
              type="primary"
              plain
              @click="openForm('create')"
              v-hasPermi="['pdu:PDU-device:create']"
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
      <ContentWrap  v-show="switchValue">
        <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toPDUDisplayScreen" >
          <el-table-column label="编号" align="center" prop="tableId" />
          <!-- 数据库查询 -->
          <el-table-column label="所在位置" align="center" prop="location" />
          <el-table-column label="运行状态" align="center" prop="status" >
            <template #default="scope">
              <el-tag  v-if="scope.row.status == 0 && scope.row.apparentPow == 0">空载设备</el-tag>
              <el-tag  v-if="scope.row.status == 0 && scope.row.apparentPow != 0">正常设备</el-tag>
              <el-tag type="warning" v-if="scope.row.status == 1">预警设备</el-tag>
              <el-tag type="danger" v-if="scope.row.status == 2">告警设备</el-tag>
              <el-tag type="success" v-if="scope.row.status == 3">升级设备</el-tag>
              <el-tag type="info" v-if="scope.row.status == 4">故障设备</el-tag>
              <el-tag type="info" v-if="scope.row.status == 5">离线设备</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="总视在功率" align="center" prop="apparentPow" width="130px" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.apparentPow }} kVA
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="总有功功率" align="center" prop="pow" width="130px">
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.pow }} kW
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="总电能" align="center" prop="ele" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.ele }} kWh
              </el-text>
            </template>
          </el-table-column>
          
          <!-- 数据库查询 -->
          <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" /> 
          <el-table-column
            label="数据更新时间"
            align="center"
            prop="dataUpdateTime"
            dateFormatter="hh:MM:ss"
            width="180px"
          />
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="toPDUDisplayScreen(scope.row)"
              >
              设备管理
              </el-button>
              <el-button
                link
                type="danger"
                @click="handleDelete(scope.row.id)"
                v-if="scope.row.status == 5"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页 -->
      </ContentWrap>

      <ContentWrap v-show="!switchValue">
          <div class="arrayContainer">
            <div class="arrayItem" v-for="item in list" :key="item.id">
              <div class="devKey">{{ item.devKey }}</div>
              <div class="content">
                <div class="icon">{{item.pow}}<br/>kW</div>
                <div class="info">
                  <div >所在位置：</div>
                  <div >视在功率：{{item.apparentPow}}kVA</div>
                  <div >有功电能：{{item.ele}}kWh</div>
                  <div >更新时间：{{item.dataUpdateTime}}</div>
                  <!-- <div>AB路占比：{{item.fzb}}</div> -->
                </div>
              </div>
              <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
              <div class="status">报警</div>
              <button class="detail" @click="toPDUDisplayScreen(item)">详情</button>
            </div>
          </div>
        </ContentWrap>
        <ContentWrap>
          <Pagination
          :total="total"
          :page-size-arr="pageSizeArr"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
        </ContentWrap>
        
    </el-col>
  </el-row>
  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
// import PDUDeviceForm from './PDUDeviceForm.vue'
import { ElTree } from 'element-plus'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()

const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48])

const switchValue = ref(0)

const ip = ref("ip");

const serverRoomArr =  [
  {
    value: '1',
    label: '机房1',
    children: [
      {
        value: '1-1',
        label: '柜列1',
        children: [
        {
          value: '1-1-1',
          label: '机柜1',
        },
        {
          value: '1-1-2',
          label: '机柜2',
        },]
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
        children: [
        {
          value: '2-1-1',
          label: '机柜1',
        },
        {
          value: '2-1-2',
          label: '机柜2',
        },]
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
        children: [
        {
          value: '3-1-1',
          label: '机柜1',
        },
        {
          value: '3-1-2',
          label: '机柜2',
        },]
      },
    ],
  },
]

//折叠功能
let treeWidth = ref(3)
let isCollapsed = ref(0);

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
  { 
    id:null,
    status:null,
    apparentPow:null,
    pow:null,
    ele:null,
    devKey:null,
    location:null,
    dataUpdateTime : ""
  }
]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
    list.value = data.list
    var tableIndex = 0;
    list.value.forEach((obj) => {
      const splitArray = obj.dataUpdateTime.split(' ');
      obj.dataUpdateTime = splitArray[1];
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      obj.apparentPow = obj.apparentPow.toFixed(3);
      obj.pow = obj.pow.toFixed(3);
      obj.ele = obj.ele.toFixed(1);
    });
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListNoLoading = async () => {

  try {
    const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
    list.value = data.list
    var tableIndex = 0;
    list.value.forEach((obj) => {
      const splitArray = obj.dataUpdateTime.split(' ');
      obj.dataUpdateTime = splitArray[1];
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      obj.apparentPow = obj.apparentPow.toFixed(3);
      obj.pow = obj.pow.toFixed(3);
      obj.ele = obj.ele.toFixed(1);
    });
    total.value = data.total
  } catch (error) {
    
  }
}

const toPDUDisplayScreen = (row) =>{
  push('/pdu/pdudisplayscreen?devKey=' + row.devKey + '&location=' + row.location + '&id=' + row.id);
}

// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }

const handleCheckboxChange = () =>{
  console.log(queryParams.status);
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
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
    await PDUDeviceApi.deletePDUDevice(id)
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
    const data = await PDUDeviceApi.exportPDUDevice(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
  flashListTimer.value = setInterval((getListNoLoading), 5000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
    firstTimerCreate.value = false;
  }
})

onActivated(() => {
  getList()
  if(!firstTimerCreate.value){
    flashListTimer.value = setInterval((getListNoLoading), 5000);
  }
})
</script>

<style scoped >
/deep/ .ip:hover {
  color: blue !important;
  cursor: pointer;
}

.arrayContainer {
  display: flex;
  flex-wrap: wrap;
  .arrayItem {
    width: 25%;
    height: 140px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      display: flex;
      align-items: center;
      .icon {
        width: 30px;
        height: 30px;
        margin: 0 28px;
      }
    }
    .devKey{
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .room {
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .status {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fa3333;
      color: #fff;
      position: absolute;
      right: 8px;
      top: 8px;
    }
    .detail {
      width: 40px;
      height: 25px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 8px;
      bottom: 8px;
    }
  }
}
</style>
