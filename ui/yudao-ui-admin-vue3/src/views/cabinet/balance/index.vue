<template>
  <CommonMenu :dataList="navList" @check="handleCheck" >
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
          <el-button @click="handleSwitchModal(0)" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />阵列模式</el-button>
          <el-button @click="handleSwitchModal(1)" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div v-if="switchValue == 0" class="matrixContainer">
          <div class="item" v-for="item in tableData" :key="item.key">
            <div class="content">
              <div class="info">
                <div>昨日用能：{{item.yesterdayEq}}kW·h</div>
                <div>上周用能：{{item.lastWeekEq}}kW·h</div>
                <div>上月用能：{{item.lastMonthEq}}kW·h</div>
              </div>
            </div>
            <div class="room">{{item.local}}</div>
          </div>
        </div>
        <!-- <el-table v-if="switchValue == 1" style="width: 100%;height: calc(100vh - 320px);" :data="tableData" >
          <el-table-column type="index" width="100" label="序号" align="center" />
          <el-table-column label="位置" min-width="110" align="center" prop="local" />
          <el-table-column label="昨日用能" min-width="110" align="center" prop="yesterdayEq" />
          <el-table-column label="上周用能" min-width="110" align="center" prop="lastWeekEq" />
          <el-table-column label="上月用能" min-width="110" align="center" prop="lastMonthEq" />
        </el-table> -->
        <Pagination
          :total="queryParams.pageTotal"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getTableData(false)"
        />
      </div>
    </template>
  </CommonMenu>
</template>

<script lang="ts" setup>
import { CabinetApi } from '@/api/cabinet/info'

const tableLoading = ref(false) // 
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
      cabinetIds: cabinetIds.value,
      // roomId: null,
      runStatus: [],
      pduBox: 0,
      company: queryParams.company
    })
    console.log('res', res)
    if (res.list) {
      const list = res.list.map(item => {
        const tableItem = {
          company: item.company,
          cabinet_key: item.cabinet_key,
          cabinetName: item.cabinet_name,
          roomName: item.room_name,
          status: item.status,
          apparentTotal: item.cabinet_power.total_data.pow_apparent.toFixed(3),
          apparentA: item.cabinet_power.path_a ? item.cabinet_power.path_a.pow_apparent.toFixed(3) : '-',
          apparentB: item.cabinet_power.path_b ? item.cabinet_power.path_b.pow_apparent.toFixed(3) : '-',
          activeTotal: item.cabinet_power.total_data.pow_active.toFixed(3),
          activeA: item.cabinet_power.path_a ? item.cabinet_power.path_a.pow_active.toFixed(3) : '-',
          activeB: item.cabinet_power.path_b ? item.cabinet_power.path_b.pow_active.toFixed(3) : '-',
          eleTotal: item.cabinet_power.total_data.ele_active.toFixed(1),
          eleA: item.cabinet_power.path_a ? item.cabinet_power.path_a.ele_active.toFixed(1) : '-',
          eleB: item.cabinet_power.path_b ? item.cabinet_power.path_b.ele_active.toFixed(1) : '-',
          powerFactorTotal: item.cabinet_power.total_data.power_factor,
          powerReactiveTotal: item.cabinet_power.total_data.pow_reactive.toFixed(3),
          loadFactor: Math.ceil(item.load_factor),
          abzb: '-' as number | string
        }
        return tableItem
      })
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
    }
    .room {
      position: absolute;
      left: 10px;
      top: 8px;
      font-size: 13px;
    }
  }
}
</style>