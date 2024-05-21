<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="模块化机房" >
    <template #NavInfo>
      <div class="navInfo">
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
          <div class="name">微模块机房</div>
          <div>机房202</div>
        </div>
        <div class="line"></div> -->
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
        <div class="line"></div>
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
            <el-button style="margin-left: 12px" ><Icon icon="ep:search" />搜索</el-button>
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <el-button @click="handleSwitchModal(0)" :type="switchValue==0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />电流阵列</el-button>
          <el-button @click="handleSwitchModal(1)" :type="switchValue==1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />功率阵列</el-button>
          <el-button @click="handleSwitchModal(2)" :type="switchValue==2 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div v-if="(switchValue == 0 || switchValue == 1) && tableData.length > 0" class="matrixContainer">
          <div class="item" v-for="item in tableData" :key="item.key">
            <!-- 电流 -->
            <div class="progressContainer" v-if="switchValue == 0">
              <div class="text">AB路占比：</div>
              <div class="progress" v-if="item.abdlzb">
                <div class="left" :style="`flex: ${item.abdlzb}`">{{item.abdlzb}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${100 - item.abdlzb}`">{{100 - item.abdlzb}}%</div>
              </div>
              <div class="progress" v-else>
                <div class="left" :style="`flex: 50`">null</div>
                <div class="line"></div>
                <div class="right" :style="`flex: 50`">null</div>
              </div>
            </div>
            <!-- 功率 -->
            <div class="progressContainer" v-if="switchValue == 1">
              <div class="text">AB路占比：</div>
              <div class="progress" v-if="item.abglzb">
                <div class="left" :style="`flex: ${item.abglzb}`">{{item.abglzb}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${100 - item.abglzb}`">{{100 - item.abglzb}}%</div>
              </div>
              <div class="progress" v-else>
                <div class="left" :style="`flex: 50`">null</div>
                <div class="line"></div>
                <div class="right" :style="`flex: 50`">null</div>
              </div>
            </div>
            <!-- 电流 -->
            <div class="content" v-if="switchValue == 0">
              <div class="road">A路</div>
              <div class="valueList">
                <div>Ia：{{item.Ia0 || '0.00'}}A</div>
                <div>Ia：{{item.Ia1 || '0.00'}}A</div>
                <div>Ia：{{item.Ia2 || '0.00'}}A</div>
              </div>
              <div class="road">B路</div>
              <div class="valueList">
                <div>Ia：{{item.Ib0 || '0.00'}}A</div>
                <div>Ia：{{item.Ib1 || '0.00'}}A</div>
                <div>Ia：{{item.Ib2 || '0.00'}}A</div>
              </div>
            </div>
            <!-- 功率 -->
            <div class="content" v-if="switchValue == 1">
              <div class="road">A路</div>
              <div class="valueList">
                <div>Pa：{{item.Pa0 || '0.00'}}Kw</div>
                <div>Pa：{{item.Pa1 || '0.00'}}Kw</div>
                <div>Pa：{{item.Pa2 || '0.00'}}Kw</div>
              </div>
              <div class="road">B路</div>
              <div class="valueList">
                <div>Pa：{{item.Pb0 || '0.00'}}Kw</div>
                <div>Pa：{{item.Pb1 || '0.00'}}Kw</div>
                <div>Pa：{{item.Pb2 || '0.00'}}Kw</div>
              </div>
            </div>
            <div class="room">{{item.local}}</div>
            <button class="detail" @click.prevent="toDetail(item.id)">详情</button>
          </div>
        </div>
        <el-table v-if="switchValue == 2" style="width: 100%;" :data="tableData" >
          <el-table-column type="index" width="60" label="序号" align="center" />
          <el-table-column label="A路" align="center">
            <el-table-column label="I1(A)" min-width="90" align="center" prop="Ia0" />
            <el-table-column label="I2(A)" min-width="90" align="center" prop="Ia1" />
            <el-table-column label="I3(A)" min-width="90" align="center" prop="Ia2" />
            <el-table-column label="P1(kW)" min-width="90" align="center" prop="Pa0" />
            <el-table-column label="P2(kW)" min-width="90" align="center" prop="Pa1" />
            <el-table-column label="P3(kW)" min-width="90" align="center" prop="Pa2" />
          </el-table-column>
          <el-table-column label="B路"  align="center">
            <el-table-column label="I1(A)" min-width="90" align="center" prop="Ib0" />
            <el-table-column label="I2(A)" min-width="90" align="center" prop="Ib1" />
            <el-table-column label="I3(A)" min-width="90" align="center" prop="Ib2" />
            <el-table-column label="P1(kW)" min-width="90" align="center" prop="Pb0" />
            <el-table-column label="P2(kW)" min-width="90" align="center" prop="Pb1" />
            <el-table-column label="P3(kW)" min-width="90" align="center" prop="Pb2" />
          </el-table-column>
        </el-table>
        <Pagination
          :total="queryParams.pageTotal"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getTableData(false)"
        />
        <template v-if="tableData.length == 0 && switchValue != 2">
          <el-empty description="暂无数据" :image-size="300" />
        </template>
      </div>
    </template>
  </CommonMenu>
</template>

<script lang="ts" setup>
import { CabinetApi } from '@/api/cabinet/info'

const { push } = useRouter() // 路由跳转
const router = useRouter() // 路由跳转
const tableLoading = ref(false) // 
const isFirst = ref(true) // 是否第一次调用getTableData函数
const navList = ref([]) // 左侧导航栏树结构列表
const tableData = ref([])
const switchValue = ref(0) // 表格(1) 矩阵(0)切换
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  navList.value = res
}

