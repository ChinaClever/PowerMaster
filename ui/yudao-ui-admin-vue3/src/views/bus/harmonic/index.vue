<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="谐波监测">
    <template #NavInfo>
      <div>
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Bus.png" /></div>
        </div> -->
        <div class="status" style="margin-top:20px;">
          <div class="box">
            <div class="top">
              <div class="tag"></div>正常
            </div>
            <div class="value"><span class="number">{{ statusNumber.normal }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>离线
            </div>
            <div class="value"><span class="number">{{ statusNumber.offline }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>告警
            </div>
            <div class="value"><span class="number">{{ statusNumber.alarm }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <!--<div class="tag error"></div>-->总共
            </div>
            <div class="value"><span class="number">{{ busTotal }}</span>个</div>
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
      <el-form-item >
          <button style="height:34px;" :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">
            全部
          </button>
          <template v-for="(status, index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
            <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
          </template>
        </el-form-item>
        <!-- <el-form-item >
          <el-checkbox-group  v-model="queryParams.status" @change="handleQuery">
            <el-checkbox :label="5" :value="5">在线</el-checkbox>
          </el-checkbox-group>
        </el-form-item> -->
        <el-form-item label="网络地址" prop="devKey">
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        <el-form-item style="margin-left: 10px">
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
        </el-form-item>
        <div style="float:right">
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电流谐波</el-button>
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;switchValue = 1;" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电压谐波</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />谐波含量</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <div v-if="switchValue !== 0 && switchValue !== 1 && list.length > 0">
        <el-table v-show="switchValue == 3" v-loading="loading" style="height:720px;margin-top:-10px;overflow-y: auto;" :data="list" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" width="218px">
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.location ? scope.row.location : '未绑定' }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="设备名称" align="center" prop="busName"/>
        <el-table-column label="网络地址" align="center" prop="devKey" width="150px" :class-name="ip"/>     
        <el-table-column v-if="valueMode == 0" label="Ia" align="center" prop="acurThd" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.acurThd != null">
              {{ scope.row.acurThd }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="Ib" align="center" prop="bcurThd" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bcurThd != null">
              {{ scope.row.bcurThd }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="Ic" align="center" prop="ccurThd" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.ccurThd != null">
              {{ scope.row.ccurThd }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="Ua" align="center" prop="avolThd" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.avolThd != null">
              {{ scope.row.avolThd }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="Ub" align="center" prop="bvolThd" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bvolThd != null">
              {{ scope.row.bvolThd }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="Uc" align="center" prop="cvolThd" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cvolThd != null">
              {{ scope.row.cvolThd }}
            </el-text>
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="130px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDetail(scope.row)"
              v-if="scope.row.status != null && scope.row.status != 0"
              style="background-color:#409EFF;color:#fff;border:none;width:100px;height:30px;"
            >
            设备详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.busId)"
              v-if="scope.row.status == 0"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>    

      <div v-if="switchValue == 0  && list.length > 0" class="arrayContainer">
        <template v-for="item in list" :key="item.devKey">
          <div v-if="item.devKey !== null" class="arrayItem">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content" style="padding-left: 50px;">
            <div class="info" >                  
              <div  v-if="item.acurThd != null">Ia:{{item.acurThd}}</div>
              <div  v-if="item.bcurThd != null">Ib:{{item.bcurThd}}</div>
              <div  v-if="item.ccurThd != null">Ic:{{item.ccurThd}}</div>
            </div> 
            <div class="icon">
              <div v-if="item.acurThd != null">
                <br/>
                电流谐波
              </div> 
            </div>
                     
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <!-- <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.atemStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.atemStatus != 0 || item.btemStatus != 0  || item.ctemStatus != 0 " >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </div> -->
          <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.status == 0 " >离线</el-tag>
            <el-tag v-else-if="item.status === 1" type="success">正常</el-tag>
          </div>          
          <button class="detail" @click="toDetail(item)" v-if="item.status != null && item.status != 0">详情</button>
        </div>
        </template>
      </div>

      <el-dialog v-model="dialogVisibleCur" @close="handleClose">
        <!-- 自定义的头部内容（可选） -->
        <template #header>
          <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="120px"
      >      
        <el-form-item>  
          <span>所处位置：</span>
          <el-tag size="large">{{ adder }}</el-tag>
          <span>设备名称：</span>
          <el-tag size="large" style="margin-right:11vw">{{ location }}</el-tag>
          <el-select
            v-model="queryParams.harmonicType"
            placeholder="请选择"
            style="width: 240px"
          >
            <el-option label="A相电压谐波" :value = 0 />
            <el-option label="B相电压谐波" :value = 1 />
            <el-option label="C相电压谐波" :value = 2 />
            <el-option label="A相电流谐波" :value = 3 />
            <el-option label="B相电流谐波" :value = 4 />
            <el-option label="C相电流谐波" :value = 5 />
          </el-select>

          <el-select
            v-model="queryParams.harmonicArr"
            multiple
            placeholder="Select"
            collapse-tags
            collapse-tags-tooltip
            style="width: 240px"
          >
          <el-option label='全选' value='全选' @click='selectAll' />
          <el-option
            v-for="item in harmonicMultiple"
            :key="item.value"
            :label="item.label"
            :value="item.value"
            @click="getLabel(item.value)"
          />
          </el-select>

          <el-button 
            @click="subtractOneDay();handleDayPick()" 
          >
            &lt;
          </el-button>
          <el-date-picker
            v-model="queryParams.oldTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="date"
            :disabled-date="disabledDate"
            @change="handleDayPick"
            class="!w-160px"
          />
          <el-button 
            @click="addtractOneDay();handleDayPick()" 
          >
            &gt;
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item>
        <!-- <el-text size="large">
          报警次数：{{ pduInfo.alarm }}
        </el-text> -->
      </el-form>
        </template>
        <!-- 自定义的主要内容 -->
        <div class="custom-content">
          
        </div>
      </el-dialog>

      <div v-if="switchValue == 1  && list.length > 0" class="arrayContainer">
        <template v-for="item in list" :key="item.devKey">
          <div v-if="item.devKey !== null" class="arrayItem">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content" style="padding-left: 50px;">
            <div class="info" >                  
              <div  v-if="item.avolThd != null">Ua:{{item.avolThd}}</div>
              <div  v-if="item.bvolThd != null">Ub:{{item.bvolThd}}</div>
              <div  v-if="item.cvolThd != null">Uc:{{item.cvolThd}}</div>
            </div> 
            <div class="icon">
              <div v-if="item.avolThd != null">
                <br/>
                电压谐波
              </div> 
            </div>         
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <!-- <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.atemStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.atemStatus != 0 || item.btemStatus != 0  || item.ctemStatus != 0 " >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </div> -->
          <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.status === 0 " >离线</el-tag>
            <el-tag v-else-if="item.status === 1" type="success">正常</el-tag>
            <el-tag v-else-if="item.status === 2" type="danger">告警</el-tag>
          </div>          
          <button class="detail" @click="toDetail(item)" v-if="item.status != null && item.status != 0">详情</button>
        </div>
        </template>
      </div>
      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
      <template v-if="list.length == 0 && switchValue != null">
        <el-empty description="暂无数据" :image-size="595" />
      </template>
    </template>
  </CommonMenu>


  <!-- 表单弹窗：添加/修改 -->
  <!-- <CurbalanceColorForm ref="curBalanceColorForm" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { IndexApi } from '@/api/bus/busindex'
// import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'
// import { CurbalanceColorApi } from '@/api/pdu/curbalancecolor'

import HarmonicRealTime from '@/views/bus/harmonicdetail/component/HarmonicRealTime.vue'
import HarmonicLine from '@/views/bus/harmonicdetail/component/HarmonicLine.vue'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()
const dialogVisibleCur = ref(false) //全屏弹窗的显示隐藏
const dialogVisibleVol = ref(false) //全屏弹窗的显示隐藏
const curBalanceColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(0)
const valueMode = ref(0)
const devKeyList = ref([])

const butColor = ref(0);
const onclickColor = ref(-1);

const busTotal = ref(0)
const statusNumber = reactive({
  normal : 0,
  warn : 0,
  alarm : 0,
  offline : 0
})

const statusList = reactive([
  {
    name: '离线',
    selected: true,
    value: 0,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'
  },
  {
    name: '正常',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal'
  },
  {
    name: '告警',
    selected: true,
    value: 2,
    cssClass: 'btn_error',
    activeClass: 'btn_error error'
  }
])
const normalFlag = ref(true)
const reportFlag = ref(true)
const offlineFlag = ref(true)

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

//const handleClick = (row) => {
//  console.log("click",row)
//}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.busDevKeyList = null;
    getList();
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    if (item.type == 6) {
      ids.push(item.unique)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.busDevKeyList = [-1]
  }else{
    queryParams.busDevKeyList = ids
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
    apf : null,
    bpf : null,
    cpf : null,
    temUnbalance : null,
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
  status:undefined,
  busDevKeyList : [],
})as any
const queryParamsAll = reactive({
  pageNo: 1,
  pageSize: -1,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:undefined,
  cabinetIds : [],
  isDeleted: 0,
})as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

const allList = ref([
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

/** 查询列表 */
const getList = async () => {
  loading.value = true
  console.log(queryParams)
  try {
    const data = await IndexApi.getBusHarmonicPage(queryParams)
    list.value = data.list
    console.log('list.value',list.value)
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.acurThd == null){
        return;
      } 
      obj.acurThd = obj.acurThd?.toFixed(2);
      obj.bcurThd = obj.bcurThd?.toFixed(2);
      obj.ccurThd = obj.ccurThd?.toFixed(2);
      obj.avolThd = obj.avolThd?.toFixed(2);
      obj.bvolThd = obj.bvolThd?.toFixed(2);
      obj.cvolThd = obj.cvolThd?.toFixed(2);
    });

    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListAll = async () => {
  try {
    const data = await IndexApi.getBusHarmonicPage(queryParams)

    const allData = await IndexApi.getBusIndexStatistics()
    //设置左边数量
    if(allData) {
      statusNumber.normal = allData.normal;
      statusNumber.offline = allData.offline;
      statusNumber.alarm = allData.alarm;
      statusNumber.warn = allData.warn;
      busTotal.value = allData.total
    }
    
    list.value = data.list
    console.log('list.value',list.value)
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.acurThd == null){
        return;
      } 
      obj.acurThd = obj.acurThd?.toFixed(2);
      obj.bcurThd = obj.bcurThd?.toFixed(2);
      obj.ccurThd = obj.ccurThd?.toFixed(2);
      obj.avolThd = obj.avolThd?.toFixed(2);
      obj.bvolThd = obj.bvolThd?.toFixed(2);
      obj.cvolThd = obj.cvolThd?.toFixed(2);
    });

    list.value = data.list
  } catch (error) {
    
  }
}

const getNavList = async() => {
  const res = await IndexApi.getBusMenu()
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

const toDetail = (row) =>{
  const devKey = row.devKey;
  const busId = row.busId;
  const location = row.location;
  const busName = row.busName;
  const roomName = row.roomName;
  push({path: '/bus/busmonitor/busmonitor/busharmonicdetail', query: { devKey, busId , location , busName,roomName}});
}

const showDialogCur = () => {
  dialogVisibleCur.value = true;
}

const showDialogVol = () => {
  dialogVisibleVol.value = true;
}

// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }

const handleSelectStatus = (index) => {
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.status = [index];
  handleQuery();
}

const toggleAllStatus = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.status = [];
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
  butColor.value = 0;
  //statusList.forEach((item) => item.selected = true)
  queryParams.status = [];
  onclickColor.value = -1;
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
  getListAll();

  flashListTimer.value = setInterval((getListAll), 5000);
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
    flashListTimer.value = setInterval((getListAll), 5000);
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

@media screen and (min-width:2048px){
  .arrayContainer {
   width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
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
      padding-left: 50px;
      display: flex;
      align-items: center;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 74px;
        height: 60px;
        margin: 0 28px;
        text-align: center;
        font-size: large;
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
}

@media screen and (max-width:2048px) and (min-width:1600px){
.arrayContainer {
   width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
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
      padding-left: 20px;
      display: flex;
      align-items: center;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 74px;
        height: 60px;
        margin: 0 28px;
        text-align: center;
        font-size: large;
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
}

@media screen and (max-width:1600px){
  .arrayContainer {
   width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
  .arrayItem {
    width: 33%;
    height: 140px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      padding-left: 20px;
      display: flex;
      align-items: center;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 74px;
        height: 60px;
        margin: 0 28px;
        text-align: center;
        font-size: large;
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
}

.btnallSelected {
  margin-right: 10px;
  width: 58px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 5px;
}

.btnallNotSelected{
  margin-right: 10px;
  width: 58px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: #000000;
  border: 1px solid #409EFF;
  border-radius: 5px;
  &:hover {
    color: #7bc25a;
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

::v-deep .el-table .el-table__header th{
  background-color: #f5f7fa;
  color: #909399;
  height: 80px;

}

:deep(.el-dialog){
  margin: 0;
  width: 100%;
  height: 100%;
}

:deep(.el-card){
  --el-card-padding:5px;
}

:deep(.el-tag){
  margin-right:-60px;
}
</style>
