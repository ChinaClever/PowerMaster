<template>
  <CommonMenu :dataList="navList" @check="handleCheck"  navTitle="柜列用能">
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
        <div style="margin-left: 50px;">
          <el-form-item label="柜列名称" prop="username">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入柜列名称"
              clearable
              class="!w-160px"
              height="35"
            />
          </el-form-item>
          <el-form-item>
            <el-button style="margin-left: 12px" @click="getTableData(true)" ><Icon icon="ep:search" />搜索</el-button>
            <el-button @click="resetSearch();timeButton=0" style="width:70px;" ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
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
        <div v-if="switchValue == 0 && tableData.length > 0" class="matrixContainer">
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
        <el-table v-if="switchValue == 1" style="width: 100%;height: calc(100vh - 215px);" :data="tableData" :border="true" :stripe="true" :header-cell-style="headerCellStyle">
          <el-table-column width="75" label="序号" align="center">
            <template #default="scope">
              {{ (queryParams.pageNo - 1) * queryParams.pageSize + scope.$index + 1 }}
            </template>  
          </el-table-column>
          <el-table-column label="位置" min-width="110" align="center" prop="local" />
          <el-table-column label="昨日用能(kW·h)" min-width="110" align="center" prop="yesterdayEq" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.yesterdayEq }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="上周用能(kW·h)" min-width="110" align="center" prop="lastWeekEq" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.lastWeekEq }} 
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="上月用能(kW·h)" min-width="110" align="center" prop="lastMonthEq" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.lastMonthEq }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="详情" width="120" align="center" prop="lastMonthEq" >
            <template #default="scope" >
              <el-button
                link
                type="primary"
                @click="toDetail(scope.row.roomId, scope.row.id,scope.row.location)"
                style="background-color:#409EFF;color:#fff;border:none;width:65px;height:30px;"
              >
              详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          :total="queryParams.pageTotal"
          :page-size="queryParams.pageSize"
          :page-sizes="pageSizeArr"
          :current-page="queryParams.pageNo"
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
import { IndexApi } from '@/api/aisle/aisleindex'

const { push } = useRouter() // 路由跳转
const timeButton=ref(0)
const tableLoading = ref(false) // 
const isFirst = ref(true) // 是否第一次调用getTableData函数
const navList = ref([]) // 左侧导航栏树结构列表
const tableData = ref([])as any
const switchValue = ref(0) // 表格(1) 矩阵(0)切换
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const pageSizeArr=ref([24,36,48])
// 定义变量时明确支持字符串类型
const yesterdayMaxEq = ref<string | null>(null);
const lastWeekMaxEq = ref<string | null>(null);
const lastMonthMaxEq = ref<string | null>(null);
const queryParams = reactive({
  name: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
  timeGranularity:null,
  cabinetIds: []
}) as any

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getAisleMenu()
  navList.value = res
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
      aisleIds : queryParams.aisleIds,
      runStatus: [],
      pduBox: 0,
      name: queryParams.name,
      timeGranularity:queryParams.timeGranularity,
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
          status : item.runStatus,
          roomId: item.roomId
        }
      })
      queryParams.pageTotal = res.total
      isFirst.value = false
    }
  } finally {
    tableLoading.value = false
  }
}

// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  if(switchValue.value == 0){
    queryParams.pageSize=15;
    pageSizeArr.value=[15,25,30,50,100];
  }else{
    queryParams.pageSize=24;
    pageSizeArr.value=[24,36,48];
  }
  queryParams.pageNo = 1;
  queryParams.timeGranularity=null;
  switchValue.value = value
  getTableData(true)
}

// 处理左侧树导航选择事件
const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.aisleIds = null;
    getTableData(true)
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

  getTableData(true)
}

// 跳转详情
const toDetail = (roomId, id,location) => {
  push({path: '/aisle/aislemonitor/aisleenergydetail', query: { roomId, id,location }})
}

onBeforeMount(() => {
  getNavList()
  getTableData()
})

//排序
function changeTimeGranularity(timeGranularity){
  if(queryParams.timeGranularity == timeGranularity) return;
  queryParams.timeGranularity=timeGranularity;
  getTableData(true);
}

function resetSearch(){
  queryParams.timeGranularity=null;
  queryParams.name=null;
  getTableData(true);
}

//表头样式
function headerCellStyle() {
    return {
      backgroundColor: '#eee', // 表头背景颜色
    };
  }
function handleSizeChange(val) {
  queryParams.pageSize = val
  queryParams.pageNo = 1
  getTableData(true)
}
function handleCurrentChange(val) {
  queryParams.pageNo = val
  getTableData(false)
}
onBeforeMount(async ()=>{
  let maxEq = await IndexApi.getMaxEq();
  maxEq.forEach((item)=>{
    if(item.type==0){
      yesterdayMaxEq.value =item.roomName+"-"+item.aisleName;
    }else if(item.type==1){
      lastWeekMaxEq.value =item.roomName+"-"+item.aisleName;
    }else if(item.type==2){
      lastMonthMaxEq.value =item.roomName+"-"+item.aisleName;
    }
  })
})
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
    margin-top: 45px;
    margin-left: 20px;
    display: inline-block;
    div{
      vertical-align: left;
      font-size: 14px;
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
  height: calc(100vh - 215px);
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
      // display: flex;
      position: relative;
      align-items: center;
      .count_img {
        position: absolute;
        right: 90px;
        // margin: 0 35px 0 0px;
      }
      .info {
        line-height: 1.7;
        font-size: 13px;
        display: inline-block;
      }
    }
    .room {
      position: absolute;
      left: 10px;
      top: 8px;
      font-size: 13px;
    }
    .detail {
      width: 40px;
      height: 25px;
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
      bottom: 4px;
    }
  }
}
</style>