<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="插接箱谐波">
    <template #NavInfo>
      <div>
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
        </div> -->
        <div class="status" style="margin-top:20px">
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
            <div class="value"><span class="number">{{ statusNumber.total }}</span>个</div>
          </div>
        </div>
        <div class="line"></div>
        <!-- <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>{{ statusList[0].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.lessFifteen}}</span>个</div>
          </div>         
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>{{ statusList[1].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>{{ statusList[2].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterTwenty}}</span>个</div>
          </div>
        </div> -->
        <!-- <div class="line"></div> -->
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
      <el-form-item v-if="switchValue == 0 ">
        <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">
          全部
        </button>
        <template v-for="(status, index) in statusList" :key="index">
          <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
          <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
        </template>
        <!-- <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-if="switchValue == 0 "
          style="height:35px;"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 谐波颜色范围
        </el-button> -->
        </el-form-item>

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
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电流谐波</el-button>          
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />谐波含量</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <el-table v-show="switchValue == 3" v-loading="loading" style="height:720px;margin-top:-10px;" :data="list" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="设备名称" align="center" prop="boxName" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip"/>
        <el-table-column v-if="valueMode == 0" label="Ia" align="center" prop="acurThd" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.acurThd != null">
              {{ scope.row.acurThd }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="Ib" align="center" prop="bcurThd" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bcurThd != null">
              {{ scope.row.bcurThd }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="Ic" align="center" prop="ccurThd" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.ccurThd != null">
              {{ scope.row.ccurThd }}
            </el-text>
          </template>
        </el-table-column>

        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDetail(scope.row)"
              v-if="scope.row.status != null && scope.row.status != 5"
              style="background-color:#409EFF;color:#fff;border:none;width:100px;height:30px;"
            >
            设备详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.boxId)"
              v-if="scope.row.status == 5"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-show="switchValue == 0 && list.length > 0" class="arrayContainer">
        <template v-for="item in list" :key="item.devKey">
          <div v-if="item.devKey !== null" class="arrayItem">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey +'-' +item.boxName }}</div>
          <div class="content">
            <div class="icon">
              <div v-if="item.acurThd != null">
                <br/>
                电流谐波
              </div> 
            </div>
            <div class="info" >                  
              <div v-if="item.acurThd != null">Ia:{{item.acurThd}}</div>
              <div v-if="item.bcurThd != null">Ib:{{item.bcurThd}}</div>
              <div v-if="item.ccurThd != null">Ic:{{item.ccurThd}}</div>
            </div>          
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <!-- <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.atemStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.atemStatus != 0 || item.btemStatus != 0  || item.ctemStatus != 0 " >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </div> -->
          <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.status == 0 " >{{statusList[0].name}}</el-tag>
            <el-tag v-else-if="item.status === 1" type="success" >{{statusList[1].name}}</el-tag>
            <el-tag v-else-if="item.status === 2" type="success" >{{statusList[2].name}}</el-tag>
          </div>          
          <button class="detail" @click="toDetail(item)" v-if="item.status != null && item.status != 0" >详情</button>
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
      <template v-if="list.length == 0 && switchValue != 3">
        <el-empty description="暂无数据" :image-size="595" />
      </template>
    </template>
  </CommonMenu>


  <!-- 表单弹窗：添加/修改 -->
  <HarmonicColorForm ref="harmonicColorForm" @success="getList" />
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { IndexApi } from '@/api/bus/boxindex'
import { ElTree } from 'element-plus'
import HarmonicColorForm from './HarmonicColorForm.vue'
import { BoxHarmonicColorApi } from '@/api/bus/boxharmoniccolor'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()

const butColor = ref(0);
const onclickColor = ref(-1);

const harmonicColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(0)
const valueMode = ref(0)

const allData = ref();

const statusNumber = reactive({
  normal : 0,
  alarm : 0,
  offline : 0,
  total : 0
});

const queryParamsAll = reactive({
  pageNo: 1,
  pageSize: -1,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds : [],
})as any;

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
]) as any;// 列表的数据

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
    queryParams.boxDevKeyList = null;
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
  status:[],
  cabinetIds : [],
})as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getBoxHarmonicPage(queryParams)
    list.value = data.list
    console.log('data',data);

    //获取颜色范围
    //var range = await BoxHarmonicColorApi.getBoxHarmonicColor();
    //if(range != null){
    //  statusList[0].name = '<' + range.rangeOne + '%';
    //  statusList[1].name = range.rangeTwo + '%-' +  range.rangeThree + "%";
    //  statusList[2].name = '>' + range.rangeFour + '%';
    //}

    var tableIndex = 0;


    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.acurThd == null){
        return;
      } 
      obj.acurThd = obj.acurThd?.toFixed(2);
      obj.bcurThd = obj.bcurThd?.toFixed(2);
      obj.ccurThd = obj.ccurThd?.toFixed(2);

    });

    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListAll = async () => {
  try {
    var normal = 0;
    var offline = 0;
    var alarm = 0;

    const allData = await IndexApi.getBoxRedisPage(queryParamsAll);
    allList.value = allData.list
    allList.value.forEach((objAll) => {
      if(objAll?.dataUpdateTime == null && objAll?.phaseCur == null){
        objAll.status = 0;
        offline++;
        return;
      }
      if(objAll?.status == 1){
        normal++;
      } else if (objAll?.status == 2){
        alarm++;
      }
    });
    //设置左边数量
    statusNumber.normal = normal;
    statusNumber.offline = offline;
    statusNumber.alarm = alarm;
    statusNumber.total = allData.total;
  } catch (error) {
    
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getBoxHarmonicPage(queryParams)
    list.value = data.list

    var tableIndex = 0;    

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.acurThd == null){
        return;
      } 
      obj.acurThd = obj.acurThd?.toFixed(2);
      obj.bcurThd = obj.bcurThd?.toFixed(2);
      obj.ccurThd = obj.ccurThd?.toFixed(2);

    });

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