// 接口获取机柜列表
const getTableData = async(reset = false) => {
  tableLoading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await CabinetApi.getCabinetInfo({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      cabinetIds: isFirst.value ? null : cabinetIds.value,
      // roomId: null,
      runStatus: [],
      pduBox: 0,
      company: queryParams.company
    })
    console.log('res', res)
    if (res.list) {
      const list = res.list.map(item => {
        const tableItem = {} as any
        tableItem.local = item.room_name + '-' + item.cabinet_name
        tableItem.id = item.cabinet_key.split('-')[1]
        const PathA = item.cabinet_power.path_a
        const PathB = item.cabinet_power.path_b
        if (PathA && PathA.cur_value.length > 0) { // A路电流
        console.log('PathA.cur_value', PathA.cur_value)
          PathA.cur_value.forEach((item, index) => {
            tableItem['Ia'+index] = item.toFixed(2)
          })
        }
        if (PathA && PathA.vol_value.length > 0) { // A路电压
          PathA.vol_value.forEach((item, index) => {
            tableItem['Ua'+index] = item.toFixed(2)
          })
        }
        if (PathA && PathA.pow_value.length > 0) { // A路功率
          PathA.pow_value.forEach((item, index) => {
            tableItem['Pa'+index] = item.toFixed(2)
          })
        }
        if (PathB && PathB.cur_value.length > 0) { // B路电流
          PathB.cur_value.forEach((item, index) => {
            tableItem['Ib'+index] = item.toFixed(2)
          })
        }
        if (PathB && PathB.vol_value.length > 0) { // B路电压
          PathB.vol_value.forEach((item, index) => {
            tableItem['Ub'+index] = item.toFixed(2)
          })
        }
        if (PathB && PathB.pow_value.length > 0) { // B路功率
          PathB.pow_value.forEach((item, index) => {
            tableItem['Pb'+index] = item.toFixed(2)
          })
        }
        if (item.cabinet_power.path_a && item.cabinet_power.path_b) {
          if (item.cabinet_power.path_a.pow_apparent == 0) tableItem.abdlzb = 0
          else tableItem.abdlzb = (item.cabinet_power.path_a.pow_apparent / item.cabinet_power.total_data.pow_apparent as any).toFixed(2) * 100
          if (item.cabinet_power.path_a.pow_active == 0) tableItem.abglzb = 0
          else tableItem.abglzb = (item.cabinet_power.path_a.pow_active / item.cabinet_power.total_data.pow_active as any).toFixed(2) * 100
        }
        return tableItem
      })
      console.log('list', list)
      tableData.value = list
      queryParams.pageTotal = res.total
    }
  } finally {
    tableLoading.value = false
  }
}

// 详情跳转
const toDetail = (id) => {
  console.log('详情跳转', id, router, router.getRoutes())
  push({path: '/cabinet/cab/balanceDetail', state: { id }})
}

// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  switchValue.value = value
  if (value == 0 || value == 1) { // 阵列
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
    margin-top: 28px;
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
    min-width: 290px;
    height: 150px;
    font-size: 12px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    position: relative;
    .progressContainer {
      height: 50px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 20px;
      .text {
        font-size: 14px;
      }
      .progress {
        width: 180px;
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #eee;
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
        .line {
          width: 3px;
          height: 25px;
          background-color: #000;
        }
        .left {
          text-align: center;
          box-sizing: border-box;
          background-color: #3b8bf5;
          // border-right: 1px solid #000;
        }
        .right {
          text-align: center;
          background-color:  #f86f13;
        }
      }
    }
    .content {
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      & > .valueList:last-of-type {
        margin-right: 0;
      }
      .road {
        margin-right: 10px;
      }
      .valueList {
        margin-right: 40px;
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
      right: 10px;
      top: 8px;
    }
  }
}
</style>