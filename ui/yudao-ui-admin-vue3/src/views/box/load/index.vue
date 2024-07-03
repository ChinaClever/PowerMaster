<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="均衡配电">
    <template #NavInfo>
      <div>
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
        </div>
        <div class="line"></div>
        <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>&lt;30%
            </div>
            <div class="value"><span class="number">{{statusNumber.lessThirty}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag blue"></div>30%-60%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterNinety}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>60%-90%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&gt;90%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterSixty}}</span>个</div>
          </div>
        </div>
        <div class="line"></div>

      </div>
    </template>
    <template #ActionBar>
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
        <!-- <el-button
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 平衡度范围颜色
        </el-button> -->
        <el-form-item label="网络地址" prop="devKey" label-width="100px" >
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"
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
          <el-button @click="pageSizeArr=[24,36,48];queryParams.pageSize = 24;getList();switchValue = 2;" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>                      
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <el-table v-show="switchValue == 3" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" />
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="运行状态" align="center" prop="color" >
          <template #default="scope" >
            <el-tag type="info"  v-if="scope.row.color == 0">负载</el-tag>
            <el-tag type="success"  v-if="scope.row.color == 1">负载</el-tag>
            <el-tag type="primary"  v-if="scope.row.color == 2">负载</el-tag>
            <el-tag type="warning" v-if="scope.row.color == 3">负载</el-tag>
            <el-tag type="danger" v-if="scope.row.color == 4">负载</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="A相负载率" align="center" prop="aloadRate" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.aloadRate != null">
              {{ scope.row.aloadRate }}%
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="B相负载率" align="center" prop="bloadRate" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bloadRate != null">
              {{ scope.row.bloadRate }}%
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="C相负载率" align="center" prop="cloadRate" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cloadRate != null">
              {{ scope.row.cloadRate }}%
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
              v-if="scope.row.status != null && scope.row.status != 5"
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

      <!-- <div v-show="switchValue == 2  && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
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
            </div>
          </div>
          <div class="status" v-if="item.color != 0">
            <el-tag type="info"  v-if="item.color == 1">小电流不平衡</el-tag>
            <el-tag type="success"  v-if="item.color == 2">大电流不平衡</el-tag>
            <el-tag type="warning" v-if="item.color == 3">大电流不平衡</el-tag>
            <el-tag type="danger" v-if="item.color == 4">大电流不平衡</el-tag>
          </div>
          <button class="detail" @click="toPDUDisplayScreen(item)">详情</button>
        </div>
      </div> -->

      <div v-show="switchValue == 2  && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="icon" >
              <div v-if="item.volUnbalance != null" >
                <el-tag type="success"  v-if="item.color == 1">负载</el-tag>
                <el-tag type="primary"  v-if="item.color == 2">负载</el-tag>
                <el-tag type="warning" v-if="item.color == 3">负载</el-tag>
                <el-tag type="danger" v-if="item.color == 4">负载</el-tag>
              </div>              
            </div>
            <div class="info">                  
              <div v-if="item.aloadRate != null">A相负载率：{{item.aloadRate}}%</div>
              <div v-if="item.bloadRate != null" >B相负载率：{{item.bloadRate}}%</div>
              <div v-if="item.cloadRate != null" >C相负载率：{{item.cloadRate}}%</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" >
            <el-tag type="info"  v-if="item.color == 0">负载</el-tag>
            <el-tag type="success"  v-if="item.color == 1">负载</el-tag>
            <el-tag type="primary"  v-if="item.color == 2">负载</el-tag>
            <el-tag type="warning" v-if="item.color == 3">负载</el-tag>
            <el-tag type="danger" v-if="item.color == 4">负载</el-tag>
          </div>
          <button class="detail" @click="toPDUDisplayScreen(item)" v-if="item.status != null && item.status != 5" >详情</button>
        </div>
      </div>
      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
      <template v-if="list.length == 0 && switchValue != 3">
        <el-empty description="暂无数据" :image-size="300" />
      </template>
    </template>
  </CommonMenu>


  <!-- 表单弹窗：添加/修改 -->
  <!-- <CurbalanceColorForm ref="curBalanceColorForm" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { IndexApi } from '@/api/bus/boxindex'
// import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()

const curBalanceColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48])
const switchValue = ref(2)
const statusNumber = reactive({
  lessThirty : 0,
  greaterThirty : 0,
  greaterSixty : 0,
  greaterNinety : 0
})

const statusList = reactive([
  {
    name: '空载',
    selected: true,
    value: 0,
    cssClass: 'btn_empty',
    activeClass: 'btn_empty empty',
    color: '#aaa'
  },
  {
    name: '负载量<30%',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#3bbb00'
  },
  {
    name: '30%≤负载量<60%',
    selected: true,
    value: 2,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn',
    color: '#ffc402'
  },
  {
    name: '60%≤负载量<90%',
    selected: true,
    value: 3,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    color: '#fa3333'
  },
  {
    name: '负载量>90%',
    selected: true,
    value: 4,
    cssClass: 'btn_unbound',
    activeClass: 'btn_unbound unbound',
    color: '#05ebfc'
  },
])

const devKeyList = ref([])
const loadAll = async () => {
  var data = await IndexApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
}

const querySearch = (queryString: string, cb: any) => {

  const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}

