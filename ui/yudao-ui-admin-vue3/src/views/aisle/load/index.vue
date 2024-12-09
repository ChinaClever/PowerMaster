<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="柜列负荷">
    <template #NavInfo>
      <div>
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/aisle.png" /></div>
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
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>60%-90%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterSixty}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&gt;90%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterNinety}}</span>个</div>
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
        <!-- <el-form-item v-if="switchValue == 2 || switchValue == 3">
          <template v-for="(status, index) in statusList" :key="index">
            <button :class="status.selected ? status.activeClass : status.cssClass" @click.prevent="handleSelectStatus(index)">{{status.name}}</button>
          </template>
        </el-form-item> -->
        <!-- <el-button
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 平衡度范围颜色
        </el-button> -->
        <!-- <el-form-item >
          <el-checkbox-group  v-model="queryParams.status">
            <el-checkbox :label="5" :value="5">在线</el-checkbox>
          </el-checkbox-group>
        </el-form-item> -->
        <el-form-item label="设备Id" prop="devKey">
          <el-autocomplete
            v-model="queryParams.id"
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
          <el-button @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />A路</el-button>                      
          <el-button @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />B路</el-button>                      
          <el-button @click="pageSizeArr=[24,36,48];queryParams.pageSize = 24;switchValue = 2;" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>                      
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <el-table v-show="switchValue == 3 && valueMode == 0" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetailA" >
        <el-table-column label="编号" align="center" prop="tableId" />
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="运行状态" align="center" prop="colorA" >
          <template #default="scope" >
            <el-tag type="info"  v-if="scope.row.status == 5">离线</el-tag>
            <el-tag type="info"  v-if="scope.row.colorA == 0">空载</el-tag>
            <el-tag type="success"  v-if="scope.row.colorA == 1">&lt;30%</el-tag>
            <el-tag type="primary"  v-if="scope.row.colorA == 2">30%-60%</el-tag>
            <el-tag type="warning" v-if="scope.row.colorA == 3">60%-90%</el-tag>
            <el-tag type="danger" v-if="scope.row.colorA == 4">&gt;90%</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="A相负载率" align="center" prop="aloadRateA" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.aloadRateA != null">
              {{ scope.row.aloadRateA }}%
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="B相负载率" align="center" prop="bloadRateA" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bloadRateA != null">
              {{ scope.row.bloadRateA }}%
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="C相负载率" align="center" prop="cloadRateA" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cloadRateA != null">
              {{ scope.row.cloadRateA }}%
            </el-text>
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDetailA(scope.row)"
              v-if="scope.row.colorA != null "
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

      <el-table v-show="switchValue == 3 && valueMode == 1" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetailA" >
        <el-table-column label="编号" align="center" prop="tableId" />
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="运行状态" align="center" prop="colorB" >
          <template #default="scope" >
            <el-tag type="info"  v-if="scope.row.status == 5">离线</el-tag>
            <el-tag type="info"  v-if="scope.row.colorB == 0">空载</el-tag>
            <el-tag type="success"  v-if="scope.row.colorB == 1">&lt;30%</el-tag>
            <el-tag type="primary"  v-if="scope.row.colorB == 2">30%-60%</el-tag>
            <el-tag type="warning" v-if="scope.row.colorB == 3">60%-90%</el-tag>
            <el-tag type="danger" v-if="scope.row.colorB == 4">&gt;90%</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="A相负载率" align="center" prop="aloadRateB" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.aloadRateB != null">
              {{ scope.row.aloadRateB }}%
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="B相负载率" align="center" prop="bloadRateB" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bloadRateB != null">
              {{ scope.row.bloadRateB }}%
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="C相负载率" align="center" prop="cloadRateB" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cloadRateB != null">
              {{ scope.row.cloadRateB }}%
            </el-text>
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDetailB(scope.row)"
              v-if="scope.row.colorB != null "
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

      <div v-show="switchValue == 2 && valueMode == 0  && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="icon" v-if="item.colorA != null" >
              A路负载率            
            </div>
            <div class="info">                  
              <div v-if="item.aloadRateA != null">A相：{{item.aloadRateA}}%</div>
              <div v-if="item.bloadRateA != null">B相：{{item.bloadRateA}}%</div>
              <div v-if="item.cloadRateA != null">C相：{{item.cloadRateA}}%</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" >
            <el-tag type="info"  v-if="item.colorA == 0">空载</el-tag>
            <el-tag type="info"  v-if="item.status == 5">离线</el-tag>
            <el-tag type="success"  v-if="item.colorA == 1">&lt;30%</el-tag>
            <el-tag type="primary"  v-if="item.colorA == 2">30%-60%</el-tag>
            <el-tag type="warning" v-if="item.colorA == 3">60%-90%</el-tag>
            <el-tag type="danger" v-if="item.colorA == 4">&gt;90%</el-tag>
          </div>
          <button class="detail" @click="toDetailA(item)" v-if="item.colorA != null " >详情</button>
        </div>
      </div>

      <div v-show="switchValue == 2 && valueMode == 1  && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="icon" v-if="item.colorB != null" >
              B路负载率            
            </div>
            <div class="info">                  
              <div v-if="item.aloadRateB != null">A相：{{item.aloadRateB}}%</div>
              <div v-if="item.bloadRateB != null">B相：{{item.bloadRateB}}%</div>
              <div v-if="item.cloadRateB != null">C相：{{item.cloadRateB}}%</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" >
            <el-tag type="info"  v-if="item.colorA == 0">空载</el-tag>
            <el-tag type="info"  v-if="item.status == 5">离线</el-tag>
            <el-tag type="success"  v-if="item.colorA == 1">&lt;30%</el-tag>
            <el-tag type="primary"  v-if="item.colorA == 2">30%-60%</el-tag>
            <el-tag type="warning" v-if="item.colorA == 3">60%-90%</el-tag>
            <el-tag type="danger" v-if="item.colorA == 4">&gt;90%</el-tag>
          </div>
          <button class="detail" @click="toDetailA(item)" v-if="item.colorB != null " >详情</button>
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
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { IndexApi as BusIndexApi } from '@/api/bus/busindex'
import { IndexApi } from '@/api/aisle/aisleindex'
// import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'



