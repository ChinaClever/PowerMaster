<template>
  <CommonMenu :dataList="navList" @check="handleCheck"  navTitle="柜列用能">
    <template #NavInfo>
      <div class="navInfo">
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/aisle.png" /></div>
          <div class="name">柜列</div>
        </div>
        <!-- <div class="line"></div>
        <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>正常
            </div>
            <div class="value"><span class="number">24</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>空载
            </div>
            <div class="value"><span class="number">1</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>预警
            </div>
            <div class="value"><span class="number">1</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>故障
            </div>
            <div class="value"><span class="number">0</span>个</div>
          </div>
        </div>
        <div class="line"></div> -->
        <!-- <div class="overview">
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
        </div> -->
      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        :inline="true"
        label-width="68px"
      >
        <div>
          <el-form-item label="公司名称" prop="username">
            <el-input
              v-model="queryParams.company"
              placeholder="请输入公司名称"
              clearable
              class="!w-160px"
              height="35"
            />
          </el-form-item>
          <el-form-item>
            <el-button style="margin-left: 12px" @click="getTableData(true)" ><Icon icon="ep:search" />搜索</el-button>
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
              <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
              <div class="info">
                <div>昨日用能：{{item.yesterdayEq}}kW·h</div>
                <div>上周用能：{{item.lastWeekEq}}kW·h</div>
                <div>上月用能：{{item.lastMonthEq}}kW·h</div>
              </div>
            </div>
            <div class="room">{{item.location}}</div>
            <button class="detail" @click.prevent="toDetail(item.roomId, item.id,item.location)" >详情</button>
          </div>
        </div>
        <el-table v-if="switchValue == 1" style="width: 100%;height: calc(100vh - 320px);" :data="tableData" >
          <el-table-column type="index" width="100" label="序号" align="center" />
          <el-table-column label="位置" min-width="110" align="center" prop="local" />
          <el-table-column label="昨日用能" min-width="110" align="center" prop="yesterdayEq" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.yesterdayEq }} kW·h
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="上周用能" min-width="110" align="center" prop="lastWeekEq" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.lastWeekEq }} kW·h
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="上月用能" min-width="110" align="center" prop="lastMonthEq" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.lastMonthEq }} kW·h
              </el-text>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          :total="queryParams.pageTotal"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getTableData(false)"
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

const tableLoading = ref(false) // 
const isFirst = ref(true) // 是否第一次调用getTableData函数
const navList = ref([]) // 左侧导航栏树结构列表
const tableData = ref([])as any
const switchValue = ref(0) // 表格(1) 矩阵(0)切换
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
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
      company: queryParams.company
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
      queryParams.pageTotal = res.total
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
  } else {
    queryParams.pageSize = 10
  }
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
  push({path: '/aisle/aislemonitor/aisleenergydetail', state: { roomId, id,location }})
}

onBeforeMount(() => {
  getNavList()
  getTableData()
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
        margin: 0 35px 0 13px;
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
      top: 4px;
    }
  }
}
</style>