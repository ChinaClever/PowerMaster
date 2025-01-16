<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机柜用能">
    <template #NavInfo>
      <div class="navInfo">
        <div style="font-size:14px; margin-top:45px; margin-left:20px">
          <div ><span>用能最大机柜：</span>
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
        <!-- <div class="status-container">
          <div class="status-item">用能最大机柜：</div>
          <div class="status-item">昨日用能：{{  }}</div>
          <div class="status-item">上周用能：{{  }}</div>
          <div class="status-item">上月用能：{{  }}</div>
        </div>
        <div class="line"></div> -->
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        :inline="true"
        label-width="90px"
      >
        <div style="margin-left:-30px;">
          <el-form-item label="用能排序"  label-width="100px">
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
          <el-form-item label="公司名称" prop="username" style="margin-left:50px;">
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
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;handleSwitchModal(0)" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchModal(1)" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div v-if="switchValue == 0 && tableData.length > 0" class="matrixContainer">
          <div class="item" v-for="item in tableData" :key="item.key">
            <div class="content">
              <div class="info" style="margin-left:-10px;">
                <div>昨日：{{item.yesterdayEq}}kW·h</div>
                <div>上周：{{item.lastWeekEq}}kW·h</div>
                <div>上月：{{item.lastMonthEq}}kW·h</div>
              </div>
              <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
            </div>
            <div class="room">{{item.local}}</div>
            <button class="detail" @click.prevent="toDetail(item.roomId, item.id)">用能详情</button>
          </div>
        </div>
        <el-table v-if="switchValue == 1" style="width: 100%;height: calc(100vh - 320px);" :data="tableData" >
          <el-table-column type="index" width="100" label="序号" align="center" />
          <el-table-column label="位置" min-width="110" align="center" prop="local" />
          <el-table-column label="公司" min-width="110" align="center" prop="company" />
          <el-table-column label="昨日用能(kW·h)" min-width="110" align="center" prop="yesterdayEq" />
          <el-table-column label="上周用能(kW·h)" min-width="110" align="center" prop="lastWeekEq" />
          <el-table-column label="上月用能(kW·h)" min-width="110" align="center" prop="lastMonthEq" />
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="toDetail(scope.row.roomId, scope.row.id)"
                style="background-color:#409EFF;color:#fff;border:none;width:65px;height:30px;"
              >
              设备详情
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
          <el-empty description="暂无数据" :image-size="300" />
        </template>
      </div>
    </template>
  </CommonMenu>
</template>

<script lang="ts" setup>
import { CabinetApi } from '@/api/cabinet/info'
import { CabinetEnergyApi } from '@/api/cabinet/energy'

const { push } = useRouter() // 路由跳转
// 运行状态 0：空载 1：正常 2：预警 3：告警 4:未绑定 5：离线
const sumNoload = ref();
const sumNormal = ref();
const sumEarly = ref();
const sumInform = ref();
const sumDidnot = ref();
const sumOffline = ref();
const busName1 = ref('无数据')
const busName2 = ref('无数据')
const busName3 = ref('无数据')
const tableLoading = ref(false) // 
const isFirst = ref(true) // 是否第一次调用getTableData函数
const navList = ref([]) // 左侧导航栏树结构列表
const tableData = ref([])
const switchValue = ref(0) // 表格(1) 矩阵(0)切换
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const queryParams = reactive({
  timeGranularity: undefined,
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})
const pageSizeArr = ref([24,36,48,96]);

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  navList.value = res
}

const changeTimeGranularity = (value) => {
  queryParams.timeGranularity = value;
  getTableData(true);
}


const getMaxData = async() => {
    try {
    const res = await CabinetApi.getEqMax()
    if (res) {
        //借用type值来辅助判断是哪个时间的集合，0为昨日，1为上周，2为上月
        const dataList = res
        dataList.forEach(item => {
          if(item.type == 0){
            busName1.value = item.location
          }else if (item.type == 1){
            busName2.value = item.location
          }else if (item.type == 2){
            busName3.value = item.location
          }
        })
    }
  } finally {

  }
}

// 获取表格数据
const getTableData = async(reset = false) => {
  tableLoading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await CabinetEnergyApi.getEqPage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      cabinetIds: isFirst.value ? null : cabinetIds.value,
      // roomId: null,
      runStatus: [],
      pduBox: 0,
      company: queryParams.company
    })
    if (res.list) {
      tableData.value = res.list.map(item => {
        const roomName = item.roomName || ''; // 处理 null 值
        return {
          id: item.id,
          local: roomName + '-' + item.cabinetName,
          company: item.company ,
          yesterdayEq: item.yesterdayEq ? item.yesterdayEq.toFixed(1) : '0.0',
          lastWeekEq: item.lastWeekEq ? item.lastWeekEq.toFixed(1) : '0.0',
          lastMonthEq: item.lastMonthEq ? item.lastMonthEq.toFixed(1) : '0.0',
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
const handleCheck = (row) => {
  isFirst.value = false
  const ids = [] as any
  row.forEach(item => {
    if (item.type == 3) {
      ids.push(item.id)
    }
  })
  cabinetIds.value = ids
  getTableData(true)
}

// 跳转详情
const toDetail = (roomId, id) => {
  console.log('跳转详情', id)
  push({path: '/cabinet/cab/energyDetail', state: { roomId, id }})
}

onBeforeMount(() => {
  getNavList()
  getTableData()
  getMaxData()
  // statistics()
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
      width: 55px;
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

.status-container {
  width: 100%;
  margin-left: 10px;
}

.status-item {
  display: block;
  margin-bottom: 10px;
  font-size: 16px;
}
</style>