const handleClick = (row) => {
  console.log("click",row)
}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.cabinetIds = null;
    getList();
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    if (item.type == 7) {
      ids.push(item.unique)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.boxDevKeyList = [-1]
  }else{
    queryParams.boxDevKeyList = ids
  }

  getList();
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
    aloadRate : null,
    bloadRate : null,
    cloadRate : null,
    curUnbalance : null,
  }
]) as any// 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devAddr: undefined,
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
    const data = await IndexApi.getIndexPage(queryParams)
    list.value = data.list
    var tableIndex = 0;
    var lessThirty = 0;
    var greaterThirty = 0;
    var greaterSixty = 0;
    var greaterNinety = 0;
    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj.aloadRate != null){
        obj.aloadRate = obj.aloadRate * 100;
        obj.aloadRate = obj.aloadRate?.toFixed(0);
      }
      if(obj.bloadRate != null){
        obj.bloadRate = obj.bloadRate * 100;
        obj.bloadRate = obj.bloadRate?.toFixed(0);
      }
      if(obj.cloadRate != null){
        obj.cloadRate = obj.cloadRate * 100;
        obj.cloadRate = obj.cloadRate?.toFixed(0);
      }
      // obj.apparentPow = obj.apparentPow.toFixed(3);
      // obj.pow = obj.pow.toFixed(3);
      // obj.ele = obj.ele.toFixed(1);
      // obj.pf = obj.pf.toFixed(2);
      // obj.acur = obj.acur?.toFixed(2);
      // obj.bcur = obj.bcur?.toFixed(2);
      // obj.ccur = obj.ccur?.toFixed(2);
      // obj.curUnbalance = obj.curUnbalance?.toFixed(0);
      // obj.avol = obj.avol?.toFixed(1);
      // obj.bvol = obj.bvol?.toFixed(1);
      // obj.cvol = obj.cvol?.toFixed(1);
      // obj.volUnbalance = obj.volUnbalance?.toFixed(0);
      if(obj.color == 4){
        greaterNinety++;
      } else if (obj.color == 1) {
        lessThirty++;
      } else if (obj.color == 2) {
        greaterThirty++;
      } else if (obj.color == 3) {
        greaterSixty++;
      }
    });
    statusNumber.greaterNinety = greaterNinety;
    statusNumber.lessThirty = lessThirty;
    statusNumber.greaterThirty = greaterThirty;
    statusNumber.greaterSixty = greaterSixty;
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getIndexPage(queryParams)
    list.value = data.list
    var tableIndex = 0;    
    var lessThirty = 0;
    var greaterThirty = 0;
    var greaterSixty = 0;
    var greaterNinety = 0;
    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj.aloadRate != null){
        obj.aloadRate = obj.aloadRate * 100;
        obj.aloadRate = obj.aloadRate?.toFixed(0);
      }
      if(obj.bloadRate != null){
        obj.bloadRate = obj.bloadRate * 100;
        obj.bloadRate = obj.bloadRate?.toFixed(0);
      }
      if(obj.cloadRate != null){
        obj.cloadRate = obj.cloadRate * 100;
        obj.cloadRate = obj.cloadRate?.toFixed(0);
      }
      // obj.apparentPow = obj.apparentPow.toFixed(3);
      // obj.pow = obj.pow.toFixed(3);
      // obj.ele = obj.ele.toFixed(1);
      // obj.pf = obj.pf.toFixed(2);
      // obj.acur = obj.acur?.toFixed(2);
      // obj.bcur = obj.bcur?.toFixed(2);
      // obj.ccur = obj.ccur?.toFixed(2);
      // obj.curUnbalance = obj.curUnbalance?.toFixed(0);
      // obj.avol = obj.avol?.toFixed(1);
      // obj.bvol = obj.bvol?.toFixed(1);
      // obj.cvol = obj.cvol?.toFixed(1);
      // obj.volUnbalance = obj.volUnbalance?.toFixed(0);
      if(obj.color == 4){
        greaterNinety++;
      } else if (obj.color == 1) {
        lessThirty++;
      } else if (obj.color == 2) {
        greaterThirty++;
      } else if (obj.color == 3) {
        greaterSixty++;
      }
    });
    statusNumber.greaterNinety = greaterNinety;
    statusNumber.lessThirty = lessThirty;
    statusNumber.greaterThirty = greaterThirty;
    statusNumber.greaterSixty = greaterSixty;
    total.value = data.total
  } catch (error) {
    
  }
}

const getNavList = async() => {
  const res = await IndexApi.getBoxMenu()
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

const openForm = (type: string) => {
  curBalanceColorForm.value.open(type)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await IndexApi.deleteIndex(id)
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
    const data = await IndexApi.exportIndex(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
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

.btn_normal,
.btn_empty,
.btn_warn,
.btn_error,
.btn_unbound,
.btn_offline {
  // width: 55px;
  // height: 32px;
  padding: 3px 8px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  &:hover {
    color: #7bc25a;
  }
}
.btn_normal {
  border: 1px solid #3bbb00;
  background-color: #fff;
}
.normal {
  background-color: #3bbb00;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_empty {
  border: 1px solid #aaa;
  background-color: #fff;
}
.empty {
  background-color: #aaa;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_warn {
  border: 1px solid #3b8bf5;
  background-color: #fff;
}
.warn {
  background-color: #3b8bf5;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_error {
  border: 1px solid #ffc402;
  background-color: #fff;
}
.error {
  background-color: #ffc402;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_unbound {
  border: 1px solid #fa3333;
  background-color: #fff;
}
.unbound {
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
        .blue {
          background-color: #3b8bf5;
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
