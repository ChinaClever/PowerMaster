<template>
  <div class="master">
    <!-- 左大侧 -->
    <div class="master-left">
      <ContentWrap style="height: calc(100% - 15px)">
        <div v-if="!isCloseNav" class="nav-left">
          <!-- 左侧标题栏 -->
          <div class="navBar">微模块机房</div>
          <!-- 信息展示模式 -->
          <div v-if="switchNav">
            <div class="header">
              <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
              <div class="name">微模块机房</div>
              <div>机房202</div>
            </div>
            <div class="line"></div>
            <div class="status">
              <div class="box">
                <div class="top">
                  <div class="tag"></div>&lt;15%
                </div>
                <div class="value"><span class="number">{{statusNumber.lessFifteen}}</span>个</div>
              </div>
              <div class="box">
                <div class="top">
                  <div class="tag empty"></div>小电流
                </div>
                <div class="value"><span class="number">{{statusNumber.smallCurrent}}</span>个</div>
              </div>
              <div class="box">
                <div class="top">
                  <div class="tag warn"></div>15%-30%
                </div>
                <div class="value"><span class="number">{{statusNumber.greaterFifteen}}</span>个</div>
              </div>
              <div class="box">
                <div class="top">
                  <div class="tag error"></div>&gt;30
                </div>
                <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
              </div>
            </div>
            <div class="line"></div>
            <div class="overview">
              <div class="count">
                <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
                <div class="info">
                  <div>总电能</div>
                  <div class="value">295.87 kW·h</div>
                </div>
              </div>
              <div class="count">
                <img class="count_img" alt="" src="@/assets/imgs/dh.jpg" />
                <div class="info">
                  <div>今日用电</div>
                  <div class="value">295.87 kW·h</div>
                </div>
              </div>
              <div class="count">
                <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
                <div class="info">
                  <div>今日用电</div>
                  <div class="value">295.87 kW·h</div>
                </div>
              </div>
            </div>
          </div>
          <!-- 筛选模式 -->
          <div v-else style="margin-top: 10px">
            <NavTree :showCheckbox="true" ref="navTree"  @check="handleCheck" @node-click="handleClick" :showSearch="true"  :dataList="serverRoomArr" />
          </div>
        </div>
        <div v-if="!isCloseNav" class="openNavtree" @click.prevent="handleSwitchNav">
          <Icon icon="ep:switch" />切换
        </div>
        <div v-if="!isCloseNav" class="reduce" @click.prevent="isCloseNav = true"><Icon icon="ep:arrow-left" />收起</div>
        <div v-if="isCloseNav" class="expand" @click.prevent="isCloseNav = false"><Icon icon="ep:arrow-right" /><span>展</span><span>开</span></div>
      </ContentWrap>
    </div>
    <!-- 右大侧 -->
    <div class="master-right">
      <ContentWrap>
        
        <!-- 搜索工作栏 -->
        <el-form
          class="-mb-15px"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="68px"                          
        >
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
              <div class="devKey">{{ item.location }}</div>
              <div class="content">
                <div class="icon" >
                  <div v-if="item.curUnbalance != null" >
                    不平衡度<br/>{{ item.curUnbalance }}%
                  </div>              
                </div>
                <div class="info">                  
                  <div v-if="item.acur != null">A相电流：{{item.acur}}A</div>
                  <div v-if="item.bcur != null" >B相电流：{{item.bcur}}A</div>
                  <div v-if="item.ccur != null" >C相电流：{{item.ccur}}A</div>
                  <div >网络地址：{{ item.devKey }}</div>
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
              <el-text line-clamp="2" v-if="scope.row.volUnbalance != null">
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
              <div class="devKey">{{ item.location }}</div>
              <div class="content">
                <div class="icon" >
                  <div v-if="item.volUnbalance != null" >
                    不平衡度<br/>{{ item.volUnbalance }}%
                  </div>              
                </div>
                <div class="info">                  
                  <div v-if="item.avol != null">A相电压：{{item.avol}}V</div>
                  <div v-if="item.bvol != null" >B相电压：{{item.bvol}}V</div>
                  <div v-if="item.cvol != null" >C相电压：{{item.cvol}}V</div>
                  <div >网络地址：{{ item.devKey }}</div>
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
    </div>
  </div>

  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
// import PDUDeviceForm from './PDUDeviceForm.vue'
import { ElTree } from 'element-plus'
import { CabinetApi } from '@/api/cabinet/info'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()

const isCloseNav = ref(false) // 左侧导航是否收起
const switchNav = ref(false) //false: 导航树 true：微模块展示
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48])
const switchValue = ref(2)
const statusNumber = reactive({
  lessFifteen : 0,
  greaterFifteen : 0,
  greaterThirty : 0,
  smallCurrent : 0
})

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

const handleClick = (row) => {
  console.log("click",row)
}

const handleCheck = async (row) => {
  console.log('handleCheck!', row);
  const ids = [] as any
  row.forEach(item => {
    if (item.type == 3) {
      ids.push(item.id)
    }
  })
  queryParams.cabinetIds = ids
  getList();
}

