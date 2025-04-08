<template>
  <CommonMenu :dataList="roomListInTree" @check="handleCheck"  navTitle="机房用能">
    <template #NavInfo>
      <div class="navInfo">
        <div class="header">
          <div>用能最大柜列</div>
          <div>昨日：{{ yesterdayMaxEq==null?"无数据":yesterdayMaxEq }}</div>
          <div>上周：{{ lastWeekMaxEq==null?"无数据":lastWeekMaxEq }}</div>
          <div>上月：{{ lastMonthMaxEq==null?"无数据":lastMonthMaxEq }}</div>
        </div>
      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="用能排序"  label-width="100px" style="margin-left: -30px;">
            <el-button @click="changeTimeGranularity('yesterday');timeButton=1" :type="timeButton==1?'primary':''"
            >
              昨日
            </el-button>
            <el-button @click="changeTimeGranularity('lastWeek');timeButton=2" :type="timeButton==2?'primary':''"
            >
              上周
            </el-button>
            <el-button @click="changeTimeGranularity('lastMonth');timeButton=3" :type="timeButton==3?'primary':''"
            >
              上月
            </el-button>
        </el-form-item>
        <div style="margin-left: 25px;">
          <el-form-item label="机房名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入机房名称"
              clearable
              class="!w-160px"
              height="35"
              @keydown.enter.prevent="getTableData(true)"
            />
          </el-form-item>
          <el-form-item>
            <el-button style="margin-left: 12px" @click="getTableData(true)" ><Icon icon="ep:search" />搜索</el-button>
            <el-button @click="resetSearch()" style="width:70px;" ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <el-button @click="handleSwitchModal(0)" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />阵列模式</el-button>
          <el-button @click="handleSwitchModal(1)" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div class="dataShow" v-show="tableData.length>0">
          <el-table v-show="switchValue == 1" :data="tableData" stripe :border="true" :header-cell-style="headerCellStyle">
            <el-table-column label="序号" width="100px" align="center">
              <template #default="scope">
                {{ scope.$index + (queryParams.pageNo - 1) * queryParams.pageSize + 1 }}
              </template>
            </el-table-column>
            <el-table-column prop="location" label="位置" align="center" />
            <el-table-column prop="yesterdayEq" label="昨日用能(kW·h)" align="center" />
            <el-table-column prop="lastWeekEq" label="上周用能(kW·h)" align="center" />
            <el-table-column prop="lastMonthEq" label="上月用能(kW·h)" align="center" />
            <el-table-column label="详情" align="center" width="100px">
              <template #default="s">
                <el-button type="primary" @click="toDetail(s.row.roomId,s.row.id,s.row.location)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-show="switchValue == 0 && tableData?.length" class="matrixContainer">
          <div class="item" v-for="item in tableData" :key="item.key">
            <div class="content">
              <div class="info">
                <div>昨日用能：{{item.yesterdayEq}}kW·h</div>
                <div>上周用能：{{item.lastWeekEq}}kW·h</div>
                <div>上月用能：{{item.lastMonthEq}}kW·h</div>
              </div>
              <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
            </div>
            <div class="room">{{item.location}}</div>
            <button class="detail" @click.prevent="toDetail(item.roomId, item.id,item.location)" >详情</button>
          </div>
        </div>
        </div>
        <Pagination
          :total="pageTotal"
          v-model:current-page="queryParams.pageNo"
          :page-size="queryParams.pageSize"
          :page-sizes="pageSizes"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
        <template v-if="tableData.length == 0 && switchValue == 0">
          <el-empty description="暂无数据" :image-size="300" />
        </template>
      </div>
    </template>
  </CommonMenu>
</template>

<script lang="ts" setup>
import { IndexApi } from '@/api/room/roomindex'
import { time } from 'console'
import { id } from 'element-plus/es/locale'

