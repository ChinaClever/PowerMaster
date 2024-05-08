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
          <el-form-item v-if="switchValue == 2 || switchValue == 3">
            <template v-for="(status, index) in statusList" :key="index">
              <button :class="status.selected ? status.activeClass : status.cssClass" @click.prevent="handleSelectStatus(index)">{{status.name}}</button>
            </template>
          </el-form-item>
          <el-form-item label="网络地址" prop="devKey">
            <el-input
              v-model="queryParams.devKey"
              placeholder="请输入网络地址"
              clearable
              @keyup.enter="handleQuery"
              class="!w-200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
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
          <div style="float:right">
            <el-button @click="pageSizeArr=[24,36,48];queryParams.pageSize = 24;getList();switchValue = 2;" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电流阵列</el-button>
            <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />电流表格</el-button>
            <el-button @click="statusList.forEach((item) => item.selected = true);pageSizeArr=[24,36,48];queryParams.pageSize = 24;getList();switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电压阵列</el-button>
            <el-button @click="statusList.forEach((item) => item.selected = true);pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 1;" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />电压表格</el-button>
          </div>
        </el-form>
      </ContentWrap>

      <!-- 列表 -->
      <ContentWrap  v-show="switchValue == 3">
        <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toPDUDisplayScreen" >
          <el-table-column label="编号" align="center" prop="tableId" />
          <!-- 数据库查询 -->
          <el-table-column label="所在位置" align="center" prop="location" />
          <el-table-column label="运行状态" align="center" prop="status" >
            <template #default="scope" >
                <el-tag type="info"  v-if="scope.row.color == 1">小电流不平衡</el-tag>
                <el-tag type="success"  v-if="scope.row.color == 2">大电流不平衡</el-tag>
                <el-tag type="warning" v-if="scope.row.color == 3">大电流不平衡</el-tag>
                <el-tag type="danger" v-if="scope.row.color == 4">大电流不平衡</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="A相电流" align="center" prop="acur" width="130px" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.acur != null">
                {{ scope.row.acur }}A
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="B相电流" align="center" prop="bcur" width="130px" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.bcur != null">
                {{ scope.row.bcur }}A
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="C相电流" align="center" prop="ccur" width="130px" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.ccur != null">
                {{ scope.row.ccur }}A
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="不平衡度" align="center" prop="curUnbalance" width="130px">
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.curUnbalance != null" >
                {{ scope.row.curUnbalance }}%
              </el-text>
            </template>
          </el-table-column>
          <!-- 数据库查询 -->
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="toPDUDisplayScreen(scope.row)"
              >
              设备详情
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

      <ContentWrap v-show="switchValue == 2">
          <div class="arrayContainer">
            <div class="arrayItem" v-for="item in list" :key="item.devKey">
              <div class="devKey">{{ item.devKey }}</div>
              <div class="content">
                <div class="icon" v-if="item.curUnbalance != null">不平衡度<br/>{{ item.curUnbalance }}%</div>
                <div class="info">
                  <div >所在位置：</div>
                  <div v-if="item.acur != null">A相电流：{{item.acur}}A</div>
                  <div v-if="item.bcur != null" >B相电流：{{item.bcur}}A</div>
                  <div v-if="item.ccur != null" >C相电流：{{item.ccur}}A</div>
                  <!-- <div>AB路占比：{{item.fzb}}</div> -->
                </div>
              </div>
              <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
              <div class="status" v-if="item.color != 0">
                <el-tag type="info"  v-if="item.color == 1">小电流不平衡</el-tag>
                <el-tag type="success"  v-if="item.color == 2">大电流不平衡</el-tag>
                <el-tag type="warning" v-if="item.color == 3">大电流不平衡</el-tag>
                <el-tag type="danger" v-if="item.color == 4">大电流不平衡</el-tag>
              </div>
              <button class="detail" @click="toPDUDisplayScreen(item)">详情</button>
            </div>
          </div>
        </ContentWrap>

        <ContentWrap  v-show="switchValue == 1">
        <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toPDUDisplayScreen" >
          <el-table-column label="编号" align="center" prop="tableId" />
          <!-- 数据库查询 -->
          <el-table-column label="所在位置" align="center" prop="location" />
          <el-table-column label="A相电压" align="center" prop="avol" width="130px" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.avol">
                {{ scope.row.avol }}V
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="B相电压" align="center" prop="bvol" width="130px" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.bvol">
                {{ scope.row.bvol }}V
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="C相电压" align="center" prop="cvol" width="130px" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.cvol">
                {{ scope.row.cvol }}V
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="不平衡度" align="center" prop="volUnbalance" width="130px">
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.volUnbalance }}%
              </el-text>
            </template>
          </el-table-column>
          <!-- 数据库查询 -->
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="toPDUDisplayScreen(scope.row)"
              >
              设备详情
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

      <ContentWrap v-show="switchValue == 0">
          <div class="arrayContainer">
            <div class="arrayItem" v-for="item in list" :key="item.devKey">
              <div class="devKey">{{ item.devKey }}</div>
              <div class="content">
                <div class="icon"  v-if="item.volUnbalance != null" >不平衡度<br/>{{ item.volUnbalance }}%</div>
                <div class="info">
                  <div >所在位置：</div>
                  <div v-if="item.avol != null">A相电压：{{item.avol}}V</div>
                  <div v-if="item.bvol != null" >B相电压：{{item.bvol}}V</div>
                  <div v-if="item.cvol != null" >C相电压：{{item.cvol}}V</div>
                  <!-- <div>AB路占比：{{item.fzb}}</div> -->
                </div>
              </div>
              <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
              <div class="status" >
                <el-tag type="info" >电压不平衡</el-tag>

              </div>
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

