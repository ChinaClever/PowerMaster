<template>
  <CommonMenu :dataList="navList" @check="handleCheck"  navTitle="母线用能">
    <template #NavInfo>
      <div class="navInfo">
        <div style="font-size:14px; margin-top:45px; margin-left:20px">
          <div ><span>用能最大IP</span>
          </div>
          <div>
            <span>昨日：</span>
            <span>{{ busName1 }}</span>
          </div>
          <div >
            <span>上周：</span>
            <span>{{ busName2 }}</span>
          </div>
          <div >
            <span>上月：</span>
            <span>{{ busName3 }}</span>
          </div>
        </div>
        <div class="line"></div>        
      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="用能排序"  label-width="100px" style="margin-left:-30px;">
          <el-button @click="changeTimeGranularity('yesterday')"
          >
            昨日
          </el-button>
          <el-button @click="changeTimeGranularity('lastWeek')"
          >
            上周
          </el-button>
          <el-button @click="changeTimeGranularity('lastMonth')"
          >
            上月
          </el-button>                            
        </el-form-item>
        <el-form-item label="网络地址" prop="devKey" style="margin-left:80px">
          <el-autocomplete
            v-model="queryParamsDevKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-160px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        <el-form-item style="margin-left: 5px;margin-right: 6px">
          <el-button @click="handleQuery" style="width:70px"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetQuery" style="width:70px;" ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        </el-form-item>         
        </el-form-item >
        <el-form-item style="margin-left: auto">
          <el-button 
                @click="pageSizeArr = [24, 36, 48, 96];
                handleSwitchModal(0)" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />阵列模式</el-button>
          <el-button 
              @click="
              pageSizeArr = [15, 25, 30, 50, 100];
              queryParams.pageSize = 15;
              handleSwitchModal(1)" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div v-show="switchValue == 0 && tableData.length > 0" class="matrixContainer">
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
            <!-- <div class="name">{{item.busName}}</div> -->
            <button class="detail" @click.prevent="toDetail(item)" >详情</button>
          </div>
        </div>
        <el-table v-show="switchValue == 1" style="width: 100%;height: 720px;margin-top:-10px;overflow-y: auto;" :data="tableData" :border="true">
          <el-table-column type="index" width="80px" label="序号" align="center" />
          <el-table-column min-width="110" label="所在位置" align="center" prop="local">
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.local ? scope.row.local : '未绑定' }}
              </el-text>
            </template>
          </el-table-column>  
          <el-table-column label="设备名称"  align="center" prop="busName" />
          <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip"/>
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
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDetail(scope.row)"
              style="background-color:#409EFF;color:#fff;border:none;width:100px;height:30px;"
            >
            详情
            </el-button>
          </template>
        </el-table-column>
        </el-table>
        <Pagination
          :total="queryParams.pageTotal"
          :page-size-arr="pageSizeArr"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getTableData(false)"
        />
        <template v-if="tableData.length == 0 && switchValue == 0">
          <el-empty description="暂无数据" :image-size="595" />
        </template>
      </div>
    </template>
  </CommonMenu>
</template>

<script lang="ts" setup>
import { IndexApi } from '@/api/bus/busindex'

const { push } = useRouter() // 路由跳转
const queryParamsDevKey = ref(undefined)
const tableLoading = ref(false) // 
const isFirst = ref(true) // 是否第一次调用getTableData函数
const navList = ref([]) // 左侧导航栏树结构列表
const tableData = ref([])as any
const tableDataMax = ref([])as any
const switchValue = ref(0) // 表格(1) 矩阵(0)切换
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const yesterdayMaxEq = ref(0)
const lastWeekMaxEq = ref(0)
const lastMonthMaxEq = ref(0)
const busName1 = ref('无数据')
const busName2 = ref('无数据')
const busName3 = ref('无数据')
const pageSizeArr = ref([24, 36, 48, 96])
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
  busDevKeyList : undefined as string[] | undefined,
  isDeleted: 0,
  devKey: undefined,
  timeGranularity:'',
}) as any

const queryParamsAll = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: -1,
  pageTotal: 0,
  busDevKeyList : undefined as string[] | undefined,
  isDeleted: 0,
}) as any

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

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getBusMenu()
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
  console.log('getTableData', queryParams)
  tableLoading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await IndexApi.getEqPage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      cabinetIds: isFirst.value ? null : cabinetIds.value,
      // roomId: null,
      runStatus: [],
      pduBox: 0,
      company: queryParams.company,
      busDevKeyList : queryParams.busDevKeyList,
      isDeleted : queryParams.isDeleted,
      timeGranularity : queryParams.timeGranularity
    })
    if (res.list) {
      tableData.value = res.list.map(item => {
        return {
          id: item.id,
          devKey: item.devKey,
          busName : item.busName,
          // location: item.location ? item.location : item.devKey+item.busName,
          location: item.location || (item.devKey && item.busName ? item.devKey +'-'+ item.busName : null),
          local : item.location,
          roomName: item.roomName,
          yesterdayEq: item.yesterdayEq ? item.yesterdayEq.toFixed(1) : '0.0',
          lastWeekEq: item.lastWeekEq ? item.lastWeekEq.toFixed(1) : '0.0',
          lastMonthEq: item.lastMonthEq ? item.lastMonthEq.toFixed(1) : '0.0',
          status : item.runStatus,

        }
      })
      queryParams.pageTotal = res.total;
    }
    console.log('tableData',tableData.value);
  } finally {
    tableLoading.value = false
  }
}