const { push } = useRouter() // 路由跳转
const yesterdayMaxEq = ref<string | null>(null);
const lastWeekMaxEq = ref<string | null>(null);
const lastMonthMaxEq = ref<string | null>(null);
const tableLoading = ref(false) // 
const isFirst = ref(true) // 是否第一次调用getTableData函数
const navList = ref([]) // 左侧导航栏树结构列表
const tableData = ref([])as any
const switchValue = ref(0) // 表格(1) 矩阵(0)切换
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
}) as any
const roomListInTree=ref([])
const pageTotal=ref(0)
const pageSizes=ref([24,36,48])
const timeButton=ref(0)
// 获取表格数据
const getTableData = async(reset = false) => {
  tableLoading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await IndexApi.getEqPage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      cabinetIds: isFirst.value ? null : cabinetIds.value,
      // roomId: null,
      roomIds : queryParams.roomIds,
      runStatus: [],
      pduBox: 0,
      name: queryParams.name,
      timeGranularity:queryParams.timeGranularity
    })
    if (res.list) {
      tableData.value = res.list.map(item => {
        return {
          id: item.id,
          location: item.location ? item.location : item.devKey ,
          local : item.location,
          yesterdayEq: item.yesterdayEq ? item.yesterdayEq.toFixed(1) : '0.0',
          lastWeekEq: item.lastWeekEq ? item.lastWeekEq.toFixed(1) : '0.0',
          lastMonthEq: item.lastMonthEq ? item.lastMonthEq.toFixed(1) : '0.0',
          status : item.runStatus
        }
      })
      pageTotal.value = res.total
    }
  } finally {
    tableLoading.value = false
  }
}

// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  switchValue.value = value
  if (value == 0) { // 阵列
    queryParams.pageSize = 24
    pageSizes.value = [24,36,48]
  } else {
    queryParams.pageSize = 15
    pageSizes.value = [15,25,30,50,100]
  }
  getTableData(true)
}

// 处理左侧树导航选择事件
const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.roomIds = null;
    getTableData(true)
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    ids.push(item.id)
    haveCabinet = true;
  })
  if(!haveCabinet ){
    queryParams.roomIds = [-1]
  }else{
    queryParams.roomIds = ids
  }
  console.log("ids",ids);
  console.log("row",row)
  getTableData(true)
}

// 跳转详情
const toDetail = (roomId, id,location) => {
  push({path: '/room/roommonitor/roomenergydetail', state: { roomId, id,location }})
}
const getMaxData = async() => {
  let maxEq = await IndexApi.getMaxEq();
  maxEq.forEach((item)=>{
    if(item.type==0){
      yesterdayMaxEq.value =item.roomName;
    }else if(item.type==1){
      lastWeekMaxEq.value =item.roomName;
    }else if(item.type==2){
      lastMonthMaxEq.value =item.roomName;
    }
  })
}

onBeforeMount(() => {
  getMaxData();
  getTableData()
})

function headerCellStyle() {
  return {
    backgroundColor: '#eee', // 表头背景颜色
  };
}

async function handleNavTree(){
  const list=await IndexApi.getRoomList();
  console.log("list",list);
  roomListInTree.value=list.map((item)=>{return {id:item.id,name:item.roomName,children:[]}})
}

function handleSizeChange(val){
  queryParams.pageSize = val
  getTableData(true)
}

function handleCurrentChange(val){
  queryParams.pageNo = val
  getTableData(false)
}

function resetSearch(){
  timeButton.value=0;
  queryParams.timeGranularity=null;
  queryParams.name=null;
  getTableData(true);
}

function changeTimeGranularity(timeGranularity){
  if(queryParams.timeGranularity == timeGranularity) return;
  if(queryParams.timeGranularity == "yesterday"){
    timeButton.value=1;
  }else if(queryParams.timeGranularity == "lastWeek"){
    timeButton.value=2;
  }else if(queryParams.timeGranularity == "lastMonth"){
    timeButton.value=3;
  }
  queryParams.timeGranularity=timeGranularity;
  getTableData(true);
}
handleNavTree();
</script>

<style lang="scss" scoped>
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
.navInfo {
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
    margin-top: 5px;
    margin-left: 30px;
    display: inline-block;
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
.matrixContainer {
  height: calc(100vh - 320px);
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .item {
    width: 25%;
    min-width: 275px;
    height: 130px;
    font-size: 12px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 36px;
    position: relative;
    .content {
      padding-left: 20px;
      display: flex;
      align-items: center;
      .count_img {
        // margin: 0 35px 0 13px;
        position: absolute;
        right: 80px;
      }
      .info {
        line-height: 1.7;
        font-size: 13px;
      }
    }
    .room {
      position: absolute;
      left: 10px;
      top: 8px;
      font-size: 13px;
    }
    .detail {
      width: 35px;
      height: 20px;
      cursor: pointer;
      font-size: 12px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 5px;
      bottom: 5px;
    }
  }
}
.dataShow{
  display: inline-block;
  width: 100%;
  height: calc(100vh - 220px);
}

</style>