<template>
  <CommonMenu @check="handleCheck" :showSearch="true" :dataList="navList" navTitle="模块化机房">
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
              <div class="tag empty"></div>未开通
            </div>
            <div class="value"><span class="number">{{Loadstatus[0]}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag"></div>&lt;=30%
            </div>
            <div class="value"><span class="number">{{Loadstatus[1]}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag normal"></div>30%~60%
            </div>
            <div class="value"><span class="number">{{Loadstatus[2]}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>60%~90%
            </div>
            <div class="value"><span class="number">{{Loadstatus[3]}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&gt;90%
            </div>
            <div class="value"><span class="number">{{Loadstatus[4]}}</span>个</div>
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
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item>
          <template v-for="(status, index) in statusList" :key="index">
            <button :class="status.selected ? status.activeClass : status.cssClass" @click.prevent="handleSelectStatus(index)">{{status.name}}</button>
          </template>
        </el-form-item>
        <div>
          <el-form-item label="公司名称" prop="company">
            <el-input
              v-model="queryParams.company"
              placeholder="请输入公司名称"
              clearable
              class="!w-160px"
              height="35"
            />
          </el-form-item>
          <el-form-item>
            <el-button style="margin-left: 12px" @click="getTableData(true)"><Icon icon="ep:search" />搜索</el-button>
            <!-- <el-button @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button> -->
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <el-button @click="handleSwitchModal(0)" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />视在功率</el-button>
          <el-button @click="handleSwitchModal(1)" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />有功功率</el-button>
          <el-button @click="handleSwitchModal(2)" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <el-table v-show="switchValue == 2" style="width: 100%;" v-loading="loading" :data="listPage" >
        <el-table-column label="位置" min-width="110" align="center" prop="local" />
        <el-table-column label="负载率" min-width="80" align="center" prop="load_factor" />
        <el-table-column label="电力容量(kVA)" min-width="100" align="center" prop="pow_capacity" />
        <el-table-column label="总视在功率(kVA)" min-width="110" align="center" prop="apparentTotal" />
        <el-table-column label="A路视在功率(kVA)" min-width="120" align="center" prop="apparentA" />
        <el-table-column label="B路视在功率(kVA)" min-width="120" align="center" prop="apparentB" />
        <el-table-column label="总有功功率(kW)" min-width="110" align="center" prop="activeTotal" />
        <el-table-column label="A路有功功率(kW)" min-width="120" align="center" prop="activeA" />
        <el-table-column label="B路有功功率(kW)" min-width="120" align="center" prop="activeB" />
        <el-table-column label="更新时间" min-width="110" align="center" prop="date_time" />
      </el-table>
      <div v-show="(switchValue == 0 || switchValue == 1) && listPage.length > 0" v-loading="loading" class="loadContainer">
        <div class="loadItem" v-for="load in listPage" :key="load.key">
          <div class="content">
            <div class="info" v-if="switchValue == 0">
              <div>总视在功率：{{load.apparentTotal}}KVA</div>
              <div>A路视在功率：{{load.apparentA}}KVA</div>
              <div>B路视在功率：{{load.apparentB}}KVA</div>
              <!-- <div>电力容量：{{load.pow_capacity}}</div> -->
            </div>
            <div class="info" v-else>
              <div>总有功功率：{{load.activeTotal}}kW</div>
              <div>A路有功功率：{{load.activeA}}kW</div>
              <div>B路有功功率：{{load.activeB}}kW</div>
              <!-- <div>电力容量：{{load.pow_capacity}}</div> -->
            </div>
            <div class="waterPoloBox">
              <LiquidBall  :precent="load.load_factor"/>
            </div>
            <!-- <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div> -->
          </div>
          <div class="room">{{load.local}}</div>
          <button class="detail" @click.prevent="toMachineDetail">详情</button>
        </div>
      </div>
      <Pagination
        :total="queryParams.pageTotal"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getTableData(false)"
      />
      <template v-if="listPage.length == 0 && switchValue != 2 && !loading">
        <el-empty description="暂无数据" :image-size="300" />
      </template>
    </template>
  </CommonMenu>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts'
import 'echarts-liquidfill'
import { CabinetApi } from '@/api/cabinet/info'
import LiquidBall from './compoent/LiquidBall.vue'

const loading = ref(false)
const isFirst = ref(true) // 是否第一次调用getTableData函数
const navList = ref([])
const Loadstatus = ref([0,0,0,0,0])
const switchValue = ref(0)
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const listPage = ref<any>([]) // 表格数据
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})

const echartsOption = reactive({
  series: [
    {
      type: 'liquidFill',
      data: [0.38], // 设置水球图的填充比例
      label: {
        fontSize: 12, // 设置字体大小
        fontWeight: 'bold' // 设置字体粗细
      },
      radius: '100%',
      amplitude: 2, // 调整波浪的振幅
      outline: {
        show: false // 不显示外圈轮廓线
      },

      color: ['#3b8bf5'],
      backgroundStyle: {
        color: '#fff'
      }
    }
  ]
})

const statusList = reactive([
  {
    name: '未开通',
    selected: true,
    value: 0,
    cssClass: 'btn_empty',
    activeClass: 'btn_empty empty',
    color: '#aaa'
  },
  {
    name: '负载量<30%',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#3bbb00'
  },
  {
    name: '30%≤负载量<60%',
    selected: true,
    value: 2,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn',
    color: '#ffc402'
  },
  {
    name: '60%≤负载量<90%',
    selected: true,
    value: 3,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    color: '#fa3333'
  },
  {
    name: '负载量>90%',
    selected: true,
    value: 4,
    cssClass: 'btn_unbound',
    activeClass: 'btn_unbound unbound',
    color: '#05ebfc'
  },
])


// 接口获取机柜列表
const getTableData = async(reset = false) => {
  loading.value = true
  if (reset) queryParams.pageNo = 1
  const status =  statusList.filter(item => item.selected)
  try {
    const res = await CabinetApi.getCabinetInfo({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      cabinetIds: isFirst.value ? null : cabinetIds.value,
      // roomId: null,
      loadStatus: status.map(item => item.value),
      pduBox: 0,
      company: queryParams.company
    })
    console.log('res', res)
    if (res.list) {
      const list = res.list.map(item => {
        const tableItem = {
          key: item.cabinet_key,
          local: item.room_name + '-' + item.cabinet_name,
          load_factor: +(item.load_factor.toFixed(0)),
          pow_capacity: item.pow_capacity,
          date_time: item.date_time,
          status: item.status,
          apparentTotal: item.cabinet_power.total_data.pow_apparent.toFixed(3),
          apparentA: item.cabinet_power.path_a ? item.cabinet_power.path_a.pow_apparent.toFixed(3) : '-',
          apparentB: item.cabinet_power.path_b ? item.cabinet_power.path_b.pow_apparent.toFixed(3) : '-',
          activeTotal: item.cabinet_power.total_data.pow_active.toFixed(3),
          activeA: item.cabinet_power.path_a ? item.cabinet_power.path_a.pow_active.toFixed(3) : '-',
          activeB: item.cabinet_power.path_b ? item.cabinet_power.path_b.pow_active.toFixed(3) : '-',
        }
        return tableItem
      })
      listPage.value = list
      queryParams.pageTotal = res.total
      console.log('listPage', listPage.value)
    }
  } finally {
    loading.value = false
  }
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  console.log('接口获取机房导航列表', res)
  navList.value = res
}

// 接口获取负载状态
const getLoadStatusList = async() => {
  const res = await CabinetApi.getLoadStatus({})
  console.log('接口获取机房导航列表', res)
  Loadstatus.value = res
}

const handleClick = (row) => {
  console.log('Button clicked!', row);
}

const handleCheck = (row) => {
  console.log('handleCheck!', row);
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

// 处理状态选择事件
const handleSelectStatus = (index, event) => {
  console.log('处理状态选择事件', index, event)
  statusList[index].selected = !statusList[index].selected
  getTableData()
}
// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  switchValue.value = value
  if (value != 2) { // 阵列
    queryParams.pageSize = 24
  } else {
    queryParams.pageSize = 10
  }
  getTableData(true)
}

onBeforeMount(() => {
  getNavList()
  getTableData()
  getLoadStatusList()
})
</script>

<style lang="scss" scoped>

.loadContainer {
  height: 600px;
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .loadItem {
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
      line-height: 1.6;
      margin-top: 5px;
      .waterPoloBox {
        flex: 1;
        margin-left: 15px;
        display: flex;
        justify-content: center;
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
        .normal {
          background-color: #3B8BF5;
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
.btn_normal,
.btn_empty,
.btn_warn,
.btn_error,
.btn_unbound,
.btn_offline {
  // width: 55px;
  // height: 32px;
  padding: 3px 8px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  &:hover {
    color: #7bc25a;
  }
}
.btn_normal {
  border: 1px solid #3bbb00;
  background-color: #fff;
}
.normal {
  background-color: #3bbb00;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_empty {
  border: 1px solid #aaa;
  background-color: #fff;
}
.empty {
  background-color: #aaa;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_warn {
  border: 1px solid #3b8bf5;
  background-color: #fff;
}
.warn {
  background-color: #3b8bf5;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_error {
  border: 1px solid #ffc402;
  background-color: #fff;
}
.error {
  background-color: #ffc402;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_unbound {
  border: 1px solid #fa3333;
  background-color: #fff;
}
.unbound {
  background-color: #fa3333;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
</style>