/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()

const valueMode = ref(0)
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

// const statusList = reactive([
//   {
//     name: '空载',
//     selected: true,
//     value: 0,
//     cssClass: 'btn_empty',
//     activeClass: 'btn_empty empty',
//     color: '#aaa'
//   },
//   {
//     name: '负载量<30%',
//     selected: true,
//     value: 1,
//     cssClass: 'btn_normal',
//     activeClass: 'btn_normal normal',
//     color: '#3bbb00'
//   },
//   {
//     name: '30%≤负载量<60%',
//     selected: true,
//     value: 2,
//     cssClass: 'btn_warn',
//     activeClass: 'btn_warn warn',
//     color: '#ffc402'
//   },
//   {
//     name: '60%≤负载量<90%',
//     selected: true,
//     value: 3,
//     cssClass: 'btn_error',
//     activeClass: 'btn_error error',
//     color: '#fa3333'
//   },
//   {
//     name: '负载量>90%',
//     selected: true,
//     value: 4,
//     cssClass: 'btn_unbound',
//     activeClass: 'btn_unbound unbound',
//     color: '#05ebfc'
//   },
// ])

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
    queryParams.aisleIds = null;
    getList();
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    if (item.type == 2) {
      ids.push(item.id)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.aisleIds = [-1]
  }else{
    queryParams.aisleIds = ids
  }
  queryParams.id = ""
  // if(haveCabinet ){
  //   queryParams.aisleIds = ids
  //   getList();
  // }

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
  status:undefined,
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
      

      if(obj.colorA == 4){
        greaterNinety++;
      } else if (obj.colorA == 1) {
        lessThirty++;
      } else if (obj.colorA == 2) {
        greaterThirty++;
      } else if (obj.colorA == 3) {
        greaterSixty++;
      }
      if(obj.colorB == 4){
        greaterNinety++;
      } else if (obj.colorB == 1) {
        lessThirty++;
      } else if (obj.colorB == 2) {
        greaterThirty++;
      } else if (obj.colorB == 3) {
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
      if(obj.aloadRateA != null){
        obj.aloadRateA = obj.aloadRateA * 100;
        obj.aloadRateA = obj.aloadRateA?.toFixed(0);
      }
      if(obj.bloadRateA != null){
        obj.bloadRateA = obj.bloadRateA * 100;
        obj.bloadRateA = obj.bloadRateA?.toFixed(0);
      }
      if(obj.cloadRateA != null){
        obj.cloadRateA = obj.cloadRateA * 100;
        obj.cloadRateA = obj.cloadRateA?.toFixed(0);
      }

      if(obj.aloadRateB != null){
        obj.aloadRateB = obj.aloadRateB * 100;
        obj.aloadRateB = obj.aloadRateB?.toFixed(0);
      }
      if(obj.bloadRateB != null){
        obj.bloadRateB = obj.bloadRateB * 100;
        obj.bloadRateB = obj.bloadRateB?.toFixed(0);
      }
      if(obj.cloadRateB != null){
        obj.cloadRateB = obj.cloadRateB * 100;
        obj.cloadRateB = obj.cloadRateB?.toFixed(0);
      }

      if(obj.colorA == 4){
        greaterNinety++;
      } else if (obj.colorA == 1) {
        lessThirty++;
      } else if (obj.colorA == 2) {
        greaterThirty++;
      } else if (obj.colorA == 3) {
        greaterSixty++;
      }
      if(obj.colorB == 4){
        greaterNinety++;
      } else if (obj.colorB == 1) {
        lessThirty++;
      } else if (obj.colorB == 2) {
        greaterThirty++;
      } else if (obj.colorB == 3) {
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
  const res = await IndexApi.getAisleMenu()
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

const toDetailA = async (row) =>{
  const devKey = row.devKeyA;
  const busId = await BusIndexApi.getBusIdByDevKey({devKey : devKey})
  const location = row.location != null ? row.location + '-A路' : devKey + '-A路';
  push({path: '/bus/busmonitor/powerLoadDetail', state: { devKey, busId ,location }})
}

const toDetailB = async (row) =>{
  const devKey = row.devKeyB;
  const busId = row.busId
  const location = row.location != null ? row.location + '-B路': devKey + '-B路';
  push({path: '/bus/busmonitor/powerLoadDetail', state: { devKey, busId ,location }})
}



// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }

// const handleSelectStatus = (index) => {
//   statusList[index].selected = !statusList[index].selected
//   const status =  statusList.filter(item => item.selected)
//   const statusArr = status.map(item => item.value)
//   queryParams.color = statusArr;
//   handleQuery();
// }

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  // statusList.forEach((item) => item.selected = true)
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
          font-size: large;
          font-weight: bold;
          margin-right: auto;
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
        width: 98px;
        height: 30px;
        margin: 0 28px;
        font-size: medium;
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