const toDetail = (row) =>{
  const devKey = row.devKey;
  const boxId = row.boxId
  const location = row.location ? row.location : devKey;
  const name = row.boxName
  push({path: '/bus/boxmonitor/boxharmonicdetail', state: { devKey, boxId ,location ,name}})
}

// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }

//const handleSelectStatus = (index) => {
//  statusList[index].selected = !statusList[index].selected
//  const status =  statusList.filter(item => item.selected)
//  const statusArr = status.map(item => item.value)
//  if(statusArr.length != statusList.length){
//    queryParams.color = statusArr;
//    //queryParams.status = [5];
//  }else{
//    queryParams.color = null;
//    //queryParams.status = [];
//  }
//  
//  handleQuery();
//}

const handleSelectStatus = (index) => {
  console.log('index',index);
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.status = [index];
  handleQuery();
}

const toggleAllStatus = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.status = [0,1,2];
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
  //statusList.forEach((item) => item.selected = true)
  butColor.value = 0;
  queryParams.color = [];
  onclickColor.value = -1;
  handleQuery()
}

/** 添加/修改操作 */

const openForm = (type: string) => {
  harmonicColorForm.value.open(type)
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
  getList();
  getNavList();
  getListAll();
  flashListTimer.value = setInterval((getListNoLoading), 5000);
  flashListTimer.value = setInterval((getListAll), 5000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value);
    flashListTimer.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value);
    flashListTimer.value = null;
    firstTimerCreate.value = false;
  }
})

onActivated(() => {
  getList();
  getNavList();
  if(!firstTimerCreate.value){
    flashListTimer.value = setInterval((getListNoLoading), 5000);
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
  height: 35px;
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
  height: 35px;
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

:deep(.el-card){
  --el-card-padding:5px;
}

:deep(.el-tag){
  margin-right:-40px;
}
</style>