// 处理切换按钮点击事件
const handleSwitchNav = () => {
  switchNav.value = !switchNav.value
}

const serverRoomArr =  ref([])

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()


watch(filterText, (val) => {
  treeRef.value!.filter(val)
})


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
  cabinetIds : [],
})as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PDUDeviceApi.getPDUDevicePage(queryParams)

    list.value = data.list
    var tableIndex = 0;
    var lessFifteen = 0;
    var greaterFifteen = 0;
    var greaterThirty = 0;
    var smallCurrent = 0;
    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.dataUpdateTime == null && obj?.pow == null){
        return;
      }
      const splitArray = obj.dataUpdateTime.split(' ');
      obj.dataUpdateTime = splitArray[1];
      
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
      if(obj.color == 1){
        smallCurrent++;
      } else if (obj.color == 2) {
        lessFifteen++;
      } else if (obj.color == 3) {
        greaterFifteen++;
      } else if (obj.color == 4) {
        greaterThirty++;
      }
    });
    statusNumber.smallCurrent = smallCurrent;
    statusNumber.lessFifteen = lessFifteen;
    statusNumber.greaterFifteen = greaterFifteen;
    statusNumber.greaterThirty = greaterThirty;
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
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.dataUpdateTime == null && obj?.pow == null){
        return;
      }
      const splitArray = obj.dataUpdateTime.split(' ');
      obj.dataUpdateTime = splitArray[1];      
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

const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  serverRoomArr.value = res
  if (res && res.length > 0) {
    const room = res[0]
    const keys = [] as string[]
    room.children.forEach(child => {
      if(child.children.length > 0) {
        child.children.forEach(son => {
          keys.push(son.id + '-' + son.type)
        })
      }
    })
  }
  console.log(serverRoomArr.value)
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
  getNavList();
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
  getNavList();
  if(!firstTimerCreate.value){
    flashListTimer.value = setInterval((getListNoLoading), 5000);
  }
})
</script>

<style scoped lang="scss">
:deep(.ip:hover) {
  color: blue !important;
  cursor: pointer;
}

.master {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  .master-left {
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
    margin-right: 20px;
    transition: all 0.2s linear;
    .openNavtree {
      box-sizing: border-box;
      text-align: right;
      cursor: pointer;
      position: absolute;
      right: 10px;
      top: 12px;
      font-size: 15px;
      display: flex;
      align-items: center;
    }
    .reduce {
      display: flex;
      align-items: center;
      position: absolute;
      right: 10px;
      top: 52px;
      color: #777777;
      cursor: pointer;
      font-size: 13px;
    }
    .expand {
      width: 30px;
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #777;
      cursor: pointer;
      background-color: #eef4fc;
      padding: 10px 0;
    }
  }
  .master-right {
    flex: 1;
    overflow: hidden;
  }
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

.navBar {
  box-sizing: border-box;
  width: 100%;
  height: 46px;
  line-height: 46px;
  padding-left: 10px;
  background-color: #d5ffc1;
  font-size: 14px;
}
.nav-left {
  width: 215px;
  height: 100%;
  .overview {
    padding: 0 20px;
    .count {
      height: 70px;
      margin-bottom: 15px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-left: 15px;
      padding-right: 10px;
      box-shadow: 0 3px 4px 1px rgba(0,0,0,.12);
      border-radius: 3px;
      border: 1px solid #eee;
      .info {
        height: 46px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: space-between;
        font-size: 13px;
        .value {
          font-size: 15px;
          font-weight: bold;
        }
      }
    }
  }
  .status {
    display: flex;
    flex-wrap: wrap;
    .box {
      height: 70px;
      width: 50%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      .top {
        display: flex;
        align-items: center;
        .tag {
          width: 8px;
          height: 8px;
          background-color: #3bbb00;
          margin-right: 3px;
          margin-top: 2px;
        }
        .empty {
          background-color: #ccc;
        }
        .warn {
          background-color: #ffc402;
        }
        .error {
          background-color: #fa3333;
        }
      }
      .value {
        font-size: 14px;
        margin-top: 5px;
        color: #aaa;
        .number {
          font-size: 14px;
          font-weight: bold;
          margin-right: 5px;
          color: #000;
        }
      }
    }
  }
  .header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 13px;
    padding-top: 28px;
    .header_img {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px solid #555;
      img {
        width: 75px;
        height: 75px;
      }
    }
    .name {
      font-size: 15px;
      margin: 15px 0;
    }
  }
  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
}
.progressContainer {
  display: flex;
  justify-content: center;
  align-items: center;
  .progress {
    width: 100px;
    height: 18px;
    line-height: 18px;
    font-size: 12px;
    box-sizing: border-box;
    border-radius: 150px;
    overflow: hidden;
    position: relative;
    vertical-align: middle;
    background-color: #bfa;
    display: flex;
    justify-content: center;
    .left {
      overflow: hidden;
      box-sizing: border-box;
      background-color: var(--el-color-primary);

    }
    .right {
      overflow: hidden;
      background-color:  #f56c6c;
    }
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

:deep(.master-left .el-card__body) {
  padding: 0;
}
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
</style>