const switchValue = ref(2)

const ip = ref("ip");

const statusList = reactive([
  {
    name: '<15%',
    selected: true,
    value: 2,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal'
  },
  {
    name: '15%-30%',
    selected: true,
    value: 3,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn'
  },
  {
    name: '>30%',
    selected: true,
    value: 4,
    cssClass: 'btn_error',
    activeClass: 'btn_error error'
  },
  {
    name: '小电流',
    selected: true,
    value: 1,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'
  },
])

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
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null,
    acur : null,
    bcur : null,
    ccur : null,
    curUnbalance : null,
  }
]) as any// 列表的数据
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
      obj.pf = obj.pf.toFixed(2);
      obj.acur = obj.acur?.toFixed(2);
      obj.bcur = obj.bcur?.toFixed(2);
      obj.ccur = obj.ccur?.toFixed(2);
      obj.curUnbalance = obj.curUnbalance?.toFixed(0);
      obj.avol = obj.avol?.toFixed(1);
      obj.bvol = obj.bvol?.toFixed(1);
      obj.cvol = obj.cvol?.toFixed(1);
      obj.volUnbalance = obj.volUnbalance?.toFixed(0);
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
      obj.pf = obj.pf.toFixed(2);
      obj.acur = obj.acur?.toFixed(2);
      obj.bcur = obj.bcur?.toFixed(2);
      obj.ccur = obj.ccur?.toFixed(2);
      obj.curUnbalance = obj.curUnbalance?.toFixed(0);
      obj.avol = obj.avol?.toFixed(1);
      obj.bvol = obj.bvol?.toFixed(1);
      obj.cvol = obj.cvol?.toFixed(1);
      obj.volUnbalance = obj.volUnbalance?.toFixed(0);
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

const handleSelectStatus = (index) => {
  statusList[index].selected = !statusList[index].selected
  const status =  statusList.filter(item => item.selected)
  const statusArr = status.map(item => item.value)
  queryParams.color = statusArr;
  handleQuery();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  statusList.forEach((item) => item.selected = true)
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

.btn_offline,
.btn_normal,
.btn_warn,
.btn_error {
  width: 58px;
  height: 35px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    color: #7bc25a;
  }
}
.btn_offline {
  border: 1px solid #aaa;
  background-color: #fff;
  margin-right: 8px;
}
.offline {
  background-color: #aaa;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_normal {
  border: 1px solid #3bbb00;
  background-color: #fff;
  margin-right: 8px;
}
.normal {
  background-color: #3bbb00;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_warn {
  border: 1px solid #ffc402;
  background-color: #fff;
  margin-right: 8px;
}
.warn {
  background-color: #ffc402;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_error {
  border: 1px solid #fa3333;
  background-color: #fff;
  margin-right: 8px;
}
.error {
  background-color: #fa3333;
  color: #fff;
  &:hover {
    color: #fff;
  }
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
        width: 60px;
        height: 30px;
        margin: 0 28px;
        text-align: center;
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

      color: #fff;
      position: absolute;
      right: 38px;
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
      cursor: pointer;
    }
  }
}
</style>