const getMaxData = async() => {
    try {
    const res = await IndexApi.getEqMax()
    if (res) {
        //借用id值来辅助判断是哪个时间的集合，0为昨日，1为上周，2为上月
        const dataList = res
        dataList.forEach(item => {
          if(item.id == 0){
            busName1.value = item.devKey
          }else if (item.id == 1){
            busName2.value = item.devKey
          }else if (item.id == 2){
            busName3.value = item.devKey
          }
        })
    }
  } finally {
    
  }
}

// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  switchValue.value = value
  if (value == 0) { // 阵列
    queryParams.pageSize = 24
  } else {
    queryParams.pageSize = 15
  }
  getTableData(true)
}

// 处理左侧树导航选择事件
const handleCheck = (row) => {
  if(row.length == 0){
    queryParams.busDevKeyList = null;
    getTableData();
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
  getTableData(true)
}

/** 搜索按钮操作 */
const handleQuery = () => {
  console.log('handleQuery', queryParamsDevKey.value)
  if(queryParamsDevKey.value !=null && queryParamsDevKey.value != ''){
    queryParams.busDevKeyList = [queryParamsDevKey.value]
  }else{
    queryParams.busDevKeyList = undefined as string[] | undefined
  }
  getTableData(true)
}

const resetQuery = () => {
  queryParams.busDevKeyList = undefined as string[] | undefined
  getTableData(true)
}

const changeTimeGranularity = (value) => {
  queryParams.timeGranularity = value;
  getTableData(true);
}

// 跳转详情
const toDetail = (row) => {
  console.log('toDetail', row.roomName)
   push({path: '/bus/busmonitor/busmonitor/busenergydetail', query: { devKey: row.devKey,
      roomId: row.roomId,
      id: row.id,
      location: row.location,
      busName: row.busName,
      roomName: row.roomName,
      local: row.local
    }})
}

onMounted(async () => {
  devKeyList.value = await loadAll();
})

onBeforeMount(() => {
  getNavList()
  getTableData()
  getMaxData()
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

@media screen and (min-width:2048px){
  .matrixContainer {
  height: 720px;
  width:100%;
  overflow: auto;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  margin-top: -10px;

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
      justify-content: space-between;
      .count_img {
        margin: 0 64px 0 0;
      }
      .info {
        line-height: 1.7;
        font-size: 13px;
        margin-right: 15px;
      }
    }
    .room {
      position: absolute;
      left: 10px;
      top: 8px;
      font-size: 14px;
    }
    .name {
      height: 20px;
      font-size: 14px;
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      position: absolute;
      right: 5px;
      top: 4px;
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
      bottom: 4px;
    }
  }
  }
}

@media screen and (max-width:2048px) and (min-width:1600px){
  .matrixContainer {
  height: 720px;
  width:100%;
  overflow: auto;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  margin-top: -10px;
  
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
      justify-content: space-between;
      .count_img {
        margin: 0 64px 0 0;
      }
      .info {
        line-height: 1.7;
        font-size: 13px;
        margin-right: 15px;
      }
    }
    .room {
      position: absolute;
      left: 10px;
      top: 8px;
      font-size: 14px;
    }
    .name {
      height: 20px;
      font-size: 14px;
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      position: absolute;
      right: 5px;
      top: 4px;
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
      bottom: 4px;
    }
  }
}
}

@media screen and (max-width:1600px){
  .matrixContainer {
  height: 720px;
  width:100%;
  overflow: auto;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  margin-top: -10px;
  
  .item {
    width: 33%;
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
      justify-content: space-between;
      .count_img {
        margin: 0 64px 0 0;
      }
      .info {
        line-height: 1.7;
        font-size: 13px;
        margin-right: 15px;
      }
    }
    .room {
      position: absolute;
      left: 10px;
      top: 8px;
      font-size: 14px;
    }
    .name {
      height: 20px;
      font-size: 14px;
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      position: absolute;
      right: 5px;
      top: 4px;
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
      bottom: 4px;
    }
  }
}
}

::v-deep .el-table .el-table__header th{
  background-color: #f5f7fa;
  color: #909399;
  height: 80px;

}

:deep(.el-card){
  --el-card-padding:5px;
}
</style>
