<template>
  <CommonMenu @check="handleCheck" :showSearch="true" :dataList="navList" navTitle="机架设备">
    <template #NavInfo>
      <div class="navInfo">
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
          <div class="name">微模块机房</div>
          <div>机房202</div>
        </div> -->
        <div class="line"></div>
        <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>未绑定
            </div>
            <div class="value"><span class="number">{{statistics.unbound}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>开机
            </div>
            <div class="value"><span class="number">{{statistics.powerOn}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>关机
            </div>
            <div class="value"><span class="number">{{statistics.powerOff}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>总计
            </div>
            <div class="value"><span class="number">{{statistics.totalAll}}</span>个</div>
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
            <!-- <el-button @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button> -->
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
              <div class="power">功率：{{item.powActive || 0}}kW</div>
              <div class="info">
                <div>名称：{{item.rackName}}</div>
                <div>型号：{{item.rackType}}</div>
                <div>A路电流：{{item.cura || 0}}A</div>
                <div>B路电流：{{item.curb || 0}}A</div>
              </div>
            </div>
            <div class="room">{{item.local}}</div>
            <button class="detail" @click.prevent="toFramDeviceDetail(item.id)">详情</button>
          </div>
        </div>
        <el-table v-if="switchValue == 1" style="width: 100%;height: calc(100vh - 320px);" :data="tableData" >
          <!-- <el-table-column label="编号" min-width="110" align="center" prop="bh" /> -->
          <el-table-column label="编号" align="center" prop="tableId" width="80px" >
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>  
          </el-table-column>
          <el-table-column label="位置" min-width="110" align="center" prop="local" />
          <el-table-column label="设备名" min-width="110" align="center" prop="rackName" />
          <el-table-column label="设备型号" min-width="110" align="center" prop="rackType" />
          <el-table-column label="A路电流" min-width="110" align="center" prop="cura" />
          <el-table-column label="B路电流" min-width="110" align="center" prop="curb" />
          <el-table-column label="有功功率" min-width="110" align="center" prop="powActive" />
          <el-table-column label="无功功率" min-width="110" align="center" prop="powReactive" />
          <el-table-column label="功率因素" min-width="110" align="center" prop="powerFactor" />
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
import { CabinetApi } from '@/api/cabinet/info'

const { push } = useRouter() // 路由跳转
const tableLoading = ref(false)
const switchValue = ref(0)
const tableData = ref([])
const navList = ref([]) // 左侧导航列表数据
const isFirst = ref(true) // 是否第一次调用getTableData函数
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 1,
})
const statistics = reactive({
  unbound : 0,
  powerOn : 0,
  powerOff : 0,
  totalAll : 0
})
// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRackMenuAll({})
  navList.value = res
}
const getStatistics = async() => {
  const resStatus =await CabinetApi.getRackStatistics();

    statistics.unbound = resStatus.unbound;
    statistics.powerOn = resStatus.powerOn;
    statistics.powerOff = resStatus.powerOff;
    statistics.totalAll = resStatus.total;
  console.log('resStatus', statistics)
}


// 获取机架表格数据
const getTableData = async(reset = false) => {
  tableLoading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await CabinetApi.getRackPage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      company: queryParams.company,
      rackIds: isFirst.value ? null : cabinetIds.value,
      // rackIds: [1, 2, 3, 4, 5],
      // roomId: null,
    })
    console.log('res', res)
    if (res.list) {
      // tableData.value = res.list.map(item => {
      //   return {
      //     bh: item.rack_key,
      //     name: item.rack_name,
      //     local: item.room_name + '-' + item.cabinet_name,
      //     sbm: item.rack_name,
      //     sbxh: item.type,
      //     adl: item.rack_power.total_data.cur_a.toFixed(3),
      //     bdl: item.rack_power.total_data.cur_b.toFixed(3),
      //     yggl: item.rack_power.total_data.pow_active.toFixed(1),
      //     wggl: item.rack_power.total_data.pow_reactive,
      //     glys: item.rack_power.total_data.power_factor
      //   }
      // })
      tableData.value = res.list
      queryParams.pageTotal = res.total
    }
    getStatistics();
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
  // getTableData(true)
}

// 跳转详情页
const toFramDeviceDetail = (key) => {
  console.log('toFramDeviceDetail!', key)
  push({path: '/u/frameDeviceDetail', state: { id: key }})
}

const handleCheck = (row) => {
  // console.log('handleCheck!', row)
  isFirst.value = false
  const ids = [] as any
  row.forEach(item => {
    if (item.type == 5) {
      ids.push(item.id)
    }
  })
  cabinetIds.value = ids
  getTableData(true)
}
getNavList()
getTableData()
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
    height: 140px;
    font-size: 12px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 36px;
    position: relative;
    .content {
      padding-left: 10px;
      display: flex;
      align-items: center;
      .power {
        margin: 0 0 0 13px;
      }
      .info {
        line-height: 1.7;
        font-size: 13px;
        margin: 0 auto;
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